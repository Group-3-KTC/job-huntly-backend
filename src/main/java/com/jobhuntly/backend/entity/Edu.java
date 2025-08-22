package com.jobhuntly.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "edu")
@Getter
@Setter 
public class Edu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eduId;

    private String schoolName;
    private String degree;
    private String duration;
    private String majors;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private CandidateProfile profile;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Edu))
            return false;
        Edu that = (Edu) o;
        return eduId != null && eduId.equals(that.eduId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
