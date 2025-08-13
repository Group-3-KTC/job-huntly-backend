package com.jobhuntly.backend.service;

import com.jobhuntly.backend.dto.request.SkillRequest;
import com.jobhuntly.backend.dto.response.SkillResponse;
import com.jobhuntly.backend.entity.Skill;

import java.util.List;

public interface SkillService {
    SkillResponse createSkill(SkillRequest request);
    List<SkillResponse> getSkillsByCategoryName(String categoryName);
}
