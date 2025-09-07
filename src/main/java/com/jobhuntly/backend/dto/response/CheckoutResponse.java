package com.jobhuntly.backend.dto.response;

import lombok.Builder;
import lombok.Value;

@Value @Builder
public class CheckoutResponse {
    String code;
    String message;
    String txnRef;
    String paymentUrl;
    PaymentResponse payment;
}
