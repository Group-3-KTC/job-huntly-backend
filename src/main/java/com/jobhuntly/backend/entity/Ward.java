package com.jobhuntly.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "location_ward")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ward {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ward_id")
    private Long id;

    @Column(name = "ward_name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @ManyToMany(mappedBy = "wards")
    private Set<Job> jobs = new HashSet<>();

}
