package com.seffafbagis.api.dto.request.user;

import jakarta.validation.constraints.Size;

/**
 * Profil güncelleme request DTO.
 * 
 * Kullanıcının profil bilgilerini güncellemesi için kullanılır.
 * Sadece gönderilen alanlar güncellenir (partial update).
 * 
 * Request örneği:
 * {
 *   "firstName": "Ahmet",
 *   "lastName": "Yılmaz",
 *   "displayName": "ahmetyilmaz",
 *   "bio": "Yazılım geliştirici",
 *   "preferredLanguage": "tr",
 *   "timezone": "Europe/Istanbul"
 * }
 * 
 * @author Furkan
 * @version 1.0
 */
public class UpdateProfileRequest {

    /**
     * Ad.
     */
    @Size(max = 100, message = "Ad en fazla 100 karakter olabilir")
    private String firstName;

    /**
     * Soyad.
     */
    @Size(max = 100, message = "Soyad en fazla 100 karakter olabilir")
    private String lastName;

    /**
     * Görünen ad.
     */
    @Size(max = 100, message = "Görünen ad en fazla 100 karakter olabilir")
    private String displayName;

    /**
     * Biyografi.
     */
    @Size(max = 500, message = "Biyografi en fazla 500 karakter olabilir")
    private String bio;

    /**
     * Tercih edilen dil.
     * ISO 639-1 kodu.
     */
    @Size(max = 5, message = "Dil kodu en fazla 5 karakter olabilir")
    private String preferredLanguage;

    /**
     * Saat dilimi.
     * IANA timezone formatı.
     */
    @Size(max = 50, message = "Saat dilimi en fazla 50 karakter olabilir")
    private String timezone;

    // ==================== CONSTRUCTOR ====================

    /**
     * Boş constructor.
     */
    public UpdateProfileRequest() {
        // Boş constructor
    }

    // ==================== GETTER METODLARI ====================

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getDisplayName() {
        return displayName;
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

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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
     * En az bir alan dolu mu kontrol eder.
     * 
     * @return Dolu alan varsa true
     */
    public boolean hasAnyField() {
        if (firstName != null && !firstName.trim().isEmpty()) {
            return true;
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            return true;
        }
        if (displayName != null && !displayName.trim().isEmpty()) {
            return true;
        }
        if (bio != null && !bio.trim().isEmpty()) {
            return true;
        }
        if (preferredLanguage != null && !preferredLanguage.trim().isEmpty()) {
            return true;
        }
        if (timezone != null && !timezone.trim().isEmpty()) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "UpdateProfileRequest{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", displayName='" + displayName + '\'' +
                ", bio='" + (bio != null ? bio.substring(0, Math.min(bio.length(), 20)) + "..." : null) + '\'' +
                '}';
    }
}
