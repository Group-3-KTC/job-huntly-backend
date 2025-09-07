package com.jobhuntly.backend.dto.response;

public record PackageResponse(
        Long packageId,
        String code,
        String name,
        String type,
        Integer durationDays,
        Long priceVnd,
        Boolean isActive
) {
}
