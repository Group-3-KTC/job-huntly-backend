package com.jobhuntly.backend.mapper;

import com.jobhuntly.backend.dto.request.CertificateRequest;
import com.jobhuntly.backend.dto.response.CertificateResponse;
import com.jobhuntly.backend.entity.Certificate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CertificateMapper {
    @Mapping(target = "id", source = "cerId")
    CertificateResponse toResponseDTO(Certificate cer);

    @Mapping(target = "cerId", ignore = true)
    @Mapping(target = "profile", ignore = true)
    Certificate toEntity(CertificateRequest dto);

    List<CertificateResponse> toResponseList(List<Certificate> list);
}