package com.jobhuntly.backend.security.jwt;

import com.jobhuntly.backend.config.AppProps;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {
    private String secret;
    private Duration expirySeconds;
    private String issuer;
    private String cookieName = "session";
    private String roleCookieName = "role";
}

