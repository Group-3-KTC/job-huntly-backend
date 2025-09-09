package com.jobhuntly.backend.dto.request;

import com.jobhuntly.backend.entity.enums.Status;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {
    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email không được để trống")
    private String email;
    
    private String googleId;
    
    private String password;
    
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;
    
    private String phone;
    
    @NotNull(message = "Role ID không được để trống")
    private Integer roleId;
    
    private Boolean isActive;
    
    @NotNull(message = "Trạng thái không được để trống")
    private Status status;
} 