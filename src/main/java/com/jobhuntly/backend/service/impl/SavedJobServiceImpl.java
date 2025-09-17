package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.request.SavedJobRequest;
import com.jobhuntly.backend.dto.response.SavedJobResponse;
import com.jobhuntly.backend.entity.Company;
import com.jobhuntly.backend.entity.Job;
import com.jobhuntly.backend.entity.SavedJob;
import com.jobhuntly.backend.mapper.SavedJobMapper;
import com.jobhuntly.backend.repository.CompanyRepository;
import com.jobhuntly.backend.repository.JobRepository;
import com.jobhuntly.backend.repository.SavedJobRepository;
import com.jobhuntly.backend.repository.SkillRepository;
import com.jobhuntly.backend.service.SavedJobService;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@AllArgsConstructor
@Transactional
public class SavedJobServiceImpl implements SavedJobService {
    private final SavedJobRepository savedJobRepository;
    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final SkillRepository skillRepository;
    private final SavedJobMapper savedJobMapper;
    @Override
    public SavedJobResponse create(Long userId, SavedJobRequest request) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"USER_NOT_AUTHENTICATED");
        }
        final Long jobId = request.getJobId();
        if (jobId == null) {
            throw new IllegalArgumentException("job_id is required");
        }

        // 1) Đã lưu trước đó?
        var existing = savedJobRepository.findByUserIdAndJobId(userId, jobId).orElse(null);
        if (existing != null) {
            return buildResponse(existing, jobId);
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));

        SavedJob toSave = savedJobMapper.toEntity(request);
        toSave.setUserId(userId);
        toSave.setJobId(jobId);

        try {
            SavedJob saved = savedJobRepository.save(toSave);
            return buildResponse(saved, job.getId());
        } catch (DataIntegrityViolationException e) {
            var dup = savedJobRepository.findByUserIdAndJobId(userId, jobId)
                    .orElseThrow(() -> e);
            return buildResponse(dup, job.getId());
        }
    }

    @Override
    public boolean delete(Long userId, Long jobId) {
        long affected = savedJobRepository.deleteByUserIdAndJobId(userId, jobId);
        return affected > 0;
    }

    @Override
    public List<SavedJobResponse> getByUserId(Long userId) {
        List<SavedJob> savedList = savedJobRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<SavedJobResponse> result = new ArrayList<>(savedList.size());
        for (SavedJob s : savedList) {
            result.add(buildResponse(s, s.getJobId()));
        }
        return result;
    }

    @Override
    public boolean exists(Long userId, Long jobId) {
        return savedJobRepository.findByUserIdAndJobId(userId, jobId).isPresent();
    }

    @Override
    public Set<Long> findSavedJobIds(Long userId, Collection<Long> jobIds) {
        if (userId == null || jobIds == null || jobIds.isEmpty()) return Set.of();
        return new HashSet<>(savedJobRepository.findSavedJobIdsIn(userId, jobIds));
    }

    private SavedJobResponse buildResponse(SavedJob saved, Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));

        Company company = job.getCompany();
        if (company == null) {
            throw new IllegalStateException("Job " + jobId + " has no company");
        }

        List<String> skills = skillRepository.findNamesByJobId(jobId);

        return savedJobMapper.toResponse(saved, job, company, skills);
    }
}
