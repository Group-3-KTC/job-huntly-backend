package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.request.RegisterRequest;
import com.jobhuntly.backend.dto.response.AuthResponse;
import com.jobhuntly.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @GetMapping("/activate")
    public AuthResponse activate(@RequestParam("token") String token) {
        return authService.activateAccount(token);
    }

}
