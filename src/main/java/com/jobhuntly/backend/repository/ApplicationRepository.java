package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.Application;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    @EntityGraph(attributePaths = { "job", "job.company" })
    Page<Application> findAllByUser_Id(Long userId, Pageable pageable);

    Page<Application> findAllByJob_Id(Long jobId, Pageable pageable);

    boolean existsByUser_IdAndJob_Id(Long userId, Long jobId);

    Optional<Application> findByUser_IdAndJob_Id(Integer userId, Integer jobId);

    boolean existsByUser_IdAndJob_Id(Long userId, Integer jobId);

    Optional<Application> findByUser_IdAndJob_Id(Long userId, Integer jobId);

    // Khóa ghi để tránh race condition khi user re-apply cùng lúc nhiều request
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Application a where a.user.id = :userId and a.job.id = :jobId")
    Optional<Application> lockByUserAndJob(@Param("userId") Long userId, @Param("jobId") Long jobId);

}
