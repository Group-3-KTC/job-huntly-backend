package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.Edu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EduRepository extends JpaRepository<Edu, Long> {
    List<Edu> findByProfileUserId(Long userId);
}