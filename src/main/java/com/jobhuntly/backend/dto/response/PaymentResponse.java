package com.jobhuntly.backend.dto.response;

import java.time.OffsetDateTime;

public record PaymentResponse(
        Long paymentId,
        Long companyId,
        Long packageId,
        Long amountVnd,
        String currency,
        String status,
        String provider,
        String txnRef,
        String providerTxn,
        OffsetDateTime paidAt,
        OffsetDateTime createdAt
) {
}
