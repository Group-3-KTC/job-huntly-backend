package com.jobhuntly.backend.entity;

import com.jobhuntly.backend.entity.enums.ReportType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "report_type_id")
    private ReportType reportType;

    @Column(name = "reported_content_id")
    private Long reportedContentId;

    private String description;
    private String status;

    @PrePersist
    void prePersist() {
        if (this.status == null || this.status.isBlank()) {
            this.status = "PROCESS";
        }
    }

    @Column(name = "create_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
