package com.jobhuntly.backend.dto.auth.request;

public record UserMeDto(Long id, String email, String fullName, String role, String phone) {
}
