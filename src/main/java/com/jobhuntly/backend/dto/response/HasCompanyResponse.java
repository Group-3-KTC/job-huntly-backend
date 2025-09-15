package com.jobhuntly.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HasCompanyResponse {
    private Long userId;
    private Boolean hasCompany;
    private Long companyId;
    private String companyName;
    private String message;
} 