package com.jobhuntly.backend.service.impl;


import com.jobhuntly.backend.dto.response.*;
import com.jobhuntly.backend.vnpay.VnPayGateway;
import com.jobhuntly.backend.entity.Payment;
import com.jobhuntly.backend.mapper.PaymentMapper;
import com.jobhuntly.backend.repository.PackageRepository;
import com.jobhuntly.backend.repository.PaymentRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepo;
    private final PackageRepository packageRepo;
    private final SubscriptionService subscriptionService;
    private final VnPayGateway vnPayGateway;
    private final PaymentMapper paymentMapper;

    @Transactional
    public CheckoutResponse createCheckout(Long companyId, Long packageId, Long amountVnd,
                                           String bankCode, String locale, HttpServletRequest req) {
        if (amountVnd == null) {
            var pkg = packageRepo.findById(packageId)
                    .orElseThrow(() -> new IllegalArgumentException("Package not found: " + packageId));
            amountVnd = pkg.getPriceVnd();
        }

        String txnRef = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

        var payment = Payment.builder()
                .companyId(companyId)
                .packageId(packageId)
                .amountVnd(amountVnd)
                .provider("VNPAY")
                .status(Payment.Status.PENDING)
                .currency("VND")
                .txnRef(txnRef)
                .build();

        var saved = paymentRepo.save(payment);
        String paymentUrl = vnPayGateway.buildPayUrl(txnRef, amountVnd, bankCode, locale, req);

        PaymentResponse paymentDto = paymentMapper.toResponse(saved);
        return CheckoutResponse.builder()
                .code("00")
                .message("success")
                .txnRef(txnRef)
                .paymentUrl(paymentUrl)
                .payment(paymentDto)
                .build();
    }

    @Transactional
    public CallbackResponse handleVnpayCallback(Map<String, String> data, String source) {
        if (!vnPayGateway.verifySignature(data)) {
            return CallbackResponse.builder()
                    .success(false).message("INVALID_SIGNATURE")
                    .payment(null).subscription(null).build();
        }

        String responseCode = data.get("vnp_ResponseCode"); // "00" = success
        String txnRef = data.get("vnp_TxnRef");
        String transactionNo = data.get("vnp_TransactionNo");

        var payment = paymentRepo.findByTxnRef(txnRef)
                .orElseThrow(() -> new IllegalArgumentException("Unknown txnRef " + txnRef));

        if ("00".equals(responseCode) && payment.getStatus() == Payment.Status.PAID) {
            return CallbackResponse.builder()
                    .success(true).message("ALREADY_PAID")
                    .payment(paymentMapper.toResponse(payment))
                    .subscription(null)
                    .build();
        }

        if ("00".equals(responseCode)) {
            payment.setStatus(Payment.Status.PAID);
            payment.setPaidAt(OffsetDateTime.now());
            payment.setProviderTxn(transactionNo);
            var saved = paymentRepo.save(payment);

            CompanySubscriptionResponse subDto = null;
            if (saved.getPackageId() != null) {
                subDto = subscriptionService.activateVip(
                        saved.getCompanyId(), saved.getPackageId(), saved.getPaymentId(), saved.getPaidAt());
            }

            return CallbackResponse.builder()
                    .success(true).message("PAID")
                    .payment(paymentMapper.toResponse(saved))
                    .subscription(subDto)
                    .build();
        } else {
            payment.setStatus(Payment.Status.FAILED);
            var saved = paymentRepo.save(payment);
            return CallbackResponse.builder()
                    .success(false).message("FAILED_CODE_" + responseCode)
                    .payment(paymentMapper.toResponse(saved))
                    .subscription(null)
                    .build();
        }
    }

    public Page<PaymentResponseByCompany> getByCompany(Long companyId, Pageable pageable) {
        int size = Math.min(Math.max(pageable.getPageSize(), 1), 100);
        int page = Math.max(pageable.getPageNumber(), 0);
        Pageable enforced = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paymentId"));

        return paymentRepo.findByCompanyId(companyId, enforced).map(paymentMapper::toList);
    }
}
