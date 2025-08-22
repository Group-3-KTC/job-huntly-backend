package com.jobhuntly.backend.service;

import com.jobhuntly.backend.dto.request.CandidateSkillRequest;
import com.jobhuntly.backend.dto.response.CandidateSkillResponse;

import java.util.List;

public interface CandidateSkillService {
    CandidateSkillResponse create(Long userId, CandidateSkillRequest dto);

    List<CandidateSkillResponse> getAll(Long userId);

    CandidateSkillResponse update(Long userId, Long skillId, CandidateSkillRequest dto);

    void delete(Long userId, Long skillId);
}