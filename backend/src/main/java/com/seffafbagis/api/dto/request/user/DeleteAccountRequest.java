package com.seffafbagis.api.dto.request.user;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for account deletion confirmation.
 */
public class DeleteAccountRequest {

    @NotBlank(message = "Password is required for account deletion")
    private String password;

    @AssertTrue(message = "You must confirm the deletion")
    private Boolean confirmDeletion;

    @Size(max = 500, message = "Reason must be at most 500 characters")
    private String reason;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getConfirmDeletion() {
        return confirmDeletion;
    }

    public void setConfirmDeletion(Boolean confirmDeletion) {
        this.confirmDeletion = confirmDeletion;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
