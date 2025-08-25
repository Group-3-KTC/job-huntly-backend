package com.jobhuntly.backend.service;

import java.util.List;

import com.jobhuntly.backend.dto.request.EduRequest;
import com.jobhuntly.backend.dto.response.EduResponse;

public interface EduService {

    EduResponse create(Long userId, EduRequest dto);

    List<EduResponse> getAll(Long userId);

    EduResponse update(Long userId, Long id, EduRequest dto);

    void delete(Long userId, Long id);
}
