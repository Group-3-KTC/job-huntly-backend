package com.jobhuntly.backend.dto.request;


import lombok.Data;

import java.sql.Date;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CandidateProfileRequest {
    private String fullName;
    private String aboutMe;
    private String personalLink;
    private String gender;
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth; 
    private String phone;
    private String title;
    private MultipartFile avatar;
}