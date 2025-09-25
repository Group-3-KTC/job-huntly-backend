package com.jobhuntly.backend.dto.response;

import java.time.LocalDateTime;

public record InterviewMetaDto(
        Long interviewId,
        Long jobId,
        String jobTitle,
        Long companyId,
        String companyName,
        LocalDateTime scheduledAt,
        Integer durationMinutes,
        String meetingRoom,
        String meetingUrl,
        String status) {
}
