package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.request.ReplyRequest;
import com.jobhuntly.backend.dto.response.InboxItemDto;
import com.jobhuntly.backend.dto.response.MessageDto;
import com.jobhuntly.backend.dto.response.ReplyResultDto;
import com.jobhuntly.backend.entity.Ticket;
import com.jobhuntly.backend.entity.TicketMessage;
import com.jobhuntly.backend.repository.TicketMessageRepository;
import com.jobhuntly.backend.service.email.MailInboxService;
import com.jobhuntly.backend.service.email.MailPollingService;
import com.jobhuntly.backend.service.email.ReplyService;
import com.jobhuntly.backend.service.email.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${backend.prefix}/tickets")
@RequiredArgsConstructor
@Slf4j
public class TicketController {

    private final MailInboxService mailInboxService;
    private final ReplyService replyService;
    private final MailPollingService mailPollingService;
    private final TicketService ticketService;

    // GET /api/v1/tickets?status=OPEN&customerEmail=abc@gmail.com&q=hello&page=0&size=20&sort=createdAt,DESC
    @GetMapping
    public Page<InboxItemDto> listTickets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String customerEmail,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort
    ) {
        Sort parsed = parseSort(sort);
        return mailInboxService.listTickets(status, customerEmail, q, page, size, parsed);
    }

    // GET /api/v1/tickets/{id}/messages?page=0&size=100
    @GetMapping("/{id}/messages")
    public Page<MessageDto> listMessages(
            @PathVariable("id") Long ticketId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        return mailInboxService.listMessages(ticketId, page, size);
    }

    //    GET /api/v1/tickets/{id}/messages?page=0&size=100
    @PostMapping("/{id}/reply")
    public ResponseEntity<ReplyResultDto> reply(
            @PathVariable("id") Long ticketId,
            @RequestBody ReplyRequest request
    ) {
        ReplyResultDto result = replyService.replyToTicket(ticketId, request);
        URI location = URI.create("/api/v1/tickets/" + result.ticketId() + "/messages?page=0&size=100");
        return ResponseEntity
                .created(location)
                .body(result);
    }

    private Sort parseSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.unsorted();
        }
        try {
            String[] p = sort.split(",", 2);
            String field = p[0];
            Sort.Direction dir = (p.length > 1 && "ASC".equalsIgnoreCase(p[1])) ? Sort.Direction.ASC : Sort.Direction.DESC;
            return Sort.by(dir, field);
        } catch (Exception e) {
            return Sort.unsorted();
        }
    }

    @RequestMapping(value = "/poll-now", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
    public ResponseEntity<Map<String, Object>> pollNow() {
        log.info("Polling now");
        int processed = mailPollingService.pollNow();
        return ResponseEntity.ok(Map.of("processed", processed));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest req) {
        Ticket t = ticketService.updateStatus(id, req.status());
        return ResponseEntity.ok(Map.of(
                "id", t.getId(),
                "status", t.getStatus().name()
        ));
    }

    public record StatusUpdateRequest(String status) {}
}

