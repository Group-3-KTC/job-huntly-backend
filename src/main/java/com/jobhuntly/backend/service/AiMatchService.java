package com.jobhuntly.backend.service;

import com.jobhuntly.backend.dto.ai.MatchResponse;

public interface AiMatchService {
    MatchResponse matchCandidateToJob(Long userId, Long jobId, String resumeFileId, String resumeText, boolean useFileApi);

    MatchResponse matchByUploadedFile(Long userId, Long jobId, byte[] pdfBytes, boolean useFileApi);
}