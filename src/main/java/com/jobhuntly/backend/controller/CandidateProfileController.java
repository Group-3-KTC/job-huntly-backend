package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.response.CandidateProfileResponse;
import com.jobhuntly.backend.dto.response.ProfileCombinedResponse;
import com.jobhuntly.backend.dto.request.CandidateProfileRequest;
import com.jobhuntly.backend.security.SecurityUtils;
import com.jobhuntly.backend.service.CandidateProfileService;
import com.jobhuntly.backend.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("${backend.prefix}/candidate/profile")
@RequiredArgsConstructor
public class CandidateProfileController {

    private final CandidateProfileService candidateProfileService;
    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<CandidateProfileResponse> getProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(candidateProfileService.getCandidateProfile(userId));
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CandidateProfileResponse> updateProfile(
            @ModelAttribute CandidateProfileRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(candidateProfileService.updateCandidateProfile(userId, request));
    }

    @GetMapping("/combined")
    public ResponseEntity<ProfileCombinedResponse> getCombinedProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(profileService.getCombinedProfile(userId));
    }
}
