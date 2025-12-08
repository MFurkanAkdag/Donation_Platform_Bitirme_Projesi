package com.seffafbagis.api.dto.response.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.seffafbagis.api.enums.UserRole;

import java.time.Instant;
import java.util.UUID;

/**
 * Authentication response DTO.
 * 
 * Login ve register sonrası döndürülen bilgiler.
 * 
 * Response örneği:
 * {
 *   "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *   "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *   "tokenType": "Bearer",
 *   "expiresIn": 900,
 *   "user": {
 *     "id": "550e8400-e29b-41d4-a716-446655440000",
 *     "email": "kullanici@example.com",
 *     "role": "DONOR",
 *     "fullName": "Ahmet Yılmaz",
 *     "emailVerified": true
 *   }
 * }
 * 
 * @author Furkan
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    /**
     * Access token.
     * API istekleri için kullanılır.
     */
    private String accessToken;

    /**
     * Refresh token.
     * Access token yenilemek için kullanılır.
     */
    private String refreshToken;

    /**
     * Token tipi.
     * Her zaman "Bearer".
     */
    private String tokenType = "Bearer";

    /**
     * Access token geçerlilik süresi (saniye).
     */
    private long expiresIn;

    /**
     * Kullanıcı bilgileri.
     */
    private UserInfo user;

    // ==================== CONSTRUCTOR ====================

    /**
     * Boş constructor.
     */
    public AuthResponse() {
        // Boş constructor
    }

    /**
     * Temel constructor.
     * 
     * @param accessToken Access token
     * @param refreshToken Refresh token
     * @param expiresIn Geçerlilik süresi (saniye)
     */
    public AuthResponse(String accessToken, String refreshToken, long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.tokenType = "Bearer";
    }

    /**
     * Kullanıcı bilgileri ile constructor.
     * 
     * @param accessToken Access token
     * @param refreshToken Refresh token
     * @param expiresIn Geçerlilik süresi (saniye)
     * @param user Kullanıcı bilgileri
     */
    public AuthResponse(String accessToken, String refreshToken, long expiresIn, UserInfo user) {
        this(accessToken, refreshToken, expiresIn);
        this.user = user;
    }

    // ==================== GETTER METODLARI ====================

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public UserInfo getUser() {
        return user;
    }

    // ==================== SETTER METODLARI ====================

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    // ==================== INNER CLASS ====================

    /**
     * Kullanıcı bilgileri inner class.
     * 
     * Login/register sonrası frontend'e gönderilen
     * temel kullanıcı bilgileri.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserInfo {

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
         * Tam ad veya görünen ad.
         */
        private String fullName;

        /**
         * Avatar URL.
         */
        private String avatarUrl;

        /**
         * E-posta doğrulanmış mı?
         */
        private Boolean emailVerified;

        /**
         * Son giriş zamanı.
         */
        private Instant lastLoginAt;

        // ==================== CONSTRUCTOR ====================

        /**
         * Boş constructor.
         */
        public UserInfo() {
            // Boş constructor
        }

        /**
         * Temel constructor.
         * 
         * @param id Kullanıcı ID
         * @param email E-posta
         * @param role Rol
         */
        public UserInfo(UUID id, String email, UserRole role) {
            this.id = id;
            this.email = email;
            this.role = role;
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

        public String getFullName() {
            return fullName;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public Boolean getEmailVerified() {
            return emailVerified;
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

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }

        public void setEmailVerified(Boolean emailVerified) {
            this.emailVerified = emailVerified;
        }

        public void setLastLoginAt(Instant lastLoginAt) {
            this.lastLoginAt = lastLoginAt;
        }
    }
}
