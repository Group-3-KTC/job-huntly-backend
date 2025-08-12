package com.jobhuntly.backend.service;

import com.jobhuntly.backend.dto.auth.request.GoogleLoginRequest;
import com.jobhuntly.backend.dto.auth.request.LoginRequest;
import com.jobhuntly.backend.dto.auth.request.RegisterRequest;
import com.jobhuntly.backend.dto.auth.response.LoginResponse;
import com.jobhuntly.backend.dto.auth.response.RegisterResponse;

public interface AuthService {
    RegisterResponse register(RegisterRequest request);
    RegisterResponse activateAccount(String token);
    LoginResponse login(LoginRequest request);
    LoginResponse loginWithGoogle(GoogleLoginRequest request);
}
