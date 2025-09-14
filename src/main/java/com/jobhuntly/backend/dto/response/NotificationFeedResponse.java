package com.jobhuntly.backend.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record NotificationFeedResponse(
        long unreadCount,
        int page,
        int size,
        long totalElements,
        int totalPages,
        List<NotificationItemResponse> items
) {
}
