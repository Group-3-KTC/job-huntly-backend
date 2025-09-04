package com.jobhuntly.backend.dto.response;

import lombok.Builder;
import lombok.Value;

@Value @Builder
public class CallbackResponse {
    boolean success;
    String message;                  // "PAID" | "ALREADY_PAID" | "FAILED_CODE_xx" | "INVALID_SIGNATURE"
    PaymentResponse payment;         // payment sau khi update
    CompanySubscriptionResponse subscription; // null nếu thất bại hoặc packageId null
}
