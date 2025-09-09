package com.jobhuntly.backend.dto.response;

public record FollowResponse(
        Long followId,
        Long userId,
        Long companyId,
        String companyName,
        String companyAvatar,
        Long jobsCount,
        Integer quantityEmployee
) {
}
