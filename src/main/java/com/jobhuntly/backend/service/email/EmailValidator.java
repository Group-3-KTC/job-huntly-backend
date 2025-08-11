package com.jobhuntly.backend.service.email;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class EmailValidator {
    private static final Pattern VALID_EMAIL_REGEX =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public boolean test(String email) {
        return VALID_EMAIL_REGEX.matcher(email).matches();
    }
}
