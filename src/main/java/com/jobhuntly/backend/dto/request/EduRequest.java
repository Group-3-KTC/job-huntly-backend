package com.jobhuntly.backend.dto.request;

import lombok.Data;

@Data
public class EduRequest {
    private String schoolName;
    private String degree;
    private String duration;
    private String majors;
}