package com.jobhuntly.backend.service;

import com.jobhuntly.backend.dto.request.CandidateProfileRequest;
import com.jobhuntly.backend.dto.response.CandidateProfileResponse;

public interface CandidateProfileService {
    CandidateProfileResponse getCandidateProfile(Long userId);

    CandidateProfileResponse updateCandidateProfile(Long userId, CandidateProfileRequest request);
}
