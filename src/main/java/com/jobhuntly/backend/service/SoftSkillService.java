package com.jobhuntly.backend.service;

import com.jobhuntly.backend.dto.request.SoftSkillRequest;
import com.jobhuntly.backend.dto.response.SoftSkillResponse;

import java.util.List;

public interface SoftSkillService {
    SoftSkillResponse create(Long userId, SoftSkillRequest dto);

    List<SoftSkillResponse> getAll(Long userId);

    SoftSkillResponse update(Long userId, Long id, SoftSkillRequest dto);

    void delete(Long userId, Long id);
}