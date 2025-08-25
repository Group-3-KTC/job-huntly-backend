package com.jobhuntly.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.sql.Date;

@Data
@Builder
public class CandidateProfileResponse {
    private Long id;
    private String aboutMe;
    private String personalLink;
    private String gender;
    private Date dateOfBirth;
    private String email;
    private String phone;
    private String title;
    private String fullName;
    private String avatar;
}