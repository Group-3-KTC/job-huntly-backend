package com.jobhuntly.backend.service.email;

import com.jobhuntly.backend.entity.Ticket;
import com.jobhuntly.backend.entity.enums.TicketStatus;
import com.jobhuntly.backend.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;

    private static TicketStatus parseStatus(String s) {
        try {
            return TicketStatus.valueOf(s.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status: " + s);
        }
    }

    @Transactional
    public Ticket updateStatus(Long ticketId, String statusStr) {
        TicketStatus newStatus = parseStatus(statusStr);
        Ticket t = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found: " + ticketId));

        if (t.getStatus() != newStatus) {
            t.setStatus(newStatus);
            ticketRepository.save(t);
        }
        return t;
    }

    @Transactional
    public void markOpenOnInbound(Long ticketId) {
        Ticket t = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found: " + ticketId));
        if (t.getStatus() != TicketStatus.OPEN) {
            t.setStatus(TicketStatus.OPEN);
            ticketRepository.save(t);
        }
    }

    @Transactional
    public void markPendingOnOutbound(Long ticketId) {
        Ticket t = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found: " + ticketId));
        if (t.getStatus() != TicketStatus.PENDING) {
            t.setStatus(TicketStatus.PENDING);
            ticketRepository.save(t);
        }
    }
}