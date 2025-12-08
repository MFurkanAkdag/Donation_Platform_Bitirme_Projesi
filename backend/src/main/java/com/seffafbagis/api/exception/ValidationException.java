package com.seffafbagis.api.exception;

import org.springframework.http.HttpStatus;
import java.util.HashMap;
import java.util.Map;

/**
 * Thrown when validation of input fails.
 * Supports both simple messages and field-level validation errors.
 * 
 * @author Furkan
 * @version 1.0
 */
public class ValidationException extends BusinessException {

    /**
     * Field-level validation errors.
     * Maps field names to error messages.
     */
    private Map<String, String> fieldErrors;

    /**
     * Constructor with message only.
     * 
     * @param message Validation error message
     */
    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
        this.fieldErrors = new HashMap<>();
    }

    /**
     * Constructor with message and field errors.
     * 
     * @param message General validation error message
     * @param fieldErrors Map of field names to error messages
     */
    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
        this.fieldErrors = fieldErrors != null ? fieldErrors : new HashMap<>();
    }

    /**
     * Gets field-level validation errors.
     * 
     * @return Map of field names to error messages
     */
    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    // ==================== FACTORY METHODS ====================

    /**
     * Factory method for password validation error.
     * 
     * @return ValidationException for weak/invalid password
     */
    public static ValidationException passwordsDoNotMatch() {
        return new ValidationException("Passwords do not match");
    }

    /**
     * Factory method for terms not accepted error.
     * 
     * @return ValidationException when user doesn't accept terms
     */
    public static ValidationException termsNotAccepted() {
        return new ValidationException("Terms and conditions must be accepted");
    }

    /**
     * Factory method for KVKK consent not accepted error.
     * 
     * @return ValidationException when KVKK consent is not provided
     */
    public static ValidationException kvkkNotAccepted() {
        return new ValidationException("KVKK consent must be accepted");
    }
}
