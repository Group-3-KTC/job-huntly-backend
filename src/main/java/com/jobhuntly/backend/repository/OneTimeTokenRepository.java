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

    Optional<OneTimeToken> findTopByUser_IdAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(
            Long userId, OneTimeTokenPurpose purpose);

    Optional<OneTimeToken> findTopByUser_IdAndPurposeOrderByCreatedAtDesc(Long userId, OneTimeTokenPurpose purpose);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update OneTimeToken t set t.consumedAt = CURRENT_TIMESTAMP " +
            "where t.tokenId = :id and t.consumedAt is null")
    int markConsumed(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from OneTimeToken t where t.user.id = :uid and t.purpose = :p and t.consumedAt is null")
    int deleteActiveTokens(@Param("uid") Long userId, @Param("p") OneTimeTokenPurpose purpose);

    @Modifying
    @Query("delete from OneTimeToken t where t.expiresAt < :cutoff")
    int deleteExpired(@Param("cutoff") Instant cutoff);

    @Query("""
    select t from OneTimeToken t
    join fetch t.user u
    where t.purpose = :purpose
      and t.tokenHash = :hash
      and t.consumedAt is null
    """)
    Optional<OneTimeToken> findActiveWithUser(
            @Param("purpose") OneTimeTokenPurpose purpose,
            @Param("hash") String hash);
}
