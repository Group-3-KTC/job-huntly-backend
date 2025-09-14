package com.jobhuntly.backend.security;

import com.jobhuntly.backend.dto.auth.AppPrincipal;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof AppPrincipal appPrincipal) {
            return appPrincipal.id();
        }
        
        if (principal instanceof String userIdString) {
            if ("anonymousUser".equalsIgnoreCase(userIdString)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Anonymous user");
            }
            try {
                return Long.valueOf(userIdString);
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user ID format");
            }
        }
        
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
            "Unsupported principal type: " + principal.getClass().getName());
    }

    public static AppPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof AppPrincipal appPrincipal) {
            return appPrincipal;
        }
        
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
            "Principal is not AppPrincipal: " + principal.getClass().getName());
    }
}
