package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.response.AnalyticsKpiResponse;
import com.jobhuntly.backend.dto.response.AnalyticsTrendPoint;
import com.jobhuntly.backend.entity.User;
import com.jobhuntly.backend.repository.ApplicationRepository;
import com.jobhuntly.backend.repository.CompanyRepository;
import com.jobhuntly.backend.repository.JobRepository;
import com.jobhuntly.backend.repository.UserRepository;
import com.jobhuntly.backend.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("${backend.prefix}/analytics")
public class AnalyticsController {
    private final ApplicationRepository applicationRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    private void ensureOwnerOrAdmin(Long requesterUserId, Long companyId) {
        User requester = userRepository.findById(requesterUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        boolean isAdmin = requester.getRole() != null && "ADMIN".equalsIgnoreCase(requester.getRole().getRoleName());
        Long ownerId = companyRepository.findById(companyId)
                .map(c -> c.getUser().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
        boolean isOwner = ownerId.equals(requesterUserId);
        if (!isAdmin && !isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }
    }

    @GetMapping("/kpi")
    public AnalyticsKpiResponse getKpi(@RequestParam Long companyId) {
        Long requester = SecurityUtils.getCurrentUserId();
        ensureOwnerOrAdmin(requester, companyId);

        long activeJobs = jobRepository.findByCompany_Id(companyId, PageRequest.of(0,1))
                .map(j -> 0L).getTotalElements();
        long totalApplicants = applicationRepository.findAllByJob_Company_Id(companyId, PageRequest.of(0,1))
                .map(a -> 0L).getTotalElements();
        LocalDateTime from = LocalDate.now().minusDays(30).atStartOfDay();
        long last30 = applicationRepository.countByCompanyIdAndCreatedAtAfter(companyId, from);
        return new AnalyticsKpiResponse(activeJobs, totalApplicants, last30);
    }

    @GetMapping("/trend")
    public List<AnalyticsTrendPoint> getTrend(
            @RequestParam Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        Long requester = SecurityUtils.getCurrentUserId();
        ensureOwnerOrAdmin(requester, companyId);
        List<Object[]> rows = applicationRepository.countDailyByCompany(companyId, from, to);
        return rows.stream()
                .map(arr -> new AnalyticsTrendPoint((Date) arr[0], ((Number) arr[1]).longValue()))
                .collect(Collectors.toList());
    }
} 