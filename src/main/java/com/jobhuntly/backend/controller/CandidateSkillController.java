package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.request.CandidateSkillRequest;
import com.jobhuntly.backend.dto.response.CandidateSkillResponse;
import com.jobhuntly.backend.security.jwt.JwtUtil;
import com.jobhuntly.backend.service.CandidateSkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${backend.prefix}/profile/candidate-skills")
public class CandidateSkillController {
    private final CandidateSkillService service;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<CandidateSkillResponse> create(@Valid @RequestBody CandidateSkillRequest dto,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(service.create(userId, dto));
    }

    @GetMapping
    public ResponseEntity<List<CandidateSkillResponse>> getAll(@RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(service.getAll(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CandidateSkillResponse> update(@PathVariable Long id, 
            @Valid @RequestBody CandidateSkillRequest dto,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(service.update(userId, id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, 
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        service.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

    private Long extractUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.extractUserId(token);
    }
}