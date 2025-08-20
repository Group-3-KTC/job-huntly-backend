package com.jobhuntly.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "security")
public class AppProps {
    private Cors cors;
    private Jwt jwt;

    @Data
    public static class Cors {
        private List<String> allowedOrigins;
        private boolean allowCredentials = true;
    }

    @Data
    public static class Jwt {
        private String secret;
        private long expirySeconds;
        private String issuer;
        private String cookieName = "session";
        private String roleCookieName = "role";
    }
}
