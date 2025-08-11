package com.jobhuntly.backend.service;

import com.jobhuntly.backend.dto.response.WardResponse;

import java.util.List;

public interface WardService {
    List<WardResponse> getWardByCityName(String cityName);
}
