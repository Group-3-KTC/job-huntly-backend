package com.jobhuntly.backend.entity;

import com.jobhuntly.backend.entity.enums.AuthProvider;
import com.jobhuntly.backend.entity.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email"),
                @Index(name = "idx_users_google_id", columnList = "google_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_users_email", columnNames = {"email"})
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

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "google_id", unique = true, length = 64)
    private String googleId;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Status status;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", length = 20)
    private AuthProvider authProvider; // LOCAL | GOOGLE

    @Column(name = "password_set", nullable = false)
    @Builder.Default
    private Boolean passwordSet = false;

    @Column(name = "sms_notification_active", nullable = false)
    @Builder.Default
    private Boolean smsNotificationActive = false;

    @Column(name = "email_notification_active", nullable = false)
    @Builder.Default
    private Boolean emailNotificationActive = true;

    @CreationTimestamp
    @Column(name = "create_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "password_changed_at")
    private Instant passwordChangedAt;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Company company;
}
