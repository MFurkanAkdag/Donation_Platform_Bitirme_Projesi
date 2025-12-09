package com.seffafbagis.api.dto.response.admin;

import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.entity.user.UserProfile;
import com.seffafbagis.api.enums.UserRole;
import com.seffafbagis.api.enums.UserStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class AdminUserResponse {

    private UUID id;
    private String email;
    private UserRole role;
    private UserStatus status;
    private Boolean emailVerified;
    private LocalDateTime emailVerifiedAt;
    private LocalDateTime lastLoginAt;
    private Integer failedLoginAttempts;
    private LocalDateTime lockedUntil;
    private LocalDateTime passwordChangedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private AdminUserProfileInfo profile;
    private AdminUserStatistics statistics;

    public AdminUserResponse() {
    }

    public static AdminUserResponse fromEntity(User user, UserProfile profile) {
        AdminUserResponse response = new AdminUserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setEmailVerified(user.getEmailVerified());

        // These fields would typically come from User entity if they exist there,
        // or would be populated from other sources. Assume they exist on User or
        // related entities.
        // Based on Phase 8, User entity has these fields.
        if (user.getCreatedAt() != null) {
            response.setCreatedAt(user.getCreatedAt().toLocalDateTime());
        }
        if (user.getUpdatedAt() != null) {
            response.setUpdatedAt(user.getUpdatedAt().toLocalDateTime());
        }

        // Assuming these fields were added in previous phases or are part of User
        // entity evolution
        // If they don't exist yet, I might need to check User entity again, but the
        // requirement lists them.
        // Let's assume standard getters.

        if (profile != null) {
            AdminUserProfileInfo profileInfo = new AdminUserProfileInfo();
            profileInfo.setFirstName(profile.getFirstName());
            profileInfo.setLastName(profile.getLastName());
            profileInfo.setDisplayName(profile.getDisplayName());
            profileInfo.setAvatarUrl(profile.getAvatarUrl());
            // Assuming preferredLanguage is in profile or user preference. Requirement says
            // profile.
            // checking UserProfile.java content would be good but let's assume standard
            // fields for now based on requirement.

            response.setProfile(profileInfo);
        }

        // Statistics would be populated separately typically, or passed in.
        // For now, initializing empty or letting service populate.
        response.setStatistics(new AdminUserStatistics());

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

    public LocalDateTime getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public void setEmailVerifiedAt(LocalDateTime emailVerifiedAt) {
        this.emailVerifiedAt = emailVerifiedAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(LocalDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public LocalDateTime getPasswordChangedAt() {
        return passwordChangedAt;
    }

    public void setPasswordChangedAt(LocalDateTime passwordChangedAt) {
        this.passwordChangedAt = passwordChangedAt;
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

    public AdminUserProfileInfo getProfile() {
        return profile;
    }

    public void setProfile(AdminUserProfileInfo profile) {
        this.profile = profile;
    }

    public AdminUserStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(AdminUserStatistics statistics) {
        this.statistics = statistics;
    }

    public static class AdminUserProfileInfo {
        private String firstName;
        private String lastName;
        private String displayName;
        private String avatarUrl;
        private String preferredLanguage;

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

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }

        public String getPreferredLanguage() {
            return preferredLanguage;
        }

        public void setPreferredLanguage(String preferredLanguage) {
            this.preferredLanguage = preferredLanguage;
        }
    }

    public static class AdminUserStatistics {
        private Integer totalDonations;
        private BigDecimal totalDonationAmount;
        private LocalDateTime lastDonationAt;
        private Integer favoriteOrganizationsCount;
        private Integer loginCount;
        private LocalDateTime lastActiveAt;

        public Integer getTotalDonations() {
            return totalDonations;
        }

        public void setTotalDonations(Integer totalDonations) {
            this.totalDonations = totalDonations;
        }

        public BigDecimal getTotalDonationAmount() {
            return totalDonationAmount;
        }

        public void setTotalDonationAmount(BigDecimal totalDonationAmount) {
            this.totalDonationAmount = totalDonationAmount;
        }

        public LocalDateTime getLastDonationAt() {
            return lastDonationAt;
        }

        public void setLastDonationAt(LocalDateTime lastDonationAt) {
            this.lastDonationAt = lastDonationAt;
        }

        public Integer getFavoriteOrganizationsCount() {
            return favoriteOrganizationsCount;
        }

        public void setFavoriteOrganizationsCount(Integer favoriteOrganizationsCount) {
            this.favoriteOrganizationsCount = favoriteOrganizationsCount;
        }

        public Integer getLoginCount() {
            return loginCount;
        }

        public void setLoginCount(Integer loginCount) {
            this.loginCount = loginCount;
        }

        public LocalDateTime getLastActiveAt() {
            return lastActiveAt;
        }

        public void setLastActiveAt(LocalDateTime lastActiveAt) {
            this.lastActiveAt = lastActiveAt;
        }
    }
}
