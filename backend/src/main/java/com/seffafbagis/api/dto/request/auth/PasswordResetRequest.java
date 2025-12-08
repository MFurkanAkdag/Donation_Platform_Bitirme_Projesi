package com.seffafbagis.api.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Şifre sıfırlama isteği DTO.
 * 
 * Şifresini unutan kullanıcı e-posta adresini girerek
 * sıfırlama linki talep eder.
 * 
 * Request örneği:
 * {
 *   "email": "kullanici@example.com"
 * }
 * 
 * @author Furkan
 * @version 1.0
 */
public class PasswordResetRequest {

    /**
     * E-posta adresi.
     */
    @NotBlank(message = "E-posta adresi zorunludur")
    @Email(message = "Geçerli bir e-posta adresi giriniz")
    @Size(max = 255, message = "E-posta adresi en fazla 255 karakter olabilir")
    private String email;

    // ==================== CONSTRUCTOR ====================

    /**
     * Boş constructor.
     */
    public PasswordResetRequest() {
        // Boş constructor
    }

    /**
     * E-posta ile constructor.
     * 
     * @param email E-posta adresi
     */
    public PasswordResetRequest(String email) {
        this.email = email;
    }

    // ==================== GETTER METODLARI ====================

    public String getEmail() {
        return email;
    }

    // ==================== SETTER METODLARI ====================

    public void setEmail(String email) {
        if (email != null) {
            this.email = email.toLowerCase().trim();
        } else {
            this.email = null;
        }
    }

    @Override
    public String toString() {
        return "PasswordResetRequest{" +
                "email='" + email + '\'' +
                '}';
    }
}
