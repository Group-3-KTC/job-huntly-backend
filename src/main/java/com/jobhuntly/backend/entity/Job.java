package com.jobhuntly.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "date_post")
    private LocalDate datePost;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "expired_date")
    private LocalDate expiredDate;

    @Column(name = "salary_min")
    private Long salaryMin;

    @Column(name = "salary_max")
    private Long salaryMax;

    @Column(name = "salary_type", nullable = false)
    @Builder.Default
    private Integer salaryType = 0;

    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "benefits", columnDefinition = "TEXT")
    private String benefits;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "status", length = 200)
    private String status;

    @ManyToMany
    @JoinTable(
            name = "job_category",
            joinColumns = @JoinColumn(name = "job_id"),
            inverseJoinColumns = @JoinColumn(name = "cate_id")
    )
    private Set<Category> categories = new HashSet<>();

     @ManyToMany
     @JoinTable(name = "job_skill",
        joinColumns = @JoinColumn(name = "job_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id"))
     private Set<Skill> skills = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "job_level",
            joinColumns = @JoinColumn(name = "job_id"),
            inverseJoinColumns = @JoinColumn(name = "level_id")
    )
    private Set<Level> levels = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "job_work_type",
            joinColumns = @JoinColumn(name = "job_id"),
            inverseJoinColumns = @JoinColumn(name = "work_type_id")
    )
    private Set<WorkType> workTypes = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "ward_job",
            joinColumns = @JoinColumn(name = "job_id"),
            inverseJoinColumns = @JoinColumn(name = "ward_id")
    )
    private Set<Ward> wards = new HashSet<>();
}
