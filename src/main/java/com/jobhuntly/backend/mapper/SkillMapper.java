package com.jobhuntly.backend.mapper;

import com.jobhuntly.backend.dto.request.SkillRequest;
import com.jobhuntly.backend.dto.response.SkillResponse;
import com.jobhuntly.backend.entity.Skill;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SkillMapper {
    SkillResponse toResponse (Skill skill);
    Skill toEntity (SkillRequest skillRequest);
    List<SkillResponse> toListSkill (List<Skill> skillList);
}
