package com.jobhuntly.backend.service;

import com.jobhuntly.backend.dto.request.CityRequest;

import java.util.List;

public interface CityService {
    List<CityRequest> getAllCity();
    List<CityRequest> getCityByName(String namePart);
}
