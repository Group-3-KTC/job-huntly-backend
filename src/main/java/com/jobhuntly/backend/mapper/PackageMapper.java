package com.jobhuntly.backend.mapper;

import com.jobhuntly.backend.dto.request.PackageRequest;
import com.jobhuntly.backend.dto.response.PackageResponse;
import com.jobhuntly.backend.entity.PackageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PackageMapper {
    PackageResponse toResponse(PackageEntity entity);
    @Mapping(target = "packageId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PackageEntity toEntity(PackageRequest request);
}
