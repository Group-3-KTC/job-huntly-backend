package com.jobhuntly.backend.service.email;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class EmailService implements EmailSender {
    private final JavaMailSender mailSender;
    private final JavaMailSender gmailMailSender;

    public EmailService(
            JavaMailSender mailSender,
            @Qualifier("gmailMailSender") JavaMailSender gmailMailSender
    ) {
        this.mailSender = mailSender;
        this.gmailMailSender = gmailMailSender;
    }


    @Override
    public void send(String to, String subject, String content) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setFrom("noreply@jobhuntly.io.vn", "JobHuntly Support");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true); // true = gửi HTML

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new IllegalStateException("Can't send the email", e);
        }
    }

    public String sendWithThreading(
            String to,
            String subject,
            String htmlBody,
            String inReplyTo,
            String references,
            String fromEmail,
            String fromName,
            @Nullable List<String> cc,
            @Nullable List<String> bcc
    ) {
        try {
            MimeMessage mime = gmailMailSender
                    .createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, false, "utf-8");

            String effFromEmail = StringUtils.hasText(fromEmail) ? fromEmail : "contact.jobhuntly@gmail.com";
            String effFromName  = StringUtils.hasText(fromName)  ? fromName  : "JobHuntly Support";

            helper.setFrom(effFromEmail, effFromName);
            helper.setTo(to);
            if (cc != null && !cc.isEmpty()) helper.setCc(cc.toArray(String[]::new));
            if (bcc != null && !bcc.isEmpty()) helper.setBcc(bcc.toArray(String[]::new));
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            if (StringUtils.hasText(inReplyTo)) {
                mime.addHeader("In-Reply-To", inReplyTo);
                if (!StringUtils.hasText(references)) {
                    references = inReplyTo;
                } else if (!references.contains(inReplyTo)) {
                    references = references + " " + inReplyTo;
                }
            }
            if (StringUtils.hasText(references)) {
                mime.addHeader("References", references);
            }

            mime.saveChanges(); // ensure Message-ID generated
            String messageId = mime.getMessageID();

            gmailMailSender.send(mime);
            return messageId;
        } catch (Exception e) {
            throw new IllegalStateException("Can't send the email with threading", e);
        }
    }
}
