package com.jobhuntly.backend.mapper;

import com.jobhuntly.backend.dto.request.ReportRequest;
import com.jobhuntly.backend.dto.response.ReportResponse;
import com.jobhuntly.backend.entity.Report;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReportMapper {
    Report toEntity(ReportRequest request);
    @Mapping(target = "userId", source = "user.id")
    ReportResponse toResponse(Report report);
}
