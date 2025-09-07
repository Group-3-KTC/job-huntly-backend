package com.jobhuntly.backend.vnpay;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.jobhuntly.backend.vnpay.VnPayProperties;
import org.springframework.stereotype.Component;


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class VnPayGateway {

    private final VnPayProperties props;

    private static final DateTimeFormatter VNP_TS_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final ZoneId VN_TZ = ZoneId.of("Asia/Ho_Chi_Minh");

    /** Public API khớp với PaymentService */
    public String buildPayUrl(String txnRef,
                              long amountVnd,
                              String bankCode,
                              String locale,
                              HttpServletRequest req) {

        long vnpAmount = amountVnd * 100; // VNPay yêu cầu *100

        OffsetDateTime now = OffsetDateTime.now(VN_TZ);
        OffsetDateTime exp = now.plusMinutes(props.getExpireMinutes());

        String clientIp = normalizeIp(extractClientIp(req));
        String effectiveLocale = (locale != null && !locale.isBlank()) ? locale : props.getLocale();

        SortedMap<String, String> params = new TreeMap<>();
        params.put("vnp_Version", props.getVersion());
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", props.getTmnCode());
        params.put("vnp_Amount", String.valueOf(vnpAmount));
        params.put("vnp_CurrCode", props.getCurrCode());   // VND
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", "Thanh toan don hang " + txnRef);
        params.put("vnp_OrderType", props.getOrderType()); // other/billpayment...
        params.put("vnp_Locale", effectiveLocale);         // vn|en
        params.put("vnp_IpAddr", clientIp);
        params.put("vnp_ReturnUrl", props.getReturnUrl());
        params.put("vnp_CreateDate", VNP_TS_FMT.format(now));
        params.put("vnp_ExpireDate", VNP_TS_FMT.format(exp));

        if (bankCode != null && !bankCode.isBlank()) {
            params.put("vnp_BankCode", bankCode);
        }

        // ---- LOG bối cảnh
        log.info("[VNPAY] Build URL: txnRef={}, amountVnd={}, vnp_Amount={}, bankCode={}, locale={}, ip={}",
                txnRef, amountVnd, vnpAmount, bankCode, effectiveLocale, clientIp);
        log.info("[VNPAY] Props: tmnCode={}, version={}, returnUrl={}, currCode={}, orderType={}, expireMin={}, secret.len={}",
                props.getTmnCode(), props.getVersion(), props.getReturnUrl(), props.getCurrCode(),
                props.getOrderType(), props.getExpireMinutes(),
                props.getSecretKey() == null ? null : props.getSecretKey().length());
        log.debug("[VNPAY] Sorted params -> {}", params);

        // === QUAN TRỌNG: KÝ TRÊN CHUỖI ĐÃ URL-ENCODE (hashData) ===
        String hashData = joinQuery(params, /*rawForHash=*/false); // ENCODED
        String secureHash = HmacUtil.hmacSHA512(props.getSecretKey(), hashData);

        log.info("[VNPAY] HashData(ENCODED) = {}", hashData);
        log.info("[VNPAY] CalculatedHash    = {}", secureHash);

        String finalUrl = props.getPayUrl() + "?" + hashData
                + "&vnp_SecureHashType=HmacSHA512"
                + "&vnp_SecureHash=" + secureHash;
        log.info("[VNPAY] Final Redirect URL = {}", finalUrl);

        return finalUrl;
    }

    /** Verify chữ ký cho RETURN/IPN */
    public boolean verifySignature(Map<String, String> queryParams) {
        SortedMap<String, String> params = new TreeMap<>();
        for (Map.Entry<String, String> e : queryParams.entrySet()) {
            String k = e.getKey();
            if ("vnp_SecureHash".equalsIgnoreCase(k) || "vnp_SecureHashType".equalsIgnoreCase(k)) continue;
            params.put(k, e.getValue());
        }
        String received = queryParams.get("vnp_SecureHash");

        // === QUAN TRỌNG: RE-ENCODE lại rồi mới ký (giống buildPayUrl) ===
        String hashData = joinQuery(params, /*rawForHash=*/false); // ENCODED
        String calculated = HmacUtil.hmacSHA512(props.getSecretKey(), hashData);

        log.info("[VNPAY][VERIFY] HashData(ENCODED) = {}", hashData);
        log.info("[VNPAY][VERIFY] ReceivedHash      = {}", received);
        log.info("[VNPAY][VERIFY] Calculated        = {}", calculated);
        log.info("[VNPAY][VERIFY] MATCH? {}", String.valueOf(calculated != null && calculated.equalsIgnoreCase(received)));

        return calculated != null && calculated.equalsIgnoreCase(received);
    }

    /* ===== Helpers ===== */

    private String extractClientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) ip = req.getRemoteAddr();
        if (ip != null && ip.contains(",")) ip = ip.split(",")[0].trim();
        return ip;
    }

    private String normalizeIp(String ip) {
        if (ip == null) return null;
        // map IPv6 loopback về IPv4 cho dễ nhìn
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) return "127.0.0.1";
        return ip;
    }

    private String joinQuery(SortedMap<String, String> params, boolean rawForHash) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (!first) sb.append("&");
            String k = e.getKey();
            String v = e.getValue();

            if (rawForHash) {
                // (Không dùng cho VNPAY)
                sb.append(k).append("=").append(v);
            } else {
                // Dùng đúng URLEncoder mặc định (space -> '+')
                sb.append(URLEncoder.encode(k, StandardCharsets.UTF_8))
                        .append("=")
                        .append(URLEncoder.encode(v, StandardCharsets.UTF_8));
            }
            first = false;
        }
        return sb.toString();
    }

    private String mask(String s) {
        if (s == null || s.length() <= 6) return "******";
        int keep = 4;
        return s.substring(0, keep) + "***" + s.substring(s.length() - keep);
    }
}
