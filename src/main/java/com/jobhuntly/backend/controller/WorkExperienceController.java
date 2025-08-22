// WorkExperienceController.java (updated to inject interface)
package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.request.WorkExperienceRequest;
import com.jobhuntly.backend.dto.response.WorkExperienceResponse;
import com.jobhuntly.backend.service.WorkExperienceService;
import com.jobhuntly.backend.security.jwt.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${backend.prefix}/profile/work-experience")
public class WorkExperienceController {
    private final WorkExperienceService service;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<WorkExperienceResponse> create(@Valid @RequestBody WorkExperienceRequest dto,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(service.create(userId, dto));
    }

    @GetMapping
    public ResponseEntity<List<WorkExperienceResponse>> getAll(@RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(service.getAll(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkExperienceResponse> update(@PathVariable Long id,
            @Valid @RequestBody WorkExperienceRequest dto, @RequestHeader("Authorization") String authHeader) {
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