package com.seffafbagis.api.dto.response.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.entity.user.UserProfile;
import com.seffafbagis.api.enums.UserRole;
import com.seffafbagis.api.enums.UserStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Kullanıcı bilgileri response DTO.
 * 
 * Kullanıcı entity'sinden frontend'e güvenli şekilde
 * aktarılacak bilgileri içerir.
 * 
 * Response örneği:
 * {
 *   "id": "550e8400-e29b-41d4-a716-446655440000",
 *   "email": "kullanici@example.com",
 *   "role": "DONOR",
 *   "status": "ACTIVE",
 *   "emailVerified": true,
 *   "profile": {
 *     "firstName": "Ahmet",
 *     "lastName": "Yılmaz",
 *     "displayName": "ahmetyilmaz",
 *     "avatarUrl": "https://example.com/avatar.jpg",
 *     "bio": "Yazılım geliştirici"
 *   },
 *   "createdAt": "2024-01-15T10:30:00Z",
 *   "lastLoginAt": "2024-01-20T15:45:00Z"
 * }
 * 
 * @author Furkan
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    /**
     * Kullanıcı ID.
     */
    private UUID id;

    /**
     * E-posta adresi.
     */
    private String email;

    /**
     * Kullanıcı rolü.
     */
    private UserRole role;

    /**
     * Hesap durumu.
     */
    private UserStatus status;

    /**
     * E-posta doğrulanmış mı?
     */
    private Boolean emailVerified;

    /**
     * Profil bilgileri.
     */
    private ProfileInfo profile;

    /**
     * Kayıt tarihi.
     */
    private Instant createdAt;

    /**
     * Son giriş tarihi.
     */
    private Instant lastLoginAt;

    // ==================== CONSTRUCTOR ====================

    /**
     * Boş constructor.
     */
    public UserResponse() {
        // Boş constructor
    }

    /**
     * User entity'den constructor.
     * 
     * @param user User entity
     */
    public UserResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.status = user.getStatus();
        this.emailVerified = user.getEmailVerified();
        this.createdAt = user.getCreatedAt();
        this.lastLoginAt = user.getLastLoginAt();

        // Profil varsa ekle
        UserProfile userProfile = user.getProfile();
        if (userProfile != null) {
            this.profile = new ProfileInfo(userProfile);
        }
    }

    // ==================== FACTORY METODLAR ====================

    /**
     * User entity'den UserResponse oluşturur.
     * 
     * @param user User entity
     * @return UserResponse
     */
    public static UserResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(user);
    }

    // ==================== GETTER METODLARI ====================

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public ProfileInfo getProfile() {
        return profile;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    // ==================== SETTER METODLARI ====================

    public void setId(UUID id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public void setProfile(ProfileInfo profile) {
        this.profile = profile;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    // ==================== YARDIMCI METODLAR ====================

    /**
     * Kullanıcının tam adını döndürür.
     * Profil varsa oradan, yoksa e-postadan.
     * 
     * @return Tam ad
     */
    public String getFullName() {
        if (profile != null) {
            String fullName = profile.getFullName();
            if (fullName != null && !fullName.trim().isEmpty()) {
                return fullName;
            }
        }
        return email;
    }

    // ==================== INNER CLASS ====================

    /**
     * Profil bilgileri inner class.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProfileInfo {

        /**
         * Ad.
         */
        private String firstName;

        /**
         * Soyad.
         */
        private String lastName;

        /**
         * Görünen ad.
         */
        private String displayName;

        /**
         * Avatar URL.
         */
        private String avatarUrl;

        /**
         * Biyografi.
         */
        private String bio;

        /**
         * Tercih edilen dil.
         */
        private String preferredLanguage;

        /**
         * Saat dilimi.
         */
        private String timezone;

        // ==================== CONSTRUCTOR ====================

        /**
         * Boş constructor.
         */
        public ProfileInfo() {
            // Boş constructor
        }

        /**
         * UserProfile entity'den constructor.
         * 
         * @param profile UserProfile entity
         */
        public ProfileInfo(UserProfile profile) {
            this.firstName = profile.getFirstName();
            this.lastName = profile.getLastName();
            this.displayName = profile.getDisplayName();
            this.avatarUrl = profile.getAvatarUrl();
            this.bio = profile.getBio();
            this.preferredLanguage = profile.getPreferredLanguage();
            this.timezone = profile.getTimezone();
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
    }
}
