package com.seffafbagis.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when authentication is required but not provided or invalid (HTTP 401).
 * 
 * This exception is raised when access to a resource requires authentication
 * that is either missing or invalid (e.g., expired or malformed JWT).
 * 
 * @author Furkan
 * @version 1.0
 */
public class UnauthorizedException extends BusinessException {

    /**
     * Default message for authentication errors.
     */
    private static final String DEFAULT_MESSAGE = "Authentication required";

    /**
     * Constructor with default message.
     * Uses default message "Authentication required".
     */
    public UnauthorizedException() {
        super(DEFAULT_MESSAGE, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }

    /**
     * Constructor accepting custom message.
     * 
     * @param message User-friendly error message
     */
    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }

    /**
     * Constructor accepting message and custom error code.
     * 
     * @param message User-friendly error message
     * @param errorCode Machine-readable error code
     */
    public UnauthorizedException(String message, String errorCode) {
        super(message, HttpStatus.UNAUTHORIZED, errorCode);
    }
}
