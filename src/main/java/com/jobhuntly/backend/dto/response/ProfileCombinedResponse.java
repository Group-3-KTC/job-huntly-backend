package com.jobhuntly.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class ProfileCombinedResponse {
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
    
    private List<EduResponse> education;
    private List<WorkExperienceResponse> workExperience;
    private List<CertificateResponse> certificates;
    private List<AwardResponse> awards;
    private List<SoftSkillResponse> softSkills;
    private List<CandidateSkillResponse> candidateSkills;
}