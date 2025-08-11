package com.jobhuntly.backend.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService implements EmailSender {
    private final JavaMailSender mailSender;
    @Override
    public void send(String to, String subject, String content) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setFrom("noreply@jobhuntly.com", "JobHuntly Support");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true); // true = gửi HTML

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new IllegalStateException("Không thể gửi email", e);
        }
    }
}
