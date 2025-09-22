package com.jobhuntly.backend.security;

import com.jobhuntly.backend.dto.auth.AppPrincipal;
import com.jobhuntly.backend.entity.User;
import com.jobhuntly.backend.repository.UserRepository;
import com.jobhuntly.backend.security.cookie.AuthCookieService;
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
    private final AuthCookieService authCookieService;
    private final UserRepository userRepository;

    @Value("${backend.prefix:/api/v1}")
    private String backendPrefix;

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // skip preflight và các route public
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        String p = request.getServletPath();
        String base = backendPrefix.endsWith("/") ? backendPrefix.substring(0, backendPrefix.length() - 1) : backendPrefix;

        // auth/me ko đc bỏ qua mà phải filter
        if (p.startsWith(base + "/auth/me")) return false;

        // skip các endpoint auth (login/register/refresh/activate...)
        if (p.startsWith(base + "/auth")) return true;

        // skip swagger, docs, static files
        if (PATH_MATCHER.match("/swagger-ui/**", p)) return true;
        if (PATH_MATCHER.match("/v3/api-docs/**", p)) return true;
        if (PATH_MATCHER.match("/actuator/**", p)) return true;
        if (PATH_MATCHER.match("/public/**", p)) return true;

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(req, res);
            return;
        }

        String at = extractAccessToken(req);
        boolean authenticated = false;

        if (at != null && !at.isBlank()) {
            try {
                Claims c = jwtUtil.parseAndValidate(at);
                if (jwtUtil.isAccess(c)) {
                    String email = c.getSubject();
                    // String role = String.valueOf(c.get(JwtUtil.CLAIM_ROLE));
                    Object roleClaim = c.get(JwtUtil.CLAIM_ROLE); 
                    String roleName = null;
                    Long userId = jwtUtil.userIdFromClaims(c);
                    // String authority = (role != null && role.startsWith("ROLE_")) ? role : "ROLE_" + role;
                    if (roleClaim instanceof java.util.Map<?, ?> m) {
                        Object rn = m.get("roleName");
                        if (rn != null)
                            roleName = rn.toString();
                    } else if (roleClaim != null) {
                        roleName = roleClaim.toString();
                    }

                    if (roleName == null || roleName.isBlank()) {
                        log.debug("No role found in token");
                        chain.doFilter(req, res);
                        return;
                    }

                    String authority = "ROLE_" + roleName.toUpperCase();
                    AppPrincipal principal = new AppPrincipal(userId, email);
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(principal, null, List.of(new SimpleGrantedAuthority(authority)));
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    authenticated = true;
                } else {
                    log.debug("Reject token: not an access token");
                }
            } catch (ExpiredJwtException e) {
                log.debug("Access token expired: {}", e.getMessage());
            } catch (JwtException e) {
                log.debug("Invalid access token: {}", e.getMessage());
            } catch (Exception e) {
                log.debug("JWT filter error", e);
            }
        }

        if (!authenticated) {
            String rt = extractCookie(req, cookieProps.getRefreshName(), "RT");
            if (rt != null && !rt.isBlank()) {
                try {
                    Claims rc = jwtUtil.parseAndValidate(rt);
                    if (jwtUtil.isRefresh(rc)) {
                        Long userId = jwtUtil.userIdFromClaims(rc);
                        User user = userRepository.findById(userId).orElse(null);
                        if (user != null) {
                            String newAt = jwtUtil.issueAccessToken(user);

                            authCookieService.setAccessCookie(res, newAt, jwtUtil.getAccessTtl());

                            setAuthFromUser(req, user);

                            authenticated = true;
                        } else {
                            log.debug("Silent refresh: user {} not found", userId);
                        }
                    }
                } catch (ExpiredJwtException e) {
                    log.debug("Refresh token expired: {}", e.getMessage());
                } catch (JwtException e) {
                    log.debug("Invalid refresh token: {}", e.getMessage());
                } catch (Exception e) {
                    log.debug("Silent refresh error", e);
                }
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
    private String extractCookie(HttpServletRequest req, String confName, String fallback) {
        String name = (confName != null && !confName.isBlank()) ? confName : fallback;
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) if (name.equals(c.getName())) return c.getValue();
        return null;
    }

    private void setAuthFromUser(HttpServletRequest req, User user) {
        String roleName = (user.getRole() != null && user.getRole().getRoleName() != null)
                ? user.getRole().getRoleName().toUpperCase()
                : "USER";
        String authority = "ROLE_" + roleName;
        AppPrincipal principal = new AppPrincipal(user.getId(), user.getEmail());
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        principal, null, List.of(new SimpleGrantedAuthority(authority)));
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }


}
