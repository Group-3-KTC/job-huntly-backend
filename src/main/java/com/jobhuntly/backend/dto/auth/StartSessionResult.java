package com.jobhuntly.backend.dto.auth;

import com.jobhuntly.backend.entity.UserSession;

public record StartSessionResult(
        String refreshToken,
        UserSession session
) {}
