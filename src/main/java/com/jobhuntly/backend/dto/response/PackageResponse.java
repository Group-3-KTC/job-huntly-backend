package com.jobhuntly.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageResponse {
    private Long packageId;
    private String code;
    private String name;
    private String type;
    private Integer durationDays;
    private Long priceVnd;
    private Boolean isActive;
}
