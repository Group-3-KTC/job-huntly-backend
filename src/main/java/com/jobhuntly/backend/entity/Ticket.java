package com.jobhuntly.backend.entity;

import com.jobhuntly.backend.entity.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(
        name = "tickets",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tickets_thread_id", columnNames = "thread_id")
        },
        indexes = {
                @Index(name = "ix_tickets_created_at", columnList = "created_at DESC"),
                @Index(name = "ix_tickets_status", columnList = "status"),
                @Index(name = "ix_tickets_customer_email", columnList = "customer_email")
        }
)
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(length = 500)
    private String subject;

    @Column(name = "from_email", length = 320, nullable = false)
    private String fromEmail; // from của lần ingest đầu (có thể là hệ thống)

    @Column(name = "customer_email", length = 254)
    private String customerEmail; // email người mở thread (đã canonicalize)

    @Column(name = "thread_id", length = 512, nullable = false)
    private String threadId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    @Builder.Default
    private TicketStatus status = TicketStatus.OPEN;

    // Quan hệ 1-n với messages (tuỳ bạn có muốn cascade/orphanRemoval hay không)
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TicketMessage> messages = new ArrayList<>();

    /* Convenience helpers */
    public void addMessage(TicketMessage m) {
        if (m == null) return;
        m.setTicket(this);
        this.messages.add(m);
    }

    public void removeMessage(TicketMessage m) {
        if (m == null) return;
        m.setTicket(null);
        this.messages.remove(m);
    }
}
