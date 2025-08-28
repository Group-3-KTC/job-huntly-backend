package com.jobhuntly.backend.dto.request;

import com.jobhuntly.backend.entity.enums.ReportType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportRequest {
    @NotNull(message = "reportType is required")
    private ReportType reportType;

    @NotNull(message = "reportedContentId is required")
    private Long reportedContentId;

    @Size(max = 200, message = "description must be <= 200 characters")
    private String description;
}
