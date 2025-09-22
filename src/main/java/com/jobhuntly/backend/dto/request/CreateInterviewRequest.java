package com.jobhuntly.backend.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CreateInterviewRequest(
    @NotNull Long jobId,
    @NotNull Long companyId,
    @NotNull Long candidateId,
    @NotNull @Future LocalDateTime scheduledAt,
    @Min(15) Integer durationMinutes) {
}