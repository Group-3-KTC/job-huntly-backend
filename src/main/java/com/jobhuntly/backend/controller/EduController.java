package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.request.EduRequest;
import com.jobhuntly.backend.dto.response.EduResponse;
import com.jobhuntly.backend.service.EduService;
import com.jobhuntly.backend.security.jwt.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${backend.prefix}/profile/education")
public class EduController {
    private final EduService service;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<EduResponse> create(@Valid @RequestBody EduRequest dto,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(service.create(userId, dto));
    }

    @GetMapping
    public ResponseEntity<List<EduResponse>> getAll(@RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(service.getAll(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EduResponse> update(@PathVariable Long id, @Valid @RequestBody EduRequest dto,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(service.update(userId, id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        service.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

    private Long extractUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.extractUserId(token);
    }
}