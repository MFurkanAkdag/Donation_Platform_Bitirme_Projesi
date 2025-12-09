package com.seffafbagis.api.dto.request.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for email verification request.
 * 
 * Used when user clicks the email verification link to verify their email
 * address.
 */
public class VerifyEmailRequest {

    /**
     * Email verification token from email link
     */
    @NotBlank(message = "Verification token is required")
    private String token;

    public VerifyEmailRequest() {
    }

    public VerifyEmailRequest(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
