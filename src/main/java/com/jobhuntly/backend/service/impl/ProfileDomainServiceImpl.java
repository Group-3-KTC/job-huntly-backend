package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.entity.CandidateProfile;
import com.jobhuntly.backend.entity.User;
import com.jobhuntly.backend.entity.enums.Gender;
import com.jobhuntly.backend.exception.ResourceNotFoundException;
import com.jobhuntly.backend.repository.CandidateProfileRepository;
import com.jobhuntly.backend.repository.UserRepository;
import com.jobhuntly.backend.service.ProfileDomainService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class ProfileDomainServiceImpl implements ProfileDomainService {

    private final CandidateProfileRepository candidateProfileRepository;
    private final UserRepository userRepository;

    @Override
    public CandidateProfile getOrCreateProfile(Long userId) {
        return candidateProfileRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                    CandidateProfile newProfile = createDefaultProfile(user);
                    return candidateProfileRepository.save(newProfile);
                });
    }

    @Override
    public CandidateProfile getProfileOrThrow(Long userId) {
        return candidateProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
    }

    @Override
    public void checkOwnership(Long ownerId, Long userId) {
        if (!ownerId.equals(userId)) {
            throw new RuntimeException("You are not allowed to modify this resource");
        }
    }

    private CandidateProfile createDefaultProfile(User user) {
        CandidateProfile profile = new CandidateProfile();
        profile.setUser(user);
        profile.setAboutMe("");
        profile.setPersonalLink("");
        profile.setGender(Gender.Other);
        profile.setDateOfBirth(null);
        profile.setTitle("");
        profile.setAvatar("");

        profile.setAwards(new HashSet<>());
        profile.setWorkExperiences(new HashSet<>());
        profile.setCertificates(new HashSet<>());
        profile.setEducations(new HashSet<>());
        profile.setSoftSkills(new HashSet<>());
        profile.setCandidateSkills(new HashSet<>());
        return profile;
    }
}