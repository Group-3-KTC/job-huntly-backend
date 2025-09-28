package com.jobhuntly.backend.service.email;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Year;

@Service
@RequiredArgsConstructor
public class MailTemplateService {
    private final TemplateEngine templateEngine;

    public String renderSetPasswordEmail(String setPasswordLink, String ttlText) {
        Context context = new Context();
        context.setVariable("appName", "JobHuntly");
        context.setVariable("setPasswordLink", setPasswordLink);
        context.setVariable("ttlText", ttlText);
        context.setVariable("title", "Set your password");
        context.setVariable("buttonText", "Set password");

        context.setVariable("supportEmail", "help.jobhuntly@gmail.com");
        context.setVariable("logoUrl", "https://res.cloudinary.com/dfbqhd5ht/image/upload/v1757058535/logo-title-white_yjzvvr.png");

        return templateEngine.process("set-password-email", context);
    }

    public String renderResetPasswordEmail(String resetLink, String ttlText) {
        Context context = new Context();
        context.setVariable("appName", "JobHuntly");
        context.setVariable("resetLink", resetLink);
        context.setVariable("ttlText", ttlText);
        context.setVariable("supportEmail", "help.jobhuntly@gmail.com");
        context.setVariable("logoUrl", "https://res.cloudinary.com/dfbqhd5ht/image/upload/v1757058535/logo-title-white_yjzvvr.png");

        return templateEngine.process("reset-password-email", context);
    }

    public String renderGenericReply(String appName,
                                     String logoUrl,
                                     String supportEmail,
                                     String customerName,
                                     String heading,
                                     String plainMessage,
                                     String ctaUrl,
                                     String ctaLabel,
                                     String preheader) {
        Context ctx = new Context();
        ctx.setVariable("appName", appName);
        ctx.setVariable("logoUrl", logoUrl);
        ctx.setVariable("supportEmail", supportEmail);
        ctx.setVariable("customerName", customerName);
        ctx.setVariable("heading", heading);
        ctx.setVariable("ctaUrl", ctaUrl);
        ctx.setVariable("ctaLabel", ctaLabel);
        ctx.setVariable("preheader", preheader);
        ctx.setVariable("year", Year.now().getValue());

        String safeHtml = StringEscapeUtils.escapeHtml4(plainMessage)
                .replace("\n", "<br/>");
        ctx.setVariable("messageHtml", safeHtml);

        return templateEngine.process("generic-reply.html", ctx);
    }
}
