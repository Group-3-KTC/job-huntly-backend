package com.jobhuntly.backend.controller.admin;

import com.jobhuntly.backend.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/jobs/maintenance")
@RequiredArgsConstructor
public class JobMaintenanceController {

    private final JobRepository jobRepository;

    @PostMapping("/deactivate-expired")
    public ResponseEntity<Map<String, Object>> deactivateExpiredJobs() {
        int updated = jobRepository.markExpiredJobsInactive();
        Map<String, Object> body = new HashMap<>();
        body.put("updated", updated);
        body.put("message", "Expired jobs have been set to inactive");
        return ResponseEntity.ok(body);
    }
} 