package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.response.CallbackResponse;
import com.jobhuntly.backend.dto.response.CheckoutResponse;
import com.jobhuntly.backend.dto.response.PaymentResponseByCompany;
import com.jobhuntly.backend.service.impl.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
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

    @GetMapping("/return")
    public ResponseEntity<Void> vnpReturn(@RequestParam Map<String, String> queryParams,
                                          HttpServletRequest request) {
        CallbackResponse res = paymentService.handleVnpayCallback(queryParams, "RETURN");

        String txnRef       = queryParams.get("vnp_TxnRef");
        String payStatus    = res.isSuccess() ? "success" : "fail";
        String payCode      = res.getMessage();

        String redirectUrl = UriComponentsBuilder
                .fromHttpUrl(resolveFrontendBaseUrl(request))
                .path(resolveVipPath())
                .queryParam("pay_status", payStatus)
                .queryParam("pay_code",   payCode)
                .queryParam("txnRef",     txnRef)
                .build()
                .toUriString();

        return ResponseEntity.status(HttpStatus.FOUND)  // 302
                .location(URI.create(redirectUrl))
                .build();
    }

    @Value("${app.frontend.base-url:}")
    private String configuredFrontendBaseUrl;

    @Value("${app.frontend.vip-path:/recruiter/companyVip}")
    private String vipPath;

    /** Ưu tiên cấu hình; nếu trống thì thử Origin/Referer; cuối cùng fallback localhost */
    private String resolveFrontendBaseUrl(HttpServletRequest req) {
        if (configuredFrontendBaseUrl != null && !configuredFrontendBaseUrl.isBlank()) {
            return configuredFrontendBaseUrl;
        }
        String origin = req.getHeader("Origin");
        if (origin != null && !origin.isBlank()) return origin;

        String referer = req.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            try {
                URI u = URI.create(referer);
                String scheme = (u.getScheme() != null) ? u.getScheme() : "https";
                String host   = u.getHost();
                String port   = (u.getPort() > 0) ? (":" + u.getPort()) : "";
                return scheme + "://" + host + port;
            } catch (Exception ignore) {}
        }
        return "http://localhost:3000";
    }

    private String resolveVipPath() {
        return (vipPath != null && !vipPath.isBlank()) ? vipPath : "/recruiter/companyVip";
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
