package com.seffafbagis.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown for invalid request data or business rule violations (HTTP 400).
 * 
 * This exception is raised when a client sends invalid or malformed data
 * that violates application business rules.
 * 
 * @author Furkan
 * @version 1.0
 */
public class BadRequestException extends BusinessException {

    /**
     * Error code for the exception.
     * Machine-readable code used by clients for proper error handling.
     */
    private final String errorCode;

    /**
     * Constructor accepting message only.
     * Uses default error code "BAD_REQUEST".
     * 
     * @param message User-friendly error message
     */
    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "BAD_REQUEST");
        this.errorCode = "BAD_REQUEST";
    }

    /**
     * Constructor accepting message and error code.
     * 
     * @param message User-friendly error message
     * @param errorCode Machine-readable error code
     */
    public BadRequestException(String message, String errorCode) {
        super(message, HttpStatus.BAD_REQUEST, errorCode);
        this.errorCode = errorCode;
    }

    /**
     * Gets the error code for this exception.
     * 
     * @return Error code
     */
    @Override
    public String getErrorCode() {
        return errorCode;
    }
}
