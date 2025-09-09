package com.jobhuntly.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record SavedJobResponse(
        Long jobId,
        LocalDateTime createdAt,
        String companyName,
        String companyAvatar,
        Long companyId,
        String titleJob,
        String salaryDisplay,
        @JsonProperty("skill_job") List<String> skillJob
) {
}
