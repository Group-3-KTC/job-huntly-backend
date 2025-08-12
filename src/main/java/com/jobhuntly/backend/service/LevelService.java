package com.jobhuntly.backend.service;

import com.jobhuntly.backend.dto.response.LevelResponse;

import java.util.List;

public interface LevelService {
    List<LevelResponse> getAllLevels();
}
