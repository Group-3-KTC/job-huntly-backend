package com.jobhuntly.backend.dto.response;

import java.time.OffsetDateTime;

public record PaymentResponseByCompany(
        Long id,
        Long companyId,
        String provider,     // VNPAY / MOMO ...
        String status,       // SUCCESS / FAILED ...
        Long amountVnd,
        String currency,     // VND
        String txnRef,
        String providerTxn,
        String orderInfo,
        OffsetDateTime paidAt,
        OffsetDateTime createdAt
) {
}
