package com.jobhuntly.backend.dto.response;

import lombok.Data;

import java.util.Date;

@Data
public class CertificateResponse {
    private Long id;
    private String cerName;
    private Date date;
    private String description;
    private String issuer;
}