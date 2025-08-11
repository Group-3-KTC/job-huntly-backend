package com.jobhuntly.backend.service;

import com.jobhuntly.backend.dto.request.RegisterRequest;
import com.jobhuntly.backend.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse activateAccount(String token);
}
