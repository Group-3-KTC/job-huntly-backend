package com.jobhuntly.backend.service;

import com.jobhuntly.backend.dto.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest request);
}
