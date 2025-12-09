package com.seffafbagis.api.dto.request.admin;

import com.seffafbagis.api.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateUserRoleRequest {

    @NotNull(message = "Role is required")
    private UserRole role;

    @NotBlank(message = "Reason is required")
    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;

    private Boolean notifyUser = true;

    public UpdateUserRoleRequest() {
    }

    public UpdateUserRoleRequest(UserRole role, String reason, Boolean notifyUser) {
        this.role = role;
        this.reason = reason;
        this.notifyUser = notifyUser;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Boolean getNotifyUser() {
        return notifyUser;
    }

    public void setNotifyUser(Boolean notifyUser) {
        this.notifyUser = notifyUser;
    }
}
