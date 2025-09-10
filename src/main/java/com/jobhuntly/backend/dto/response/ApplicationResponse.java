package com.jobhuntly.backend.dto.response;

import java.time.LocalDateTime;

public record ApplicationResponse(
        Integer id,
        Long userId,
        Long jobId,
        String cv,
        String email,
        String status,
        String phoneNumber,
        String candidateName,
        String description,
        LocalDateTime createdAt,
        String cvDownload
) {
}
