package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTxnRef(String txnRef);
    boolean existsByTxnRefAndStatus(String txnRef, Payment.Status status);

    Page<Payment> findByCompanyId(Long companyId, Pageable pageable);
}
