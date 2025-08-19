package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.request.SavedJobRequest;
import com.jobhuntly.backend.dto.response.SavedJobResponse;
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

    @PostMapping("/create")
    public ResponseEntity<SavedJobResponse> create(@Valid @RequestBody SavedJobRequest request) {
        SavedJobResponse resp = savedJobService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestParam("user_id") Long userId,
                                       @RequestParam("job_id") Long jobId) {
        boolean deleted = savedJobService.delete(userId, jobId);
        return deleted ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    // GET BY USER
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SavedJobResponse>> getByUserId(@PathVariable Long userId) {
        List<SavedJobResponse> list = savedJobService.getByUserId(userId);
        return ResponseEntity.ok(list);
    }
}
