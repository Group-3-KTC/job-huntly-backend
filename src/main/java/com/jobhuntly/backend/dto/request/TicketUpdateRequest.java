package com.jobhuntly.backend.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class TicketUpdateRequest {
    @Size(max = 500)
    private String subject;
}
