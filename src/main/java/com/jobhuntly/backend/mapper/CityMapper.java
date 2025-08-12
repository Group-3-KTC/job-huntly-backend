package com.jobhuntly.backend.mapper;

import com.jobhuntly.backend.dto.request.CityDTO;
import com.jobhuntly.backend.entity.City;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CityMapper {
    CityDTO toDTO(City city);
    City toEntity(CityDTO cityDTO);
    List<CityDTO> toDTOList(List<City> cities);
}
