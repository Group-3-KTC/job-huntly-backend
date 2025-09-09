package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.request.ReplyRequest;
import com.jobhuntly.backend.dto.response.InboxItemDto;
import com.jobhuntly.backend.dto.response.MessageDto;
import com.jobhuntly.backend.dto.response.ReplyResultDto;
import com.jobhuntly.backend.entity.TicketMessage;
import com.jobhuntly.backend.repository.TicketMessageRepository;
import com.jobhuntly.backend.service.email.MailInboxService;
import com.jobhuntly.backend.service.email.ReplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("${backend.prefix}/tickets")
@RequiredArgsConstructor
@Slf4j
public class TicketController {

    private final MailInboxService mailInboxService;
    private final TicketMessageRepository ticketMessageRepository;
    private final ReplyService replyService;

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
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "sentAt"));
        Page<TicketMessage> data = ticketMessageRepository.findByTicket_IdOrderBySentAtAsc(ticketId, pageable);
        return data.map(MessageDto::from);
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
}

