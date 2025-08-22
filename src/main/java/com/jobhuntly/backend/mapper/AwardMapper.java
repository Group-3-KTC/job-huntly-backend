package com.jobhuntly.backend.mapper;

import com.jobhuntly.backend.dto.request.AwardRequest;
import com.jobhuntly.backend.dto.response.AwardResponse;
import com.jobhuntly.backend.entity.Award;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AwardMapper {
    @Mapping(target = "id", source = "awardId")
    AwardResponse toResponseDTO(Award award);

    @Mapping(target = "awardId", ignore = true)
    @Mapping(target = "profile", ignore = true)
    Award toEntity(AwardRequest dto);

    List<AwardResponse> toResponseList(List<Award> list);
}