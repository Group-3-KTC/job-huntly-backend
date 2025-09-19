package com.jobhuntly.backend.dto.response;

import com.jobhuntly.backend.entity.TicketMessage;

import java.time.Instant;
import java.util.List;

public record MessageDto(
        Long id,
        Long ticketId,
        String messageId,
        String inReplyTo,
        String fromEmail,
        Instant sentAt,
        String direction,
        String bodyText,
        String bodyHtml,
        List<AttachmentDto> attachments
) {
    public static MessageDto from(TicketMessage m, List<AttachmentDto> atts) {
        return new MessageDto(
                m.getId(),
                m.getTicket().getId(),
                m.getMessageId(),
                m.getInReplyTo(),
                m.getFromEmail(),
                m.getSentAt(),
                m.getDirection().name(),
                m.getBodyText(),
                m.getBodyHtml(),
                atts
        );
    }

    public static MessageDto from(TicketMessage m) {
        return from(m, List.of());
    }
}
