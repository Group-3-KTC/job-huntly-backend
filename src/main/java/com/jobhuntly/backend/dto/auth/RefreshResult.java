package com.jobhuntly.backend.dto.auth;

import com.jobhuntly.backend.entity.User;
import com.jobhuntly.backend.entity.UserSession;

public record RefreshResult(
        User user,
        String newRefreshToken, // raw
        UserSession newSession
) {}
