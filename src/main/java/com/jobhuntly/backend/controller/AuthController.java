package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.auth.request.*;
import com.jobhuntly.backend.dto.auth.response.LoginResponse;
import com.jobhuntly.backend.dto.auth.response.MeResponse;
import com.jobhuntly.backend.dto.auth.response.RegisterResponse;
import com.jobhuntly.backend.service.AuthService;
import com.jobhuntly.backend.service.impl.AuthCookieServiceImpl;
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
    private final AuthCookieServiceImpl authCookieServiceImpl;

    // --- Registration & Activation ---
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

    // --- Email/Password Login (LOCAL) ---
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest reqBody,
                                               HttpServletRequest req,
                                               HttpServletResponse res) {
        LoginResponse body = authService.login(reqBody, req, res);
        return ResponseEntity.ok(body);
    }

    // --- Google OAuth callback/exchange ---
    @PostMapping("/google")
    public ResponseEntity<LoginResponse> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest reqBody,
                                                         HttpServletRequest req,
                                                         HttpServletResponse res) {
        return ResponseEntity.ok(authService.loginWithGoogle(reqBody, req, res));
    }

    // --- Logout (khuyến nghị POST) ---
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest req, HttpServletResponse res) {
        authCookieServiceImpl.clearAuthCookie(req, res);
        return ResponseEntity.noContent().build();
    }

    // --- Current user ---
    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(@AuthenticationPrincipal(expression = "email") String email) {
        if (email == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        MeResponse dto = authService.getUserMe(email);
        return ResponseEntity.ok(dto);
    }

    // ----------------------------------------------------------------
    //  A) SET PASSWORD cho tài khoản GOOGLE (chưa có password_hash)
    // ----------------------------------------------------------------

    // Gửi link đặt mật khẩu lần đầu (token TTL ngắn, 15–30m)
    @PostMapping("/password/set-link")
    public ResponseEntity<Void> sendSetPasswordLink(@Valid @RequestBody EmailOnlyRequest req) {
        authService.sendSetPasswordLink(req.getEmail());
        return ResponseEntity.noContent().build();
    }

    // Đặt mật khẩu lần đầu bằng token
    @PostMapping("/password/set")
    public ResponseEntity<Void> setPassword(@Valid @RequestBody PasswordWithTokenRequest req) {
        authService.setPassword(req.getToken(), req.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------------------
    //  B) FORGOT/RESET PASSWORD cho tài khoản LOCAL
    // ----------------------------------------------------------------

    // Gửi link reset password
    @PostMapping("/password/forgot")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody EmailOnlyRequest req) {
        authService.sendResetPasswordLink(req.getEmail());
        return ResponseEntity.noContent().build();
    }

    // Reset password bằng token
    @PostMapping("/password/reset")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordWithTokenRequest req) {
        authService.resetPassword(req.getToken(), req.getNewPassword());
        return ResponseEntity.noContent().build();
    }
}

