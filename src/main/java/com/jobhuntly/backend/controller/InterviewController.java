package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.request.CreateInterviewRequest;
import com.jobhuntly.backend.dto.request.UpdateInterviewStatusRequest;
import com.jobhuntly.backend.dto.response.CandidateInterviewResponse;
import com.jobhuntly.backend.dto.response.InterviewMetaDto;
import com.jobhuntly.backend.dto.response.RecruiterInterviewResponse;
import com.jobhuntly.backend.security.SecurityUtils;
import com.jobhuntly.backend.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${backend.prefix}/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('RECRUITER','ADMIN')")
    public RecruiterInterviewResponse create(@RequestBody @Valid CreateInterviewRequest req) throws Exception {
        return interviewService.create(req);
    }

    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasAnyRole('RECRUITER','ADMIN')")
    public Page<RecruiterInterviewResponse> listForCompany(
            @PathVariable Long companyId,
            @PageableDefault(size = 20, sort = "scheduledAt") Pageable pageable) {
        return interviewService.listForCompany(companyId, pageable);
    }

    @GetMapping("/candidate")
    @PreAuthorize("hasAnyRole('CANDIDATE','ADMIN')")
    public Page<CandidateInterviewResponse> listForCandidate(
            @PageableDefault(size = 20, sort = "scheduledAt") Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        return interviewService.listForCandidate(userId, pageable);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('CANDIDATE','RECRUITER','ADMIN')")
    public Object updateStatus(@PathVariable Long id, @RequestBody @Valid UpdateInterviewStatusRequest req) {
        return interviewService.updateStatus(id, req.status());
    }

    @GetMapping("/{id}/meta")
    @PreAuthorize("hasAnyRole('CANDIDATE','RECRUITER','ADMIN')")
    public InterviewMetaDto meta(@PathVariable("id") Long id) {
        return interviewService.getMeta(id);
    }
}
