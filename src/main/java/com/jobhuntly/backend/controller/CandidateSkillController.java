package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.request.CandidateSkillRequest;
import com.jobhuntly.backend.dto.response.CandidateSkillResponse;
import com.jobhuntly.backend.security.SecurityUtils;
import com.jobhuntly.backend.service.CandidateSkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${backend.prefix}/candidate/profile/candidateSkills")
public class CandidateSkillController {

    private final CandidateSkillService service;

    @PostMapping
    public ResponseEntity<CandidateSkillResponse> create(@Valid @RequestBody CandidateSkillRequest dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(service.create(userId, dto));
    }

    @GetMapping
    public ResponseEntity<List<CandidateSkillResponse>> getAll() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(service.getAll(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CandidateSkillResponse> update(@PathVariable Long id,
            @Valid @RequestBody CandidateSkillRequest dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(service.update(userId, id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        service.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
