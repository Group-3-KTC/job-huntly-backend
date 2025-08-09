package com.jobhuntly.backend.modules.auth.service;

import com.jobhuntly.backend.modules.auth.dto.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest request);
}
