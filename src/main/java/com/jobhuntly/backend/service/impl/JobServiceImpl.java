package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.request.JobFilterRequest;
import com.jobhuntly.backend.dto.request.JobRequest;
import com.jobhuntly.backend.dto.response.JobResponse;
import com.jobhuntly.backend.entity.*;
import com.jobhuntly.backend.mapper.JobMapper;
import com.jobhuntly.backend.repository.*;
import com.jobhuntly.backend.service.JobService;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.cache.interceptor.KeyGenerator;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.jobhuntly.backend.constant.CacheConstant.JOB_LIST_DEFAULT;

@Service
@AllArgsConstructor
@Transactional
public class JobServiceImpl implements JobService {
    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final SkillRepository skillRepository;
    private final WardRepository wardRepository;
    private final JobMapper jobMapper;
    private final CategoryRepository categoryRepository;
    private final LevelRepository levelRepository;
    private final WorkTypeRepository workTypeRepository;


    @Override
    @CacheEvict(cacheNames = JOB_LIST_DEFAULT, allEntries = true)
    public JobResponse create(JobRequest request) {
        validateDatesAndSalary(request);
        Long companyId = request.getCompanyId();
        if (companyId == null) {
            throw new IllegalArgumentException("companyId is required");
        }
        if (!companyRepository.existsById(companyId)) {
            throw new IllegalArgumentException("Company not found with id=" + companyId);
        }

        // 2) dùng reference để không SELECT toàn bộ company
        Company companyRef = companyRepository.getReferenceById(companyId);

        Job job = jobMapper.toEntity(request);
        job.setCompany(companyRef);

        // Nếu list null: bỏ qua
        if (request.getCategoryNames() != null) {
            Set<Category> categories = loadExistingByNames(
                    sanitizeAndDedupNames(request.getCategoryNames()),
                    categoryRepository::findByNameIgnoreCase
            );
            if (!categories.isEmpty()) job.setCategories(categories);
            else throw new IllegalArgumentException("No valid categories found");
        }

        if (request.getSkillNames() != null) {
            Set<Skill> skills = loadExistingByNames(
                    sanitizeAndDedupNames(request.getSkillNames()),
                    skillRepository::findByNameIgnoreCase
            );
            if (!skills.isEmpty()) job.setSkills(skills);
            else throw new IllegalArgumentException("No valid skills found");
        }

        if (request.getLevelNames() != null) {
            Set<Level> levels = loadExistingByNames(
                    sanitizeAndDedupNames(request.getLevelNames()),
                    levelRepository::findByNameIgnoreCase
            );
            if (!levels.isEmpty()) job.setLevels(levels);
            else throw new IllegalArgumentException("No valid levels found");
        }

        if (request.getWorkTypeNames() != null) {
            Set<WorkType> workTypes = loadExistingByNames(
                    sanitizeAndDedupNames(request.getWorkTypeNames()),
                    workTypeRepository::findByNameIgnoreCase
            );
            if (!workTypes.isEmpty()) job.setWorkTypes(workTypes);
            else throw new IllegalArgumentException("No valid work types found");
        }

        if (request.getWardIds() != null) {
            Set<Long> wardIds = request.getWardIds().stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            if (!wardIds.isEmpty()) {
                Set<Ward> wards = loadExistingByIds(wardIds, wardRepository::findAllById);
                if (!wards.isEmpty()) job.setWards(wards);
                else throw new IllegalArgumentException("No valid wards found");
            }
        }
        // ÉP NULL min/max khi type != 0
        enforceSalaryPolicy(job, request);

        Job saved = jobRepository.save(job);
        return jobMapper.toResponse(saved);
    }

    @Override
    public JobResponse getById(Long id) {
        Job job = jobRepository.findByIdWithAssociations(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with id=" + id));
        return jobMapper.toResponse(job);
    }

    @Override
    public JobResponse update(Long id, JobRequest request) {
        return null;
    }

    @Override
    @CacheEvict(cacheNames = JOB_LIST_DEFAULT, allEntries = true)
    public void delete(Long id) {
    }

    @Override
    public Page<JobResponse> list(Pageable pageable) {
        return jobRepository.findAll(pageable)
                .map(jobMapper::toResponseLite);
    }

    @Override
    public Page<JobResponse> listByCompany(Long companyId, Pageable pageable) {
        return jobRepository.findByCompanyIdWithAssociations(companyId, pageable).map(jobMapper::toResponse);
    }

    @Override
    public Page<JobResponse> searchLite(JobFilterRequest request, Pageable pageable) {
        Specification<Job> spec = JobSpecifications.build(request);
        Page<Job> page = jobRepository.findAll(spec, pageable);
        List<Long> ids = page.getContent().stream().map(Job::getId).toList();
        if (ids.isEmpty()) return Page.empty(pageable);

        List<Job> rich = jobRepository.findByIdIn(ids);
        Map<Long, Job> byId = rich.stream().collect(Collectors.toMap(Job::getId, j -> j));

        List<JobResponse> data = ids.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .map(jobMapper::toResponseLite)
                .toList();

        return new PageImpl<>(data, pageable, page.getTotalElements());
    }

    private void validateDatesAndSalary(JobRequest req) {
        if (req.getDatePost() != null && req.getExpiredDate() != null
                && req.getExpiredDate().isBefore(req.getDatePost())) {
            throw new IllegalArgumentException("expired_date must be >= date_post");
        }
        if (req.getSalaryType() != null && req.getSalaryType() == 0) { // RANGE
            if (req.getSalaryMin() != null && req.getSalaryMax() != null
                    && req.getSalaryMin() > req.getSalaryMax()) {
                throw new IllegalArgumentException("salary_min must be <= salary_max");
            }
        }
    }

    private String normalize(String s) {
        if (s == null) return "";
        String t = s.trim();
        return t.replaceAll("\\s+", " ");
    }

    private List<String> sanitizeAndDedupNames(List<String> raw) {
        if (raw == null) return List.of();
        LinkedHashMap<String, String> dedup = new LinkedHashMap<>();
        for (String name : raw) {
            String n = normalize(name);
            if (n.isBlank()) continue;
            dedup.putIfAbsent(n.toLowerCase(Locale.ROOT), n);
        }
        return new ArrayList<>(dedup.values());
    }

    private <E> Set<E> loadExistingByNames(List<String> names,
                                           Function<String, Optional<E>> finder) {
        if (names == null || names.isEmpty()) return Set.of();
        LinkedHashSet<E> result = new LinkedHashSet<>();
        for (String n : names) {
            finder.apply(n).ifPresent(result::add);
        }
        return result;
    }

    private <E, ID> Set<E> loadExistingByIds(Collection<ID> ids,
                                             Function<Collection<ID>, Iterable<E>> loader) {
        if (ids == null || ids.isEmpty()) return Set.of();
        LinkedHashSet<E> result = new LinkedHashSet<>();
        for (E e : loader.apply(ids)) {
            result.add(e);
        }
        return result;
    }

    private void enforceSalaryPolicy(Job job, JobRequest req) {
        Integer effectiveType = (req.getSalaryType() != null) ? req.getSalaryType() : job.getSalaryType();

        if (effectiveType != null && effectiveType != 0) {
            job.setSalaryMin(null);
            job.setSalaryMax(null);
        }
    }
}
