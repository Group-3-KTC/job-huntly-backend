package com.jobhuntly.backend.controller;

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
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // GET BY USER
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SavedJobResponse>> getByUserId() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<SavedJobResponse> list = savedJobService.getByUserId(userId);
        return ResponseEntity.ok(list);
    }
}
