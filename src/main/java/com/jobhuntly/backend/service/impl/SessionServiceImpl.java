package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.auth.RefreshResult;
import com.jobhuntly.backend.dto.auth.StartSessionResult;
import com.jobhuntly.backend.entity.User;
import com.jobhuntly.backend.entity.UserSession;
import com.jobhuntly.backend.repository.UserSessionRepository;
import com.jobhuntly.backend.security.jwt.JwtProperties;
import com.jobhuntly.backend.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final UserSessionRepository sessionRepo;
    private final JwtProperties jwtProps;

    private static final SecureRandom RNG = new SecureRandom();

    @Override
    @Transactional
    public StartSessionResult startSession(User user, HttpServletRequest req, String deviceLabel) {
        String familyId = UUID.randomUUID().toString();
        String raw = newUrlSafeToken(32);
        String hash = DigestUtils.sha256Hex(raw);

        Instant now = Instant.now();
        Instant exp = now.plus(jwtProps.getRefreshTtl());

        UserSession s = UserSession.builder()
                .user(user)
                .sessionFamilyId(familyId)
                .refreshTokenHash(hash)
                .refreshExpiresAt(exp)
                .createdAt(now)
                .lastSeenAt(now)
                .ipAddress(clientIp(req))
                .userAgent(safe(req.getHeader("User-Agent"), 255))
                .deviceLabel(safe(deviceLabel, 100))
                .build();

        sessionRepo.save(s);
        return new StartSessionResult(raw, s);
    }

    @Override
    @Transactional
    public RefreshResult rotate(String rawRefreshToken, HttpServletRequest req) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing refresh token");
        }
        String hash = DigestUtils.sha256Hex(rawRefreshToken);

        UserSession s1 = sessionRepo.findByRefreshTokenHash(hash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        Instant now = Instant.now();
        if (s1.getRefreshExpiresAt() == null || !s1.getRefreshExpiresAt().isAfter(now)) {
            if (s1.getRevokedAt() == null) {
                s1.setRevokedAt(now);
                sessionRepo.save(s1);
            }
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh expired");
        }

        if (s1.getRevokedAt() != null) {
            if (s1.getReplacedBy() != null) {
                markReuseAndRevokeFamily(s1, now);
            }
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh revoked");
        }

        String raw2 = newUrlSafeToken(32);
        String hash2 = DigestUtils.sha256Hex(raw2);

        UserSession s2 = UserSession.builder()
                .user(s1.getUser())
                .sessionFamilyId(s1.getSessionFamilyId())
                .parent(s1)
                .refreshTokenHash(hash2)
                .refreshExpiresAt(now.plus(jwtProps.getRefreshTtl()))
                .createdAt(now)
                .lastSeenAt(now)
                .ipAddress(clientIp(req))
                .userAgent(safe(req.getHeader("User-Agent"), 255))
                .deviceLabel(s1.getDeviceLabel())
                .build();

        sessionRepo.save(s2);

        s1.setReplacedBy(s2);
        s1.setRevokedAt(now);
        s1.setLastSeenAt(now);
        sessionRepo.save(s1);

        return new RefreshResult(s1.getUser(), raw2, s2);
    }

    @Override
    @Transactional
    public void revokeCurrent(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) return;
        String hash = DigestUtils.sha256Hex(rawRefreshToken);
        sessionRepo.findByRefreshTokenHash(hash).ifPresent(s -> {
            if (s.getRevokedAt() == null) {
                s.setRevokedAt(Instant.now());
                sessionRepo.save(s);
            }
        });
    }

    @Override
    @Transactional
    public int revokeAll(Long userId) {
        return sessionRepo.revokeAllByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSession> listActiveSessions(Long userId) {
        Instant now = Instant.now();
        return sessionRepo.findByUser_IdAndRevokedAtIsNull(userId)
                .stream()
                .filter(s -> s.getRefreshExpiresAt() != null && s.getRefreshExpiresAt().isAfter(now))
                .toList();
    }


    private void markReuseAndRevokeFamily(UserSession reused, Instant when) {
        if (reused.getReuseDetectedAt() == null) {
            reused.setReuseDetectedAt(when);
            sessionRepo.save(reused);
        }
        int n = sessionRepo.revokeFamily(reused.getSessionFamilyId());
        log.warn("Detected refresh reuse; revoked {} session(s) in family={}", n, reused.getSessionFamilyId());
    }

    private static String clientIp(HttpServletRequest req) {
        if (req == null) return null;
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int comma = xff.indexOf(',');
            return comma > 0 ? xff.substring(0, comma).trim() : xff.trim();
        }
        return req.getRemoteAddr();
    }

    private static String newUrlSafeToken(int bytes) {
        byte[] buf = new byte[bytes];
        RNG.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    private static String safe(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
