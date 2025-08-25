package com.jobhuntly.backend.service;

import com.jobhuntly.backend.dto.request.AwardRequest;
import com.jobhuntly.backend.dto.response.AwardResponse;

import java.util.List;

public interface AwardService {

    AwardResponse create(Long userId, AwardRequest dto);

    List<AwardResponse> getAll(Long userId);

    AwardResponse update(Long userId, Long id, AwardRequest dto);

    void delete(Long userId, Long id);
}