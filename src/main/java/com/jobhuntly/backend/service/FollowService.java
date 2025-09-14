package com.jobhuntly.backend.service;

import com.jobhuntly.backend.dto.response.FollowResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FollowService {

    void create(Long userId, Long companyId);

    void delete(Long userId, Long companyId);

    long countFollowers(Long companyId);

    Page<FollowResponse> getFollowedCompanies(Long userId, Pageable pageable);

    boolean isFollowed(Long userId, Long companyId);
}
