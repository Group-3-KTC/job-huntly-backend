package com.jobhuntly.backend.service;

import com.jobhuntly.backend.entity.User;
import com.jobhuntly.backend.entity.enums.OneTimeTokenPurpose;

import java.time.Duration;
import java.time.Instant;

public interface OneTimeTokenService {

    /** Phát token 1-lần (xóa các token chưa dùng cùng purpose trước đó để thu gọn bề mặt tấn công). Trả về RAW token để gửi email. */
    String issue(User user, OneTimeTokenPurpose purpose, Duration ttl);

    /** Xác thực token (đúng mục đích, chưa dùng, chưa hết hạn), sau đó consume. Trả về User gắn với token. */
    User verifyAndConsumeOrThrow(String rawToken, OneTimeTokenPurpose expectedPurpose);

    /** Chống spam gửi lại: true nếu đã qua cooldown kể từ lần phát gần nhất (theo created_at). */
    boolean canResend(User user, OneTimeTokenPurpose purpose, Duration cooldown);

    /** Dọn rác: xóa token đã hết hạn trước mốc cutoff. Trả về số lượng đã xóa. */
    int pruneExpired(Instant cutoff);
}
