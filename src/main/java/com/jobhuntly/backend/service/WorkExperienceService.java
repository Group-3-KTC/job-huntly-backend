package com.jobhuntly.backend.service;

import java.util.List;

import com.jobhuntly.backend.dto.request.WorkExperienceRequest;
import com.jobhuntly.backend.dto.response.WorkExperienceResponse;

public interface WorkExperienceService {
    WorkExperienceResponse create(Long userId, WorkExperienceRequest dto);

    List<WorkExperienceResponse> getAll(Long userId);

    WorkExperienceResponse update(Long userId, Long id, WorkExperienceRequest dto);

    void delete(Long userId, Long id);
}
