package com.jobhuntly.backend.service.email;

import com.jobhuntly.backend.dto.request.ReplyRequest;
import com.jobhuntly.backend.dto.response.ReplyResultDto;
import com.jobhuntly.backend.entity.Ticket;
import com.jobhuntly.backend.entity.TicketMessage;
import com.jobhuntly.backend.entity.enums.MessageDirection;
import com.jobhuntly.backend.repository.TicketMessageRepository;
import com.jobhuntly.backend.repository.TicketRepository;
import com.jobhuntly.backend.util.EmailCanonicalizer;
import com.jobhuntly.backend.util.EmailParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final TicketRepository ticketRepo;
    private final TicketMessageRepository msgRepo;
    private final EmailService emailService;

    // Địa chỉ from hệ thống của bạn (nhớ đồng bộ với MailPollingService.SYSTTEM_EMAILS)
    private static final String SYSTEM_FROM = "contact.jobhuntly@gmail.com";
    private static final String SYSTEM_FROM_NAME = "JobHuntly Support";

    @Transactional
    public ReplyResultDto replyToTicket(Long ticketId, ReplyRequest req) {
        Ticket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        // To (ưu tiên req.to, fallback customerEmail)
        String to = null;
        if (!CollectionUtils.isEmpty(req.getTo())) {
            to = req.getTo().get(0);
        }
        if (!StringUtils.hasText(to)) {
            to = ticket.getCustomerEmail();
        }
        if (!StringUtils.hasText(to)) {
            // fallback lấy inbound gần nhất
            var lastInbound = msgRepo.findTopByTicket_IdAndDirectionOrderBySentAtDesc(ticket.getId(), MessageDirection.INBOUND);
            to = lastInbound.map(TicketMessage::getFromEmail).orElseThrow(() -> new IllegalStateException("No recipient for ticket"));
        }

        String subject = StringUtils.hasText(req.getSubjectOverride()) ? req.getSubjectOverride() : ticket.getSubject();
        String inReplyTo = StringUtils.hasText(req.getReplyToMessageId()) ? req.getReplyToMessageId() : null;

        String html = StringUtils.hasText(req.getBodyHtml()) ? req.getBodyHtml() : "<p></p>";

        // Gửi mail
        String messageId = emailService.sendWithThreading(
                to, subject, html, inReplyTo, null, SYSTEM_FROM, SYSTEM_FROM_NAME,
                req.getCc(), null
        );

        // Persist OUTBOUND ngay
        TicketMessage tm = new TicketMessage();
        tm.setTicket(ticket);
        tm.setMessageId(messageId);
        tm.setInReplyTo(inReplyTo);
        tm.setFromEmail(EmailCanonicalizer.canonicalize(SYSTEM_FROM));
        tm.setSentAt(Instant.now());
        tm.setDirection(MessageDirection.OUTBOUND);

        // bodyText -> extractNewContentPlain (fallback htmlToPlain)
        String bodyText = EmailParser.extractNewContentPlain(html, null);
        if (!StringUtils.hasText(bodyText)) {
            bodyText = EmailParser.htmlToPlain(html);
        }
        tm.setBodyText(bodyText);
        tm.setBodyHtml(html);

        msgRepo.save(tm);
        return new ReplyResultDto(
                ticket.getId(),
                tm.getId(),
                messageId,
                tm.getDirection().name(),
                tm.getSentAt()
        );
    }
}