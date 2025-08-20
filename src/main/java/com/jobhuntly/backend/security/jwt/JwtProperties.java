package com.jobhuntly.backend.security.jwt;

import com.jobhuntly.backend.config.AppProps;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtProperties {
    private final AppProps props;

    public String getSecret() {
        return props.getJwt().getSecret();
    }

    public long getExpirySeconds() {
        return props.getJwt().getExpirySeconds();
    }

    public String getIssuer() {
        return props.getJwt().getIssuer();
    }

    public String getCookieName() {
        return props.getJwt().getCookieName();
    }

    public String getRoleCookieName() {
        return props.getJwt().getRoleCookieName();
    }
}

