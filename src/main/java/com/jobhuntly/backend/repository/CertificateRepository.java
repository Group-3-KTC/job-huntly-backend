package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    List<Certificate> findByProfileUserId(Long userId);
}