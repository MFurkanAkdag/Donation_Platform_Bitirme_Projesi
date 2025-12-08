package com.seffafbagis.api.dto.response.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardized error response structure for all API error responses.
 * 
 * This class provides a consistent format for error responses across
 * the entire application. It includes optional field-level validation errors.
 * 
 * JSON Output Example (Simple Error):
 * {
 *   "success": false,
 *   "error": {
 *     "code": "NOT_FOUND",
 *     "message": "User not found with id: 123"
 *   },
 *   "timestamp": "2024-01-15T10:30:00",
 *   "path": "/api/v1/users/123"
 * }
 * 
 * JSON Output Example (Validation Error):
 * {
 *   "success": false,
 *   "error": {
 *     "code": "VALIDATION_ERROR",
 *     "message": "Validation failed",
 *     "fieldErrors": [
 *       {
 *         "field": "email",
 *         "message": "Invalid email format",
 *         "rejectedValue": "invalid-email"
 *       }
 *     ]
 *   },
 *   "timestamp": "2024-01-15T10:30:00",
 *   "path": "/api/v1/auth/register"
 * }
 * 
 * @author Furkan
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * Always false for error responses.
     */
    private boolean success;

    /**
     * Nested error details including code, message, and optional field errors.
     */
    private ErrorDetail error;

    /**
     * Timestamp when the error occurred (ISO-8601 format).
     */
    private LocalDateTime timestamp;

    /**
     * Request URI path that caused the error.
     * Useful for logging and debugging.
     */
    private String path;

    /**
     * Nested class containing error details.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetail {

        /**
         * Machine-readable error code for client-side handling.
         */
        private String code;

        /**
         * User-friendly error message to display to end users.
         */
        private String message;

        /**
         * Optional list of field-level validation errors.
         */
        private List<FieldError> fieldErrors;

        // ==================== CONSTRUCTORS ====================

        /**
         * Default constructor for JSON deserialization.
         */
        public ErrorDetail() {
        }

        /**
         * Constructor accepting code and message only.
         * 
         * @param code Machine-readable error code
         * @param message User-friendly error message
         */
        public ErrorDetail(String code, String message) {
            this.code = code;
            this.message = message;
        }

        /**
         * Constructor accepting all fields including field errors.
         * 
         * @param code Machine-readable error code
         * @param message User-friendly error message
         * @param fieldErrors List of field-level validation errors
         */
        public ErrorDetail(String code, String message, List<FieldError> fieldErrors) {
            this.code = code;
            this.message = message;
            this.fieldErrors = fieldErrors;
        }

        // ==================== GETTERS ====================

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public List<FieldError> getFieldErrors() {
            return fieldErrors;
        }

        // ==================== SETTERS ====================

        public void setCode(String code) {
            this.code = code;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void setFieldErrors(List<FieldError> fieldErrors) {
            this.fieldErrors = fieldErrors;
        }
    }

    /**
     * Nested class representing a single field validation error.
     */
    public static class FieldError {

        /**
         * Name of the field that failed validation.
         */
        private String field;

        /**
         * Validation error message for this field.
         */
        private String message;

        /**
         * The value that was rejected (optional).
         * Not included if the value is sensitive or null.
         */
        private Object rejectedValue;

        // ==================== CONSTRUCTORS ====================

        /**
         * Default constructor for JSON deserialization.
         */
        public FieldError() {
        }

        /**
         * Constructor accepting field name and message.
         * 
         * @param field Field name
         * @param message Validation error message
         */
        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        /**
         * Constructor accepting all fields.
         * 
         * @param field Field name
         * @param message Validation error message
         * @param rejectedValue The invalid value
         */
        public FieldError(String field, String message, Object rejectedValue) {
            this.field = field;
            this.message = message;
            this.rejectedValue = rejectedValue;
        }

        // ==================== GETTERS ====================

        public String getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }

        public Object getRejectedValue() {
            return rejectedValue;
        }

        // ==================== SETTERS ====================

        public void setField(String field) {
            this.field = field;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void setRejectedValue(Object rejectedValue) {
            this.rejectedValue = rejectedValue;
        }
    }

    // ==================== CONSTRUCTORS ====================

    /**
     * Default constructor for JSON deserialization.
     */
    public ErrorResponse() {
        this.success = false;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor accepting error details and path.
     * 
     * @param code Machine-readable error code
     * @param message User-friendly error message
     * @param path Request URI path
     */
    public ErrorResponse(String code, String message, String path) {
        this.success = false;
        this.error = new ErrorDetail(code, message);
        this.timestamp = LocalDateTime.now();
        this.path = path;
    }

    /**
     * Constructor with all fields including field errors.
     * 
     * @param code Machine-readable error code
     * @param message User-friendly error message
     * @param fieldErrors Field-level validation errors
     * @param path Request URI path
     */
    public ErrorResponse(String code, String message, List<FieldError> fieldErrors, String path) {
        this.success = false;
        this.error = new ErrorDetail(code, message, fieldErrors);
        this.timestamp = LocalDateTime.now();
        this.path = path;
    }

    // ==================== FACTORY METHODS ====================

    /**
     * Creates an error response without field errors.
     * 
     * @param code Machine-readable error code
     * @param message User-friendly error message
     * @param path Request URI path
     * @return ErrorResponse instance
     */
    public static ErrorResponse of(String code, String message, String path) {
        return new ErrorResponse(code, message, path);
    }

    /**
     * Creates an error response with field errors for validation failures.
     * 
     * @param code Machine-readable error code
     * @param message User-friendly error message
     * @param fieldErrors List of field-level validation errors
     * @param path Request URI path
     * @return ErrorResponse instance
     */
    public static ErrorResponse of(String code, String message, List<FieldError> fieldErrors, String path) {
        return new ErrorResponse(code, message, fieldErrors, path);
    }

    // ==================== GETTERS ====================

    public boolean isSuccess() {
        return success;
    }

    public ErrorDetail getError() {
        return error;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getPath() {
        return path;
    }

    // ==================== SETTERS ====================

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setError(ErrorDetail error) {
        this.error = error;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
