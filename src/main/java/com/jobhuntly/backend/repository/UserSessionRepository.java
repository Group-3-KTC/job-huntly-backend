package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findByRefreshTokenHash(String refreshTokenHash);

    List<UserSession> findByUser_IdAndRevokedAtIsNull(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update UserSession s set s.revokedAt = CURRENT_TIMESTAMP " +
            "where s.sessionId = :sessionId and s.revokedAt is null")
    int revokeBySessionId(@Param("sessionId") Long sessionId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update UserSession s set s.revokedAt = CURRENT_TIMESTAMP " +
            "where s.user.id = :userId and s.revokedAt is null")
    int revokeAllByUserId(@Param("userId") Long userId);

    /** Revoke toàn bộ family (khi phát hiện reuse) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update UserSession s set s.revokedAt = CURRENT_TIMESTAMP " +
            "where s.sessionFamilyId = :familyId and s.revokedAt is null")
    int revokeFamily(@Param("familyId") String familyId);

    /** Dọn rác: xoá phiên đã hết hạn trước thời điểm cutoff */
    @Modifying
    @Query("delete from UserSession s where s.refreshExpiresAt < :cutoff")
    int deleteExpired(@Param("cutoff") Instant cutoff);
}
