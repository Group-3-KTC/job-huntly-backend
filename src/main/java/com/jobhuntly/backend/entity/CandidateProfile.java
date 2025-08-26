package com.jobhuntly.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.jobhuntly.backend.entity.enums.Gender;


@Entity
@Table(name = "candidate_profile")
@Getter
@Setter
public class CandidateProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long profileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    private String avatar;

    @Column(columnDefinition = "TEXT")
    private String aboutMe;

    private String personalLink;

    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;

    private String title;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Award> awards = new HashSet<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<WorkExperience> workExperiences = new HashSet<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Certificate> certificates = new HashSet<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Edu> educations = new HashSet<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<SoftSkill> softSkills = new HashSet<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<CandidateSkill> candidateSkills = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CandidateProfile))
            return false;
        CandidateProfile that = (CandidateProfile) o;
        return profileId != null && profileId.equals(that.profileId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
