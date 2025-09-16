package com.jobhuntly.backend.exception;

public class GoogleAccountNeedsPasswordException extends RuntimeException {
    private final String email;

    public GoogleAccountNeedsPasswordException(String message, String email) {
        super(message);
        this.email = email;
    }

    public GoogleAccountNeedsPasswordException(String message) {
        super(message);
        this.email = null;
    }

    public String getEmail() {
        return email;
    }
}
