package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByUserIdAndCompanyId(Long userId, Long companyId);

    Optional<Follow> findByUserIdAndCompanyId(Long userId, Long companyId);

    long countByCompanyId(Long companyId);

    int deleteByUserIdAndCompanyId(Long userId, Long companyId);

    Page<Follow> findByUserId(Long userId, Pageable pageable);

    @Query("select f.userId from Follow f where f.companyId = :companyId")
    List<Long> findUserIdsByCompanyId(Long companyId);
}
