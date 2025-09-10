package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.request.ApplicationRequest;
import com.jobhuntly.backend.dto.response.ApplicationByUserResponse;
import com.jobhuntly.backend.dto.response.ApplicationResponse;
import com.jobhuntly.backend.dto.response.ApplyStatusResponse;
import com.jobhuntly.backend.security.SecurityUtils;
import com.jobhuntly.backend.security.jwt.JwtUtil;
import com.jobhuntly.backend.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${backend.prefix}/application")
public class ApplicationController {
    private final ApplicationService applicationService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse create(@Valid @ModelAttribute ApplicationRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return applicationService.create(userId,request);
    }

    @GetMapping(value = "/by-user", produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<ApplicationByUserResponse> getByUser(
                                                     @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
                                                     Pageable pageable
                                                     ) {
        Long userId = SecurityUtils.getCurrentUserId();
        return applicationService.getByUser(userId, pageable);
    }

    @PostMapping(
            value = "/{jobId}/reapply",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    public ApplicationResponse reapply(
            @PathVariable Long jobId,
            @Valid @ModelAttribute ApplicationRequest request
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (request.getJobId() == null || !request.getJobId().equals(jobId)) {
            request.setJobId(jobId);
        }
        return applicationService.update(userId, jobId, request);
    }

    @GetMapping(value = "/detail/{jobId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApplicationResponse getDetailByJob(
            @PathVariable Integer jobId
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        return applicationService.getDetail(userId, jobId);
    }

    @GetMapping(value = "/by-job/{jobId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<ApplicationResponse> getByJob(
            @PathVariable Integer jobId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return applicationService.getByJob(jobId, pageable);
    }

    @GetMapping("/status")
    public ApplyStatusResponse getApplyStatus(@RequestParam("job_id") Long jobId) {
        Long userId = SecurityUtils.getCurrentUserId();
        boolean applied = applicationService.hasApplied(userId, jobId);
        return new ApplyStatusResponse(applied);
    }

}
