package com.jobhuntly.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedJobRequest {
//    @NotNull
//    @JsonProperty("user_id")
//    private Long userId;

    @NotNull
    @JsonProperty("job_id")
    private Long jobId;
}
