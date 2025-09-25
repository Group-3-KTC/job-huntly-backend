package com.jobhuntly.backend.service;

import com.jobhuntly.backend.dto.ai.MatchResponse;

public interface AiMatchService {
    MatchResponse matchCandidateToJob(Long userId, Long jobId, String resumeFileId, String resumeText, boolean useFileApi);

    // MỚI: chấm điểm từ file PDF người dùng upload trực tiếp
    MatchResponse matchByUploadedFile(Long userId, Long jobId, byte[] pdfBytes, boolean useFileApi);
}