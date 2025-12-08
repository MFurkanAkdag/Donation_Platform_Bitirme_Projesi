package com.seffafbagis.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when file storage operations fail (HTTP 500).
 * 
 * This exception is raised when file upload, download, or storage
 * operations encounter errors.
 * 
 * @author Furkan
 * @version 1.0
 */
public class FileStorageException extends BusinessException {

    /**
     * Root cause exception, if any.
     */
    private final Throwable cause;

    /**
     * Constructor accepting message only.
     * 
     * @param message Error message describing the storage failure
     */
    public FileStorageException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, "FILE_STORAGE_ERROR");
        this.cause = null;
    }

    /**
     * Constructor accepting message and cause exception.
     * 
     * @param message Error message describing the storage failure
     * @param cause Root cause exception
     */
    public FileStorageException(String message, Throwable cause) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, "FILE_STORAGE_ERROR");
        this.cause = cause;
    }

    /**
     * Constructor accepting message, cause, and custom error code.
     * 
     * @param message Error message describing the storage failure
     * @param cause Root cause exception
     * @param errorCode Machine-readable error code
     */
    public FileStorageException(String message, Throwable cause, String errorCode) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, errorCode);
        this.cause = cause;
    }

    /**
     * Gets the root cause exception.
     * 
     * @return Root cause, or null if none
     */
    @Override
    public Throwable getCause() {
        return cause;
    }
}
