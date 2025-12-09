package com.seffafbagis.api.dto.request.system;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateSettingRequest {

    @NotBlank(message = "Setting key is required")
    @Size(max = 100, message = "Setting key cannot exceed 100 characters")
    @Pattern(regexp = "^[a-z_]+$", message = "Setting key must be in snake_case (lowercase letters and underscores only)")
    private String settingKey;

    @NotBlank(message = "Setting value is required")
    @Size(max = 5000, message = "Setting value cannot exceed 5000 characters")
    private String settingValue;

    @Pattern(regexp = "^(string|number|boolean|json)$", message = "Value type must be one of: string, number, boolean, json")
    private String valueType = "string";

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    private Boolean isPublic = false;

    // Getters and Setters
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
}
