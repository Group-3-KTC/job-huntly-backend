package com.jobhuntly.backend.dto.response;

import java.time.LocalDateTime;

public record CandidateInterviewResponse(
        Long interviewId,
        LocalDateTime scheduledAt,
        Integer durationMinutes,
        String status,
        String meetingUrl,

        Long jobId,
        String jobTitle,

        Long companyId,
        String companyName
) {
}