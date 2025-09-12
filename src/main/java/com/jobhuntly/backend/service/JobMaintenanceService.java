package com.jobhuntly.backend.service;

import com.jobhuntly.backend.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobMaintenanceService {

    private final JobRepository jobRepository;

    // Chạy lúc 00:30 mỗi ngày (theo timezone server)
    @Scheduled(cron = "0 30 0 * * *")
    public void deactivateExpiredJobsDaily() {
        int updated = jobRepository.markExpiredJobsInactive();
        if (updated > 0) {
            log.info("Deactivated {} expired jobs", updated);
        }
    }
} 