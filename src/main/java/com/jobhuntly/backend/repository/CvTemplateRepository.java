package com.jobhuntly.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jobhuntly.backend.entity.CvTemplate;

public interface CvTemplateRepository extends JpaRepository<CvTemplate, Long> {
}