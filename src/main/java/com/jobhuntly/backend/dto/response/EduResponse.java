package com.jobhuntly.backend.dto.response;

import lombok.Data;

@Data
public class EduResponse {
    private Long id;
    private String schoolName;
    private String degree;
    private String duration;
    private String majors;
}
