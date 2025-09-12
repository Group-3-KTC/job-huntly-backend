package com.jobhuntly.backend.dto.response;

import lombok.Builder;

import java.time.Instant;

@Builder
public record NotificationPayload(
        String type,
        String title,
        String message,
        Long companyId,
        Long jobId,
        Long applicationId,
        Instant createdAt
) {
}
