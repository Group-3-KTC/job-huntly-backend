package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.request.SoftSkillRequest;
import com.jobhuntly.backend.dto.response.SoftSkillResponse;
import com.jobhuntly.backend.service.SoftSkillService;
import com.jobhuntly.backend.security.jwt.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${backend.prefix}/profile/soft-skills")
public class SoftSkillController {
    private final SoftSkillService service;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<SoftSkillResponse> create(@Valid @RequestBody SoftSkillRequest dto,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(service.create(userId, dto));
    }

    @GetMapping
    public ResponseEntity<List<SoftSkillResponse>> getAll(@RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(service.getAll(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SoftSkillResponse> update(@PathVariable Long id, @Valid @RequestBody SoftSkillRequest dto,
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