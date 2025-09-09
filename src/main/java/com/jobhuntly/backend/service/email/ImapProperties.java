package com.jobhuntly.backend.service.email;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "mail.imap")
@Data
@Component
public class ImapProperties {
    private String host;
    private Integer port;
    private String username;
    private String password;
    private String folder = "INBOX";
    private Long pollIntervalMs = 30000L;
    private Boolean markSeen = true;
    private String moveToFolder;
    private Boolean peek = true;
}
