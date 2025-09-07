package com.jobhuntly.backend.vnpay;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "vnpay")
public class VnPayProperties {
    /** https://sandbox.vnpayment.vn/paymentv2/vpcpay.html */
    private String payUrl;

    /** https://sandbox.vnpayment.vn/merchant_webapi/api/transaction */
    private String apiUrl;

    /** https://your-domain.com/api/v1/payments/vnpay/return */
    private String returnUrl;

    /** Merchant code */
    private String tmnCode;

    private String secretKey;

    private String version = "2.1.0";

    private Integer expireMinutes = 15;

    private String currCode = "VND";
    private String locale = "vn";
    private String orderType = "other";
}
