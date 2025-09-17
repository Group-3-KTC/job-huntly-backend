package com.jobhuntly.backend.mapper;

import com.jobhuntly.backend.dto.request.PackageRequest;
import com.jobhuntly.backend.dto.response.PackageResponse;
import com.jobhuntly.backend.entity.PackageEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PackageMapper {
    PackageResponse toResponse(PackageEntity entity);
    @BeanMapping(ignoreByDefault = false)
    PackageEntity toEntity(PackageRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget PackageEntity target, PackageRequest req);
}
