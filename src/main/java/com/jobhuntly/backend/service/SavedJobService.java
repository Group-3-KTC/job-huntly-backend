package com.jobhuntly.backend.service;

import com.jobhuntly.backend.dto.request.SavedJobRequest;
import com.jobhuntly.backend.dto.response.SavedJobResponse;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface SavedJobService {
    SavedJobResponse create(Long userId, SavedJobRequest request);
    boolean delete(Long userId, Long jobId);
    List<SavedJobResponse> getByUserId(Long userId);
    boolean exists(Long userId, Long jobId);
    Set<Long> findSavedJobIds(Long userId, Collection<Long> jobIds);
}
