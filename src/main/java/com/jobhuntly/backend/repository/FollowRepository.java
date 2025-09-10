package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByUserIdAndCompanyId(Long userId, Long companyId);

    Optional<Follow> findByUserIdAndCompanyId(Long userId, Long companyId);

    long countByCompanyId(Long companyId);

    // Trả về số bản ghi bị xóa (0 hoặc 1) — idempotent delete
    int deleteByUserIdAndCompanyId(Long userId, Long companyId);

    Page<Follow> findByUserId(Long userId, Pageable pageable);
}
