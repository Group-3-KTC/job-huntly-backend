package com.jobhuntly.backend.dto.response;


import java.time.Instant;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CvTemplateResponse {
    private Long id;
    private String name;
    private String htmlUrl;
    private String previewImageUrl;
    private Instant createdAt;
    private Instant updatedAt;
}