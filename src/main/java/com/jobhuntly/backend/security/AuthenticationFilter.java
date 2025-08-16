package com.jobhuntly.backend.security;

import com.jobhuntly.backend.security.jwt.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    public static final String ACCESS_TOKEN_COOKIE = "ACCESS_TOKEN";

    private final JwtUtil jwtUtil;
    private final UserDetailsService uds;
    private final HandlerExceptionResolver exceptionResolver;

    // ✅ Constructor thủ công + Qualifier để chọn đúng bean
    public AuthenticationFilter(
            JwtUtil jwtUtil,
            UserDetailsService uds,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver
    ) {
        this.jwtUtil = jwtUtil;
        this.uds = uds;
        this.exceptionResolver = exceptionResolver;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest req,
                                    @NonNull HttpServletResponse res,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(req, res);
            return;
        }

        // 1) Lấy token từ cookie
        String token = extractCookie(req, ACCESS_TOKEN_COOKIE);
        // 2) Fallback: Authorization: Bearer ...
        if (token == null || token.isBlank()) {
            token = extractBearer(req.getHeader(HttpHeaders.AUTHORIZATION));
        }

        if (token == null || token.isBlank()) {
            chain.doFilter(req, res);
            return;
        }

        try {
            String username = jwtUtil.extractUsername(token);
            if (username != null && jwtUtil.isTokenValid(token, username)) {
                UserDetails user = uds.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception ex) {
            log.debug("JWT filter error: {}", ex.getMessage());
            // ✅ Đẩy lỗi về GlobalExceptionHandler
            exceptionResolver.resolveException(req, res, null, ex);
            return; // dừng chain sau khi đã resolve
        }

        chain.doFilter(req, res);
    }

    private static String extractCookie(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;
        return Arrays.stream(cookies)
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private static String extractBearer(String authHeader) {
        return Optional.ofNullable(authHeader)
                .filter(h -> h.startsWith("Bearer "))
                .map(h -> h.substring(7))
                .orElse(null);
    }
}
