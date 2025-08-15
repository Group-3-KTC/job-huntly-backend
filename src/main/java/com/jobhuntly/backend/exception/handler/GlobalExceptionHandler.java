package com.jobhuntly.backend.exception.handler;

import com.jobhuntly.backend.exception.error.ApiError;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;        // <--- Spring 6
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;          // <--- thêm import
import org.springframework.web.bind.annotation.RestControllerAdvice;   // <--- thêm import
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.sql.SQLException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /* =======================
       4xx: Client Errors
       ======================= */

    // @Valid trên @RequestBody
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        String path = getPath(request);
        log.debug("400 Validation error at {} -> {}", path, details);
        return build(HttpStatus.BAD_REQUEST, details, path);
    }

    // Thiếu query param bắt buộc
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
                                                                          HttpHeaders headers,
                                                                          HttpStatusCode status,
                                                                          WebRequest request) {
        String path = getPath(request);
        return build(HttpStatus.BAD_REQUEST, "Thiếu tham số: " + ex.getParameterName(), path);
    }

    // JSON parse lỗi / body rỗng
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        String path = getPath(request);
        log.debug("400 NotReadable at {} -> {}", path,
                ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Payload không hợp lệ hoặc không đọc được.", path);
    }

    // Kiểu tham số sai: /by-user/{userId} với userId=abc
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String msg = "Tham số '" + ex.getName() + "' không đúng kiểu"
                + (ex.getRequiredType() != null ? " (" + ex.getRequiredType().getSimpleName() + ")" : "");
        return build(HttpStatus.BAD_REQUEST, msg, req.getRequestURI());
    }

    // @Validated trên @PathVariable/@RequestParam
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        String details = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, details, req.getRequestURI());
    }

    // Sort sai field: ?sort=createAt,desc
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<Object> handlePropertyReference(PropertyReferenceException ex, HttpServletRequest req) {
        String msg = "Trường sort không hợp lệ: " + ex.getPropertyName();
        return build(HttpStatus.BAD_REQUEST, msg, req.getRequestURI());
    }

    // 401/403 từ Spring Security
    @ExceptionHandler({ BadCredentialsException.class, InsufficientAuthenticationException.class })
    public ResponseEntity<Object> handleAuth(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "Chưa xác thực hoặc token không hợp lệ.", req.getRequestURI());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "Không có quyền truy cập.", req.getRequestURI());
    }

    // 404: JPA không tìm thấy entity
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(EntityNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req.getRequestURI());
    }

    // 404: Không có handler cho URL (cần bật cấu hình trong application.properties)
    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex,
                                                                   HttpHeaders headers,
                                                                   HttpStatusCode status,
                                                                   WebRequest request) {
        String path = getPath(request);
        return build(HttpStatus.NOT_FOUND, "Không tìm thấy endpoint: " + ex.getRequestURL(), path);
    }

    // 405: Sai HTTP method
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                         HttpHeaders headers,
                                                                         HttpStatusCode status,
                                                                         WebRequest request) {
        String path = getPath(request);
        return build(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), path);
    }

    // 415: Content-Type không hỗ trợ
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
                                                                     HttpHeaders headers,
                                                                     HttpStatusCode status,
                                                                     WebRequest request) {
        String path = getPath(request);
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content-Type không được hỗ trợ.", path);
    }

    // Multipart lỗi (thiếu cvFile / boundary sai)
    @ExceptionHandler({ MissingServletRequestPartException.class, MultipartException.class })
    public ResponseEntity<Object> handleMultipart(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Yêu cầu multipart không hợp lệ (thiếu cvFile hoặc file rỗng).", req.getRequestURI());
    }

    // 413: File quá lớn
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Object> handleMaxUpload(MaxUploadSizeExceededException ex, HttpServletRequest req) {
        return build(HttpStatus.PAYLOAD_TOO_LARGE, "File vượt quá dung lượng cho phép.", req.getRequestURI());
    }

    // Vi phạm ràng buộc DB (unique, FK…) → 409
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        String rootMsg = rootSqlMessage(ex);
        String msg = (rootMsg != null) ? rootMsg : "Dữ liệu vi phạm ràng buộc.";
        return build(HttpStatus.CONFLICT, msg, req.getRequestURI());
    }

    // Business conflict / request không hợp lệ
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalState(IllegalStateException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req.getRequestURI());
    }

    // ResponseStatusException tuỳ ý
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatus(ResponseStatusException ex, HttpServletRequest req) {
        HttpStatusCode code = ex.getStatusCode();
        HttpStatus status = (code instanceof HttpStatus hs) ? hs : HttpStatus.valueOf(code.value());
        String msg = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        return build(status, msg, req.getRequestURI());
    }

    // JPA validation bọc trong TransactionSystemException
    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<Object> handleTx(TransactionSystemException ex, HttpServletRequest req) {
        Throwable cause = ex.getRootCause();
        if (cause instanceof ConstraintViolationException cve) {
            String details = cve.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; "));
            return build(HttpStatus.BAD_REQUEST, details, req.getRequestURI());
        }
        log.error("500 Transaction error at {}", req.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi giao dịch.", req.getRequestURI());
    }

    /* =======================
       5xx: Server Errors
       ======================= */

    // Bắt tất cả còn lại
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnknown(Exception ex, HttpServletRequest req) {
        log.error("500 Unhandled error at {}", req.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", req.getRequestURI());
    }

    /* =======================
       Helpers
       ======================= */

    // Trả đúng kiểu ResponseEntity<Object> cho các override
    private ResponseEntity<Object> build(HttpStatus status, String message, String path) {
        return ResponseEntity.status(status).body((Object) new ApiError(status, message, path));
    }

    private String getPath(WebRequest request) {
        String desc = request.getDescription(false); // dạng "uri=/api/v1/..."
        return (desc != null && desc.startsWith("uri=")) ? desc.substring(4) : desc;
    }

    private String rootSqlMessage(Throwable ex) {
        Throwable t = ex;
        while (t != null) {
            if (t instanceof SQLException sqlEx && sqlEx.getMessage() != null) {
                return sqlEx.getMessage();
            }
            t = t.getCause();
        }
        return null;
    }
}
