package com.jobhuntly.backend.mapper;

import com.jobhuntly.backend.dto.response.CompanyDto;
import com.jobhuntly.backend.entity.Company;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompanyMapper {
    @Mapping(source = "user.id", target = "userId")
    CompanyDto toDto(Company company);

    @Mapping(target = "user", ignore = true)
    Company toEntity(CompanyDto companyDto);

    List<CompanyDto> toDtoList(List<Company> companies);

    @Mapping(target = "user", ignore = true)
    void updateEntityFromDto(CompanyDto dto, @MappingTarget Company entity);
}
