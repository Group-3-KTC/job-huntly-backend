package com.jobhuntly.backend.dto.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FollowRequest {
    @NotNull(message = "companyId is required")
    private Long companyId;
}
