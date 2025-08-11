package com.jobhuntly.backend.service.email;

public interface EmailSender {
    void send(String to, String subject, String content);
}
