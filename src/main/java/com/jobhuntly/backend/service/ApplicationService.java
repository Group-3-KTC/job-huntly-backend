package com.jobhuntly.backend.service;

import com.jobhuntly.backend.dto.request.ApplicationRequest;
import com.jobhuntly.backend.dto.response.ApplicationByUserResponse;
import com.jobhuntly.backend.dto.response.ApplicationResponse;
import com.jobhuntly.backend.dto.response.ApplyStatusResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.Set;

public interface ApplicationService {
    ApplicationResponse create(Long userId, ApplicationRequest req);

    Page<ApplicationByUserResponse> getByUser(Long userId, Pageable pageable);
    Page<ApplicationResponse> getByJob(Integer jobId, Pageable pageable);

    // Lấy application theo companyId (dành cho recruiter/admin)
    Page<ApplicationResponse> getByCompany(Long requesterUserId, Long companyId, Pageable pageable);

    ApplicationResponse update(Long userId, Long jobId, ApplicationRequest req);

    ApplicationResponse getDetail(Long userId, Long jobId);

    ApplyStatusResponse hasApplied(Long userId, Long jobId);

    ApplicationResponse updateStatusByStaff(Long userId, Long applicationId, String status);
    Set<Long> findAppliedJobIds(Long userId, Collection<Long> jobIds);
}
