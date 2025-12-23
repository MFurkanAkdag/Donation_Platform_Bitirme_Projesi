package com.seffafbagis.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when there's a conflict with existing data (HTTP 409).
 * 
 * This exception is raised when attempting to create a resource
 * that already exists with the same unique identifier.
 * 
 * @author Furkan
 * @version 1.0
 */
public class ConflictException extends BusinessException {

    /**
     * Name of the resource type (e.g., "User", "Campaign").
     */
    private final String resourceName;

    /**
     * Field name that caused the conflict (e.g., "email", "username").
     */
    private final String fieldName;

    /**
     * The conflicting value.
     */
    private final Object fieldValue;

    /**
     * Constructor accepting resourceName, fieldName, and fieldValue.
     * Generates message: "{resourceName} already exists with {fieldName}:
     * {fieldValue}"
     * 
     * @param resourceName Resource type name
     * @param fieldName    Field name causing conflict
     * @param fieldValue   Value that caused conflict
     */
    public ConflictException(String resourceName, String fieldName, Object fieldValue) {
        super(
                resourceName + " already exists with " + fieldName + ": " + fieldValue,
                HttpStatus.CONFLICT,
                "CONFLICT");
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    /**
     * Simple constructor with just a message.
     * 
     * @param message Error message
     */
    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT, "CONFLICT");
        this.resourceName = null;
        this.fieldName = null;
        this.fieldValue = null;
    }

    /**
     * Constructor with custom error code.
     * 
     * @param resourceName Resource type name
     * @param fieldName    Field name causing conflict
     * @param fieldValue   Value that caused conflict
     * @param errorCode    Machine-readable error code
     */
    public ConflictException(String resourceName, String fieldName, Object fieldValue, String errorCode) {
        super(
                resourceName + " already exists with " + fieldName + ": " + fieldValue,
                HttpStatus.CONFLICT,
                errorCode);
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    /**
     * Gets the resource name.
     * 
     * @return Resource name
     */
    public String getResourceName() {
        return resourceName;
    }

    /**
     * Gets the field name that caused the conflict.
     * 
     * @return Field name
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Gets the field value that caused the conflict.
     * 
     * @return Field value
     */
    public Object getFieldValue() {
        return fieldValue;
    }
}
