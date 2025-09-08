package com.jobhuntly.backend.dto.response;

import com.jobhuntly.backend.entity.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String roleName;
    private Boolean isActive;
    private Status status;
}
