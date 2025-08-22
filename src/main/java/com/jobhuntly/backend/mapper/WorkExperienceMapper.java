package com.jobhuntly.backend.mapper;

import com.jobhuntly.backend.dto.request.WorkExperienceRequest;
import com.jobhuntly.backend.dto.response.WorkExperienceResponse;
import com.jobhuntly.backend.entity.WorkExperience;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WorkExperienceMapper {
    @Mapping(target = "id", source = "experId")
    WorkExperienceResponse toResponseDTO(WorkExperience exper);

    @Mapping(target = "experId", ignore = true)
    @Mapping(target = "profile", ignore = true)
    WorkExperience toEntity(WorkExperienceRequest dto);

    List<WorkExperienceResponse> toResponseList(List<WorkExperience> list);
}