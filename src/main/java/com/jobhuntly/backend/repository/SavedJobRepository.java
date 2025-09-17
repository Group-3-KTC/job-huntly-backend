package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.SavedJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {
    List<SavedJob> findByUserIdOrderByCreatedAtDesc(Long userId);
    Page<SavedJob> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    long deleteByUserIdAndJobId(Long userId, Long jobId);

    boolean existsByUserIdAndJobId(Long userId, Long jobId);
    Optional<SavedJob> findByUserIdAndJobId(Long userId, Long jobId);

    @Query(value = """
      select job_id 
      from saved_job 
      where user_id = :uid and job_id in (:ids)
      """, nativeQuery = true)
    List<Long> findSavedJobIdsIn(@Param("uid") Long userId, @Param("ids") Collection<Long> jobIds);

}
