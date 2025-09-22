package com.jobhuntly.backend.mapper;

import com.jobhuntly.backend.dto.response.PaymentResponse;
import com.jobhuntly.backend.dto.response.PaymentResponseByCompany;
import com.jobhuntly.backend.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PaymentResponse toResponse(Payment payment);

    @Mapping(target = "id", source = "paymentId")
    @Mapping(target = "companyId", source = "companyId")
    @Mapping(target = "provider", source = "provider") // provider là String
    @Mapping(target = "paidAt", source = "paidAt")
    @Mapping(target = "status",   expression = "java(payment.getStatus() != null ? payment.getStatus().name() : null)")
    PaymentResponseByCompany toList(Payment payment);
}
