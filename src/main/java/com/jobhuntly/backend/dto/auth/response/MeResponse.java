package com.jobhuntly.backend.dto.auth.response;


public record MeResponse(String email, String fullName, String role, String avatar) {
}
