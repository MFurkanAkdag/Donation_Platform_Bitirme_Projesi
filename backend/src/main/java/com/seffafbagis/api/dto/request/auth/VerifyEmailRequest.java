package com.seffafbagis.api.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for email verification request.
 * 
 * Used when user clicks the email verification link to verify their email address.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyEmailRequest {

    /**
     * Email verification token from email link
     */
    @NotBlank(message = "Verification token is required")
    private String token;
}
