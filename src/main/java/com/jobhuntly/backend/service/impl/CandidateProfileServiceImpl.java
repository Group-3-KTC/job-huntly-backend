package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.request.CandidateProfileRequest;
import com.jobhuntly.backend.dto.response.CandidateProfileResponse;
import com.jobhuntly.backend.entity.CandidateProfile;
import com.jobhuntly.backend.entity.User;
import com.jobhuntly.backend.exception.ResourceNotFoundException;
import com.jobhuntly.backend.mapper.CandidateProfileMapper;
import com.jobhuntly.backend.repository.CandidateProfileRepository;
import com.jobhuntly.backend.repository.UserRepository;
import com.jobhuntly.backend.service.CandidateProfileService;
import com.jobhuntly.backend.service.ProfileDomainService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;


@Service
@RequiredArgsConstructor
public class CandidateProfileServiceImpl implements CandidateProfileService {

    private final CandidateProfileRepository candidateProfileRepository;
    private final CandidateProfileMapper mapper;
    private final ProfileDomainService profileDomainService;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional()
    public CandidateProfileResponse getCandidateProfile(Long userId) {
        CandidateProfile profile = profileDomainService.getOrCreateProfile(userId);
        return mapper.toResponseDTO(profile);
    }

    @Override
    @Transactional
    public CandidateProfileResponse updateCandidateProfile(Long userId, CandidateProfileRequest request) {
        CandidateProfile profile = profileDomainService.getOrCreateProfile(userId);
        
        mapper.updateEntity(profile, request);

        User user = profile.getUser();
        if (request.getFullName() != null && !request.getFullName().isEmpty()) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            user.setPhone(request.getPhone());
        }

        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            try {
                String avatarUrl = cloudinaryService.uploadUserAvatar(userId, request.getAvatar()).secureUrl();
                profile.setAvatar(avatarUrl);
            } catch (Exception e) {
                throw new IllegalStateException("Upload avatar thất bại.", e);
            }
        }

        CandidateProfile savedProfile = candidateProfileRepository.save(profile);
        return mapper.toResponseDTO(savedProfile);
    }
}
