package com.jobhuntly.backend.dto.response;

import lombok.Builder;

import java.time.Instant;

@Builder
public record NotificationItemResponse(
        Long id,
        String type,
        String title,
        String message,
        String link,
        Long companyId,
        Long jobId,
        Long applicationId,
        Instant createdAt,
        Instant readAt,
        boolean isRead
) {
}
