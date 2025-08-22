package com.jobhuntly.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "awards")
@Getter
@Setter 
public class Award {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long awardId;

    private String name;
    private String issuer;

    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private CandidateProfile profile;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Award))
            return false;
        Award that = (Award) o;
        return awardId != null && awardId.equals(that.awardId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
