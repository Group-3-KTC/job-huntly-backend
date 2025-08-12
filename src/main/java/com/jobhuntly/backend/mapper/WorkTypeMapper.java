package com.jobhuntly.backend.mapper;

import com.jobhuntly.backend.dto.response.WorkTypeResponse;
import com.jobhuntly.backend.entity.WorkType;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WorkTypeMapper {
    WorkTypeResponse toResponse(WorkType workType);
    List<WorkTypeResponse> toResponseList (List<WorkType> workTypeList);
}
