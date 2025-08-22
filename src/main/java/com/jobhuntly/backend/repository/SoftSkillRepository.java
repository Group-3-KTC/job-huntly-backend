package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.SoftSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SoftSkillRepository extends JpaRepository<SoftSkill, Long> {
    List<SoftSkill> findByProfileUserId(Long userId);
}