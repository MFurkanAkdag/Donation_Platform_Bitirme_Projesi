package com.seffafbagis.api.dto.response.admin;

import com.seffafbagis.api.enums.UserStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public class UserStatusChangeResponse {

    private UUID userId;
    private UserStatus previousStatus;
    private UserStatus newStatus;
    private String reason;
    private UUID changedBy;
    private LocalDateTime changedAt;
    private Boolean notificationSent;

    public UserStatusChangeResponse() {
        this.changedAt = LocalDateTime.now();
    }

    public UserStatusChangeResponse(UUID userId, UserStatus previousStatus, UserStatus newStatus, String reason,
            UUID changedBy, Boolean notificationSent) {
        this.userId = userId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.reason = reason;
        this.changedBy = changedBy;
        this.notificationSent = notificationSent;
        this.changedAt = LocalDateTime.now();
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UserStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(UserStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public UserStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(UserStatus newStatus) {
        this.newStatus = newStatus;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public UUID getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(UUID changedBy) {
        this.changedBy = changedBy;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public Boolean getNotificationSent() {
        return notificationSent;
    }

    public void setNotificationSent(Boolean notificationSent) {
        this.notificationSent = notificationSent;
    }
}
