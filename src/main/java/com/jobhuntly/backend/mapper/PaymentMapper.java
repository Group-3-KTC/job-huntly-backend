package com.jobhuntly.backend.mapper;

import com.jobhuntly.backend.dto.response.PaymentResponse;
import com.jobhuntly.backend.dto.response.PaymentResponseByCompany;
import com.jobhuntly.backend.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PaymentResponse toResponse(Payment payment);

    @Mapping(target = "companyId", source = "companyId") // nếu là quan hệ: source = "company.id"
    @Mapping(target = "provider", expression = "java(p.getProvider() != null ? p.getProvider().name() : null)")
    @Mapping(target = "status", expression = "java(p.getStatus() != null ? p.getStatus().name() : null)")
    PaymentResponseByCompany toList(Payment payment);
}
