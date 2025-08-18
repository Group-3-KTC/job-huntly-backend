package com.jobhuntly.backend.dto.response;

import lombok.Builder;

@Builder
public record LocationCompanyResponse(
        String name
) {
}
