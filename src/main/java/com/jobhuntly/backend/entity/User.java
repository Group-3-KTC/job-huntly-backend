package com.jobhuntly.backend.entity;

import com.jobhuntly.backend.entity.enums.AuthProvider;
import com.jobhuntly.backend.entity.enums.PasswordTokenPurpose;
import com.jobhuntly.backend.entity.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email"),
                @Index(name = "idx_users_google_id", columnList = "google_id"),
                @Index(name = "idx_users_pwdtoken_purpose_hash", columnList = "password_token_purpose, password_token_hash")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_users_email", columnNames = {"email"})
                // Lưu ý: MySQL cho phép nhiều NULL ở unique index; google_id đã unique = true ở @Column
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    // -------- Profile / Account --------
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "google_id", unique = true, length = 64)
    private String googleId;

    // Cho phép null để account social không cần password
    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "phone_number", length = 20, unique = true)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "city_id")
    private City city;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    // -------- Trạng thái / Kích hoạt --------
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Status status;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "activation_token", length = 64)
    private String activationToken;

    @Column(name = "activation_token_expires_at")
    private Instant activationTokenExpiresAt;

    // -------- Provider / Multi-auth --------
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", length = 20)
    private AuthProvider authProvider; // LOCAL | GOOGLE

    @Column(name = "password_set", nullable = false)
    @Builder.Default
    private Boolean passwordSet = false;

    // -------- Token đặt/reset mật khẩu (dùng chung 3 cột) --------
    @Column(name = "password_token_hash", length = 64)
    private String passwordTokenHash; // SHA-256 hex của token thô

    @Enumerated(EnumType.STRING)
    @Column(name = "password_token_purpose", length = 16)
    private PasswordTokenPurpose passwordTokenPurpose; // SET | RESET

    @Column(name = "password_token_expires_at")
    private Instant passwordTokenExpiresAt;

    // -------- Notification flags --------
    @Column(name = "sms_notification_active", nullable = false)
    @Builder.Default
    private Boolean smsNotificationActive = false;

    @Column(name = "email_notification_active", nullable = false)
    @Builder.Default
    private Boolean emailNotificationActive = true;

    // -------- Timestamps --------
    @CreationTimestamp
    @Column(name = "create_at", updatable = false)
    private Instant createdAt;

    // -------- Quan hệ khác --------
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Company company;
}
