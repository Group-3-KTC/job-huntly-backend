package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.Report;
import com.jobhuntly.backend.entity.enums.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findByReportType(ReportType type, Pageable pageable);
    Page<Report> findByStatusIgnoreCase(String status, Pageable pageable);

    boolean existsByUser_IdAndReportTypeAndReportedContentId(Long userId, ReportType reportType, Long reportedContentId);

    Page<Report> findByReportTypeAndStatusIgnoreCase(ReportType type, String status, Pageable pageable);
}
