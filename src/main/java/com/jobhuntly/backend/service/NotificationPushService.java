package com.jobhuntly.backend.service;

import com.jobhuntly.backend.dto.response.NotificationFeedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationPushService {
    long getUnreadCount(Long userId);

    NotificationFeedResponse getFeed(Long userId, Pageable pageable);

    int markRead(Long userId, List<Long> notificationIds);

    int markAllRead(Long userId);
}
