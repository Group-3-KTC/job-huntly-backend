package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.response.NotificationFeedResponse;
import com.jobhuntly.backend.security.SecurityUtils;
import com.jobhuntly.backend.service.NotificationPushService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${backend.prefix}/notifications")
@Validated
public class NotificationController {
    private final NotificationPushService notificationPushService;

    @GetMapping("/unread-count")
    public long getUnreadCount() {
        Long userId = SecurityUtils.getCurrentUserId();
        return notificationPushService.getUnreadCount(userId);
    }

    @GetMapping("/feed")
    public NotificationFeedResponse getFeed(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        return notificationPushService.getFeed(userId, pageable);
    }

    @PostMapping("/mark-read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@RequestBody List<Long> ids) {
        Long userId = SecurityUtils.getCurrentUserId();
        notificationPushService.markRead(userId, ids);
    }

    @PostMapping("/mark-all-read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllRead() {
        Long userId = SecurityUtils.getCurrentUserId();
        notificationPushService.markAllRead(userId);
    }
}
