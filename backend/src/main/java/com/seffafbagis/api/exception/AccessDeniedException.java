package com.seffafbagis.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a user is not authorized to perform an action.
 */
public class AccessDeniedException extends BusinessException {

    public AccessDeniedException(String message) {
        super(message, HttpStatus.FORBIDDEN, "ACCESS_DENIED");
    }

    /**
     * Factory method for admin-only operations.
     * 
     * @return AccessDeniedException when non-admin tries admin action
     */
    public static AccessDeniedException adminRequired() {
        return new AccessDeniedException("This operation requires administrator privileges");
    }

    /**
     * Factory method for resource ownership check.
     * 
     * @return AccessDeniedException when user is not resource owner
     */
    public static AccessDeniedException notOwner() {
        return new AccessDeniedException("You do not have permission to access this resource");
    }
}
