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
    @Column(name = "company_id")
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    private String description;
    private String email;
    private String address;
    
    @Column(name = "quantity_employee")
    private int quantityEmployees;
    
    private String status;
    private String avatar;
    
    @Column(name = "avatar_cover")
    private String avatarCover;
    
    @Column(name = "company_name")
    private String companyName;
}
