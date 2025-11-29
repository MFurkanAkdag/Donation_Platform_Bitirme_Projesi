package com.seffafbagis.api.entity.user;

import com.seffafbagis.api.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Kullanıcı hassas verileri entity sınıfı.
 * 
 * KVKK (6698 sayılı Kişisel Verilerin Korunması Kanunu) kapsamında
 * "özel nitelikli kişisel veri" olarak sınıflandırılan bilgiler
 * bu tabloda şifreli olarak saklanır.
 * 
 * GÜVENLİK ÖNLEMLERİ:
 * - Tüm veriler AES-256-GCM ile şifrelenir
 * - Şifreleme anahtarı environment variable'da saklanır
 * - Veritabanı yöneticileri bile plain text göremez
 * - Her alan ayrı IV (Initialization Vector) ile şifrelenir
 * 
 * ŞİFRELEME FORMATI:
 * encrypted_data = IV (12 byte) + ciphertext + auth_tag (16 byte)
 * 
 * @author Furkan
 * @version 1.0
 */
@Entity
@Table(
    name = "user_sensitive_data",
    indexes = {
        @Index(name = "idx_user_sensitive_user_id", columnList = "user_id")
    }
)
public class UserSensitiveData extends BaseEntity {

    /**
     * İlişkili kullanıcı.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    /**
     * TC Kimlik Numarası (şifreli).
     * 
     * - 11 haneli numara
     * - AES-256-GCM ile şifrelenir
     * - Vergi makbuzu oluşturmak için gerekebilir
     */
    @Column(name = "tc_kimlik_encrypted", columnDefinition = "BYTEA")
    private byte[] tcKimlikEncrypted;

    /**
     * Telefon numarası (şifreli).
     * 
     * - Uluslararası format: +905551234567
     * - SMS doğrulama ve bildirimler için
     */
    @Column(name = "phone_encrypted", columnDefinition = "BYTEA")
    private byte[] phoneEncrypted;

    /**
     * Adres (şifreli).
     * 
     * - Tam açık adres
     * - Yardım başvuruları için gerekebilir
     */
    @Column(name = "address_encrypted", columnDefinition = "BYTEA")
    private byte[] addressEncrypted;

    /**
     * Doğum tarihi (şifreli).
     * 
     * - ISO format: YYYY-MM-DD
     * - Yaş doğrulama için
     */
    @Column(name = "birth_date_encrypted", columnDefinition = "BYTEA")
    private byte[] birthDateEncrypted;

    /**
     * KVKK veri işleme onayı.
     * 
     * Kullanıcı açık rıza vermiş mi?
     */
    @Column(name = "data_processing_consent")
    private Boolean dataProcessingConsent = false;

    /**
     * KVKK onay tarihi.
     */
    @Column(name = "consent_date")
    private Instant consentDate;

    /**
     * KVKK onay versiyonu.
     * 
     * Hangi versiyon sözleşmeyi onayladı?
     * Sözleşme değiştiğinde yeniden onay istenebilir.
     */
    @Column(name = "consent_version", length = 20)
    private String consentVersion;

    /**
     * KVKK onay IP adresi.
     * 
     * Onayın verildiği IP adresi (delil için).
     */
    @Column(name = "consent_ip_address", length = 45)
    private String consentIpAddress;

    // ==================== CONSTRUCTOR ====================

    /**
     * Boş constructor.
     * JPA için gerekli.
     */
    public UserSensitiveData() {
        // JPA için boş constructor
    }

    /**
     * User ile constructor.
     * 
     * @param user İlişkili kullanıcı
     */
    public UserSensitiveData(User user) {
        this.user = user;
    }

    // ==================== GETTER METODLARI ====================

    public User getUser() {
        return user;
    }

    public byte[] getTcKimlikEncrypted() {
        return tcKimlikEncrypted;
    }

    public byte[] getPhoneEncrypted() {
        return phoneEncrypted;
    }

    public byte[] getAddressEncrypted() {
        return addressEncrypted;
    }

    public byte[] getBirthDateEncrypted() {
        return birthDateEncrypted;
    }

    public Boolean getDataProcessingConsent() {
        return dataProcessingConsent;
    }

    public Instant getConsentDate() {
        return consentDate;
    }

    public String getConsentVersion() {
        return consentVersion;
    }

    public String getConsentIpAddress() {
        return consentIpAddress;
    }

    // ==================== SETTER METODLARI ====================

    public void setUser(User user) {
        this.user = user;
    }

    public void setTcKimlikEncrypted(byte[] tcKimlikEncrypted) {
        this.tcKimlikEncrypted = tcKimlikEncrypted;
    }

    public void setPhoneEncrypted(byte[] phoneEncrypted) {
        this.phoneEncrypted = phoneEncrypted;
    }

    public void setAddressEncrypted(byte[] addressEncrypted) {
        this.addressEncrypted = addressEncrypted;
    }

    public void setBirthDateEncrypted(byte[] birthDateEncrypted) {
        this.birthDateEncrypted = birthDateEncrypted;
    }

    public void setDataProcessingConsent(Boolean dataProcessingConsent) {
        this.dataProcessingConsent = dataProcessingConsent;
    }

    public void setConsentDate(Instant consentDate) {
        this.consentDate = consentDate;
    }

    public void setConsentVersion(String consentVersion) {
        this.consentVersion = consentVersion;
    }

    public void setConsentIpAddress(String consentIpAddress) {
        this.consentIpAddress = consentIpAddress;
    }

    // ==================== YARDIMCI METODLAR ====================

    /**
     * KVKK onayı verilmiş mi kontrol eder.
     * 
     * @return Onay verilmişse true
     */
    public boolean hasConsent() {
        if (dataProcessingConsent == null) {
            return false;
        }
        return dataProcessingConsent;
    }

    /**
     * TC Kimlik kaydedilmiş mi kontrol eder.
     * 
     * @return Kaydedilmişse true
     */
    public boolean hasTcKimlik() {
        return tcKimlikEncrypted != null && tcKimlikEncrypted.length > 0;
    }

    /**
     * Telefon kaydedilmiş mi kontrol eder.
     * 
     * @return Kaydedilmişse true
     */
    public boolean hasPhone() {
        return phoneEncrypted != null && phoneEncrypted.length > 0;
    }

    /**
     * Adres kaydedilmiş mi kontrol eder.
     * 
     * @return Kaydedilmişse true
     */
    public boolean hasAddress() {
        return addressEncrypted != null && addressEncrypted.length > 0;
    }

    /**
     * KVKK onayını kaydeder.
     * 
     * @param version Sözleşme versiyonu
     * @param ipAddress Onay veren IP adresi
     */
    public void grantConsent(String version, String ipAddress) {
        this.dataProcessingConsent = true;
        this.consentDate = Instant.now();
        this.consentVersion = version;
        this.consentIpAddress = ipAddress;
    }

    /**
     * KVKK onayını iptal eder.
     * Tüm hassas verileri siler.
     */
    public void revokeConsent() {
        this.dataProcessingConsent = false;
        this.consentDate = null;
        this.consentVersion = null;
        this.consentIpAddress = null;
        
        // Tüm hassas verileri sil
        this.tcKimlikEncrypted = null;
        this.phoneEncrypted = null;
        this.addressEncrypted = null;
        this.birthDateEncrypted = null;
    }

    /**
     * Tüm hassas verileri temizler.
     * KVKK "unutulma hakkı" için.
     */
    public void clearAllSensitiveData() {
        this.tcKimlikEncrypted = null;
        this.phoneEncrypted = null;
        this.addressEncrypted = null;
        this.birthDateEncrypted = null;
    }

    @Override
    public String toString() {
        // GÜVENLİK: Hassas veri içeriği asla log'lanmaz
        return "UserSensitiveData{" +
                "id=" + getId() +
                ", hasConsent=" + hasConsent() +
                ", hasTcKimlik=" + hasTcKimlik() +
                ", hasPhone=" + hasPhone() +
                ", hasAddress=" + hasAddress() +
                '}';
    }
}
