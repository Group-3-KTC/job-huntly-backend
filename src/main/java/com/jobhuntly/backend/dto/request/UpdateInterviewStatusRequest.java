package com.jobhuntly.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateInterviewStatusRequest(@NotBlank String status) {
}