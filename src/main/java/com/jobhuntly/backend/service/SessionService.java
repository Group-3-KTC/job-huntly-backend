package com.jobhuntly.backend.service;

import com.jobhuntly.backend.dto.auth.RefreshResult;
import com.jobhuntly.backend.dto.auth.StartSessionResult;
import com.jobhuntly.backend.entity.User;
import com.jobhuntly.backend.entity.UserSession;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface SessionService {

    /** Tạo phiên mới khi đăng nhập: trả về refresh token thô để set cookie */
    StartSessionResult startSession(User user, HttpServletRequest req, String deviceLabel);

    /** Dùng refresh token hiện tại (từ cookie) để rotate:
     *  - Trả new access (để issue ở chỗ khác), new refresh (để set cookie), user, session mới */
    RefreshResult rotate(String rawRefreshToken, HttpServletRequest req);

    /** Thu hồi phiên hiện tại (dựa trên refresh token trong cookie) */
    void revokeCurrent(String rawRefreshToken);

    /** Thu hồi tất cả phiên của user */
    int revokeAll(Long userId);

    /** Liệt kê các phiên đang còn hiệu lực của user (chưa revoke, chưa hết hạn) */
    List<UserSession> listActiveSessions(Long userId);
}
