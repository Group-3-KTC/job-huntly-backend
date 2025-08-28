// package com.jobhuntly.backend.controller;

// import com.jobhuntly.backend.dto.request.CandidateProfileRequest;
// import com.jobhuntly.backend.dto.response.CandidateProfileResponse;
// import com.jobhuntly.backend.dto.response.ProfileCombinedResponse;
// import com.jobhuntly.backend.service.CandidateProfileService;
// import com.jobhuntly.backend.service.ProfileService;
// import com.jobhuntly.backend.security.jwt.JwtUtil;
// import lombok.RequiredArgsConstructor;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// @RestController
// @RequestMapping("${backend.prefix}/candidate/profile")
// @RequiredArgsConstructor
// public class CandidateProfileController {

//     private final CandidateProfileService candidateProfileService;
//     private final ProfileService profileService;
//     private final JwtUtil jwtUtil;

//     @GetMapping
//     public ResponseEntity<CandidateProfileResponse> getProfile(
//             @RequestHeader("Authorization") String authHeader) {
//         Long userId = extractUserId(authHeader);
//         return ResponseEntity.ok(candidateProfileService.getCandidateProfile(userId));
//     }

//     @PutMapping
//     public ResponseEntity<CandidateProfileResponse> updateProfile(
//             @RequestHeader("Authorization") String authHeader,
//             @RequestBody CandidateProfileRequest request) {
//         Long userId = extractUserId(authHeader);
//         return ResponseEntity.ok(candidateProfileService.updateCandidateProfile(userId, request));
//     }

//     @GetMapping("/combined")
//     public ResponseEntity<ProfileCombinedResponse> getCombinedProfile(
//             @RequestHeader("Authorization") String authHeader) {
//         Long userId = extractUserId(authHeader);
//         return ResponseEntity.ok(profileService.getCombinedProfile(userId));
//     }

//     private Long extractUserId(String authHeader) {
//         String token = authHeader.replace("Bearer ", "");
//         return jwtUtil.extractUserId(token);
//     }
// }
package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.request.CandidateProfileRequest;
import com.jobhuntly.backend.dto.response.CandidateProfileResponse;
import com.jobhuntly.backend.dto.response.ProfileCombinedResponse;
import com.jobhuntly.backend.service.CandidateProfileService;
import com.jobhuntly.backend.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${backend.prefix}/candidate/profile")
@RequiredArgsConstructor
public class CandidateProfileController {

    private final CandidateProfileService candidateProfileService;
    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<CandidateProfileResponse> getProfile() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(candidateProfileService.getCandidateProfile(userId));
    }

    @PutMapping
    public ResponseEntity<CandidateProfileResponse> updateProfile(@RequestBody CandidateProfileRequest request) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(candidateProfileService.updateCandidateProfile(userId, request));
    }

    @GetMapping("/combined")
    public ResponseEntity<ProfileCombinedResponse> getCombinedProfile() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(profileService.getCombinedProfile(userId));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Long) authentication.getPrincipal(); // JwtAuthenticationFilter đã set userId vào principal
    }
}
