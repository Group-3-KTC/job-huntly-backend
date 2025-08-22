package com.jobhuntly.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "certificates")
@Getter
@Setter 
public class Certificate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cerId;

    private String cerName;

    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String issuer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private CandidateProfile profile;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Certificate))
            return false;
        Certificate that = (Certificate) o;
        return cerId != null && cerId.equals(that.cerId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
