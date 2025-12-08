package com.seffafbagis.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Base business exception for application-level errors.
 * Minimal implementation to provide message, http status and an error code.
 */
public abstract class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public BusinessException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status == null ? HttpStatus.INTERNAL_SERVER_ERROR : status;
        this.errorCode = errorCode == null ? "BUSINESS_ERROR" : errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
