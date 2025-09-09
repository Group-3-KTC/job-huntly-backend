package com.jobhuntly.backend.dto.response;

import com.jobhuntly.backend.entity.TicketMessage;

import java.time.Instant;

public record MessageDto(
        Long id,
        Long ticketId,
        String messageId,
        String inReplyTo,
        String fromEmail,
        Instant sentAt,
        String direction, // "INBOUND"/"OUTBOUND"
        String bodyText,
        String bodyHtml
) {
    public static MessageDto from(TicketMessage m) {
        return new MessageDto(
                m.getId(),
                m.getTicketId(),
                m.getMessageId(),
                m.getInReplyTo(),
                m.getFromEmail(),
                m.getSentAt(),
                m.getDirection().name(),
                m.getBodyText(),
                m.getBodyHtml()
        );
    }
}
