package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.TicketMessage;
import com.jobhuntly.backend.entity.enums.MessageDirection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TicketMessageRepository extends JpaRepository<TicketMessage, Long> {

    Page<TicketMessage> findByTicket_IdOrderBySentAtAsc(Long ticketId, Pageable pageable);
    Optional<TicketMessage> findTopByTicket_IdAndDirectionOrderBySentAtDesc(
            Long ticketId, MessageDirection direction
    );

    @Query(
            value = """
        SELECT * FROM (
            SELECT tm.*,
                   ROW_NUMBER() OVER (PARTITION BY tm.ticket_id ORDER BY tm.sent_at DESC, tm.id DESC) AS rn
            FROM ticket_messages tm
            WHERE tm.ticket_id IN (:ticketIds)
        ) x
        WHERE x.rn = 1
        """,
            nativeQuery = true
    )
    List<TicketMessage> findLastMessagesForTickets(@Param("ticketIds") Collection<Long> ticketIds);
    Optional<TicketMessage> findByMessageId(String messageId);
}
