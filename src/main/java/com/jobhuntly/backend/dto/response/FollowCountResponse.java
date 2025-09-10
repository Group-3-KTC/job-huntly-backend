package com.jobhuntly.backend.dto.response;

public record FollowCountResponse(
        Long companyId,
        Long totalFollowers
) {
}
