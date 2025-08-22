package com.jobhuntly.backend.dto.response;


import lombok.Data;
import java.util.Date;

@Data
public class AwardResponse {
    private Long id;
    private String name;
    private String issuer;
    private Date date;
    private String description;
}