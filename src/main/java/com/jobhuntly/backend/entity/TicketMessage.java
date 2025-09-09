package com.jobhuntly.backend.entity;

import com.jobhuntly.backend.entity.enums.MessageDirection;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "ticket_messages",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ticket_messages_message_id", columnNames = "message_id")
        },
        indexes = {
                @Index(name = "ix_ticket_messages_ticket_id", columnList = "ticket_id"),
                @Index(name = "ix_ticket_messages_sent_at", columnList = "sent_at"),
                @Index(name = "ix_ticket_messages_in_reply_to", columnList = "in_reply_to"),
                @Index(name = "ix_ticket_messages_from_email", columnList = "from_email")
        }
)
public class TicketMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "ticket_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_ticket_messages_ticket")
    )
    private Ticket ticket;

    @Column(name = "message_id", length = 512, nullable = false)
    private String messageId;

    @Column(name = "in_reply_to", length = 512)
    private String inReplyTo;

    @Column(name = "from_email", length = 320, nullable = false)
    private String fromEmail;

    @Column(name = "sent_at")
    private Instant sentAt; // DB default CURRENT_TIMESTAMP(6) nếu null

    @Lob
    @Column(name = "body_text", columnDefinition = "longtext")
    private String bodyText;

    @Lob
    @Column(name = "body_html", columnDefinition = "longtext")
    private String bodyHtml;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 16)
    @Builder.Default
    private MessageDirection direction = MessageDirection.INBOUND;

    /* Convenience getters/setters cho ticketId (hữu ích cho DTO binding) */
    @Transient
    public Long getTicketId() {
        return ticket != null ? ticket.getId() : null;
    }

    public void setTicketId(Long ticketId) {
        if (ticketId == null) {
            this.ticket = null;
        } else {
            Ticket t = new Ticket();
            t.setId(ticketId);
            this.ticket = t;
        }
    }
}
