package com.seffafbagis.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when attempting to create a resource that already exists.
 */
public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException(String message) {
        super(message, HttpStatus.CONFLICT, "DUPLICATE_RESOURCE");
    }

    /**
     * Factory method for email already exists error.
     * 
     * @param email The email that already exists
     * @return DuplicateResourceException
     */
    public static DuplicateResourceException emailExists(String email) {
        return new DuplicateResourceException("Email already exists: " + email);
    }
}
