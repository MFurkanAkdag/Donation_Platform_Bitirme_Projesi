package com.seffafbagis.api.dto.response.user;

import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.UserRole;
import com.seffafbagis.api.enums.UserStatus;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Response DTO for basic user information.
 */
public class UserResponse {

    private UUID id;
    private String email;
    private UserRole role;
    private UserStatus status;
    private Boolean emailVerified;
    private LocalDateTime createdAt;

    public static UserResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setEmailVerified(user.getEmailVerified());

        if (user.getCreatedAt() != null) {
            response.setCreatedAt(LocalDateTime.ofInstant(user.getCreatedAt().toInstant(), ZoneId.systemDefault()));
        }

        return response;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
