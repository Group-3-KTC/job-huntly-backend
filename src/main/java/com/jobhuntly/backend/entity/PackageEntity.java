package com.jobhuntly.backend.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name="packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "package_id")
    private Long packageId;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    private String type;

    @Column(nullable = false)
    private Integer durationDays;
    @Column(nullable = false)
    private Long priceVnd;
    @Column(nullable = false)
    private Boolean isActive;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @PrePersist void prePersist(){ createdAt = updatedAt = OffsetDateTime.now(); }
    @PreUpdate void preUpdate(){ updatedAt = OffsetDateTime.now(); }
}
