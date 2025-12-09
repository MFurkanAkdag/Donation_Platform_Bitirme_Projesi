package com.seffafbagis.api.dto.request.system;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateSettingRequest {

    @NotBlank(message = "Setting value is required")
    @Size(max = 5000, message = "Setting value cannot exceed 5000 characters")
    private String settingValue;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    private Boolean isPublic;

    // Getters and Setters
    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
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
}
