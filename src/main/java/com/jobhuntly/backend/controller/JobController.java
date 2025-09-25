package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.auth.AppPrincipal;
import com.jobhuntly.backend.dto.request.JobFilterRequest;
import com.jobhuntly.backend.dto.request.JobPatchRequest;
import com.jobhuntly.backend.dto.request.JobRequest;
import com.jobhuntly.backend.dto.response.JobItemWithStatus;
import com.jobhuntly.backend.dto.response.JobResponse;
import com.jobhuntly.backend.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
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

    @PatchMapping("/{id}")
    public ResponseEntity<JobResponse> patch(@PathVariable Long id, @RequestBody JobPatchRequest request) {
        return ResponseEntity.ok(jobService.patch(id, request));
    }

    @GetMapping("/all")
    public ResponseEntity<Page<JobResponse>> list(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(jobService.list(pageable));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<Page<JobResponse>> listByCompany(@PathVariable Long companyId,
                                                           @PageableDefault(size = 10, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC)
                                                           Pageable pageable) {
        return ResponseEntity.ok(jobService.listByCompany(companyId, pageable));
    }

    @PostMapping("/search-lite")
    public Page<JobResponse> searchLite(
            @RequestBody JobFilterRequest request,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return jobService.searchLite(request, pageable);
    }

    @PostMapping("/company/{companyId}/search")
    public Page<JobResponse> searchByCompany(
            @PathVariable Long companyId,
            @RequestBody JobFilterRequest request,
            @PageableDefault(size = 10, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable
    ) {
        return jobService.searchByCompany(companyId, request, pageable);
    }

    @GetMapping("/all-with-status")
    public ResponseEntity<Page<JobItemWithStatus>> listWithStatus(
            @AuthenticationPrincipal AppPrincipal me,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long uid = (me == null) ? null : me.id();
        return ResponseEntity.ok(jobService.listWithStatus(pageable, uid));
    }

    @PostMapping("/search-lite-with-status")
    public ResponseEntity<Page<JobItemWithStatus>> searchLiteWithStatus(
            @AuthenticationPrincipal AppPrincipal me,
            @RequestBody JobFilterRequest request,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long uid = (me == null) ? null : me.id();
        return ResponseEntity.ok(jobService.searchLiteWithStatus(request, pageable, uid));
    }

}
