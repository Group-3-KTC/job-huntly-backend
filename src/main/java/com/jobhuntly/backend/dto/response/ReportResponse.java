package com.jobhuntly.backend.dto.response;

import com.jobhuntly.backend.entity.enums.ReportType;

import java.time.LocalDateTime;

public record ReportResponse(
        Long id,
        ReportType reportType,
        Long reportedContentId,
        String description,
        String status,
        LocalDateTime createdAt,
        Long userId
) {
}
