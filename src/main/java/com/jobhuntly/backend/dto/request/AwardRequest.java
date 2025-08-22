package com.jobhuntly.backend.dto.request;

import lombok.Data;

import java.util.Date;

@Data
public class AwardRequest {
    private String name;
    private String issuer;
    private Date date;
    private String description;
}