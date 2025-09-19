package com.jobhuntly.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

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

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    private String website;

    private String address;

    @Column(name = "location_city")
    private String locationCity;

    @Column(name = "location_country")
    private String locationCountry;

    @Column(name = "founded_year")
    private Integer foundedYear; // dùng Integer để map YEAR

    @Column(name = "quantity_employee")
    private Integer quantityEmployee;

    private String status; // "active", "inactive", "pending"

    @Column(name = "is_pro_company")
    private Boolean proCompany;

    @Column(name = "followers_count")
    private Integer followersCount;

    @Column(name = "jobs_count")
    private Long jobsCount;

    @Column(name = "facebook_url")
    private String facebookUrl;

    @Column(name = "twitter_url")
    private String twitterUrl;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "map_embed_url", columnDefinition = "TEXT")
    private String mapEmbedUrl;

    private String avatar;

    @Column(name = "avatar_cover")
    private String avatarCover;

    @Column(name = "vip_until")
    private OffsetDateTime vipUntil;

    @Column(name = "is_vip")
    private Boolean isVip;

    // Many-to-Many relationship with Category
    @ManyToMany
    @JoinTable(
            name = "company_categories",
            joinColumns = @JoinColumn(name = "company_id"),
            inverseJoinColumns = @JoinColumn(name = "cate_id")
    )
    private Set<Category> categories = new HashSet<>();
}