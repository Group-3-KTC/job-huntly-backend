package com.jobhuntly.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "work_experience")
@Getter
@Setter
public class WorkExperience {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long experId;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String companyName;
    private String position;
    private String duration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private CandidateProfile profile;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof WorkExperience))
            return false;
        WorkExperience that = (WorkExperience) o;
        return experId != null && experId.equals(that.experId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
