package com.jobhuntly.backend.websocket;

import com.jobhuntly.backend.security.WsPrincipal;
import com.jobhuntly.backend.security.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WsHandshakeAuthInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest servlet) {
            HttpServletRequest http = servlet.getServletRequest();

            String token = null;
            Cookie[] cookies = http.getCookies();
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if ("AT".equals(c.getName()) || "access_token".equals(c.getName())) {
                        token = c.getValue();
                        break;
                    }
                }
            }

            if (token != null) {
                try {
                    Claims claims = jwtUtil.parseAndValidate(token);
                    if (jwtUtil.isAccess(claims)) {
                        Long userId = jwtUtil.userIdFromClaims(claims);
                        if (userId != null) {
                            attributes.put("wsPrincipal", new WsPrincipal(userId));
                            return true;
                        }
                    }
                } catch (JwtException ex) {
                    // invalid/expired signature, ignore -> unauthorized below
                }
            }
        }

        try { response.setStatusCode(HttpStatus.UNAUTHORIZED); } catch (Exception ignored) {}
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception ex) {
        // no-op
    }
}
