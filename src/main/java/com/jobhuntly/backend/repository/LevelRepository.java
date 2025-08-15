package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.Level;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LevelRepository extends JpaRepository<Level,Long> {
    Optional<Level> findByNameIgnoreCase(String name);
}
