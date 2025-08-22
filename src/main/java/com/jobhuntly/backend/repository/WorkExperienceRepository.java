
package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.WorkExperience;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkExperienceRepository extends JpaRepository<WorkExperience, Long> {
    List<WorkExperience> findByProfileUserId(Long userId);
}