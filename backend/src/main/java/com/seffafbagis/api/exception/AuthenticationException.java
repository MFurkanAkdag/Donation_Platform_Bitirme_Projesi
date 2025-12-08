package com.seffafbagis.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when authentication fails.
 */
public class AuthenticationException extends BusinessException {

    public AuthenticationException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED");
    }

    /**
     * Factory method for invalid token error.
     * 
     * @return AuthenticationException for invalid/expired token
     */
    public static AuthenticationException invalidToken() {
        return new AuthenticationException("Invalid or expired authentication token");
    }

    /**
     * Factory method for invalid credentials error.
     * 
     * @return AuthenticationException for invalid credentials
     */
    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException("Invalid email or password");
    }

    /**
     * Factory method for account suspended error.
     * 
     * @return AuthenticationException when account is suspended
     */
    public static AuthenticationException accountSuspended() {
        return new AuthenticationException("Your account has been suspended");
    }

    /**
     * Factory method for account inactive error.
     * 
     * @return AuthenticationException when account is inactive
     */
    public static AuthenticationException accountInactive() {
        return new AuthenticationException("Your account is not active");
    }
}
