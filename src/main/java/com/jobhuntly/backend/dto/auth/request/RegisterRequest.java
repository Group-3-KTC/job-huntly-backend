package com.jobhuntly.backend.dto.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100)
    private String password;

    @NotBlank(message = "Phone number is required")
    // Regex cơ bản: chỉ chấp nhận số, có thể thêm +84 hoặc 0 ở đầu
    @Pattern(regexp = "^(\\+84|0)\\d{9,10}$", message = "Invalid phone number format")
    private String phone;

    @NotBlank(message = "Role is required")
    private String role; // CANDIDATE / RECRUITER / ADMIN
}
