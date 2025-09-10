package com.jobhuntly.backend.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthCookieService {
    void setAuthCookie(HttpServletRequest req, HttpServletResponse res, String token, long ttlSeconds);
    void clearAuthCookie(HttpServletRequest req, HttpServletResponse res);
}
