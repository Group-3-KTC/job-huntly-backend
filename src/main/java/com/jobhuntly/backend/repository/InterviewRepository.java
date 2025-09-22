package com.jobhuntly.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.jobhuntly.backend.entity.Interview;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    Page<Interview> findByCompanyIdOrderByScheduledAtDesc(Long companyId, Pageable pageable);

    Page<Interview> findByCandidateIdOrderByScheduledAtDesc(Long candidateId, Pageable pageable);
}
