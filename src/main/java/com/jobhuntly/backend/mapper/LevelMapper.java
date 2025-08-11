package com.jobhuntly.backend.mapper;

import com.jobhuntly.backend.dto.response.LevelResponse;
import com.jobhuntly.backend.dto.response.WorkTypeResponse;
import com.jobhuntly.backend.entity.Level;
import com.jobhuntly.backend.entity.WorkType;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LevelMapper {
    LevelResponse toResponse(Level level);
    List<LevelResponse> toResponseList (List<Level> levelList);
}
