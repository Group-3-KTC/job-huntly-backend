package com.jobhuntly.backend.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;


    @Column(name = "cv", length = 200)
    private String cv;

    @Column(name = "email", length = 200)
    private String email;

    @Column(name = "status", length = 200)
    private String status; // APPLIED, REVIEWED, REJECTED

    @Column(name = "phone_number", length = 200)
    private String phoneNumber;

    @Column(name = "candidate_name", length = 200)
    private String candidateName;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name="attempt_count", nullable=false)
    private int attemptCount = 1;

    @Column(name="last_user_action_at")
    private LocalDateTime lastUserActionAt;

    @PrePersist
    void prePersist() {
        if (this.status == null || this.status.isBlank()) {
            this.status = "Applied";
        }
    }
}
