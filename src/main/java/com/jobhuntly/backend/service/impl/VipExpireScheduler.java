package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.repository.CompanyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class VipExpireScheduler {
    private final CompanyRepository companyRepo;

    @Scheduled(cron = "0 5 0 * * *") // giây phút giờ ngày tháng thứ
    @Transactional
    public void expireVipCompanies() {
        OffsetDateTime now = OffsetDateTime.now();
        int affected = companyRepo.expireVip(now);
        if (affected > 0) {
            log.info("Expired VIP for {} companies at {}", affected, now);
        }
    }
}
