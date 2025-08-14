package com.jobhuntly.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "work_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "work_type_id")
    private Long id;

    @Column(name = "work_type_name")
    private String name;

    @ManyToMany(mappedBy = "workTypes")
    private Set<Job> jobs = new HashSet<>();
}
