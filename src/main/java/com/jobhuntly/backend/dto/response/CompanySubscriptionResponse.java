package com.jobhuntly.backend.dto.response;

import java.time.OffsetDateTime;

public record CompanySubscriptionResponse(
        Long subscriptionId,
        Long companyId,
        Long packageId,
        String status,
        OffsetDateTime startAt,
        OffsetDateTime endAt
) {
}
