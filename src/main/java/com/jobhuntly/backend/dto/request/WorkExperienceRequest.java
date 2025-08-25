package com.jobhuntly.backend.dto.request;

import lombok.Data;

@Data
public class WorkExperienceRequest {
    private String description;
    private String companyName;
    private String position;
    private String duration;
}