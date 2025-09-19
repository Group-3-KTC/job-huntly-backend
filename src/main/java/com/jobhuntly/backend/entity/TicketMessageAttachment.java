package com.jobhuntly.backend.entity;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "ticket_message_attachments")
@Getter @Setter
public class TicketMessageAttachment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_message_id", nullable = false)
    private TicketMessage ticketMessage;

    @Column(name = "filename")
    private String filename;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "content_id")
    private String contentId; // để thay cid

    @Column(name = "inline", nullable = false)
    private boolean inline;   // true = inline (cid), false = normal attachment

    @Column(name = "storage_provider", nullable = false)
    private String storageProvider = "CLOUDINARY";

    @Column(name = "storage_public_id")
    private String storagePublicId;

    @Column(name = "storage_url")
    private String storageUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
