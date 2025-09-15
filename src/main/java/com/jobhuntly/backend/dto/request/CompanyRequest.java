package com.jobhuntly.backend.dto.request;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyRequest {
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
    private Boolean isProCompany;
    private String facebookUrl;
    private String twitterUrl;
    private String linkedinUrl;
    private String mapEmbedUrl;
    
    // Files for upload
    private MultipartFile avatarFile;
    private MultipartFile avatarCoverFile;
    
    // URLs (sẽ được set sau khi upload)
    private String avatar;
    private String avatarCover;
    
    // ID các category con - Spring sẽ tự động bind từ FormData
    private Set<Long> categoryIds;
} 