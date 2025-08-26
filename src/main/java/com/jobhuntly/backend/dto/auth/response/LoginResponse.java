package com.jobhuntly.backend.dto.auth.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String email;
    private String fullName;
    private String role;
    private String avatarUrl;
}
