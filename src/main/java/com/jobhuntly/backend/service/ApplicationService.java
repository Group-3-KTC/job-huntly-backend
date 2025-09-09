package com.jobhuntly.backend.service;

import com.jobhuntly.backend.dto.request.ApplicationRequest;
import com.jobhuntly.backend.dto.response.ApplicationByUserResponse;
import com.jobhuntly.backend.dto.response.ApplicationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApplicationService {
    ApplicationResponse create(Long userId, ApplicationRequest req);

    Page<ApplicationByUserResponse> getByUser(Long userId, Pageable pageable);
    Page<ApplicationResponse> getByJob(Integer jobId, Pageable pageable);

    ApplicationResponse update(Long userId, Long jobId, ApplicationRequest req);

    ApplicationResponse getDetail(Long userId, Integer jobId);
}
