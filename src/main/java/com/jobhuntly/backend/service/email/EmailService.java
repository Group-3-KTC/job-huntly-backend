package com.jobhuntly.backend.service.email;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

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
            helper.setText(content, true);

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new IllegalStateException("Can't send the email", e);
        }
    }

    @Override
    public SendResult send(OutgoingEmail mail) {
        Objects.requireNonNull(mail, "OutgoingEmail must not be null");
        if (mail.to() == null || mail.to().isBlank()) {
            throw new IllegalArgumentException("OutgoingEmail.to is required");
        }
        if (mail.subject() == null || mail.subject().isBlank()) {
            throw new IllegalArgumentException("OutgoingEmail.subject is required");
        }

        try {
            MimeMessage msg = gmailMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    msg,
                    true,
                    StandardCharsets.UTF_8.name()
            );

            if (mail.from() != null) {
                helper.setFrom(mail.from());
            } else {
                 helper.setFrom(new InternetAddress("help.jobhuntly@gmail.com", "JobHuntly Support", StandardCharsets.UTF_8.name()));
            }

            helper.setTo(mail.to());
            helper.setSubject(mail.subject());

            String html = mail.html();
            String text = mail.text();

            if (html != null && !html.isBlank()) {
                helper.setText(text != null ? text : "", html);
            } else {
                helper.setText(text != null ? text : "", false);
            }

            if (mail.inReplyTo() != null && !mail.inReplyTo().isBlank()) {
                msg.setHeader("In-Reply-To", ensureAngle(mail.inReplyTo()));
            }
            if (mail.references() != null && !mail.references().isBlank()) {
                msg.setHeader("References", ensureAngle(mail.references()));
            }

            // Gửi
            gmailMailSender.send(msg);

            String messageId = msg.getMessageID();
            if (messageId == null || messageId.isBlank()) {
                String domain = "gmail.com";
                messageId = "<" + java.util.UUID.randomUUID() + "@" + domain + ">";
            }
            return new SendResult(messageId);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to send email", e);
        }
    }

    private String ensureAngle(String v) {
        String s = v.trim();
        if (!s.startsWith("<")) s = "<" + s;
        if (!s.endsWith(">")) s = s + ">";
        return s;
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

            String effFromEmail = StringUtils.hasText(fromEmail) ? fromEmail : "help.jobhuntly@gmail.com";
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

            mime.saveChanges();
            String messageId = mime.getMessageID();

            gmailMailSender.send(mime);
            return messageId;
        } catch (Exception e) {
            throw new IllegalStateException("Can't send the email with threading", e);
        }
    }
}
