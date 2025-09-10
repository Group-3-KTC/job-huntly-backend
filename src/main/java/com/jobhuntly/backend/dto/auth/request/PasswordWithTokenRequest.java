package com.jobhuntly.backend.dto.auth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordWithTokenRequest {
    @NotBlank
    private String token;
    @NotBlank
    @Size(min = 8, max = 100)
    private String newPassword;
}
