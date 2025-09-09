package com.jobhuntly.backend.mapper;


import com.jobhuntly.backend.dto.response.FollowResponse;
import com.jobhuntly.backend.entity.Company;
import com.jobhuntly.backend.entity.Follow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FollowMapper {
    @Mapping(target = "followId",         source = "follow.followId")
    @Mapping(target = "userId",           source = "follow.userId")
    @Mapping(target = "companyId",        source = "company.id")
    @Mapping(target = "companyName",      source = "company.companyName")
    @Mapping(target = "companyAvatar",    source = "company.avatar")
    @Mapping(target = "jobsCount",        source = "company.jobsCount")
    @Mapping(target = "quantityEmployee", source = "company.quantityEmployee")
    FollowResponse toResponse(Follow follow, Company company);
}
