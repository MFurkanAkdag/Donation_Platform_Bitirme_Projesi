package com.seffafbagis.api.dto.response.system;

import java.time.LocalDateTime;
import java.util.Map;

public class PublicSettingsResponse {
    private Map<String, Object> settings;
    private LocalDateTime fetchedAt;

    public PublicSettingsResponse() {
        this.fetchedAt = LocalDateTime.now();
    }

    public PublicSettingsResponse(Map<String, Object> settings) {
        this.settings = settings;
        this.fetchedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Map<String, Object> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, Object> settings) {
        this.settings = settings;
    }

    public LocalDateTime getFetchedAt() {
        return fetchedAt;
    }

    public void setFetchedAt(LocalDateTime fetchedAt) {
        this.fetchedAt = fetchedAt;
    }
}
