package com.jobhuntly.backend.service;

import com.jobhuntly.backend.dto.request.ReportRequest;
import com.jobhuntly.backend.dto.request.ReportStatusUpdateRequest;
import com.jobhuntly.backend.dto.response.ReportResponse;
import com.jobhuntly.backend.dto.response.ReportStatsResponse;
import com.jobhuntly.backend.entity.enums.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ReportService {

    ReportResponse create(ReportRequest request, Long userId);
    ReportResponse update(Long reportId, ReportStatusUpdateRequest request);
    void delete(Long reportId);
    Page<ReportResponse> getAll(Pageable pageable);
    ReportResponse getDetailByReportId(Long reportId);
    Page<ReportResponse> getAll(ReportType type, String status, Pageable pageable);

    ReportStatsResponse getStats(ReportType type);
}
