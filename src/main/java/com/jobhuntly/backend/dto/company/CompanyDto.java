package com.jobhuntly.backend.dto.company;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyDto {
    private Long id;
    private Integer userId;
    private String description;
    private String email;
    private String address;
    private int quantityEmployees;
    private String status; // active, inactive, suspended
    private String avatar;
    private String avatarCover;
    private String companyName;
}
