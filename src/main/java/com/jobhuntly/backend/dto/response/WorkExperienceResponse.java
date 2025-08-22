package com.jobhuntly.backend.dto.response;

import lombok.Data;

@Data
public class WorkExperienceResponse {
    private Long id;
    private String description;
    private String companyName;
    private String position;
    private String duration;
}