package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.request.ReportRequest;
import com.jobhuntly.backend.dto.request.ReportStatusUpdateRequest;
import com.jobhuntly.backend.dto.response.ReportResponse;
import com.jobhuntly.backend.dto.response.ReportStatsResponse;
import com.jobhuntly.backend.entity.enums.ReportType;
import com.jobhuntly.backend.security.SecurityUtils;
import com.jobhuntly.backend.security.jwt.JwtUtil;
import com.jobhuntly.backend.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${backend.prefix}/report")
public class ReportController {
    private final ReportService reportService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<ReportResponse> create(
            @Valid @RequestBody ReportRequest request
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        ReportResponse resp = reportService.create(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ReportResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ReportStatusUpdateRequest req
    ) {
        return ResponseEntity.ok(reportService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reportService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportResponse> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getDetailByReportId(id));
    }

    @GetMapping
    public ResponseEntity<Page<ReportResponse>> getAll(
            @RequestParam(required = false) ReportType type,
            @RequestParam(required = false) String status,
            // mặc định sort bản ghi mới nhất
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        if (type == null && (status == null || status.isBlank())) {
            return ResponseEntity.ok(reportService.getAll(pageable));
        }
        return ResponseEntity.ok(reportService.getAll(type, status, pageable));
    }

    @GetMapping("/stats")
    public ResponseEntity<ReportStatsResponse> getStats(
            @RequestParam(required = false) ReportType type
    ) {
        return ResponseEntity.ok(reportService.getStats(type));
    }
}
