package com.jobhuntly.backend.security;

import com.jobhuntly.backend.dto.auth.AppPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppPrincipal principal = (AppPrincipal) authentication.getPrincipal();
        return principal.id();
    }

    public static AppPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (AppPrincipal) authentication.getPrincipal();
    }
}
