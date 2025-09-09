package com.jobhuntly.backend.service;

import com.jobhuntly.backend.dto.response.FollowResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FollowService {
    /**
     * Idempotent create: nếu đã follow rồi thì trả về record hiện có.
     */
    void create(Long userId, Long companyId);

    /**
     * Idempotent delete: xóa nếu có, nếu không có thì bỏ qua.
     */
    void delete(Long userId, Long companyId);

    /**
     * Đếm số follower của 1 company.
     */
    long countFollowers(Long companyId);

    /**
     * Trả về danh sách company mà userId đã follow (phân trang),
     * theo cấu trúc FollowResponse.
     */
    Page<FollowResponse> getFollowedCompanies(Long userId, Pageable pageable);
}
