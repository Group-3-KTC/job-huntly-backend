package com.jobhuntly.backend.dto.response;

import java.time.LocalDateTime;

public record ApplicationResponse(
        Integer id,
        Integer userId,
        Integer jobId,
        String cv,
        String email,
        String status,
        String phoneNumber,
        String candidateName,
        LocalDateTime createdAt
) {
}
