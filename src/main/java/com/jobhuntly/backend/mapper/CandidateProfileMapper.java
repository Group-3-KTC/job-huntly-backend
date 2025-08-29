package com.jobhuntly.backend.mapper;

import com.jobhuntly.backend.dto.request.CandidateProfileRequest;
import com.jobhuntly.backend.dto.response.CandidateProfileResponse;
import com.jobhuntly.backend.entity.CandidateProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CandidateProfileMapper {

    @Mapping(target = "id", source = "profileId")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "phone", source = "user.phone")
    @Mapping(target = "fullName", source = "user.fullName")
    CandidateProfileResponse toResponseDTO(CandidateProfile entity);

    @Mapping(target = "profileId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "educations", ignore = true)
    @Mapping(target = "workExperiences", ignore = true)
    @Mapping(target = "certificates", ignore = true)
    @Mapping(target = "awards", ignore = true)
    @Mapping(target = "softSkills", ignore = true)
    @Mapping(target = "candidateSkills", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    CandidateProfile toEntity(CandidateProfileRequest dto);

    @Mapping(target = "profileId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "educations", ignore = true)
    @Mapping(target = "workExperiences", ignore = true)
    @Mapping(target = "certificates", ignore = true)
    @Mapping(target = "awards", ignore = true)
    @Mapping(target = "softSkills", ignore = true)
    @Mapping(target = "candidateSkills", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    void updateEntity(@MappingTarget CandidateProfile entity, CandidateProfileRequest dto);
}