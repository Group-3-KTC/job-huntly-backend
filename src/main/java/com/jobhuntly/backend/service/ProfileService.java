package com.jobhuntly.backend.service;

import com.jobhuntly.backend.dto.response.ProfileCombinedResponse;

public interface ProfileService {
    ProfileCombinedResponse getCombinedProfile(Long userId);
}
