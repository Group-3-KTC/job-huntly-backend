package com.jobhuntly.backend.vnpay;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

final class HmacUtil {
    private HmacUtil() {}

    static String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) sb.append(String.format("%02X", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot sign data using HmacSHA512", e);
        }
    }
}
