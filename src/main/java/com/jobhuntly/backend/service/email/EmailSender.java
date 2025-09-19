package com.jobhuntly.backend.service.email;

import jakarta.mail.internet.InternetAddress;
import lombok.Data;
import lombok.experimental.Accessors;

public interface EmailSender {
    @Deprecated
    default void send(String to, String subject, String content) {
        send(new OutgoingEmail()
                .to(to)
                .subject(subject)
                .text(content)
                .html(null));
    }

    SendResult send(OutgoingEmail email);

    record SendResult(String messageId) {}

    @Data
    @Accessors(fluent = true)
    class OutgoingEmail {
        private InternetAddress from;
        private String to;
        private String subject;
        private String text;
        private String html;
        private String inReplyTo;
        private String references;
    }
}
