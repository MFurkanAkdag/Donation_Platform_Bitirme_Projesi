package com.seffafbagis.api.dto.request.user;

import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for updating user preferences.
 * All fields are optional to allow partial updates.
 */
public class UpdatePreferencesRequest {

    private Boolean emailNotifications;

    private Boolean smsNotifications;

    @Pattern(regexp = "^(PUBLIC|ANONYMOUS|PRIVATE)$", message = "Donation visibility must be PUBLIC, ANONYMOUS, or PRIVATE")
    private String donationVisibility;

    private Boolean showInDonorList;

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
}
