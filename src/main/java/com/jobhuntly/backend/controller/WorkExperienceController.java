package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.request.WorkExperienceRequest;
import com.jobhuntly.backend.dto.response.WorkExperienceResponse;
import com.jobhuntly.backend.security.SecurityUtils;
import com.jobhuntly.backend.service.WorkExperienceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${backend.prefix}/candidate/profile/workExperience")
public class WorkExperienceController {

    private final WorkExperienceService service;

    @PostMapping
    public ResponseEntity<WorkExperienceResponse> create(@Valid @RequestBody WorkExperienceRequest dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(service.create(userId, dto));
    }

    @GetMapping
    public ResponseEntity<List<WorkExperienceResponse>> getAll() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(service.getAll(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkExperienceResponse> update(@PathVariable Long id,
            @Valid @RequestBody WorkExperienceRequest dto) {
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
