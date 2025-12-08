package com.seffafbagis.api.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Şifre değiştirme request DTO.
 * 
 * Giriş yapmış kullanıcının mevcut şifresini bildiği durumda
 * yeni şifre belirlemesi için kullanılır.
 * 
 * Request örneği:
 * {
 *   "currentPassword": "EskiSifre123!",
 *   "newPassword": "YeniSifre456!",
 *   "confirmPassword": "YeniSifre456!"
 * }
 * 
 * @author Furkan
 * @version 1.0
 */
public class ChangePasswordRequest {

    /**
     * Mevcut şifre.
     */
    @NotBlank(message = "Mevcut şifre zorunludur")
    @Size(max = 100, message = "Şifre en fazla 100 karakter olabilir")
    private String currentPassword;

    /**
     * Yeni şifre.
     * 
     * Şifre kuralları:
     * - En az 8 karakter
     * - En az 1 büyük harf
     * - En az 1 küçük harf
     * - En az 1 rakam
     * - En az 1 özel karakter
     */
    @NotBlank(message = "Yeni şifre zorunludur")
    @Size(min = 8, max = 100, message = "Şifre en az 8, en fazla 100 karakter olmalıdır")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#$%^&*(),.?\":{}|<>]{8,}$",
        message = "Şifre en az 1 büyük harf, 1 küçük harf, 1 rakam ve 1 özel karakter içermelidir"
    )
    private String newPassword;

    /**
     * Şifre onayı.
     */
    @NotBlank(message = "Şifre onayı zorunludur")
    private String confirmPassword;

    // ==================== CONSTRUCTOR ====================

    /**
     * Boş constructor.
     */
    public ChangePasswordRequest() {
        // Boş constructor
    }

    // ==================== GETTER METODLARI ====================

    public String getCurrentPassword() {
        return currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    // ==================== SETTER METODLARI ====================

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    // ==================== YARDIMCI METODLAR ====================

    /**
     * Yeni şifreler eşleşiyor mu kontrol eder.
     * 
     * @return Eşleşiyorsa true
     */
    public boolean passwordsMatch() {
        if (newPassword == null || confirmPassword == null) {
            return false;
        }
        return newPassword.equals(confirmPassword);
    }

    /**
     * Yeni şifre eski şifreden farklı mı kontrol eder.
     * 
     * @return Farklı ise true
     */
    public boolean isNewPasswordDifferent() {
        if (currentPassword == null || newPassword == null) {
            return true;
        }
        return !currentPassword.equals(newPassword);
    }

    @Override
    public String toString() {
        // GÜVENLİK: Şifreler asla log'lanmaz
        return "ChangePasswordRequest{}";
    }
}
