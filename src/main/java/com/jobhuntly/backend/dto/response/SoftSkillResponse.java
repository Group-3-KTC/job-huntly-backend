package com.jobhuntly.backend.dto.response;

import lombok.Data;

@Data
public class SoftSkillResponse {
    private Long id;
    private String name;
    private String description;
    private String level;
}