package com.seffafbagis.api.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for password reset request.
 * 
 * Used when user submits new password using a reset token.
 * Validates that passwords match and meet strength requirements.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

    /**
     * Password reset token from email
     */
    @NotBlank(message = "Token is required")
    private String token;

    /**
     * New password (must be 8-128 characters)
     */
    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    private String newPassword;

    /**
     * Password confirmation (must match newPassword)
     */
    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;
}
