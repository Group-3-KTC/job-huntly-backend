package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.entity.User;
import com.jobhuntly.backend.entity.enums.OneTimeTokenPurpose;
import com.jobhuntly.backend.entity.enums.PasswordTokenPurpose;
import com.jobhuntly.backend.repository.OneTimeTokenRepository;
import com.jobhuntly.backend.service.OneTimeTokenService;
import com.jobhuntly.backend.service.PasswordTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class PasswordTokenServiceImpl implements PasswordTokenService {

    private final OneTimeTokenService oneTimeTokenService;
    private final OneTimeTokenRepository oneTimeTokenRepository;

    @Override
    public String issuePasswordToken(User user, PasswordTokenPurpose purpose, Duration ttl) {
        return oneTimeTokenService.issue(user, mapPurpose(purpose), ttl);
    }

    @Override
    public User verifyPasswordTokenOrThrow(String rawToken, PasswordTokenPurpose expectedPurpose) {
        return oneTimeTokenService.verifyAndConsumeOrThrow(rawToken, mapPurpose(expectedPurpose));
    }

    @Override
    public void clearPasswordToken(User user) {
        if (user == null || user.getId() == null) return;
        oneTimeTokenRepository.deleteActiveTokens(user.getId(), OneTimeTokenPurpose.SET_PASSWORD);
        oneTimeTokenRepository.deleteActiveTokens(user.getId(), OneTimeTokenPurpose.RESET_PASSWORD);
    }

    private static OneTimeTokenPurpose mapPurpose(PasswordTokenPurpose p) {
        return switch (p) {
            case SET   -> OneTimeTokenPurpose.SET_PASSWORD;
            case RESET -> OneTimeTokenPurpose.RESET_PASSWORD;
        };
    }
}
