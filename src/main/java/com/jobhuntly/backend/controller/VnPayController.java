package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.response.CallbackResponse;
import com.jobhuntly.backend.dto.response.CheckoutResponse;
import com.jobhuntly.backend.dto.response.PaymentResponseByCompany;
import com.jobhuntly.backend.service.impl.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${backend.prefix}/payments/vnpay")
@RequiredArgsConstructor
public class VnPayController {
    private final PaymentService paymentService;

    /**
     * Tạo URL thanh toán VNPay (redirect)
     * - amountVnd: optional (null -> lấy giá từ package)
     * - bankCode: NCB, locale: optional
     */
    @PostMapping(value = "/checkout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CheckoutResponse> checkout(
            @RequestParam Long companyId,
            @RequestParam Long packageId,
            @RequestParam(required = false) Long amountVnd,
            @RequestParam(required = false) String bankCode,
            @RequestParam(required = false, defaultValue = "vn") String locale,
            HttpServletRequest request
    ) {
        CheckoutResponse res = paymentService.createCheckout(
                companyId, packageId, amountVnd, bankCode, locale, request
        );
        return ResponseEntity.ok(res);
    }

    /**
     * VNPay Return URL (user browser quay về)
     * VNPay sẽ gọi dạng GET: /return?vnp_Amount=...&vnp_ResponseCode=...&vnp_TxnRef=...&...
     *
     * Lưu ý: nếu bạn muốn redirect về FE thay vì trả JSON ở đây:
     *  - Query trạng thái qua service
     *  - Sau đó 302 về FE: /payment-result?code=...&txnRef=...
     */
    @GetMapping(value = "/return", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CallbackResponse> vnpReturn(@RequestParam Map<String, String> queryParams) {
        CallbackResponse res = paymentService.handleVnpayCallback(queryParams, "RETURN");
        return ResponseEntity.ok(res);
    }

    /**
     * VNPay IPN (Instant Payment Notification)
     * VNPay sẽ gọi server-to-server (thường là GET, đôi khi POST).
     * Ở đây chấp nhận cả hai cho tiện.
     *
     * Theo tài liệu VNPay, bạn nên trả JSON/text đơn giản.
     */
    @RequestMapping(
            value = "/ipn",
            method = {RequestMethod.GET, RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<CallbackResponse> vnpIpn(
            @RequestParam Map<String, String> queryParams,
            @RequestBody(required = false) MultiValueMap<String, String> formParams // phòng trường hợp VNPay POST form
    ) {
        Map<String, String> data = new HashMap<>(queryParams);
        if (formParams != null) {
            // Merge các field từ form (nếu POST)
            for (String key : formParams.keySet()) {
                if (!data.containsKey(key)) {
                    data.put(key, formParams.getFirst(key));
                }
            }
        }
        CallbackResponse res = paymentService.handleVnpayCallback(data, "IPN");
        return ResponseEntity.ok(res);
    }

    // Get lịch sử thanh toán by Company
    @GetMapping(value = "/companies/{companyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<PaymentResponseByCompany> getCompanyPayments(@PathVariable Long companyId,
                                                             @PageableDefault(size = 10) Pageable pageable
                                                             ) {
        return paymentService.getByCompany(companyId, pageable);
    }
}
