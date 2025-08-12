package com.jobhuntly.backend.security;

public class Endpoints {
    public static final String FRONT_END_HOST = "http://localhost:3000";

    public static class Public {
        public static final String[] GET = {
                "/api/auth/activate"
        };

        public static final String[] POST = {
                "/api/auth/register",
                "/api/auth/login",
                "/api/auth/google"

        };

        public static final String[] PUT = {
                // Ví dụ: "/api/auth/reset-password"
        };

        public static final String[] DELETE = {
                // Ví dụ: "/api/auth/remove-temp"
        };
    }

    public static class Admin {
        public static final String[] ALL = {
                "/api/admin/**"
        };
    }
}
