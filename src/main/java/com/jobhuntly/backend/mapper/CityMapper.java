package com.jobhuntly.backend.mapper;

import com.jobhuntly.backend.dto.request.CityRequest;
import com.jobhuntly.backend.entity.City;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CityMapper {
    CityRequest toDTO(City city);
    City toEntity(CityRequest cityRequest);
    List<CityRequest> toDTOList(List<City> cities);
}
