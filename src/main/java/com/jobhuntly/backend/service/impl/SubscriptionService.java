package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.response.CompanySubscriptionResponse;
import com.jobhuntly.backend.entity.CompanySubscription;
import com.jobhuntly.backend.entity.PackageEntity;
import com.jobhuntly.backend.mapper.CompanySubscriptionMapper;
import com.jobhuntly.backend.repository.CompanyRepository;
import com.jobhuntly.backend.repository.CompanySubscriptionRepository;
import com.jobhuntly.backend.repository.PackageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final CompanySubscriptionRepository subRepo;
    private final PackageRepository packageRepo;
    private final CompanyRepository companyRepo; // nếu bạn dùng vip_until
    private final CompanySubscriptionMapper subscriptionMapper;

    @Transactional
    public CompanySubscriptionResponse activateVip(Long companyId, Long packageId, Long paymentId, OffsetDateTime paidAt) {
        PackageEntity pkg = packageRepo.findById(packageId)
                .orElseThrow(() -> new IllegalArgumentException("Package not found: " + packageId));
        if (pkg.getType() == null || !pkg.getType().equalsIgnoreCase("VIP")) {
            throw new IllegalStateException("Package is not VIP type");
        }

        var now = paidAt != null ? paidAt : OffsetDateTime.now();
        var currentOpt = subRepo.findActiveByCompany(companyId, now);
        CompanySubscription sub;
        if (currentOpt.isEmpty()) {
            sub = CompanySubscription.builder()
                    .companyId(companyId)
                    .packageId(packageId)
                    .status(CompanySubscription.Status.ACTIVE)
                    .startAt(now)
                    .endAt(now.plusDays(pkg.getDurationDays()))
                    .latestPaymentId(paymentId)
                    .build();
        } else {
            sub = currentOpt.get();
            sub.setEndAt(sub.getEndAt().plusDays(pkg.getDurationDays())); // cộng dồn thời gian
            sub.setLatestPaymentId(paymentId);
        }

        var saved = subRepo.save(sub);

        companyRepo.upsertVipAndFlag(companyId, saved.getEndAt());

        return subscriptionMapper.toResponse(saved);
    }
}
