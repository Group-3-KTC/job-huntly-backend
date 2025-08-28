package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.request.EduRequest;
import com.jobhuntly.backend.dto.response.EduResponse;
import com.jobhuntly.backend.security.SecurityUtils;
import com.jobhuntly.backend.service.EduService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${backend.prefix}/candidate/profile/education")
public class EduController {

    private final EduService service;

    @PostMapping
    public ResponseEntity<EduResponse> create(@Valid @RequestBody EduRequest dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(service.create(userId, dto));
    }

    @GetMapping
    public ResponseEntity<List<EduResponse>> getAll() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(service.getAll(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EduResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody EduRequest dto) {
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
