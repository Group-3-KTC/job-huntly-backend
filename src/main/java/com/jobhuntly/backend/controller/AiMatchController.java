package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.ai.MatchRequest;
import com.jobhuntly.backend.dto.ai.MatchResponse;
import com.jobhuntly.backend.security.SecurityUtils;
import com.jobhuntly.backend.service.AiMatchService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("${backend.prefix}/ai")
public class AiMatchController {

    private final AiMatchService aiMatchService;

    public AiMatchController(AiMatchService aiMatchService) {
        this.aiMatchService = aiMatchService;
    }

    @PostMapping("/match")
    public ResponseEntity<MatchResponse> match(@RequestBody MatchRequest req) {
        Long jobId = req.getJobId();
        Long userId = SecurityUtils.getCurrentUserId();
        MatchResponse resp = aiMatchService.matchCandidateToJob(
                userId,
                jobId,
                req.getResumeFileId(),
                req.getResumeText(),
                Boolean.TRUE.equals(req.getUseFileApi())
        );
        return ResponseEntity.ok(resp);
    }

    @PostMapping(value = "/match/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MatchResponse> matchUpload(
            @RequestParam("jobId") Long jobId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "useFileApi", required = false, defaultValue = "false") boolean useFileApi
    ) throws Exception {
        Long userId = SecurityUtils.getCurrentUserId();
        byte[] bytes = file.getBytes();
        MatchResponse resp = aiMatchService.matchByUploadedFile(userId, jobId, bytes, useFileApi);
        return ResponseEntity.ok(resp);
    }
}