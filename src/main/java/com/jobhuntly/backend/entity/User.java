package com.jobhuntly.backend.entity;

import com.jobhuntly.backend.entity.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
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
    @Column(name = "google_id", unique = true)
    private String googleId;
    // Cho phép null để account social không cần password
    @Column(name = "password_hash", nullable = true)
    private String passwordHash;
    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false) // FK tới bảng roles
    private Role role;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "activation_token", length = 64)
    private String activationToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Company company;
}
