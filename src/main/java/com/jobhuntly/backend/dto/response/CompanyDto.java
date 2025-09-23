package com.jobhuntly.backend.dto.response;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyDto {
    private Long id;
    private Long userId;
    private String companyName;
    private String description;
    private String email;
    private String phoneNumber;
    private String website;
    private String address;
    private String locationCity;
    private String locationCountry;
    private Integer foundedYear;
    private Integer quantityEmployee;
    private String status;
    private Boolean proCompany;
    private Integer followersCount;
    private Long jobsCount;
    private String facebookUrl;
    private String twitterUrl;
    private String linkedinUrl;
    private String mapEmbedUrl;
    private String avatar;
    private String avatarCover;

    // Tên các category con
    private Set<String> categories;

    // Tên các category cha
    private Set<String> parentCategories;

    // ID các category con
    private Set<Long> categoryIds;
}
