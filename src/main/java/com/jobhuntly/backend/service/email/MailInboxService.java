package com.jobhuntly.backend.service.email;

import com.jobhuntly.backend.dto.response.InboxItemDto;
import com.jobhuntly.backend.dto.response.MessageDto;
import com.jobhuntly.backend.entity.Ticket;
import com.jobhuntly.backend.entity.TicketMessage;
import com.jobhuntly.backend.entity.enums.TicketStatus;
import com.jobhuntly.backend.repository.TicketMessageRepository;
import com.jobhuntly.backend.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MailInboxService {

    private final TicketRepository ticketRepository;
    private final TicketMessageRepository ticketMessageRepository;

    public Page<InboxItemDto> listTickets(String statusStr,
                                          String customerEmail,
                                          String q,
                                          int page,
                                          int size,
                                          Sort sort) {

        TicketStatus status = null;
        if (StringUtils.hasText(statusStr)) {
            status = TicketStatus.valueOf(statusStr.toUpperCase(Locale.ROOT));
        }

        // sort fallback: lastMessageAt DESC (UI mong muốn). Vì lastMessageAt không ở bảng Ticket,
        // ta sort theo createdAt DESC ở query, rồi trong trang hiện tại, đã có lastMessageAt để hiển thị.
        Sort fallback = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, (sort == null || sort.isUnsorted()) ? fallback : sort);

        Page<Ticket> ticketsPage = ticketRepository.searchTickets(status, blankToNull(customerEmail), blankToNull(q), pageable);

        List<Long> ids = ticketsPage.getContent().stream().map(Ticket::getId).toList();
        Map<Long, TicketMessage> lastByTicket = ids.isEmpty()
                ? Collections.emptyMap()
                : ticketMessageRepository.findLastMessagesForTickets(ids).stream()
                .collect(Collectors.toMap(tm -> tm.getTicket().getId(), Function.identity()));

        List<InboxItemDto> items = ticketsPage.getContent().stream()
                .map(t -> {
                    TicketMessage last = lastByTicket.get(t.getId());
                    return new InboxItemDto(
                            t.getId(),
                            t.getSubject(),
                            t.getStatus(),
                            t.getCustomerEmail(),
                            t.getCreatedAt(),
                            t.getThreadId(),
                            last != null ? last.getSentAt() : t.getCreatedAt(),
                            last != null ? last.getFromEmail() : t.getFromEmail(),
                            last != null ? snippetOf(last) : null
                    );
                })
                .toList();

        return new PageImpl<>(items, ticketsPage.getPageable(), ticketsPage.getTotalElements());
    }

    public Page<MessageDto> listMessages(Long ticketId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "sentAt"));
        Page<TicketMessage> pageData = ticketMessageRepository.findByTicket_IdOrderBySentAtAsc(ticketId, pageable);
        return pageData.map(MessageDto::from);
    }

    private String snippetOf(TicketMessage m) {
        String raw = (m.getBodyText() != null && !m.getBodyText().isBlank())
                ? m.getBodyText()
                : (m.getBodyHtml() != null ? m.getBodyHtml().replaceAll("<[^>]+>", " ") : "");
        raw = raw == null ? "" : raw.trim().replaceAll("\\s+", " ");
        return raw.length() > 180 ? raw.substring(0, 180) : raw;
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
