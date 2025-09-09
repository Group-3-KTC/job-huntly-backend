package com.jobhuntly.backend.dto.response;


import com.jobhuntly.backend.entity.enums.TicketStatus;

import java.time.Instant;

public record InboxItemDto(
        Long id,
        String subject,
        TicketStatus status,
        String customerEmail,
        Instant createdAt,
        String threadId,
        Instant lastMessageAt,
        String lastFrom,
        String lastSnippet
) {}
