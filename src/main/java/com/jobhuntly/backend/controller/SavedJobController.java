package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.auth.AppPrincipal;
import com.jobhuntly.backend.dto.request.SavedJobRequest;
import com.jobhuntly.backend.dto.response.SavedJobResponse;
import com.jobhuntly.backend.security.SecurityUtils;
import com.jobhuntly.backend.security.jwt.JwtUtil;
import com.jobhuntly.backend.service.SavedJobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("${backend.prefix}/save-job")
public class SavedJobController {
    private final SavedJobService savedJobService;
    private final JwtUtil jwtUtil;

    @PostMapping("/create")
    public ResponseEntity<SavedJobResponse> create(@Valid @RequestBody SavedJobRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("userId in controller = {}", userId);
        SavedJobResponse resp = savedJobService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(
                                       @RequestParam("job_id") Long jobId) {
        Long userId = SecurityUtils.getCurrentUserId();
        boolean deleted = savedJobService.delete(userId, jobId);
        return deleted ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> isSaved(@RequestParam("job_id") Long jobId, Authentication auth) {
        Long userId = resolveUserId(auth);
        boolean saved = savedJobService.exists(userId, jobId);
        return ResponseEntity.ok(Map.of("saved", saved));
    }

    // GET BY USER
    @GetMapping("/by-user")
    public ResponseEntity<List<SavedJobResponse>> getByUserId() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<SavedJobResponse> list = savedJobService.getByUserId(userId);
        return ResponseEntity.ok(list);
    }

    private Long resolveUserId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }
        Object p = auth.getPrincipal();

        if (p instanceof AppPrincipal ap) return ap.id();

        if (p instanceof String s) {
            if ("anonymousUser".equalsIgnoreCase(s)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Anonymous");
            }
            try {
                return Long.valueOf(s);
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unsupported principal string");
            }
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Unsupported principal type: " + p.getClass().getName());
    }
}
