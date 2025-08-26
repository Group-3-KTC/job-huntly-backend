package com.jobhuntly.backend.dto.auth.response;


public record MeResponse(Long id, String email, String fullName, String role, String avatar) {
}
