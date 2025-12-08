package com.seffafbagis.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when user is authenticated but not authorized for the action (HTTP 403).
 * 
 * This exception is raised when a user attempts to perform an action
 * they don't have permission for.
 * 
 * @author Furkan
 * @version 1.0
 */
public class ForbiddenException extends BusinessException {

    /**
     * Constructor with default message.
     * Uses default message "Access denied".
     */
    public ForbiddenException() {
        super("Access denied", HttpStatus.FORBIDDEN, "FORBIDDEN");
    }

    /**
     * Constructor accepting custom message.
     * 
     * @param message User-friendly error message
     */
    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN, "FORBIDDEN");
    }

    /**
     * Constructor accepting message and custom error code.
     * 
     * @param message User-friendly error message
     * @param errorCode Machine-readable error code
     */
    public ForbiddenException(String message, String errorCode) {
        super(message, HttpStatus.FORBIDDEN, errorCode);
    }
}
