package com.jobhuntly.backend.security.cookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthCookieServiceImpl implements AuthCookieService {

    private final CookieProperties props;

    @Override
    public void setAccessCookie(HttpServletResponse res, String token, Duration ttl) {
        addCookie(res, props.getAccessName(), token, ttl, true);
    }

    @Override
    public void setRefreshCookie(HttpServletResponse res, String token, Duration ttl) {
        addCookie(res, props.getRefreshName(), token, ttl, true);
    }

    @Override
    public void clearAccessCookie(HttpServletResponse res) {
        addCookie(res, props.getAccessName(), "", Duration.ZERO, true);
    }

    @Override
    public void clearRefreshCookie(HttpServletResponse res) {
        addCookie(res, props.getRefreshName(), "", Duration.ZERO, true);
    }

    @Override
    public void clearAuthCookies(HttpServletRequest req, HttpServletResponse res) {
        clearAccessCookie(res);
        clearRefreshCookie(res);
    }

    @Override
    public Optional<String> readCookie(HttpServletRequest req, String name) {
        if (req.getCookies() == null) return Optional.empty();
        return Arrays.stream(req.getCookies())
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    private void addCookie(HttpServletResponse res, String name, String value, Duration ttl, boolean httpOnly) {
        boolean secure = props.isSecure();
        String sameSite = normalizeSameSite(props.getSameSite());

        if ("None".equalsIgnoreCase(sameSite) && !secure) {
            secure = true;
        }

        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .path("/")
                .httpOnly(httpOnly)
                .secure(secure)
                .sameSite(sameSite);

        if (props.getDomain() != null && !props.getDomain().isBlank()) {
            builder.domain(props.getDomain());
        }

        if (ttl != null) {
            if (Duration.ZERO.equals(ttl)) {
                builder.maxAge(0);
            } else {
                long seconds = Math.max(0, ttl.getSeconds());
                builder.maxAge(seconds);
            }
        }

        ResponseCookie cookie = builder.build();
        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String normalizeSameSite(String s) {
        if (s == null) return "Lax";
        String v = s.trim();
        if (v.equalsIgnoreCase("none")) return "None";
        if (v.equalsIgnoreCase("lax"))  return "Lax";
        if (v.equalsIgnoreCase("strict")) return "Strict";
        return "Lax";
    }
}
