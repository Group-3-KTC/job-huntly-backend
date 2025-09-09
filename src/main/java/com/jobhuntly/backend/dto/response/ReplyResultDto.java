package com.jobhuntly.backend.dto.response;

import java.time.Instant;

public record ReplyResultDto(
        Long ticketId,
        Long ticketMessageId,
        String messageId,       // SMTP Message-ID đã gán
        String direction,       // OUTBOUND
        Instant sentAt          // thời điểm persist
) {}