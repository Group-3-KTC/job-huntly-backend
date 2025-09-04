package com.jobhuntly.backend.mapper;

import com.jobhuntly.backend.dto.response.CompanySubscriptionResponse;
import com.jobhuntly.backend.entity.CompanySubscription;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CompanySubscriptionMapper {
    CompanySubscriptionResponse toResponse(CompanySubscription subscription);
}
