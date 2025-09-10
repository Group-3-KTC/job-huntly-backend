package com.jobhuntly.backend.service;

import com.jobhuntly.backend.entity.User;
import com.jobhuntly.backend.entity.enums.PasswordTokenPurpose;

import java.time.Duration;

public interface PasswordTokenService {
    /** Tạo token thô, lưu hash+purpose+expires vào user, rồi trả về token thô để gửi email */
    String issuePasswordToken(User user, PasswordTokenPurpose purpose, Duration ttl);

    /** Verify token: trả về User hợp lệ; nếu sai/expired -> throw */
    User verifyPasswordTokenOrThrow(String rawToken, PasswordTokenPurpose expectedPurpose);

    /** Xoá 3 cột token_* trong bảng users */
    void clearPasswordToken(User user);
}
