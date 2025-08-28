package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.request.CertificateRequest;
import com.jobhuntly.backend.dto.response.CertificateResponse;
import com.jobhuntly.backend.service.CertificateService;
import com.jobhuntly.backend.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${backend.prefix}/candidate/profile/certificates")
public class CertificateController {

    private final CertificateService service;

    @PostMapping
    public ResponseEntity<CertificateResponse> create(@Valid @RequestBody CertificateRequest dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(service.create(userId, dto));
    }

    @GetMapping
    public ResponseEntity<List<CertificateResponse>> getAll() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(service.getAll(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CertificateResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CertificateRequest dto) {
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
