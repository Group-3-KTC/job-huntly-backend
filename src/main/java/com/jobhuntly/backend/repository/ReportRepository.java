package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.Report;
import com.jobhuntly.backend.entity.enums.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findByReportType(ReportType type, Pageable pageable);
    Page<Report> findByStatusIgnoreCase(String status, Pageable pageable);

    boolean existsByUser_IdAndReportTypeAndReportedContentId(Long userId, ReportType reportType, Long reportedContentId);

    Page<Report> findByReportTypeAndStatusIgnoreCase(ReportType type, String status, Pageable pageable);

    interface StatusCount {
        String getStatus();
        long getCnt();
    }

    @Query("""
        SELECT r.status AS status, COUNT(r) AS cnt
        FROM Report r
        WHERE (:type IS NULL OR r.reportType = :type)
        GROUP BY r.status
        """)
    List<StatusCount> countGroupByStatus(@Param("type") ReportType type);

    @Query("""
        SELECT COUNT(r)
        FROM Report r
        WHERE (:type IS NULL OR r.reportType = :type)
        """)
    long countAllByType(@Param("type") ReportType type);
}
