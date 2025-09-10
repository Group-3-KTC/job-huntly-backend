package com.jobhuntly.backend.dto.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailOnlyRequest {
    @NotBlank
    @Email
    private String email;
}
