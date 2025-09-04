package com.jobhuntly.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PackageRequest {
    private String code;
    private String name;
    private String type;
    private Integer durationDays;
    private Long priceVnd;
    private Boolean isActive;
}
