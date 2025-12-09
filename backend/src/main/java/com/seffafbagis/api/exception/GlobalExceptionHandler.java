package com.seffafbagis.api.exception;

import com.seffafbagis.api.dto.response.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler.
 * 
 * Tüm controller'lardan fırlatılan exception'ları yakalar
 * ve tutarlı bir format'ta response döner.
 * 
 * @author Furkan
 * @version 1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ==================== CUSTOM EXCEPTION'LAR ====================

    /**
     * BusinessException handler.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {

        logger.warn("Business exception: {} - Path: {}", ex.getMessage(), request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), ex.getErrorCode());
        return new ResponseEntity<>(response, ex.getStatus());
    }

    /**
     * ResourceNotFoundException handler.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        logger.warn("Resource not found: {} - Path: {}", ex.getMessage(), request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), ex.getErrorCode());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * DuplicateResourceException handler.
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateResourceException(
            DuplicateResourceException ex,
            HttpServletRequest request) {

        logger.warn("Duplicate resource: {} - Path: {}", ex.getMessage(), request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), ex.getErrorCode());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * Custom ValidationException handler.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            ValidationException ex,
            HttpServletRequest request) {

        logger.warn("Validation error: {} - Path: {}", ex.getMessage(), request.getRequestURI());

        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", ex.getMessage());
        body.put("errorCode", ex.getErrorCode());
        body.put("errors", ex.getFieldErrors());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Custom AuthenticationException handler.
     */
    @ExceptionHandler(com.seffafbagis.api.exception.AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomAuthenticationException(
            com.seffafbagis.api.exception.AuthenticationException ex,
            HttpServletRequest request) {

        logger.warn("Authentication error: {} - Path: {} - IP: {}",
                ex.getMessage(), request.getRequestURI(), getClientIp(request));

        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), ex.getErrorCode());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Custom AccessDeniedException handler.
     */
    @ExceptionHandler(com.seffafbagis.api.exception.AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomAccessDeniedException(
            com.seffafbagis.api.exception.AccessDeniedException ex,
            HttpServletRequest request) {

        logger.warn("Access denied: {} - Path: {} - IP: {}",
                ex.getMessage(), request.getRequestURI(), getClientIp(request));

        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), ex.getErrorCode());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    // ==================== SPRING SECURITY EXCEPTION'LAR ====================

    /**
     * Spring Security AuthenticationException handler.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {

        logger.warn("Spring authentication error: {} - Path: {} - IP: {}",
                ex.getMessage(), request.getRequestURI(), getClientIp(request));

        ApiResponse<Void> response = ApiResponse.error(
                "Kimlik doğrulama hatası",
                "AUTHENTICATION_FAILED");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Spring Security AccessDeniedException handler.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {

        logger.warn("Spring access denied: {} - Path: {} - IP: {}",
                ex.getMessage(), request.getRequestURI(), getClientIp(request));

        ApiResponse<Void> response = ApiResponse.error(
                "Bu işlem için yetkiniz bulunmamaktadır",
                "ACCESS_DENIED");
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    // ==================== VALIDATION EXCEPTION'LAR ====================

    /**
     * MethodArgumentNotValidException handler.
     * 
     * @Valid annotation'dan gelen hatalar.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        logger.warn("Validation failed - Path: {}", request.getRequestURI());

        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", "Doğrulama hataları");
        body.put("errorCode", "VALIDATION_ERROR");
        body.put("errors", errors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * MissingServletRequestParameterException handler.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {

        logger.warn("Missing parameter: {} - Path: {}", ex.getParameterName(), request.getRequestURI());

        String message = "Zorunlu parametre eksik: " + ex.getParameterName();
        ApiResponse<Void> response = ApiResponse.error(message, "MISSING_PARAMETER");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * MethodArgumentTypeMismatchException handler.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        logger.warn("Type mismatch: {} - Path: {}", ex.getName(), request.getRequestURI());

        String message = "Geçersiz parametre tipi: " + ex.getName();
        ApiResponse<Void> response = ApiResponse.error(message, "TYPE_MISMATCH");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // ==================== HTTP EXCEPTION'LAR ====================

    /**
     * HttpRequestMethodNotSupportedException handler.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {

        logger.warn("Method not allowed: {} - Path: {}", ex.getMethod(), request.getRequestURI());

        String message = "HTTP method desteklenmiyor: " + ex.getMethod();
        ApiResponse<Void> response = ApiResponse.error(message, "METHOD_NOT_ALLOWED");
        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * HttpMediaTypeNotSupportedException handler.
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request) {

        logger.warn("Media type not supported: {} - Path: {}", ex.getContentType(), request.getRequestURI());

        String message = "Desteklenmeyen content type: " + ex.getContentType();
        ApiResponse<Void> response = ApiResponse.error(message, "UNSUPPORTED_MEDIA_TYPE");
        return new ResponseEntity<>(response, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    /**
     * HttpMessageNotReadableException handler.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        logger.warn("Message not readable - Path: {}", request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.error(
                "Geçersiz istek formatı",
                "INVALID_REQUEST_BODY");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * NoHandlerFoundException handler.
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFoundException(
            NoHandlerFoundException ex,
            HttpServletRequest request) {

        logger.warn("No handler found - Path: {}", request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.error(
                "Endpoint bulunamadı: " + ex.getRequestURL(),
                "NOT_FOUND");
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * MaxUploadSizeExceededException handler.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex,
            HttpServletRequest request) {

        logger.warn("File too large - Path: {}", request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.error(
                "Dosya boyutu çok büyük",
                "FILE_TOO_LARGE");
        return new ResponseEntity<>(response, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    // ==================== GENEL EXCEPTION ====================

    /**
     * Beklenmeyen exception handler.
     * Tüm yakalanmamış exception'lar burada işlenir.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(
            Exception ex,
            HttpServletRequest request) {

        // Beklenmeyen hataları ERROR seviyesinde logla
        logger.error("Unexpected error - Path: {} - IP: {}",
                request.getRequestURI(), getClientIp(request), ex);

        // Production'da detaylı hata mesajı gösterme
        ApiResponse<Void> response = ApiResponse.error(
                "Beklenmeyen bir hata oluştu",
                "INTERNAL_SERVER_ERROR");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ==================== YARDIMCI METODLAR ====================

    /**
     * Client IP adresini döndürür.
     * Proxy arkasındaki gerçek IP'yi almaya çalışır.
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // İlk IP adresi gerçek client IP'sidir
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
