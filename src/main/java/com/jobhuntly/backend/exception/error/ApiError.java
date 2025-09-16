package com.jobhuntly.backend.exception.error;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
public class ApiError {
    private final Instant timestamp = Instant.now();
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private String code;
    private Map<String, Object> extra;

    public ApiError(HttpStatus status, String message, String path, String code, Map<String, Object> extra) {
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.message = message;
        this.path = path;
        this.code = code;
        this.extra = extra;
    }

    public ApiError(HttpStatus status, String error, String message, String path) {
        this.status = status.value();
        this.error = error;
        this.message = message;
        this.path = path;
    }
}
