package com.jobhuntly.backend.mapper;

import com.jobhuntly.backend.dto.request.CandidateSkillRequest;
import com.jobhuntly.backend.dto.response.CandidateSkillResponse;
import com.jobhuntly.backend.entity.CandidateSkill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CandidateSkillMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "skillId", source = "skill.id")
    @Mapping(target = "skillName", source = "skill.name")
    @Mapping(target = "levelId", source = "level.id")
    @Mapping(target = "levelName", source = "level.name")
    @Mapping(target = "categoryId", ignore = true)
    @Mapping(target = "categoryName", ignore = true)
    @Mapping(target = "parentCategoryId", ignore = true)
    @Mapping(target = "parentCategoryName", ignore = true)
    CandidateSkillResponse toResponseDTO(CandidateSkill candidateSkill);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "skill", ignore = true)
    @Mapping(target = "level", ignore = true)
    CandidateSkill toEntity(CandidateSkillRequest dto);

    List<CandidateSkillResponse> toResponseList(List<CandidateSkill> list);
}