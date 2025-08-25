package com.jobhuntly.backend.service;

import com.jobhuntly.backend.entity.CandidateProfile;
import com.jobhuntly.backend.entity.User;
import com.jobhuntly.backend.exception.ResourceNotFoundException;
import com.jobhuntly.backend.repository.CandidateProfileRepository;
import com.jobhuntly.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class ProfileDomainService {

    private final CandidateProfileRepository candidateProfileRepository;
    private final UserRepository userRepository;

    public CandidateProfile getOrCreateProfile(Long userId) {
        return candidateProfileRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                    CandidateProfile newProfile = createDefaultProfile(user);
                    return candidateProfileRepository.save(newProfile);
                });
    }

    public CandidateProfile getProfileOrThrow(Long userId) {
        return candidateProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
    }


    private CandidateProfile createDefaultProfile(User user) {
        CandidateProfile profile = new CandidateProfile();
        profile.setUser(user);
        profile.setAboutMe("");
        profile.setPersonalLink("");
        profile.setGender(null);
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

    /**
     * Kiểm tra user có quyền sở hữu profile hay không
     * 
     * @param ownerId
     * @param userId  
     */
    public void checkOwnership(Long ownerId, Long userId) {
        if (!ownerId.equals(userId)) {
            throw new RuntimeException("You are not allowed to modify this resource");
        }
    }
}
