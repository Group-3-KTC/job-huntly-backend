package com.jobhuntly.backend.service;

import com.jobhuntly.backend.entity.User;
import com.jobhuntly.backend.entity.enums.OneTimeTokenPurpose;

import java.time.Duration;
import java.time.Instant;

public interface OneTimeTokenService {
    String issue(User user, OneTimeTokenPurpose purpose, Duration ttl);
    User verifyAndConsumeOrThrow(String rawToken, OneTimeTokenPurpose expectedPurpose);
    boolean canResend(User user, OneTimeTokenPurpose purpose, Duration cooldown);
    int pruneExpired(Instant cutoff);
}
