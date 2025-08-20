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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
    public SavedJobResponse create(SavedJobRequest request) {
        final Long userId = request.getUserId();
        final Long jobId  = request.getJobId();

        SavedJob existing = savedJobRepository.findByUserIdAndJobId(userId, jobId).orElse(null);
        if (existing != null) {
            return buildResponse(existing, jobId);
        }

        // Kiểm tra job còn tồn tại
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));

        SavedJob toSave = savedJobMapper.toEntity(request);
        SavedJob saved  = savedJobRepository.save(toSave);

        SavedJob reloaded = savedJobRepository.findByUserIdAndJobId(userId, jobId)
                .orElse(saved);

        return buildResponse(reloaded, job.getId());
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
