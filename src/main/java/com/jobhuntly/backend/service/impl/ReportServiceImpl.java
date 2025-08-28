package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.request.ReportRequest;
import com.jobhuntly.backend.dto.request.ReportStatusUpdateRequest;
import com.jobhuntly.backend.dto.response.ReportResponse;
import com.jobhuntly.backend.entity.Report;
import com.jobhuntly.backend.entity.User;
import com.jobhuntly.backend.entity.enums.ReportType;
import com.jobhuntly.backend.mapper.ReportMapper;
import com.jobhuntly.backend.repository.CompanyRepository;
import com.jobhuntly.backend.repository.JobRepository;
import com.jobhuntly.backend.repository.ReportRepository;
import com.jobhuntly.backend.repository.UserRepository;
import com.jobhuntly.backend.service.ReportService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import static com.jobhuntly.backend.entity.enums.ReportType.COMPANY;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final ReportMapper reportMapper;

    @Override
    public ReportResponse create(ReportRequest request, Long userId) {
        // 1) Validate input
        if (request.getReportType() == null) {
            throw new IllegalArgumentException("reportType là bắt buộc.");
        }
        if (request.getReportedContentId() == null) {
            throw new IllegalArgumentException("reportedContentId là bắt buộc.");
        }

        // 2) Chặn tạo report trùng (cùng user, cùng type, cùng content)
        boolean duplicated = reportRepository
                .existsByUser_IdAndReportTypeAndReportedContentId(
                        userId, request.getReportType(), request.getReportedContentId());
        if (duplicated) {
            throw new IllegalStateException("Bạn đã report nội dung này rồi.");
        }
        Report report = reportMapper.toEntity(request);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User không tồn tại"));
        report.setUser(user);

        // 5) Validate tồn tại của đối tượng bị report (job/user/company)
        validateReportedTargetExists(request.getReportType(), request.getReportedContentId());
        report.setStatus(null);
        Report saved = reportRepository.save(report);
        return reportMapper.toResponse(saved);
    }

    @Override
    public ReportResponse update(Long reportId, ReportStatusUpdateRequest request) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("Report không tồn tại: " + reportId));

        // Chuẩn hoá & validate status
        String newStatus = normalizeStatus(request.getStatus());
        validateStatus(newStatus); // ném lỗi nếu không hợp lệ (tuỳ bạn muốn cho phép giá trị nào)

        // Chỉ cập nhật mỗi status
        report.setStatus(newStatus);

        Report saved = reportRepository.save(report);
        return reportMapper.toResponse(saved);
    }

    @Override
    public void delete(Long reportId) {
        if (!reportRepository.existsById(reportId)) {
            throw new EntityNotFoundException("Report not found: " + reportId);
        }
        reportRepository.deleteById(reportId);
    }

    @Override
    public Page<ReportResponse> getAll(Pageable pageable) {
        return reportRepository.findAll(pageable).map(reportMapper::toResponse);
    }

    @Override
    public ReportResponse getDetailByReportId(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("Report not found: " + reportId));
        return reportMapper.toResponse(report);
    }

    @Override
    public Page<ReportResponse> getAll(ReportType type, String status, Pageable pageable) {
        if (type == null && (status == null || status.isBlank())) {
            return getAll(pageable);
        }
        if (type != null && (status == null || status.isBlank())) {
            return reportRepository.findByReportType(type, pageable).map(reportMapper::toResponse);
        }
        if (type == null) {
            return reportRepository.findByStatusIgnoreCase(status, pageable).map(reportMapper::toResponse);
        }
        return reportRepository.findByReportTypeAndStatusIgnoreCase(type, status, pageable)
                .map(reportMapper::toResponse);
    }

    private void validateReportedTargetExists(ReportType type, Long targetId) {
        boolean exists = switch (type) {
            case JOB -> jobRepository.existsById(targetId);
            case USER -> userRepository.existsById(targetId);
            case COMPANY -> companyRepository.existsById(targetId);
        };
        if (!exists) {
            throw new EntityNotFoundException("Reported content not found: type=" + type + ", id=" + targetId);
        }
    }

    private String normalizeStatus(String s) {
        return s == null ? null : s.trim().toUpperCase();
    }

    // Cho phép 3 trạng thái mẫu. Bạn có thể chỉnh list này theo hệ thống của bạn.
    private static final java.util.Set<String> ALLOWED_STATUSES =
            java.util.Set.of("PROCESS", "DONE", "REJECTED");

    private void validateStatus(String status) {
        if (status == null || !ALLOWED_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ. Hợp lệ: " + ALLOWED_STATUSES);
        }
    }
}
