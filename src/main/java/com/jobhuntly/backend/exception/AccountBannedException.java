package com.jobhuntly.backend.exception;

public class AccountBannedException extends RuntimeException {
    public AccountBannedException(String message) {
        super(message);
    }
}
