package com.jobhuntly.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "companies")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int userId;
    private String description;
    private String email;
    private String address;
    private int quantityEmployees;
    private String status; // active, inactive, suspended
    private String avatar;
    private String avatarCover;
    private String companyName;

    @OneToOne(mappedBy = "company")
    private User user;
}
