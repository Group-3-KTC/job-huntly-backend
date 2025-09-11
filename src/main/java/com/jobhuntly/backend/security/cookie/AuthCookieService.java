package com.jobhuntly.backend.security.cookie;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.Duration;
import java.util.Optional;

public interface AuthCookieService {
    void setAccessCookie(HttpServletResponse res, String token, Duration ttl);
    void setRefreshCookie(HttpServletResponse res, String token, Duration ttl);

    void clearAccessCookie(HttpServletResponse res);
    void clearRefreshCookie(HttpServletResponse res);
    void clearAuthCookies(HttpServletRequest req, HttpServletResponse res);

    Optional<String> readCookie(HttpServletRequest req, String name);
}
