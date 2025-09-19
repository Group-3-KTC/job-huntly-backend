package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.TicketMessageAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TicketMessageAttachmentRepository extends JpaRepository<TicketMessageAttachment, Long> {
    List<TicketMessageAttachment> findByTicketMessage_IdIn(Collection<Long> messageIds);
}