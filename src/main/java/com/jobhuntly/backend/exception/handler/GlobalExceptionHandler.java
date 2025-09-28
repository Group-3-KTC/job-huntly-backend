package com.jobhuntly.backend.exception.handler;

import com.jobhuntly.backend.exception.AccountBannedException;
import com.jobhuntly.backend.exception.GoogleAccountNeedsPasswordException;
import com.jobhuntly.backend.exception.error.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global API error handler that returns ApiError in a consistent shape.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String SUPPORT_EMAIL = "help.jobhuntly@gmail.com";


    private ResponseEntity<ApiError> build(HttpStatus status, String message,
                                           HttpServletRequest req, String code,
                                           Map<String, Object> extra) {
        ApiError body = new ApiError(
                status,
                message,
                safePath(req),
                code,
                extra == null ? Map.of() : extra
        );
        return ResponseEntity.status(status).body(body);
    }

    private String safePath(HttpServletRequest req) {
        try { return req.getRequestURI(); } catch (Exception e) { return ""; }
    }

    @ExceptionHandler(AccountBannedException.class)
    public ResponseEntity<ApiError> handleAccountBanned(AccountBannedException ex, HttpServletRequest req) {
        Map<String, Object> extra = new HashMap<>();
        extra.put("contactEmail", SUPPORT_EMAIL);
        if (ex.getMessage() != null && !ex.getMessage().isBlank()) {
            extra.putIfAbsent("reason", ex.getMessage());
        }
        return build(HttpStatus.FORBIDDEN,
                "Your account has been banned. If you believe this is a mistake, please contact our support.",
                req,
                "ACCOUNT_BANNED",
                extra);
    }


    @ExceptionHandler(GoogleAccountNeedsPasswordException.class)
    public ResponseEntity<ApiError> handleGoogleNeedsPassword(
            GoogleAccountNeedsPasswordException ex,
            HttpServletRequest req
    ) {
        var extra = new java.util.HashMap<String, Object>();
        if (ex.getEmail() != null) extra.put("email", ex.getEmail());
        extra.put("canSetPassword", true);

        ApiError body = new ApiError(
                HttpStatus.CONFLICT,                               // 409
                ex.getMessage() != null ? ex.getMessage()
                        : "This account uses Google Sign-In. Please sign in with Google or set a password first.",
                req.getRequestURI(),
                "GOOGLE_ACCOUNT_NEEDS_PASSWORD",                   // mã lỗi thống nhất cho FE
                extra
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /* ---------- Auth/Security ---------- */

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED,
                "Invalid email or password.",
                req,
                "BAD_CREDENTIALS",
                Map.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED,
                "Unauthenticated.",
                req,
                "UNAUTHENTICATED",
                Map.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN,
                "Access denied.",
                req,
                "ACCESS_DENIED",
                Map.of());
    }

    /* ---------- Validation / Request parsing ---------- */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<Map<String, Object>> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> Map.<String, Object>of(
                        "field", fe.getField(),
                        "message", fe.getDefaultMessage(),
                        "rejectedValue", fe.getRejectedValue()
                ))
                .collect(Collectors.toList());

        Map<String, Object> extra = new HashMap<>();
        extra.put("fieldErrors", fieldErrors);

        return build(HttpStatus.UNPROCESSABLE_ENTITY,
                "Validation failed.",
                req,
                "VALIDATION_ERROR",
                extra);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        List<Map<String, String>> violations = ex.getConstraintViolations()
                .stream()
                .map(v -> Map.of(
                        "property", String.valueOf(v.getPropertyPath()),
                        "message", v.getMessage()
                ))
                .collect(Collectors.toList());

        return build(HttpStatus.UNPROCESSABLE_ENTITY,
                "Validation failed.",
                req,
                "VALIDATION_ERROR",
                Map.of("violations", violations));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST,
                "Missing required parameter: " + ex.getParameterName(),
                req,
                "MISSING_PARAMETER",
                Map.of("parameter", ex.getParameterName()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST,
                "Malformed JSON request.",
                req,
                "MALFORMED_JSON_REQUEST",
                Map.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return build(HttpStatus.METHOD_NOT_ALLOWED,
                "Method not allowed.",
                req,
                "METHOD_NOT_ALLOWED",
                Map.of("method", ex.getMethod()));
    }

    /* ---------- Generic / fallback ---------- */

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST,
                ex.getMessage() != null ? ex.getMessage() : "Bad request.",
                req,
                "BAD_REQUEST",
                Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAny(Exception ex, HttpServletRequest req) {
        // Tránh lộ chi tiết nội bộ ra ngoài. Log ex đầy đủ ở server.
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error.",
                req,
                "INTERNAL_ERROR",
                Map.of());
    }


}