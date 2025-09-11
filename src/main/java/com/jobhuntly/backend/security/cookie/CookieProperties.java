package com.jobhuntly.backend.security.cookie;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "security.cookie")
public class CookieProperties {
    private String domain = "";
    private boolean secure = true;
    private String sameSite = "Lax";
    private String accessName = "AT";
    private String refreshName = "RT";
}
