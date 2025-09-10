package com.jobhuntly.backend.dto.response;

import com.jobhuntly.backend.entity.enums.ReportType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponse{
    private Long id;
    private ReportType reportType;
    private Long reportedContentId;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private Long userId;

    private JobResponse job;       // detail
    private CompanyDto company;    // detail
}
