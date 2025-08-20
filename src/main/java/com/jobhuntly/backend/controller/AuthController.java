package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.auth.request.GoogleLoginRequest;
import com.jobhuntly.backend.dto.auth.request.LoginRequest;
import com.jobhuntly.backend.dto.auth.request.RegisterRequest;
import com.jobhuntly.backend.dto.auth.request.UserMeDto;
import com.jobhuntly.backend.dto.auth.response.LoginResponse;
import com.jobhuntly.backend.dto.auth.response.RegisterResponse;
import com.jobhuntly.backend.repository.UserRepository;
import com.jobhuntly.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("${backend.prefix}/auth")
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository;

    @Value("${security.jwt.cookie-name}")
    private String COOKIE_NAME;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @GetMapping("/activate")
    public ResponseEntity<RegisterResponse> activate(@RequestParam("token") String token) {
        return authService.activateAccount(token);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse res = authService.login(request);

        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, res.getAccessToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(res.getExpiresIn()) // giây
                .build();

        ResponseCookie role = ResponseCookie.from("role", res.getRole())
                .httpOnly(false).secure(true).sameSite("None").path("/")
                .maxAge(res.getExpiresIn()).build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .header(HttpHeaders.SET_COOKIE, role.toString())
                .body(res);
    }

    @PostMapping("/google")
    public ResponseEntity<LoginResponse> loginWithGoogle(@RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(authService.loginWithGoogle(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie clear = ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0) // xóa cookie
                .build();
        ResponseCookie clearRole = ResponseCookie.from("role", "")
                .httpOnly(false).secure(true).sameSite("None").path("/").maxAge(0).build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clear.toString())
                .header(HttpHeaders.SET_COOKIE, clearRole.toString())
                .build();
    }

    @GetMapping("/auth/me")
    public ResponseEntity<UserMeDto> me(Principal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        UserMeDto dto = authService.getUserMe(principal.getName());
        return ResponseEntity.ok(dto);
    }
}
