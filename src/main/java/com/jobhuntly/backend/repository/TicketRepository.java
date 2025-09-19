package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.Ticket;
import com.jobhuntly.backend.entity.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("""
        SELECT t
        FROM Ticket t
        WHERE (:status IS NULL OR t.status = :status)
          AND (:customerEmail IS NULL OR t.customerEmail = :customerEmail)
          AND (:q IS NULL OR LOWER(t.subject) LIKE LOWER(CONCAT('%', :q, '%')))
        """)
    Page<Ticket> searchTickets(
            @Param("status") TicketStatus status,
            @Param("customerEmail") String customerEmail,
            @Param("q") String q,
            Pageable pageable
    );

    Optional<Ticket> findByThreadId(String threadId);

    Optional<Ticket> findFirstByCustomerEmailAndSubjectOrderByCreatedAtDesc(String customerEmail, String subject);
}
