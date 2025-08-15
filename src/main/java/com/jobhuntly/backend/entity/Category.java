package com.jobhuntly.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cate_id")
    private Long id;

    @Column(name = "cate_name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private List<Category> children = new ArrayList<>();

    // M:N với Skill qua skill_categories
    @ManyToMany(mappedBy = "categories")
    private Set<Skill> skills = new HashSet<>();

    @ManyToMany(mappedBy = "categories")
    private Set<Company> companies = new HashSet<>();
}