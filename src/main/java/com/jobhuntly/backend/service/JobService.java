package com.jobhuntly.backend.service;

import com.jobhuntly.backend.dto.request.JobFilterRequest;
import com.jobhuntly.backend.dto.request.JobRequest;
import com.jobhuntly.backend.dto.request.JobPatchRequest;
import com.jobhuntly.backend.dto.response.JobResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JobService {
    JobResponse create(JobRequest request);

    JobResponse getById(Long id);

    JobResponse update(Long id, JobRequest request);

    JobResponse patch(Long id, JobPatchRequest request);

    void delete(Long id);

    Page<JobResponse> list(Pageable pageable);

    Page<JobResponse> listByCompany(Long companyId, Pageable pageable);

    Page<JobResponse> searchLite(JobFilterRequest request, Pageable pageable);

    Page<JobResponse> searchByCompany(Long companyId, JobFilterRequest request, Pageable pageable);
}
