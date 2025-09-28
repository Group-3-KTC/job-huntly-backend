package com.jobhuntly.backend.service.email;

import com.jobhuntly.backend.dto.request.ReplyRequest;
import com.jobhuntly.backend.dto.response.ReplyResultDto;
import com.jobhuntly.backend.entity.Ticket;
import com.jobhuntly.backend.entity.TicketMessage;
import com.jobhuntly.backend.entity.enums.MessageDirection;
import com.jobhuntly.backend.entity.enums.TicketStatus;
import com.jobhuntly.backend.repository.TicketMessageRepository;
import com.jobhuntly.backend.repository.TicketRepository;
import com.jobhuntly.backend.util.EmailParser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Instant;
import java.time.Year;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final TicketRepository ticketRepo;
    private final TicketMessageRepository msgRepo;
    private final EmailSender emailSender;
    private final TicketService ticketService;

    private final TemplateEngine templateEngine;

    @Value("${app.brand.name:JobHuntly}")
    private String appName;

    @Value("${app.brand.logo-url:https://res.cloudinary.com/dfbqhd5ht/image/upload/v1757058535/logo-title-white_yjzvvr.png}")
    private String logoUrl;

    @Value("${mail.from.address:help.jobhuntly@gmail.com}")
    private String supportEmail;

    @Transactional
    public ReplyResultDto replyToTicket(Long ticketId, ReplyRequest req) {
        Ticket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));

        String to = null;
        if (!CollectionUtils.isEmpty(req.getTo())) to = req.getTo().get(0);
        if (!StringUtils.hasText(to)) to = ticket.getCustomerEmail();
        if (!StringUtils.hasText(to)) {
            Optional<TicketMessage> lastInboundOpt = msgRepo
                    .findTopByTicket_IdAndDirectionOrderBySentAtDesc(ticket.getId(), MessageDirection.INBOUND);
            to = lastInboundOpt.map(TicketMessage::getFromEmail)
                    .orElseThrow(() -> new IllegalStateException("No recipient for ticket " + ticketId));
        }

        String subject;
        if (StringUtils.hasText(req.getSubjectOverride())) {
            subject = req.getSubjectOverride().trim();
        } else if (StringUtils.hasText(ticket.getSubject())) {
            String s = ticket.getSubject().trim();
            subject = s.toLowerCase().startsWith("re:") ? s : "Re: " + s;
        } else {
            subject = "Re: Your request";
        }

        Optional<TicketMessage> lastInboundOpt = msgRepo
                .findTopByTicket_IdAndDirectionOrderBySentAtDesc(ticket.getId(), MessageDirection.INBOUND);
        String inReplyToMessageId = StringUtils.hasText(req.getReplyToMessageId())
                ? req.getReplyToMessageId()
                : lastInboundOpt.map(TicketMessage::getMessageId).orElse(null);

        String innerHtml = req.getBodyHtml();
        if (!StringUtils.hasText(innerHtml)) {
            innerHtml = StringUtils.hasText(req.getMessage()) ? req.getMessage() : "(No content)";
        }

        if (!innerHtml.contains("<")) {
            innerHtml = escapeHtml(innerHtml).replace("\n", "<br/>");
        }

        Context ctx = new Context();
        ctx.setVariable("appName", appName);
        ctx.setVariable("logoUrl", logoUrl);
        ctx.setVariable("supportEmail", supportEmail);
        ctx.setVariable("customerName", extractNameFromEmail(to));
        ctx.setVariable("heading", "Support Response");
        ctx.setVariable("messageHtml", innerHtml);
        ctx.setVariable("preheader", "We’ve replied to your request");
        ctx.setVariable("year", Year.now().getValue());

        String htmlToSend = templateEngine.process("generic-reply.html", ctx);
        String textToSend = EmailParser.htmlToPlain(htmlToSend);

        EmailSender.SendResult sendResult = emailSender.send(
                new EmailSender.OutgoingEmail()
                        .to(to)
                        .subject(subject)
                        .text(textToSend)
                        .html(htmlToSend)
                        .inReplyTo(inReplyToMessageId)
                        .references(inReplyToMessageId)
        );

        String messageId = (sendResult != null && StringUtils.hasText(sendResult.messageId()))
                ? sendResult.messageId()
                : "<" + UUID.randomUUID() + "@gmail.com>";

        String dbHtml = innerHtml;
        String dbText = EmailParser.htmlToPlain(dbHtml);

        TicketMessage tm = new TicketMessage();
        tm.setTicket(ticket);
        tm.setMessageId(messageId);
        tm.setInReplyTo(inReplyToMessageId);
        tm.setFromEmail(ticket.getFromEmail() != null ? ticket.getFromEmail() : supportEmail);
        tm.setSentAt(Instant.now());
        tm.setDirection(MessageDirection.OUTBOUND);
        tm.setBodyText(dbText);
        tm.setBodyHtml(dbHtml);
        msgRepo.save(tm);

        ticketService.markPendingOnOutbound(ticket.getId());

        return new ReplyResultDto(
                ticket.getId(),
                tm.getId(),
                tm.getMessageId(),
                tm.getDirection().name(),
                tm.getSentAt()
        );
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    private String extractNameFromEmail(String email) {
        if (!StringUtils.hasText(email)) return null;
        int idx = email.indexOf('@');
        if (idx > 0) return email.substring(0, idx).replace('.', ' ');
        return null;
    }
}
