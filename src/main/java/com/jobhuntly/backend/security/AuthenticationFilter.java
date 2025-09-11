package com.jobhuntly.backend.security;

import com.jobhuntly.backend.dto.auth.AppPrincipal;
import com.jobhuntly.backend.security.cookie.CookieProperties;
import com.jobhuntly.backend.security.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CookieProperties cookieProps;

    @Value("${backend.prefix:/api/v1}")
    private String backendPrefix;

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Bỏ qua preflight và các route public
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        String p = request.getServletPath();
        String base = backendPrefix.endsWith("/") ? backendPrefix.substring(0, backendPrefix.length() - 1) : backendPrefix;

        // auth/me ko đc bỏ qua mà phải filter
        if (p.startsWith(base + "/auth/me")) return false;

        // Bỏ qua các endpoint auth (login/register/refresh/activate...)
        if (p.startsWith(base + "/auth")) return true;

        // (tuỳ chọn) Bỏ qua swagger, docs, static files
        if (PATH_MATCHER.match("/swagger-ui/**", p)) return true;
        if (PATH_MATCHER.match("/v3/api-docs/**", p)) return true;
        if (PATH_MATCHER.match("/actuator/**", p)) return true;
        if (PATH_MATCHER.match("/public/**", p)) return true;

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(req, res);
            return;
        }

        String token = extractAccessToken(req);

        if (token != null && !token.isBlank()) {
            try {
                Claims c = jwtUtil.parseAndValidate(token);

                if (!jwtUtil.isAccess(c)) {
                    throw new JwtException("Wrong token type (expect access)");
                }

                String email = c.getSubject();
                String role = String.valueOf(c.get(JwtUtil.CLAIM_ROLE));
                Long userId = jwtUtil.userIdFromClaims(c);

                String authority = role != null && role.startsWith("ROLE_") ? role : "ROLE_" + role;

                AppPrincipal principal = SecurityUtils.getCurrentUser();
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(principal, null, List.of(new SimpleGrantedAuthority(authority)));
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));

                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (ExpiredJwtException e) {
                log.debug("Access token expired: {}", e.getMessage());
            } catch (JwtException e) {
                log.debug("Invalid access token: {}", e.getMessage());
            } catch (Exception e) {
                log.warn("JWT filter error", e);
            }
        }

        chain.doFilter(req, res);
    }

    private String extractAccessToken(HttpServletRequest req) {
        if (req.getCookies() != null) {
            String cookieName = (cookieProps.getAccessName() != null && !cookieProps.getAccessName().isBlank())
                    ? cookieProps.getAccessName() : "AT";
            for (Cookie c : req.getCookies()) {
                if (cookieName.equals(c.getName())) return c.getValue();
            }
        }

        String h = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (h != null && h.startsWith("Bearer ")) return h.substring(7);

        return null;
    }
}
