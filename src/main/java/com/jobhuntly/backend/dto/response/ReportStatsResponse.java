package com.jobhuntly.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReportStatsResponse {
    private long total;
    private long done;
    private long process;
    private long rejected;
}
