package com.jobhuntly.backend.mapper;

import com.jobhuntly.backend.dto.request.EduRequest;
import com.jobhuntly.backend.dto.response.EduResponse;
import com.jobhuntly.backend.entity.Edu;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EduMapper {
    @Mapping(target = "id", source = "eduId")
    EduResponse toResponseDTO(Edu edu);

    @Mapping(target = "eduId", ignore = true)
    @Mapping(target = "profile", ignore = true)
    Edu toEntity(EduRequest dto);

    List<EduResponse> toResponseList(List<Edu> list);
}