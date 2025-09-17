package com.jobhuntly.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class JobItemWithStatus {
    private JobResponse job;
    private boolean saved;
    private boolean applied;
}
