package com.jobhuntly.backend.dto.response;

public record AnalyticsKpiResponse(
        long activeJobs,
        long totalApplicants,
        long applicantsLast30Days
) {} 