package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.Award;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AwardRepository extends JpaRepository<Award, Long> {
    List<Award> findByProfileUserId(Long userId);
}