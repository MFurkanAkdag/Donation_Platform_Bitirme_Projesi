package com.seffafbagis.api.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Kullanıcı giriş request DTO.
 * 
 * Login için gerekli bilgileri taşır.
 * 
 * Request örneği:
 * {
 *   "email": "kullanici@example.com",
 *   "password": "Sifre123!",
 *   "rememberMe": true
 * }
 * 
 * @author Furkan
 * @version 1.0
 */
public class LoginRequest {

    /**
     * E-posta adresi.
     */
    @NotBlank(message = "E-posta adresi zorunludur")
    @Email(message = "Geçerli bir e-posta adresi giriniz")
    @Size(max = 255, message = "E-posta adresi en fazla 255 karakter olabilir")
    private String email;

    /**
     * Şifre.
     */
    @NotBlank(message = "Şifre zorunludur")
    @Size(max = 100, message = "Şifre en fazla 100 karakter olabilir")
    private String password;

    /**
     * Beni hatırla.
     * 
     * True ise refresh token süresi uzatılır.
     */
    private Boolean rememberMe;

    // ==================== CONSTRUCTOR ====================

    /**
     * Boş constructor.
     */
    public LoginRequest() {
        // Boş constructor
    }

    /**
     * Temel constructor.
     * 
     * @param email E-posta
     * @param password Şifre
     */
    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
        this.rememberMe = false;
    }

    // ==================== GETTER METODLARI ====================

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Boolean getRememberMe() {
        return rememberMe;
    }

    // ==================== SETTER METODLARI ====================

    public void setEmail(String email) {
        // E-postayı küçük harfe çevir ve trim yap
        if (email != null) {
            this.email = email.toLowerCase().trim();
        } else {
            this.email = null;
        }
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRememberMe(Boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    // ==================== YARDIMCI METODLAR ====================

    /**
     * Beni hatırla seçili mi kontrol eder.
     * Null güvenli kontrol.
     * 
     * @return Seçili ise true
     */
    public boolean isRememberMeEnabled() {
        if (rememberMe == null) {
            return false;
        }
        return rememberMe;
    }

    @Override
    public String toString() {
        // GÜVENLİK: Şifre asla log'lanmaz
        return "LoginRequest{" +
                "email='" + email + '\'' +
                ", rememberMe=" + rememberMe +
                '}';
    }
}
