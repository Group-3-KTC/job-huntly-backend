package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.request.SavedJobRequest;
import com.jobhuntly.backend.dto.response.SavedJobResponse;
import com.jobhuntly.backend.security.jwt.JwtUtil;
import com.jobhuntly.backend.service.SavedJobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${backend.prefix}/save-job")
public class SavedJobController {
    private final SavedJobService savedJobService;
    private final JwtUtil jwtUtil;

    @PostMapping("/create")
    public ResponseEntity<SavedJobResponse> create(@Valid @RequestBody SavedJobRequest request,
                                                        @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        SavedJobResponse resp = savedJobService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestHeader("Authorization") String authHeader,
                                       @RequestParam("job_id") Long jobId) {
        Long userId = extractUserId(authHeader);
        boolean deleted = savedJobService.delete(userId, jobId);
        return deleted ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    // GET BY USER
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SavedJobResponse>> getByUserId(@RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        List<SavedJobResponse> list = savedJobService.getByUserId(userId);
        return ResponseEntity.ok(list);
    }

    private Long extractUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.extractUserId(token);
    }
}
