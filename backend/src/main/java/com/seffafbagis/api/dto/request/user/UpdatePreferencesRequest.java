package com.seffafbagis.api.dto.request.user;

import com.seffafbagis.api.enums.DonationVisibility;

/**
 * Tercih güncelleme request DTO.
 * 
 * Kullanıcının bildirim ve gizlilik tercihlerini güncellemesi için kullanılır.
 * 
 * Request örneği:
 * {
 *   "emailNotifications": true,
 *   "smsNotifications": false,
 *   "pushNotifications": true,
 *   "donationVisibility": "ANONYMOUS",
 *   "showInDonorList": false,
 *   "weeklySummaryEmail": true,
 *   "notifyOnCampaignComplete": true,
 *   "notifyOnCampaignUpdate": true
 * }
 * 
 * @author Furkan
 * @version 1.0
 */
public class UpdatePreferencesRequest {

    /**
     * E-posta bildirimleri.
     */
    private Boolean emailNotifications;

    /**
     * SMS bildirimleri.
     */
    private Boolean smsNotifications;

    /**
     * Push bildirimleri.
     */
    private Boolean pushNotifications;

    /**
     * Bağış görünürlük tercihi.
     */
    private DonationVisibility donationVisibility;

    /**
     * Bağışçı listesinde görünsün mü?
     */
    private Boolean showInDonorList;

    /**
     * Haftalık özet e-postası.
     */
    private Boolean weeklySummaryEmail;

    /**
     * Kampanya tamamlandığında bildirim.
     */
    private Boolean notifyOnCampaignComplete;

    /**
     * Kampanya güncellemelerinde bildirim.
     */
    private Boolean notifyOnCampaignUpdate;

    // ==================== CONSTRUCTOR ====================

    /**
     * Boş constructor.
     */
    public UpdatePreferencesRequest() {
        // Boş constructor
    }

    // ==================== GETTER METODLARI ====================

    public Boolean getEmailNotifications() {
        return emailNotifications;
    }

    public Boolean getSmsNotifications() {
        return smsNotifications;
    }

    public Boolean getPushNotifications() {
        return pushNotifications;
    }

    public DonationVisibility getDonationVisibility() {
        return donationVisibility;
    }

    public Boolean getShowInDonorList() {
        return showInDonorList;
    }

    public Boolean getWeeklySummaryEmail() {
        return weeklySummaryEmail;
    }

    public Boolean getNotifyOnCampaignComplete() {
        return notifyOnCampaignComplete;
    }

    public Boolean getNotifyOnCampaignUpdate() {
        return notifyOnCampaignUpdate;
    }

    // ==================== SETTER METODLARI ====================

    public void setEmailNotifications(Boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }

    public void setSmsNotifications(Boolean smsNotifications) {
        this.smsNotifications = smsNotifications;
    }

    public void setPushNotifications(Boolean pushNotifications) {
        this.pushNotifications = pushNotifications;
    }

    public void setDonationVisibility(DonationVisibility donationVisibility) {
        this.donationVisibility = donationVisibility;
    }

    public void setShowInDonorList(Boolean showInDonorList) {
        this.showInDonorList = showInDonorList;
    }

    public void setWeeklySummaryEmail(Boolean weeklySummaryEmail) {
        this.weeklySummaryEmail = weeklySummaryEmail;
    }

    public void setNotifyOnCampaignComplete(Boolean notifyOnCampaignComplete) {
        this.notifyOnCampaignComplete = notifyOnCampaignComplete;
    }

    public void setNotifyOnCampaignUpdate(Boolean notifyOnCampaignUpdate) {
        this.notifyOnCampaignUpdate = notifyOnCampaignUpdate;
    }

    @Override
    public String toString() {
        return "UpdatePreferencesRequest{" +
                "emailNotifications=" + emailNotifications +
                ", smsNotifications=" + smsNotifications +
                ", donationVisibility=" + donationVisibility +
                ", showInDonorList=" + showInDonorList +
                '}';
    }
}
