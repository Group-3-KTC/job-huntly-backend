package com.jobhuntly.backend.security.handlers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NoArgsConstructor;

import java.io.IOException;

@NoArgsConstructor
public class RestAuthenticationEntryPoint implements org.springframework.security.web.AuthenticationEntryPoint {
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            org.springframework.security.core.AuthenticationException authException
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType("application/json");
        String body = String.format("""
      {"type":"about:blank","title":"Unauthorized","status":401,"detail":"Authentication required","instance":"%s"}
      """, request.getRequestURI());
        response.getWriter().write(body);
    }
}
