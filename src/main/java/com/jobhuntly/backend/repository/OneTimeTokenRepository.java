package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.OneTimeToken;
import com.jobhuntly.backend.entity.enums.OneTimeTokenPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface OneTimeTokenRepository extends JpaRepository<OneTimeToken, Long> {

    Optional<OneTimeToken> findByPurposeAndTokenHashAndConsumedAtIsNull(
            OneTimeTokenPurpose purpose, String tokenHash);

    /** Lấy lần phát gần nhất cho user + purpose (đã/ chưa dùng đều tính) */
    Optional<OneTimeToken> findTopByUser_IdAndPurposeOrderByCreatedAtDesc(Long userId, OneTimeTokenPurpose purpose);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update OneTimeToken t set t.consumedAt = CURRENT_TIMESTAMP " +
            "where t.tokenId = :id and t.consumedAt is null")
    int markConsumed(@Param("id") Long id);

    /** Xóa mọi token CHƯA dùng cùng purpose cho user (để khi issue mới, các token cũ không còn hiệu lực). */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from OneTimeToken t where t.user.id = :uid and t.purpose = :p and t.consumedAt is null")
    int deleteActiveTokens(@Param("uid") Long userId, @Param("p") OneTimeTokenPurpose purpose);

    /** Dọn rác token đã hết hạn */
    @Modifying
    @Query("delete from OneTimeToken t where t.expiresAt < :cutoff")
    int deleteExpired(@Param("cutoff") Instant cutoff);
}
