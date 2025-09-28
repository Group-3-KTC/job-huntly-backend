package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.entity.Interview;
import com.jobhuntly.backend.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import org.springframework.core.io.ByteArrayResource;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username:no-reply@jobhuntly.io.vn}")
    private String fromEmail;

    @Value("${app.name:JobHuntly}")
    private String appName;

    @Value("${mail.support:help.jobhuntly@gmail.com}")
    private String supportEmail;

    @Value("${mail.logo:https://res.cloudinary.com/dfbqhd5ht/image/upload/v1757058535/logo-title-white_yjzvvr.png}")
    private String logoUrl;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private void sendHtml(String to, String subject, String template, Map<String, Object> model) {
        try {
            Context ctx = new Context(Locale.getDefault());
            model.forEach(ctx::setVariable);

            String html = templateEngine.process(template, ctx);

            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    "UTF-8");
            helper.setFrom(fromEmail, appName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(msg);
        } catch (Exception e) {
            log.error("Failed to send mail '{}' to {}", subject, to, e);
        }
    }

    private Map<String, Object> baseModel(Interview i, String title, String ctaLabel, String ctaHref) {
        return Map.of(
                "appName", appName,
                "logoUrl", logoUrl,
                "supportEmail", supportEmail,
                "year", String.valueOf(java.time.Year.now().getValue()),

                // content
                "title", title,
                "meetingUrl", ctaHref,
                "scheduledAt", i.getScheduledAt() != null ? i.getScheduledAt().format(TIME_FMT) : "",
                "durationMinutes", i.getDurationMinutes(),
                "ctaHref", ctaHref,
                "ctaLabel", ctaLabel);
    }

    @Override
    public void sendInterviewCreatedTo(String toEmail, Interview i, String portalUrl) throws Exception {
        var model = baseModel(i, "Interview Scheduled", "Join the Meeting", portalUrl);
        String tpl = "mail/interview-created";

        Context ctx = new Context(Locale.getDefault());
        model.forEach(ctx::setVariable);
        String html = templateEngine.process(tpl, ctx);

        byte[] ics = buildIcsForRecipient(i, fromEmail, toEmail, portalUrl);
        sendWithIcs(toEmail, "[Interview] New interview scheduled", html, ics);
    }

    private byte[] buildIcsForRecipient(Interview i, String organizerEmail, String attendeeEmail, String joinUrl) {
        String uid = "jh-" + i.getInterviewId() + "@jobhuntly";
        ZoneId tz = ZoneId.systemDefault();
        ZonedDateTime start = i.getScheduledAt().atZone(tz);
        ZonedDateTime end = start.plusMinutes(i.getDurationMinutes() == null ? 60 : i.getDurationMinutes());
        DateTimeFormatter utc = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneId.of("UTC"));

        String desc = joinUrl == null ? "" : joinUrl;
        String loc = joinUrl == null ? "" : joinUrl;

        String ics = String.join("\r\n",
                "BEGIN:VCALENDAR",
                "PRODID:-//JobHuntly//Interview//EN",
                "VERSION:2.0",
                "CALSCALE:GREGORIAN",
                "METHOD:REQUEST",
                "BEGIN:VEVENT",
                "UID:" + uid,
                "DTSTAMP:" + utc.format(Instant.now()),
                "DTSTART:" + utc.format(start.toInstant()),
                "DTEND:" + utc.format(end.toInstant()),
                "SUMMARY:Interview",
                "DESCRIPTION:" + escapeText(desc),
                "LOCATION:" + escapeText(loc),
                "ORGANIZER:MAILTO:" + organizerEmail,
                "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=TRUE:MAILTO:" + attendeeEmail,
                "STATUS:CONFIRMED",
                "END:VEVENT",
                "END:VCALENDAR",
                "");
        return ics.getBytes(StandardCharsets.UTF_8);
    }

    private String escapeText(String s) {
        if (s == null)
            return "";
        return s.replace("\\", "\\\\")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace(",", "\\,")
                .replace(";", "\\;");
    }

    @Override
    public void sendInterviewCreated(String recruiterEmail, String candidateEmail, Interview i) {
        var model = baseModel(i, "Interview Scheduled", "Join the Meeting", i.getMeetingUrl());
        String tpl = "mail/interview-created";

        // Render HTML từ Thymeleaf (vì ta cần chuỗi để gắn vào sendWithIcs)
        try {
            Context ctx = new Context(Locale.getDefault());
            model.forEach(ctx::setVariable);
            String html = templateEngine.process(tpl, ctx);

            // Dùng fromEmail làm organizer cho ICS
            byte[] ics = buildIcs(i, fromEmail, recruiterEmail, candidateEmail);

            // Gửi cho recruiter + candidate
            sendWithIcs(recruiterEmail, "[Interview] New interview scheduled", html, ics);
            sendWithIcs(candidateEmail, "[Interview] New interview scheduled", html, ics);
        } catch (Exception e) {
            log.error("Failed to send interview created with ICS", e);
            // fallback: gửi thường nếu attach lỗi
            sendHtml(recruiterEmail, "[Interview] New interview scheduled", tpl, model);
            sendHtml(candidateEmail, "[Interview] New interview scheduled", tpl, model);
        }
    }

    @Override
    public void sendInterviewReminder(String recruiterEmail, String candidateEmail, Interview i) {
        var model = baseModel(i, "Interview Reminder (in ~1 hour)", "Open Meeting", i.getMeetingUrl());
        String tpl = "mail/interview-reminder";

        sendHtml(recruiterEmail, "[Interview] Reminder", tpl, model);
        sendHtml(candidateEmail, "[Interview] Reminder", tpl, model);
    }

    @Override
    public void sendInterviewStatusChangedToRecruiter(String recruiterEmail, Interview i, String newStatus) {
        var model = baseModel(i, "Candidate updated interview status: " + newStatus, "View in JobHuntly", null);
        String tpl = "mail/interview-status";

        sendHtml(recruiterEmail, "[Interview] Candidate " + newStatus, tpl, model);
    }

    // Gửi HTML + đính kèm ICS
    private void sendWithIcs(String to, String subject, String html, byte[] icsBytes) throws Exception {
        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8"); // multipart = true để attach
        helper.setFrom(fromEmail, appName);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        helper.addAttachment(
                "invite.ics",
                new ByteArrayResource(icsBytes),
                "text/calendar; charset=UTF-8; method=REQUEST");
        mailSender.send(msg);
    }

    // Tạo nội dung ICS (METHOD:REQUEST) đủ chuẩn cho Gmail/Outlook/Apple Calendar
    private byte[] buildIcs(Interview i, String organizerEmail, String recruiterEmail, String candidateEmail) {
        String uid = "jh-" + i.getInterviewId() + "@jobhuntly";
        ZoneId tz = ZoneId.systemDefault();
        ZonedDateTime start = i.getScheduledAt().atZone(tz);
        ZonedDateTime end = start.plusMinutes(i.getDurationMinutes() == null ? 60 : i.getDurationMinutes());
        DateTimeFormatter utc = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneId.of("UTC"));

        // Lưu ý: mô tả và location đơn giản, đủ dùng
        String desc = Optional.ofNullable(i.getMeetingUrl()).orElse("");
        String loc = Optional.ofNullable(i.getMeetingUrl()).orElse("");

        String ics = String.join("\r\n",
                "BEGIN:VCALENDAR",
                "PRODID:-//JobHuntly//Interview//EN",
                "VERSION:2.0",
                "CALSCALE:GREGORIAN",
                "METHOD:REQUEST",
                "BEGIN:VEVENT",
                "UID:" + uid,
                "DTSTAMP:" + utc.format(Instant.now()),
                "DTSTART:" + utc.format(start.toInstant()),
                "DTEND:" + utc.format(end.toInstant()),
                "SUMMARY:" + "Interview",
                "DESCRIPTION:" + desc,
                "LOCATION:" + loc,
                "ORGANIZER:MAILTO:" + organizerEmail,
                // Attendees yêu cầu RSVP
                "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=TRUE:MAILTO:" + recruiterEmail,
                "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=TRUE:MAILTO:" + candidateEmail,
                "STATUS:CONFIRMED",
                "END:VEVENT",
                "END:VCALENDAR",
                "");

        return ics.getBytes(StandardCharsets.UTF_8);
    }
}
