package com.jobhuntly.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplyStatusResponse{
    private boolean applied;
    private Integer attemptCount;
    private LocalDateTime lastUserActionAt;
}
