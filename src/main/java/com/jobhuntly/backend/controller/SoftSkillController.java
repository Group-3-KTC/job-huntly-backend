package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.request.SoftSkillRequest;
import com.jobhuntly.backend.dto.response.SoftSkillResponse;
import com.jobhuntly.backend.security.SecurityUtils;
import com.jobhuntly.backend.service.SoftSkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${backend.prefix}/candidate/profile/softSkills")
public class SoftSkillController {

    private final SoftSkillService service;

    @PostMapping
    public ResponseEntity<SoftSkillResponse> create(@Valid @RequestBody SoftSkillRequest dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(service.create(userId, dto));
    }

    @GetMapping
    public ResponseEntity<List<SoftSkillResponse>> getAll() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(service.getAll(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SoftSkillResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody SoftSkillRequest dto) {
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
