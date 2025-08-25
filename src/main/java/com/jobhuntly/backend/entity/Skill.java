package com.jobhuntly.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "skill_id")
    private Long id;

    @Column(name = "skill_name")
    private String name;


    // Bảng nối đã tạo: skill_categories(skill_id, cate_id)
    @ManyToMany
    @JoinTable(
            name = "skill_categories",
            joinColumns = @JoinColumn(name = "skill_id"),
            inverseJoinColumns = @JoinColumn(name = "cate_id")
    )
    private Set<Category> categories = new HashSet<>();

    @ManyToMany(mappedBy = "skills")
    private Set<Job> jobs = new HashSet<>();

    @OneToMany(mappedBy = "skill", fetch = FetchType.LAZY) // Added
    private Set<CandidateSkill> candidateSkills = new HashSet<>();

    @Override // Added
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Skill))
            return false;
        Skill that = (Skill) o;
        return id != null && id.equals(that.id);
    }

    @Override // Added
    public int hashCode() {
        return getClass().hashCode();
    }
}
