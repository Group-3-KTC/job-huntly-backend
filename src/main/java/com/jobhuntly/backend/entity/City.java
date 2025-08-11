package com.jobhuntly.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "location_city")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "city_id")
    private Long id;

    @Column(name = "city_name")
    private String name;
}
