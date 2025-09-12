package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.entity.OneTimeToken;
import com.jobhuntly.backend.entity.User;
import com.jobhuntly.backend.entity.enums.OneTimeTokenPurpose;
import com.jobhuntly.backend.repository.OneTimeTokenRepository;
import com.jobhuntly.backend.service.OneTimeTokenService;
import com.jobhuntly.backend.util.TokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class OneTimeTokenServiceImpl implements OneTimeTokenService {

    private final OneTimeTokenRepository tokenRepo;

    @Override
    @Transactional
    public String issue(User user, OneTimeTokenPurpose purpose, Duration ttl) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User is required");
        }
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            ttl = Duration.ofHours(24);
        }
        tokenRepo.deleteActiveTokens(user.getId(), purpose);

        String raw = TokenUtil.newUrlSafeToken(32);
        String hash = sha256Hex(raw);

        Instant now = Instant.now();
        OneTimeToken t = OneTimeToken.builder()
                .user(user)
                .purpose(purpose)
                .tokenHash(hash)
                .expiresAt(now.plus(ttl))
                .build();

        tokenRepo.save(t);
        return raw;
    }

    @Override
    @Transactional
    public User verifyAndConsumeOrThrow(String rawToken, OneTimeTokenPurpose expectedPurpose) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing token");
        }
        String hash = sha256Hex(rawToken);
        log.info("Hashed tokenRaw: {}", hash);
        log.info("expectedPurpose: {}", expectedPurpose);

        OneTimeToken t = tokenRepo
                .findActiveWithUser(expectedPurpose, hash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired link"));

        Instant now = Instant.now();
        if (t.getExpiresAt() == null || !t.getExpiresAt().isAfter(now)) {
            safeConsume(t);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired link");
        }

        int updated = tokenRepo.markConsumed(t.getTokenId());
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired link");
        }
        return t.getUser();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canResend(User user, OneTimeTokenPurpose purpose, Duration cooldown) {
        if (user == null || user.getId() == null) return true;
        if (cooldown == null || cooldown.isZero() || cooldown.isNegative()) return true;

        return tokenRepo.findTopByUser_IdAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(user.getId(), purpose)
                .map(last -> Instant.now().isAfter(last.getCreatedAt().plus(cooldown)))
                .orElse(true);
    }

    @Override
    @Transactional
    public int pruneExpired(Instant cutoff) {
        if (cutoff == null) cutoff = Instant.now();
        return tokenRepo.deleteExpired(cutoff);
    }

    private static String sha256Hex(String raw) {
        return DigestUtils.sha256Hex(raw);
    }

    private void safeConsume(OneTimeToken t) {
        try {
            tokenRepo.markConsumed(t.getTokenId());
        } catch (Exception ignore) {
        }
    }
}