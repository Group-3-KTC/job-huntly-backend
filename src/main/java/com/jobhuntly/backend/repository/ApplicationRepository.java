package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    @EntityGraph(attributePaths = { "job", "job.company" })
    Page<Application> findAllByUser_Id(Long userId, Pageable pageable);

    Page<Application> findAllByJob_Id(Integer jobId, Pageable pageable);

    boolean existsByUser_IdAndJob_Id(Long userId, Long jobId);

    Optional<Application> findByUser_IdAndJob_Id(Integer userId, Integer jobId);

}
