package com.jobhuntly.backend.service;

import com.jobhuntly.backend.dto.response.WorkTypeResponse;

import java.util.List;

public interface WorkTypeService {
    List<WorkTypeResponse> getAllWorkType();
}
