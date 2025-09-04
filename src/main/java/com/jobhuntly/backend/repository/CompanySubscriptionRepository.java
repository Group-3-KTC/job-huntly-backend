package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.CompanySubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface CompanySubscriptionRepository extends JpaRepository<CompanySubscription, Long> {
    @Query("select cs from CompanySubscription cs " +
            "where cs.companyId = :companyId and cs.status = 'ACTIVE' and cs.endAt > :now " +
            "order by cs.endAt desc")
    Optional<CompanySubscription> findActiveByCompany(Long companyId, OffsetDateTime now);

    List<CompanySubscription> findByCompanyId(Long companyId);
}
