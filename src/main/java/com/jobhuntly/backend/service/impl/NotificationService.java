package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.response.NotificationPayload;
import com.jobhuntly.backend.entity.Notification;
import com.jobhuntly.backend.repository.FollowRepository;
import com.jobhuntly.backend.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {
    private final NotificationRepository notificationRepo;
    private final FollowRepository followRepo;
    private final SimpMessagingTemplate messagingTemplate;

    public void notifyApplicationStatus(Long userId,
                                        Long jobId,
                                        Long companyId,
                                        Long applicationId,
                                        String companyName,
                                        String status) {
        if (companyId != null && !followRepo.existsByUserIdAndCompanyId(userId, companyId)) {
            return; // không follow -> không gửi
        }
        var n = Notification.builder()
                .userId(userId)
                .type("APPLICATION_STATUS")
                .title("Your application status has been updated.")
                .message("Your application status has been changed to " +status+ " by company "+companyName)
                .companyId(companyId)
                .jobId(jobId)
                .applicationId(applicationId)
                .createdAt(Instant.now())
                .build();
        n = notificationRepo.save(n);

        var payload = NotificationPayload.builder()
                .type(n.getType()).title(n.getTitle()).message(n.getMessage())
                .companyId(companyId).jobId(jobId).applicationId(applicationId)
                .createdAt(n.getCreatedAt())
                .build();

        messagingTemplate.convertAndSendToUser(String.valueOf(userId),
                "/queue/noti", payload);
    }

    public void notifyNewJobToFollowers(Long companyId, Long jobId, String companyName, String jobTitle) {
        List<Long> uids = followRepo.findUserIdsByCompanyId(companyId);
        for (Long uid : uids) {
            var n = Notification.builder()
                    .userId(uid)
                    .type("NEW_JOB")
                    .title(companyName+ " has just posted a new job: " +jobTitle)
                    .message("Apply now.")
                    .companyId(companyId)
                    .jobId(jobId)
                    .createdAt(Instant.now())
                    .build();
            n = notificationRepo.save(n);

            var payload = NotificationPayload.builder()
                    .type(n.getType()).title(n.getTitle()).message(n.getMessage())
                    .companyId(companyId).jobId(jobId)
                    .createdAt(n.getCreatedAt())
                    .build();

            messagingTemplate.convertAndSendToUser(String.valueOf(uid), "/queue/noti", payload);
        }
    }
}
