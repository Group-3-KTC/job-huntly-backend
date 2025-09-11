package com.jobhuntly.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "user_sessions",
        indexes = {
                @Index(name = "idx_user_sessions_user", columnList = "user_id"),
                @Index(name = "idx_user_sessions_family", columnList = "session_family_id"),
                @Index(name = "idx_user_sessions_expires", columnList = "refresh_expires_at"),
                @Index(name = "idx_user_sessions_revoked", columnList = "revoked_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_user_sessions_refresh_hash", columnNames = {"refresh_token_hash"})
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long sessionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Nhóm (family) các phiên sau mỗi lần rotate — dùng để revoke toàn bộ nếu phát hiện reuse */
    @Column(name = "session_family_id", nullable = false, length = 36)
    private String sessionFamilyId; // UUID string

    /** Liên kết chuỗi rotate: phiên trước đó */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_session_id")
    private UserSession parent;

    /** Liên kết chuỗi rotate: phiên kế tiếp (đã thay thế) */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replaced_by_session_id")
    private UserSession replacedBy;

    /** SHA-256 (hex) của refresh token */
    @Column(name = "refresh_token_hash", nullable = false, length = 64)
    private String refreshTokenHash;

    @Column(name = "refresh_expires_at", nullable = false)
    private Instant refreshExpiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    /** Nếu bị thu hồi (logout/compromised) */
    @Column(name = "revoked_at")
    private Instant revokedAt;

    /** Dùng lại refresh cũ sau khi rotate → dấu hiệu bị lộ */
    @Column(name = "reuse_detected_at")
    private Instant reuseDetectedAt;

    // Thông tin thiết bị/ngữ cảnh (tuỳ chọn)
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "device_label", length = 100)
    private String deviceLabel;
}
