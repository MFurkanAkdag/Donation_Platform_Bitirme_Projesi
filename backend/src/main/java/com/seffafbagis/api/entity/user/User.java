package com.seffafbagis.api.entity.user;

import com.seffafbagis.api.entity.base.BaseEntity;
import com.seffafbagis.api.enums.UserRole;
import com.seffafbagis.api.enums.UserStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Kullanıcı entity sınıfı.
 * 
 * Bu tablo şunları saklar:
 * - Temel kimlik bilgileri (email, şifre)
 * - Rol ve durum
 * - E-posta doğrulama durumu
 * - Son giriş zamanı
 * 
 * İlişkili tablolar:
 * - UserProfile: Profil bilgileri (1:1)
 * - UserPreference: Tercihler (1:1)
 * - UserSensitiveData: Hassas veriler (1:1)
 * 
 * @author Furkan
 * @version 1.0
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_role", columnList = "role"),
        @Index(name = "idx_users_status", columnList = "status")
})
public class User extends BaseEntity {

    /**
     * E-posta adresi.
     * 
     * - Benzersiz olmalı (unique = true)
     * - Giriş için kullanılır (username olarak)
     * - Küçük harfe çevrilerek saklanmalı
     */
    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email;

    /**
     * Şifre hash'i.
     * 
     * - BCrypt ile hash'lenmiş şifre saklanır
     * - Asla plain text saklanmaz
     * - Minimum 8 karakter şifre zorunlu (application seviyesinde)
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /**
     * Kullanıcı rolü.
     * 
     * - DONOR: Bağışçı
     * - FOUNDATION: Vakıf/Dernek
     * - BENEFICIARY: Faydalanıcı
     * - ADMIN: Yönetici
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role = UserRole.DONOR;

    /**
     * Hesap durumu.
     * 
     * - ACTIVE: Aktif
     * - INACTIVE: Pasif
     * - SUSPENDED: Askıya alınmış
     * - PENDING_VERIFICATION: Doğrulama bekliyor
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.PENDING_VERIFICATION;

    /**
     * E-posta doğrulanmış mı?
     */
    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    /**
     * E-posta doğrulama tarihi.
     */
    @Column(name = "email_verified_at")
    private Instant emailVerifiedAt;

    /**
     * Şifre son değiştirilme tarihi.
     */
    @Column(name = "password_changed_at")
    private Instant passwordChangedAt;

    /**
     * Son giriş tarihi.
     */
    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    /**
     * Başarısız giriş denemeleri.
     * 5 başarısız denemeden sonra hesap kilitlenir.
     */
    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    /**
     * Hesap kilit açılma zamanı.
     * 15 dakika boyunca kilidli kalır.
     */
    @Column(name = "locked_until")
    private Instant lockedUntil;

    // ==================== İLİŞKİLER ====================

    /**
     * Kullanıcı profili (1:1 ilişki).
     * 
     * - CascadeType.ALL: User silinince profile da silinir
     * - orphanRemoval: İlişki koparılınca profile silinir
     * - FetchType.LAZY: İhtiyaç olduğunda yüklenir
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private UserProfile profile;

    /**
     * Kullanıcı tercihleri (1:1 ilişki).
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private UserPreference preferences;

    /**
     * Hassas veriler (1:1 ilişki).
     * KVKK kapsamında şifreli saklanan veriler.
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private UserSensitiveData sensitiveData;

    // ==================== CONSTRUCTOR ====================

    /**
     * Boş constructor.
     * JPA için gerekli.
     */
    public User() {
        // JPA için boş constructor gerekli
    }

    /**
     * Temel bilgilerle constructor.
     * 
     * @param email        E-posta adresi
     * @param passwordHash Hash'lenmiş şifre
     * @param role         Kullanıcı rolü
     */
    public User(String email, String passwordHash, UserRole role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = UserStatus.PENDING_VERIFICATION;
        this.emailVerified = false;
    }

    // ==================== GETTER METODLARI ====================

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
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

    public Instant getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public Instant getPasswordChangedAt() {
        return passwordChangedAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts != null ? failedLoginAttempts : 0;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    /**
     * Checks if account is currently locked.
     * 
     * @return true if locked and lockout time hasn't expired
     */
    public boolean isAccountLocked() {
        if (lockedUntil == null) {
            return false;
        }
        return Instant.now().isBefore(lockedUntil);
    }

    public UserProfile getProfile() {
        return profile;
    }

    public UserPreference getPreferences() {
        return preferences;
    }

    public UserSensitiveData getSensitiveData() {
        return sensitiveData;
    }

    // ==================== SETTER METODLARI ====================

    public void setEmail(String email) {
        // E-postayı küçük harfe çevir
        if (email != null) {
            this.email = email.toLowerCase().trim();
        } else {
            this.email = null;
        }
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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

    public void setEmailVerifiedAt(Instant emailVerifiedAt) {
        this.emailVerifiedAt = emailVerifiedAt;
    }

    public void setPasswordChangedAt(Instant passwordChangedAt) {
        this.passwordChangedAt = passwordChangedAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts != null ? failedLoginAttempts : 0;
    }

    public void setLockedUntil(Instant lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    /**
     * Increments failed login attempts and locks account if necessary.
     * 
     * @return true if account was locked
     */
    public boolean incrementFailedLoginAttempts() {
        if (failedLoginAttempts == null) {
            failedLoginAttempts = 0;
        }
        failedLoginAttempts++;

        if (failedLoginAttempts >= 5) {
            this.lockedUntil = Instant.now().plus(java.time.Duration.ofMinutes(15));
            return true;
        }
        return false;
    }

    /**
     * Resets failed login attempts and clears lockout.
     */
    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    public void setProfile(UserProfile profile) {
        this.profile = profile;
        // İlişkinin diğer tarafını da set et
        if (profile != null) {
            profile.setUser(this);
        }
    }

    public void setPreferences(UserPreference preferences) {
        this.preferences = preferences;
        if (preferences != null) {
            preferences.setUser(this);
        }
    }

    public void setSensitiveData(UserSensitiveData sensitiveData) {
        this.sensitiveData = sensitiveData;
        if (sensitiveData != null) {
            sensitiveData.setUser(this);
        }
    }

    // ==================== YARDIMCI METODLAR ====================

    /**
     * Kullanıcı aktif mi?
     * 
     * @return Aktifse true
     */
    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    /**
     * E-posta doğrulanmış mı?
     * 
     * @return Doğrulanmışsa true
     */
    public boolean isVerified() {
        if (this.emailVerified == null) {
            return false;
        }
        return this.emailVerified;
    }

    /**
     * Kullanıcı admin mi?
     * 
     * @return Admin ise true
     */
    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

    /**
     * Kullanıcı vakıf/dernek mi?
     * 
     * @return Foundation ise true
     */
    public boolean isFoundation() {
        return this.role == UserRole.FOUNDATION;
    }

    /**
     * Kullanıcı bağışçı mı?
     * 
     * @return Donor ise true
     */
    public boolean isDonor() {
        return this.role == UserRole.DONOR;
    }

    /**
     * Kullanıcının tam adını döndürür.
     * Profile varsa oradan alır, yoksa e-postayı döndürür.
     * 
     * @return Tam ad veya e-posta
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

    /**
     * E-posta doğrulamasını tamamlar.
     */
    public void verifyEmail() {
        this.emailVerified = true;
        this.emailVerifiedAt = Instant.now();

        // Eğer doğrulama bekliyor durumundaysa aktif yap
        if (this.status == UserStatus.PENDING_VERIFICATION) {
            this.status = UserStatus.ACTIVE;
        }
    }

    /**
     * Son giriş zamanını günceller.
     */
    public void recordLogin() {
        this.lastLoginAt = Instant.now();
    }

    /**
     * Hesabı askıya alır.
     */
    public void suspend() {
        this.status = UserStatus.SUSPENDED;
    }

    /**
     * Hesabı aktif yapar.
     */
    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    /**
     * Hesabı pasif yapar.
     */
    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + getId() +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", status=" + status +
                ", emailVerified=" + emailVerified +
                '}';
    }
}
