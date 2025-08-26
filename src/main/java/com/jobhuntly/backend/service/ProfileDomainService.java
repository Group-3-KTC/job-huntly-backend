package com.jobhuntly.backend.service;

import com.jobhuntly.backend.entity.CandidateProfile;

public interface ProfileDomainService {

    CandidateProfile getOrCreateProfile(Long userId);

    CandidateProfile getProfileOrThrow(Long userId);

    void checkOwnership(Long ownerId, Long userId);
}