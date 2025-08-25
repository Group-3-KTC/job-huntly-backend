package com.jobhuntly.backend.mapper;

import com.jobhuntly.backend.dto.request.SoftSkillRequest;
import com.jobhuntly.backend.dto.response.SoftSkillResponse;
import com.jobhuntly.backend.entity.SoftSkill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SoftSkillMapper {
    @Mapping(target = "id", source = "softSkillId")
    SoftSkillResponse toResponseDTO(SoftSkill softSkill);

    @Mapping(target = "softSkillId", ignore = true)
    @Mapping(target = "profile", ignore = true)
    SoftSkill toEntity(SoftSkillRequest dto);

    List<SoftSkillResponse> toResponseList(List<SoftSkill> list);
}