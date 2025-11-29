package com.seffafbagis.api.entity.user;

import com.seffafbagis.api.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Kullanıcı profil bilgileri entity sınıfı.
 * 
 * Bu tablo şunları saklar:
 * - Ad ve soyad
 * - Görünen ad
 * - Avatar URL
 * - Biyografi
 * - Dil ve saat dilimi tercihleri
 * 
 * User tablosundan ayrı tutulma nedeni:
 * - Normalizasyon
 * - Lazy loading ile performans
 * - Profil bilgileri her zaman gerekli değil
 * 
 * @author Furkan
 * @version 1.0
 */
@Entity
@Table(
    name = "user_profiles",
    indexes = {
        @Index(name = "idx_user_profiles_user_id", columnList = "user_id")
    }
)
public class UserProfile extends BaseEntity {

    /**
     * İlişkili kullanıcı.
     * 
     * - OneToOne: Bire bir ilişki
     * - FetchType.LAZY: İhtiyaç olduğunda yüklenir
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    /**
     * Ad.
     */
    @Column(name = "first_name", length = 100)
    private String firstName;

    /**
     * Soyad.
     */
    @Column(name = "last_name", length = 100)
    private String lastName;

    /**
     * Görünen ad.
     * Kullanıcının platformda görünecek takma adı.
     */
    @Column(name = "display_name", length = 100)
    private String displayName;

    /**
     * Profil fotoğrafı URL'i.
     */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    /**
     * Biyografi.
     * Kullanıcının kendini tanıttığı metin.
     */
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    /**
     * Tercih edilen dil.
     * ISO 639-1 kodu (örn: "tr", "en").
     */
    @Column(name = "preferred_language", length = 5)
    private String preferredLanguage = "tr";

    /**
     * Saat dilimi.
     * IANA timezone formatı (örn: "Europe/Istanbul").
     */
    @Column(name = "timezone", length = 50)
    private String timezone = "Europe/Istanbul";

    // ==================== CONSTRUCTOR ====================

    /**
     * Boş constructor.
     * JPA için gerekli.
     */
    public UserProfile() {
        // JPA için boş constructor
    }

    /**
     * User ile constructor.
     * 
     * @param user İlişkili kullanıcı
     */
    public UserProfile(User user) {
        this.user = user;
    }

    /**
     * Tam bilgilerle constructor.
     * 
     * @param user İlişkili kullanıcı
     * @param firstName Ad
     * @param lastName Soyad
     */
    public UserProfile(User user, String firstName, String lastName) {
        this.user = user;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // ==================== GETTER METODLARI ====================

    public User getUser() {
        return user;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getBio() {
        return bio;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public String getTimezone() {
        return timezone;
    }

    // ==================== SETTER METODLARI ====================

    public void setUser(User user) {
        this.user = user;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    // ==================== YARDIMCI METODLAR ====================

    /**
     * Tam adı döndürür.
     * Ad ve soyadı birleştirir.
     * 
     * @return Tam ad
     */
    public String getFullName() {
        StringBuilder fullName = new StringBuilder();

        if (firstName != null && !firstName.trim().isEmpty()) {
            fullName.append(firstName.trim());
        }

        if (lastName != null && !lastName.trim().isEmpty()) {
            if (fullName.length() > 0) {
                fullName.append(" ");
            }
            fullName.append(lastName.trim());
        }

        return fullName.toString();
    }

    /**
     * Görünen adı veya tam adı döndürür.
     * Öncelik sırası: displayName > fullName > "Kullanıcı"
     * 
     * @return Görüntülenecek ad
     */
    public String getDisplayNameOrFullName() {
        // Önce displayName kontrol et
        if (displayName != null && !displayName.trim().isEmpty()) {
            return displayName.trim();
        }

        // Sonra fullName kontrol et
        String fullName = getFullName();
        if (!fullName.isEmpty()) {
            return fullName;
        }

        // Hiçbiri yoksa varsayılan döndür
        return "Kullanıcı";
    }

    /**
     * Profil tamamlanmış mı kontrol eder.
     * Ad ve soyad girilmiş mi kontrol edilir.
     * 
     * @return Tamamlanmışsa true
     */
    public boolean isProfileComplete() {
        boolean hasFirstName = firstName != null && !firstName.trim().isEmpty();
        boolean hasLastName = lastName != null && !lastName.trim().isEmpty();
        return hasFirstName && hasLastName;
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "id=" + getId() +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", displayName='" + displayName + '\'' +
                '}';
    }
}
