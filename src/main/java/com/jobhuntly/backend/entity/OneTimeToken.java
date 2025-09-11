package com.jobhuntly.backend.entity;

import com.jobhuntly.backend.entity.enums.OneTimeTokenPurpose;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "user_one_time_tokens",
        indexes = {
                @Index(name = "idx_ott_user_purpose", columnList = "user_id, purpose"),
                @Index(name = "idx_ott_expires", columnList = "expires_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_ott_purpose_hash", columnNames = {"purpose", "token_hash"})
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class OneTimeToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long tokenId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, length = 32)
    private OneTimeTokenPurpose purpose; // ACTIVATION | SET_PASSWORD | RESET_PASSWORD | EMAIL_CHANGE

    /** SHA-256 (hex) của token thô */
    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /** Set != null sau khi dùng xong để chống reuse và phục vụ audit */
    @Column(name = "consumed_at")
    private Instant consumedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}