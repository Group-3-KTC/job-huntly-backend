package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.request.AwardRequest;
import com.jobhuntly.backend.dto.response.AwardResponse;
import com.jobhuntly.backend.security.SecurityUtils;
import com.jobhuntly.backend.service.AwardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${backend.prefix}/candidate/profile/awards")
public class AwardController {

    private final AwardService service;

    @PostMapping
    public ResponseEntity<AwardResponse> create(@Valid @RequestBody AwardRequest dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(service.create(userId, dto));
    }

    @GetMapping
    public ResponseEntity<List<AwardResponse>> getAll() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(service.getAll(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AwardResponse> update(@PathVariable Long id,
            @Valid @RequestBody AwardRequest dto) {
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
