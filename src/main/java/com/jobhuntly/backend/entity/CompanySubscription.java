package com.jobhuntly.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity @Table(name="company_subscriptions",
        indexes = @Index(name="idx_company_end", columnList="companyId,endAt"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CompanySubscription {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long subscriptionId;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "package_id")
    private Long packageId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false) private Status status;
    public enum Status { ACTIVE, EXPIRED, CANCELLED }

    @Column(name = "start_at", nullable = false) private OffsetDateTime startAt;
    @Column(name = "end_at", nullable = false) private OffsetDateTime endAt;

    @Column(name = "latest_payment_id")
    private Long latestPaymentId;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist void prePersist(){ createdAt = updatedAt = OffsetDateTime.now(); }
    @PreUpdate void preUpdate(){ updatedAt = OffsetDateTime.now(); }

    public boolean isActiveNow() {
        return status == Status.ACTIVE && endAt.isAfter(OffsetDateTime.now());
    }
}
