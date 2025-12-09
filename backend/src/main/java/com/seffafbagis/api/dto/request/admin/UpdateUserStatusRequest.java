package com.seffafbagis.api.dto.request.admin;

import com.seffafbagis.api.enums.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateUserStatusRequest {

    @NotNull(message = "Status is required")
    private UserStatus status;

    @NotBlank(message = "Reason is required")
    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;

    private Boolean notifyUser = true;

    private Integer duration; // Days for suspension

    public UpdateUserStatusRequest() {
    }

    public UpdateUserStatusRequest(UserStatus status, String reason, Boolean notifyUser, Integer duration) {
        this.status = status;
        this.reason = reason;
        this.notifyUser = notifyUser;
        this.duration = duration;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
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

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }
}
