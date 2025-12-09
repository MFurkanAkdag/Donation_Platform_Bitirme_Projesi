package com.seffafbagis.api.dto.response.user;

import com.seffafbagis.api.entity.user.UserPreference;
import com.seffafbagis.api.enums.DonationVisibility;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Response DTO for user preferences.
 */
public class UserPreferenceResponse {

    private UUID id;
    private Boolean emailNotifications;
    private Boolean smsNotifications;
    private String donationVisibility;
    private Boolean showInDonorList;
    private LocalDateTime updatedAt;

    public static UserPreferenceResponse fromEntity(UserPreference preference) {
        if (preference == null) {
            return null;
        }
        UserPreferenceResponse response = new UserPreferenceResponse();
        response.setId(preference.getId());
        response.setEmailNotifications(preference.getEmailNotifications());
        response.setSmsNotifications(preference.getSmsNotifications());

        if (preference.getDonationVisibility() != null) {
            response.setDonationVisibility(preference.getDonationVisibility().name());
        }

        response.setShowInDonorList(preference.getShowInDonorList());

        if (preference.getUpdatedAt() != null) {
            response.setUpdatedAt(
                    LocalDateTime.ofInstant(preference.getUpdatedAt().toInstant(), ZoneId.systemDefault()));
        }

        return response;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Boolean getEmailNotifications() {
        return emailNotifications;
    }

    public void setEmailNotifications(Boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }

    public Boolean getSmsNotifications() {
        return smsNotifications;
    }

    public void setSmsNotifications(Boolean smsNotifications) {
        this.smsNotifications = smsNotifications;
    }

    public String getDonationVisibility() {
        return donationVisibility;
    }

    public void setDonationVisibility(String donationVisibility) {
        this.donationVisibility = donationVisibility;
    }

    public Boolean getShowInDonorList() {
        return showInDonorList;
    }

    public void setShowInDonorList(Boolean showInDonorList) {
        this.showInDonorList = showInDonorList;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
