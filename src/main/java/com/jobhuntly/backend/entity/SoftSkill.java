package com.jobhuntly.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.jobhuntly.backend.entity.enums.Level;

@Entity
@Table(name = "soft_skills")
@Getter
@Setter
public class SoftSkill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long softSkillId;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private Level level; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private CandidateProfile profile;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SoftSkill))
            return false;
        SoftSkill that = (SoftSkill) o;
        return softSkillId != null && softSkillId.equals(that.softSkillId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}