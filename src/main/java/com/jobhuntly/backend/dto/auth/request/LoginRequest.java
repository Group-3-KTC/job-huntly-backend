package com.jobhuntly.backend.dto.auth.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
    private String role;
}
