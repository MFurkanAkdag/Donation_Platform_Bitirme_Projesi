package com.seffafbagis.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when encryption or decryption operations fail (HTTP 500).
 * 
 * This exception is raised when cryptographic operations encounter errors.
 * Stack traces and sensitive details are never exposed to clients.
 * 
 * @author Furkan
 * @version 1.0
 */
public class EncryptionException extends BusinessException {

    /**
     * Root cause exception, if any.
     */
    private final Throwable cause;

    /**
     * Constructor accepting message only.
     * 
     * @param message Error message (generic, doesn't expose implementation details)
     */
    public EncryptionException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR");
        this.cause = null;
    }

    /**
     * Constructor accepting message and cause exception.
     * 
     * @param message Error message (generic, doesn't expose implementation details)
     * @param cause Root cause exception for server-side logging
     */
    public EncryptionException(String message, Throwable cause) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR");
        this.cause = cause;
    }

    /**
     * Constructor accepting message, cause, and custom error code.
     * 
     * @param message Error message
     * @param cause Root cause exception
     * @param errorCode Machine-readable error code
     */
    public EncryptionException(String message, Throwable cause, String errorCode) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, errorCode);
        this.cause = cause;
    }

    /**
     * Gets the root cause exception.
     * This is for server-side logging only and should never be exposed to clients.
     * 
     * @return Root cause, or null if none
     */
    @Override
    public Throwable getCause() {
        return cause;
    }
}
