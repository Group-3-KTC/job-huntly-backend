package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.request.JobFilterRequest;
import com.jobhuntly.backend.dto.request.JobPatchRequest;
import com.jobhuntly.backend.dto.request.JobRequest;
import com.jobhuntly.backend.dto.response.JobItemWithStatus;
import com.jobhuntly.backend.dto.response.JobResponse;
import com.jobhuntly.backend.entity.*;
import com.jobhuntly.backend.mapper.JobMapper;
import com.jobhuntly.backend.repository.*;
import com.jobhuntly.backend.service.ApplicationService;
import com.jobhuntly.backend.service.JobService;
import com.jobhuntly.backend.service.SavedJobService;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final NotificationService notificationService;
    private final SavedJobService savedJobService;
    private final ApplicationService applicationService;


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

        Company companyRef = companyRepository.getReferenceById(companyId);

        Job job = jobMapper.toEntity(request);
        job.setCompany(companyRef);

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

        enforceSalaryPolicy(job, request);

        Job saved = jobRepository.save(job);
        try {
            notificationService.notifyNewJobToFollowers(
                    companyRef.getId(),
                    saved.getId(),
                    companyRef.getCompanyName(),
                    saved.getTitle()
            );
        } catch (Exception ignore) {
            // không làm hỏng flow tạo job nếu gửi noti lỗi
        }
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
    public JobResponse patch(Long id, JobPatchRequest request) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with id=" + id));

        // scalar fields
        if (request.getTitle() != null) job.setTitle(normalize(request.getTitle()));
        if (request.getDescription() != null) job.setDescription(request.getDescription());
        if (request.getRequirements() != null) job.setRequirements(request.getRequirements());
        if (request.getBenefits() != null) job.setBenefits(request.getBenefits());
        if (request.getLocation() != null) job.setLocation(request.getLocation());
        if (request.getStatus() != null) job.setStatus(request.getStatus());
        if (request.getDatePost() != null) job.setDatePost(request.getDatePost());
        if (request.getExpiredDate() != null) job.setExpiredDate(request.getExpiredDate());

        if (request.getSalaryType() != null) job.setSalaryType(request.getSalaryType());
        if (request.getSalaryMin() != null) job.setSalaryMin(request.getSalaryMin());
        if (request.getSalaryMax() != null) job.setSalaryMax(request.getSalaryMax());

        if (request.getCompanyId() != null) {
            Long cid = request.getCompanyId();
            Company ref = companyRepository.findById(cid)
                    .orElseThrow(() -> new IllegalArgumentException("Company not found with id=" + cid));
            job.setCompany(ref);
        }

        if (request.getCategoryNames() != null) {
            Set<Category> categories = loadExistingByNames(
                    sanitizeAndDedupNames(request.getCategoryNames()),
                    categoryRepository::findByNameIgnoreCase
            );
            job.setCategories(categories);
        }

        if (request.getSkillNames() != null) {
            Set<Skill> skills = loadExistingByNames(
                    sanitizeAndDedupNames(request.getSkillNames()),
                    skillRepository::findByNameIgnoreCase
            );
            job.setSkills(skills);
        }

        if (request.getLevelNames() != null) {
            Set<Level> levels = loadExistingByNames(
                    sanitizeAndDedupNames(request.getLevelNames()),
                    levelRepository::findByNameIgnoreCase
            );
            job.setLevels(levels);
        }

        if (request.getWorkTypeNames() != null) {
            Set<WorkType> workTypes = loadExistingByNames(
                    sanitizeAndDedupNames(request.getWorkTypeNames()),
                    workTypeRepository::findByNameIgnoreCase
            );
            job.setWorkTypes(workTypes);
        }

        if (request.getWardIds() != null) {
            Set<Long> wardIds = request.getWardIds().stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            Set<Ward> wards = loadExistingByIds(wardIds, wardRepository::findAllById);
            job.setWards(wards);
        }

        JobRequest shim = JobRequest.builder()
                .salaryType(request.getSalaryType())
                .salaryMin(request.getSalaryMin())
                .salaryMax(request.getSalaryMax())
                .datePost(request.getDatePost())
                .expiredDate(request.getExpiredDate())
                .build();
        validateDatesAndSalary(shim);
        enforceSalaryPolicy(job, shim);

        Job saved = jobRepository.save(job);
        return jobMapper.toResponse(saved);
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
    public Page<JobItemWithStatus> listWithStatus(Pageable pageable, Long userId) {
        Page<Job> jobPage = jobRepository.findAll(pageable);
        if (jobPage.isEmpty()) {
            return Page.empty(pageable);
        }
        List<JobResponse> jobs = jobPage.getContent()
                .stream().map(jobMapper::toResponse).toList();

        List<Long> ids = jobs.stream().map(JobResponse::getId).toList();

        // chưa logged in = skip
        Set<Long> savedIds   = (userId == null) ? Set.of() : savedJobService.findSavedJobIds(userId, ids);
        Set<Long> appliedIds = (userId == null) ? Set.of() : applicationService.findAppliedJobIds(userId, ids);

        List<JobItemWithStatus> items = jobs.stream()
                .map(j -> new JobItemWithStatus(
                        j,
                        savedIds.contains(j.getId()),
                        appliedIds.contains(j.getId())
                ))
                .toList();

        return new PageImpl<>(items, pageable, jobPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobItemWithStatus> searchLiteWithStatus(
            JobFilterRequest request,
            Pageable pageable,
            Long userId
    ) {
        Page<JobResponse> page = this.searchLite(request, pageable);
        if (page.isEmpty()) {
            return Page.empty(pageable);
        }

        List<JobResponse> jobs = page.getContent();
        List<Long> jobIds = jobs.stream()
                .map(JobResponse::getId)
                .filter(Objects::nonNull)
                .toList();

        final Set<Long> savedIds =
                (userId == null || jobIds.isEmpty())
                        ? Collections.emptySet()
                        : savedJobService.findSavedJobIds(userId, jobIds);

        final Set<Long> appliedIds =
                (userId == null || jobIds.isEmpty())
                        ? Collections.emptySet()
                        : applicationService.findAppliedJobIds(userId, jobIds);

        List<JobItemWithStatus> items = jobs.stream()
                .map(j -> new JobItemWithStatus(
                        j,
                        savedIds.contains(j.getId()),
                        appliedIds.contains(j.getId())
                ))
                .toList();

        return new PageImpl<>(items, pageable, page.getTotalElements());
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

    @Override
    public Page<JobResponse> searchByCompany(Long companyId, JobFilterRequest request, Pageable pageable) {
        Specification<Job> companySpec = (root, query, cb) -> cb.equal(root.get("company").get("id"), companyId);
        Specification<Job> spec = Specification.allOf(companySpec, JobSpecifications.build(request));

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
