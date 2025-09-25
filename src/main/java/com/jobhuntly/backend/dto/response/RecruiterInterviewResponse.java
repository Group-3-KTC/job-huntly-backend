package com.jobhuntly.backend.dto.response;

import java.time.LocalDateTime;

public record RecruiterInterviewResponse(
        Long interviewId,
        LocalDateTime scheduledAt,
        Integer durationMinutes,
        String status,
        String meetingUrl,

        Long jobId,
        String jobTitle,

        Long candidateId,
        String candidateName,
        String candidateEmail,
        String meetingRoom) {
}
