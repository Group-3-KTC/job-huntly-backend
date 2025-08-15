package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.WorkType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkTypeRepository extends JpaRepository<WorkType, Long> {
    Optional<WorkType> findByNameIgnoreCase(String name);

}
