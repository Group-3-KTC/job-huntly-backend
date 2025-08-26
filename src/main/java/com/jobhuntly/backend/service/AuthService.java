package com.jobhuntly.backend.service;

import com.jobhuntly.backend.dto.auth.request.GoogleLoginRequest;
import com.jobhuntly.backend.dto.auth.request.LoginRequest;
import com.jobhuntly.backend.dto.auth.request.RegisterRequest;
import com.jobhuntly.backend.dto.auth.response.LoginResponse;
import com.jobhuntly.backend.dto.auth.response.MeResponse;
import com.jobhuntly.backend.dto.auth.response.RegisterResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<RegisterResponse> register(RegisterRequest request);

    ResponseEntity<RegisterResponse> activateAccount(String token);

    LoginResponse login(LoginRequest request, HttpServletRequest req,
                        HttpServletResponse res);

    LoginResponse loginWithGoogle(GoogleLoginRequest request,
                                  HttpServletRequest req,
                                  HttpServletResponse res);

    MeResponse getUserMe(String email);
}
