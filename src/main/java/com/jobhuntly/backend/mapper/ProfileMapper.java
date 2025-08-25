package com.jobhuntly.backend.mapper;

import com.jobhuntly.backend.dto.response.ProfileCombinedResponse;
import com.jobhuntly.backend.entity.CandidateProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {
        EduMapper.class,
        WorkExperienceMapper.class,
        CertificateMapper.class,
        AwardMapper.class,
        SoftSkillMapper.class,
        CandidateSkillMapper.class
})
public interface ProfileMapper {

    @Mapping(target = "id", source = "profileId")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "phone", source = "user.phone")
    @Mapping(target = "fullName", source = "user.fullName")
    @Mapping(target = "education", source = "educations")
    @Mapping(target = "workExperience", source = "workExperiences")
    @Mapping(target = "certificates", source = "certificates")
    @Mapping(target = "awards", source = "awards")
    @Mapping(target = "softSkills", source = "softSkills")
    @Mapping(target = "candidateSkills", source = "candidateSkills")
    ProfileCombinedResponse toCombinedResponse(CandidateProfile entity);
}