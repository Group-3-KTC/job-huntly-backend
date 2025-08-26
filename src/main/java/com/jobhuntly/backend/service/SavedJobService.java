package com.jobhuntly.backend.service;

import com.jobhuntly.backend.dto.request.SavedJobRequest;
import com.jobhuntly.backend.dto.response.SavedJobResponse;

import java.util.List;

public interface SavedJobService {
    SavedJobResponse create(Long userId, SavedJobRequest request);
    boolean delete(Long userId, Long jobId);
    List<SavedJobResponse> getByUserId(Long userId);
}
