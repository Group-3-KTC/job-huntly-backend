package com.jobhuntly.backend.mapper;

import com.jobhuntly.backend.dto.response.CvTemplateResponse;
import com.jobhuntly.backend.entity.CvTemplate;

public class CvTemplateMapper {
    public static CvTemplateResponse toResponse(CvTemplate e) {
        if (e == null)
            return null;
        return CvTemplateResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .htmlUrl(e.getHtmlUrl())
                .previewImageUrl(e.getPreviewImageUrl())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}