package com.jobhuntly.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "levels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Level {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "level_id")
    private Long id;

    @Column(name = "level_name")
    private String name;

    @ManyToMany(mappedBy = "levels")
    private Set<Job> jobs = new HashSet<>();

    @OneToMany(mappedBy = "level", fetch = FetchType.LAZY) // Added
    private Set<CandidateSkill> candidateSkills = new HashSet<>();

    @Override // Added
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Level))
            return false;
        Level that = (Level) o;
        return id != null && id.equals(that.id);
    }

    @Override // Added
    public int hashCode() {
        return getClass().hashCode();
    }
}
