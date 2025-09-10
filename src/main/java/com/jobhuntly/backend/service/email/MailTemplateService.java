package com.jobhuntly.backend.service.email;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class MailTemplateService {
    private final TemplateEngine templateEngine;

    public String renderSetPasswordEmail(String actionLink, String ttlText) {
        Context ctx = new Context();
        ctx.setVariable("actionLink", actionLink);
        ctx.setVariable("ttlText", ttlText);
        ctx.setVariable("title", "Set your password");
        ctx.setVariable("buttonText", "Set password");
        return templateEngine.process("set-password-email", ctx);
    }

    public String renderResetPasswordEmail(String actionLink, String ttlText) {
        Context ctx = new Context();
        ctx.setVariable("actionLink", actionLink);
        ctx.setVariable("ttlText", ttlText);
        ctx.setVariable("title", "Reset your password");
        ctx.setVariable("buttonText", "Reset password");
        return templateEngine.process("reset-password-email", ctx);
    }
}
