package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.response.NotificationFeedResponse;
import com.jobhuntly.backend.dto.response.NotificationItemResponse;
import com.jobhuntly.backend.entity.Notification;
import com.jobhuntly.backend.repository.NotificationRepository;
import com.jobhuntly.backend.service.NotificationPushService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationPushServiceImpl implements NotificationPushService {

    private final NotificationRepository notificationRepository;

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadAtIsNull(userId);
    }

    @Override
    public NotificationFeedResponse getFeed(Long userId, Pageable pageable) {
        Page<Notification> page = notificationRepository.findFeedByUser(userId, pageable);

        List<NotificationItemResponse> items = page.getContent().stream()
                .map(this::toItem)
                .toList();

        long unread = notificationRepository.countByUserIdAndReadAtIsNull(userId);

        return NotificationFeedResponse.builder()
                .unreadCount(unread)
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .items(items)
                .build();
    }

    @Override
    public int markRead(Long userId, List<Long> notificationIds) {
        if (notificationIds == null || notificationIds.isEmpty()) return 0;
        return notificationRepository.markReadByIds(userId, notificationIds);
    }

    @Override
    public int markAllRead(Long userId) {
        return notificationRepository.markAllRead(userId);
    }

    private NotificationItemResponse toItem(Notification n) {
        return NotificationItemResponse.builder()
                .id(n.getNotificationId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .link(n.getLink())
                .companyId(n.getCompanyId())
                .jobId(n.getJobId())
                .applicationId(n.getApplicationId())
                .createdAt(n.getCreatedAt())
                .readAt(n.getReadAt())
                .isRead(n.getReadAt() != null)
                .build();
    }
}
