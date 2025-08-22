package com.jobhuntly.backend.dto.request;

import lombok.Data;

@Data
public class SoftSkillRequest {
    private String name;
    private String description;
    private String level; 
}