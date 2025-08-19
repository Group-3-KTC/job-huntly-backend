package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.SavedJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {
    List<SavedJob> findByUserIdOrderByCreatedAtDesc(Long userId);
    Page<SavedJob> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    long deleteByUserIdAndJobId(Long userId, Long jobId);

    boolean existsByUserIdAndJobId(Long userId, Long jobId);
    Optional<SavedJob> findByUserIdAndJobId(Long userId, Long jobId);
}
