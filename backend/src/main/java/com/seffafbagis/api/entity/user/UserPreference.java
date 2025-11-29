package com.seffafbagis.api.entity.user;

import com.seffafbagis.api.entity.base.BaseEntity;
import com.seffafbagis.api.enums.DonationVisibility;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;


/**
 * Kullanıcı tercihleri entity sınıfı.
 * 
 * Bu tablo şunları saklar:
 * - Bildirim tercihleri (e-posta, SMS)
 * - Bağış görünürlük tercihi
 * - Bağışçı listesinde görünme tercihi
 * 
 * @author Furkan
 * @version 1.0
 */
@Entity
@Table(
    name = "user_preferences",
    indexes = {
        @Index(name = "idx_user_preferences_user_id", columnList = "user_id")
    }
)
public class UserPreference extends BaseEntity {

    /**
     * İlişkili kullanıcı.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    /**
     * E-posta bildirimleri aktif mi?
     * 
     * - Bağış onayı
     * - Kampanya güncellemeleri
     * - Hatırlatmalar
     */
    @Column(name = "email_notifications")
    private Boolean emailNotifications = true;

    /**
     * SMS bildirimleri aktif mi?
     * 
     * Varsayılan false çünkü SMS maliyetli.
     */
    @Column(name = "sms_notifications")
    private Boolean smsNotifications = false;

    /**
     * Push bildirimleri aktif mi?
     * 
     * Mobil uygulama için.
     */
    @Column(name = "push_notifications")
    private Boolean pushNotifications = true;

    /**
     * Bağış görünürlük tercihi.
     * 
     * - PUBLIC: Herkes görebilir
     * - ANONYMOUS: İsim gizli, miktar görünür
     * - PRIVATE: Tamamen gizli
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "donation_visibility", length = 20)
    private DonationVisibility donationVisibility = DonationVisibility.ANONYMOUS;

    /**
     * Bağışçı listesinde görünsün mü?
     * 
     * Kampanya sayfalarındaki bağışçı listesi.
     */
    @Column(name = "show_in_donor_list")
    private Boolean showInDonorList = false;

    /**
     * Haftalık özet e-postası alsın mı?
     */
    @Column(name = "weekly_summary_email")
    private Boolean weeklySummaryEmail = false;

    /**
     * Kampanya tamamlandığında bildirim alsın mı?
     */
    @Column(name = "notify_on_campaign_complete")
    private Boolean notifyOnCampaignComplete = true;

    /**
     * Bağış yapılan kampanyadan güncelleme geldiğinde bildirim alsın mı?
     */
    @Column(name = "notify_on_campaign_update")
    private Boolean notifyOnCampaignUpdate = true;

    // ==================== CONSTRUCTOR ====================

    /**
     * Boş constructor.
     * JPA için gerekli.
     */
    public UserPreference() {
        // JPA için boş constructor
    }

    /**
     * User ile constructor.
     * Varsayılan değerlerle oluşturur.
     * 
     * @param user İlişkili kullanıcı
     */
    public UserPreference(User user) {
        this.user = user;
    }

    // ==================== GETTER METODLARI ====================

    public User getUser() {
        return user;
    }

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

    public void setUser(User user) {
        this.user = user;
    }

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

    // ==================== YARDIMCI METODLAR ====================

    /**
     * E-posta bildirimleri aktif mi kontrol eder.
     * Null güvenli kontrol.
     * 
     * @return Aktifse true
     */
    public boolean isEmailNotificationsEnabled() {
        if (emailNotifications == null) {
            return true; // Varsayılan true
        }
        return emailNotifications;
    }

    /**
     * SMS bildirimleri aktif mi kontrol eder.
     * Null güvenli kontrol.
     * 
     * @return Aktifse true
     */
    public boolean isSmsNotificationsEnabled() {
        if (smsNotifications == null) {
            return false; // Varsayılan false
        }
        return smsNotifications;
    }

    /**
     * Bağış anonim mi kontrol eder.
     * 
     * @return Anonimse true
     */
    public boolean isDonationAnonymous() {
        if (donationVisibility == null) {
            return true; // Varsayılan anonim
        }
        return donationVisibility == DonationVisibility.ANONYMOUS 
            || donationVisibility == DonationVisibility.PRIVATE;
    }

    /**
     * Tüm bildirimleri kapatır.
     */
    public void disableAllNotifications() {
        this.emailNotifications = false;
        this.smsNotifications = false;
        this.pushNotifications = false;
        this.weeklySummaryEmail = false;
        this.notifyOnCampaignComplete = false;
        this.notifyOnCampaignUpdate = false;
    }

    /**
     * Varsayılan tercihlere sıfırlar.
     */
    public void resetToDefaults() {
        this.emailNotifications = true;
        this.smsNotifications = false;
        this.pushNotifications = true;
        this.donationVisibility = DonationVisibility.ANONYMOUS;
        this.showInDonorList = false;
        this.weeklySummaryEmail = false;
        this.notifyOnCampaignComplete = true;
        this.notifyOnCampaignUpdate = true;
    }

    @Override
    public String toString() {
        return "UserPreference{" +
                "id=" + getId() +
                ", emailNotifications=" + emailNotifications +
                ", smsNotifications=" + smsNotifications +
                ", donationVisibility=" + donationVisibility +
                '}';
    }
}
