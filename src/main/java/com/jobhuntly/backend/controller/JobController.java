package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.request.JobRequest;
import com.jobhuntly.backend.dto.response.JobResponse;
import com.jobhuntly.backend.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${backend.prefix}/job")
public class JobController {
    private final JobService jobService;

    @PostMapping("/create")
    public ResponseEntity<JobResponse> create(@Valid @RequestBody JobRequest request) {
        JobResponse created = jobService.create(request);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<Page<JobResponse>> list(
            @PageableDefault(size = 10, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(jobService.list(pageable));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<Page<JobResponse>> listByCompany(@PathVariable Long companyId,
                                                           @PageableDefault(size = 10, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC)
                                                           Pageable pageable) {
        return ResponseEntity.ok(jobService.listByCompany(companyId, pageable));
    }
}
