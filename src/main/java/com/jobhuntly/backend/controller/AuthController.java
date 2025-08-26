package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.auth.request.GoogleLoginRequest;
import com.jobhuntly.backend.dto.auth.request.LoginRequest;
import com.jobhuntly.backend.dto.auth.request.RegisterRequest;
import com.jobhuntly.backend.dto.auth.response.LoginResponse;
import com.jobhuntly.backend.dto.auth.response.MeResponse;
import com.jobhuntly.backend.dto.auth.response.RegisterResponse;
import com.jobhuntly.backend.service.AuthService;
import com.jobhuntly.backend.service.auth.AuthCookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("${backend.prefix}/auth")
public class AuthController {
    private final AuthService authService;
    private final AuthCookieService authCookieService;


    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @GetMapping("/activate")
    public ResponseEntity<RegisterResponse> activate(@RequestParam("token") String token) {
        return authService.activateAccount(token);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest reqBody,
                                               HttpServletRequest req,
                                               HttpServletResponse res) {
        LoginResponse body = authService.login(reqBody, req, res);


        return ResponseEntity.ok(body);
    }

    @PostMapping("/google")
    public ResponseEntity<LoginResponse> loginWithGoogle(@RequestBody GoogleLoginRequest reqBody,
                                                         HttpServletRequest req,
                                                         HttpServletResponse res) {
        return ResponseEntity.ok(authService.loginWithGoogle(reqBody, req, res));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest req, HttpServletResponse res) {
        authCookieService.clearAuthCookie(req, res);
        authCookieService.clearCookie(req, res, "role", false);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(Principal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        MeResponse dto = authService.getUserMe(principal.getName());
        return ResponseEntity.ok(dto);
    }
}
