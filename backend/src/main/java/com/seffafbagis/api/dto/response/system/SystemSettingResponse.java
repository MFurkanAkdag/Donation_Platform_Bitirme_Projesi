package com.seffafbagis.api.dto.response.system;

import com.seffafbagis.api.entity.system.SystemSetting;
import com.seffafbagis.api.entity.user.User;

import java.time.LocalDateTime;
import java.util.UUID;

public class SystemSettingResponse {
    private UUID id;
    private String settingKey;
    private String settingValue;
    private String valueType;
    private String description;
    private Boolean isPublic;
    private UserSummary updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SystemSettingResponse fromEntity(SystemSetting setting) {
        SystemSettingResponse response = new SystemSettingResponse();
        response.setId(setting.getId());
        response.setSettingKey(setting.getSettingKey());
        response.setSettingValue(setting.getSettingValue());
        response.setValueType(setting.getValueType());
        response.setDescription(setting.getDescription());
        response.setIsPublic(setting.getIsPublic());

        if (setting.getUpdatedBy() != null) {
            response.setUpdatedBy(UserSummary.fromEntity(setting.getUpdatedBy()));
        }

        response.setCreatedAt(setting.getCreatedAt());
        response.setUpdatedAt(setting.getUpdatedAt());
        return response;
    }

    public static class UserSummary {
        private UUID id;
        private String email;
        private String fullName;

        public static UserSummary fromEntity(User user) {
            UserSummary summary = new UserSummary();
            summary.setId(user.getId());
            summary.setEmail(user.getEmail());
            summary.setFullName(user.getFullName());
            return summary;
        }

        // Getters and Setters
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

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public UserSummary getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(UserSummary updatedBy) {
        this.updatedBy = updatedBy;
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
