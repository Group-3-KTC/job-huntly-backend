package com.jobhuntly.backend.dto.response;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record ApplicationByUserResponse(
        Long applicationId,
        String status,
        String cv,
        String description,
        LocalDateTime createdAt,

        Integer jobId,
        String title,
        String salaryDisplay,

        LocalDate expiredDate,

        Integer companyId,
        String companyName,
        String companyAvatar
) {
}
