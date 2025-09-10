package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.service.AuthCookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthCookieServiceImpl implements AuthCookieService {

    @Value("${security.jwt.cookie-name:access_token}")
    private String cookieName;

    @Value("${security.jwt.cookie-domain:}") // thêm nếu có domain
    private String cookieDomain;

    public void setAuthCookie(HttpServletRequest req, HttpServletResponse res, String token, long ttlSeconds) {
        boolean secure = isSecure(req);
        String sameSite = secure ? "None" : "Lax";

        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(ttlSeconds);

        if (cookieDomain != null && !cookieDomain.isBlank()) {
            b.domain(cookieDomain);
        }

        res.addHeader(HttpHeaders.SET_COOKIE, b.build().toString());
    }

    public void clearAuthCookie(HttpServletRequest req, HttpServletResponse res) {
        boolean secure = isSecure(req);
        String sameSite = secure ? "None" : "Lax";

        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(0);

        if (cookieDomain != null && !cookieDomain.isBlank()) {
            b.domain(cookieDomain);
        }

        res.addHeader(HttpHeaders.SET_COOKIE, b.build().toString());
    }

    // check http or https
    private boolean isSecure(HttpServletRequest req) {
        if (req == null) return false;
        String xfProto = req.getHeader("X-Forwarded-Proto");
        if (xfProto != null) return "https".equalsIgnoreCase(xfProto);
        return req.isSecure();
    }


}
