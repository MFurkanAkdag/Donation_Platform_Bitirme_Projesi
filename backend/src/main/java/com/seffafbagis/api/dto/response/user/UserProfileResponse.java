package com.seffafbagis.api.dto.response.user;

import com.seffafbagis.api.entity.user.UserProfile;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Response DTO for user profile information.
 */
public class UserProfileResponse {

    private UUID id;
    private UUID userId;
    private String firstName;
    private String lastName;
    private String displayName;
    private String fullName;
    private String avatarUrl;
    private String bio;
    private String preferredLanguage;
    private String timezone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserProfileResponse fromEntity(UserProfile profile) {
        if (profile == null) {
            return null;
        }
        UserProfileResponse response = new UserProfileResponse();
        response.setId(profile.getId());

        if (profile.getUser() != null) {
            response.setUserId(profile.getUser().getId());
        }

        response.setFirstName(profile.getFirstName());
        response.setLastName(profile.getLastName());
        response.setDisplayName(profile.getDisplayName());
        response.setFullName(profile.getFullName());
        response.setAvatarUrl(profile.getAvatarUrl());
        response.setBio(profile.getBio());
        response.setPreferredLanguage(profile.getPreferredLanguage());
        response.setTimezone(profile.getTimezone());

        if (profile.getCreatedAt() != null) {
            response.setCreatedAt(LocalDateTime.ofInstant(profile.getCreatedAt().toInstant(), ZoneId.systemDefault()));
        }

        if (profile.getUpdatedAt() != null) {
            response.setUpdatedAt(LocalDateTime.ofInstant(profile.getUpdatedAt().toInstant(), ZoneId.systemDefault()));
        }

        return response;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
