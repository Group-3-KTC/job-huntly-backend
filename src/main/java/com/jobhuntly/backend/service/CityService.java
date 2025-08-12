package com.jobhuntly.backend.service;

import com.jobhuntly.backend.dto.request.CityDTO;

import java.util.List;

public interface CityService {
    List<CityDTO> getAllCity();
    List<CityDTO> getCityByName(String namePart);
}
