package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.entity.User;
import com.jobhuntly.backend.entity.enums.PasswordTokenPurpose;
import com.jobhuntly.backend.repository.UserRepository;
import com.jobhuntly.backend.service.PasswordTokenService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class PasswordTokenServiceImpl implements PasswordTokenService {
    private final UserRepository userRepository;
    private static final SecureRandom RNG = new SecureRandom();

    @Override
    public String issuePasswordToken(User user, PasswordTokenPurpose purpose, Duration ttl) {
        String raw = newUrlSafeToken(32);
        String hash = DigestUtils.sha256Hex(raw);

        user.setPasswordTokenHash(hash);
        user.setPasswordTokenPurpose(purpose);
        user.setPasswordTokenExpiresAt(Instant.now().plus(ttl));
        userRepository.save(user);

        return raw; // gửi qua email
    }

    @Override
    public User verifyPasswordTokenOrThrow(String rawToken, PasswordTokenPurpose expectedPurpose) {
        String hash = DigestUtils.sha256Hex(rawToken);
        User u = userRepository
                .findByPasswordTokenPurposeAndPasswordTokenHash(expectedPurpose, hash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token invalid or expired"));

        if (u.getPasswordTokenExpiresAt() == null || u.getPasswordTokenExpiresAt().isBefore(Instant.now())) {
            clearPasswordToken(u);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token invalid or expired");
        }
        return u;
    }

    @Override
    public void clearPasswordToken(User user) {
        user.setPasswordTokenHash(null);
        user.setPasswordTokenPurpose(null);
        user.setPasswordTokenExpiresAt(null);
        userRepository.save(user);
    }

    private static String newUrlSafeToken(int bytes) {
        byte[] buf = new byte[bytes];
        RNG.nextBytes(buf);
        // URL-safe, không có '='
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

}
