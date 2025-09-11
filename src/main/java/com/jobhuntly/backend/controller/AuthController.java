package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.auth.AppPrincipal;
import com.jobhuntly.backend.dto.auth.request.*;
import com.jobhuntly.backend.dto.auth.response.LoginResponse;
import com.jobhuntly.backend.dto.auth.response.MeResponse;
import com.jobhuntly.backend.dto.auth.response.RegisterResponse;
import com.jobhuntly.backend.security.cookie.AuthCookieService;
import com.jobhuntly.backend.service.AuthService;
import com.jobhuntly.backend.service.impl.SessionServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("${backend.prefix}/auth")
public class AuthController {
    private final AuthService authService;
    private final AuthCookieService authCookieService;
    private final SessionServiceImpl sessionService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @GetMapping("/activate")
    public ResponseEntity<RegisterResponse> activate(@RequestParam("token") String token) {
        return authService.activateAccount(token);
    }

    @PostMapping("/activation/resend")
    public ResponseEntity<Void> resend(@RequestParam("email") String email) {
        return authService.resendActivation(email);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest reqBody,
                                               HttpServletRequest req,
                                               HttpServletResponse res) {
        LoginResponse body = authService.login(reqBody, req, res);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/google")
    public ResponseEntity<LoginResponse> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest reqBody,
                                                         HttpServletRequest req,
                                                         HttpServletResponse res) {
        return ResponseEntity.ok(authService.loginWithGoogle(reqBody, req, res));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest req, HttpServletResponse res) {
        String rawRefresh = authCookieService.readCookie(req, "RT").orElse(null);
        sessionService.revokeCurrent(rawRefresh);
        authCookieService.clearAuthCookies(req, res);
        return ResponseEntity.noContent().build();
    }

//    @PostMapping("/logout-all")
//    public ResponseEntity<Void> logoutAll(HttpServletRequest req, HttpServletResponse res) {
//        sessionService.revokeAll(currentUserId);
//        authCookieService.clearAuthCookies(req, res);
//        return ResponseEntity.noContent().build();
//    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(@AuthenticationPrincipal AppPrincipal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        MeResponse dto = authService.getUserMe(principal.email());
        return ResponseEntity.ok(dto);
    }


    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(HttpServletRequest req, HttpServletResponse res) {
        authService.refreshToken(req, res);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password/set-link")
    public ResponseEntity<Void> sendSetPasswordLink(@Valid @RequestBody EmailOnlyRequest req) {
        authService.sendSetPasswordLink(req.getEmail());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password/set")
    public ResponseEntity<Void> setPassword(@Valid @RequestBody PasswordWithTokenRequest req) {
        authService.setPassword(req.getToken(), req.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password/forgot")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody EmailOnlyRequest req) {
        authService.sendResetPasswordLink(req.getEmail());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password/reset")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordWithTokenRequest req) {
        authService.resetPassword(req.getToken(), req.getNewPassword());
        return ResponseEntity.noContent().build();
    }
}

