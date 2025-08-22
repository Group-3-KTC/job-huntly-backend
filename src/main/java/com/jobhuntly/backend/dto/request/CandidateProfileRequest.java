package com.jobhuntly.backend.dto.request;


import lombok.Data;

import java.sql.Date;

@Data
public class CandidateProfileRequest {
    private String fullName;
    private String aboutMe;
    private String personalLink;
    private String gender;
    private Date dateOfBirth;
    private String phone;
    private String title;
    private String avatar;
}