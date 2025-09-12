package com.jobhuntly.backend.security;

import java.security.Principal;

public class WsPrincipal implements Principal {
    private final Long userId;
    public WsPrincipal(Long userId) { this.userId = userId; }
    public Long getUserId() { return userId; }
    @Override public String getName() { return String.valueOf(userId); }
}
