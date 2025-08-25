package com.jobhuntly.backend.dto.request;

import java.sql.Date;

import lombok.Data;

@Data
public class CertificateRequest {
    private String cerName;
    private Date date;
    private String description;
    private String issuer;
}