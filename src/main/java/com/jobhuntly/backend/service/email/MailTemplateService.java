package com.jobhuntly.backend.service.email;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

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

        context.setVariable("supportEmail", "contact.jobhuntly@gmail.com");
        context.setVariable("logoUrl", "https://res.cloudinary.com/dfbqhd5ht/image/upload/v1757058535/logo-title-white_yjzvvr.png");

        return templateEngine.process("set-password-email", context);
    }

    public String renderResetPasswordEmail(String resetLink, String ttlText) {
        Context context = new Context();
        context.setVariable("appName", "JobHuntly");
        context.setVariable("resetLink", resetLink);
        context.setVariable("ttlText", ttlText);
        context.setVariable("supportEmail", "contact.jobhuntly@gmail.com");
        context.setVariable("logoUrl", "https://res.cloudinary.com/dfbqhd5ht/image/upload/v1757058535/logo-title-white_yjzvvr.png");

        return templateEngine.process("reset-password-email", context);
    }
}
