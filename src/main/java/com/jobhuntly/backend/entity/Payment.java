package com.jobhuntly.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity @Table(name="payments",
        indexes = @Index(name="idx_company_created", columnList="companyId,createdAt"),
        uniqueConstraints = {
                @UniqueConstraint(name="uq_txn_ref", columnNames="txnRef"),
                @UniqueConstraint(name="uq_provider_txn", columnNames={"provider","providerTxn"})
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "package_id")
    private Long packageId;

    @Column(name = "amount_vnd")
    private Long amountVnd;

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false) private Status status;
    public enum Status { PENDING, REQUIRES_ACTION, PAID, FAILED, REFUNDED, PARTIALLY_REFUNDED, CHARGEBACK }


    @Column(nullable = false)
    private String provider; // 'VNPAY', 'Momo'

    private String method;  // "QR", "WALLET"

    @Column(nullable = false)
    private String txnRef;

    private String providerTxn;
    private String providerOrderId;

    @Lob private String metadataJson;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @PrePersist void prePersist(){
        var now = OffsetDateTime.now();
        createdAt = updatedAt = now;
        if (status == null) status = Status.PENDING;
        if (currency == null) currency = "VND";
    }
    @PreUpdate void preUpdate(){ updatedAt = OffsetDateTime.now(); }
}
