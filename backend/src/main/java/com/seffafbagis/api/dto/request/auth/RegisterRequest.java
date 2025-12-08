package com.seffafbagis.api.dto.request.auth;

import com.seffafbagis.api.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Kullanıcı kayıt request DTO.
 * 
 * Yeni kullanıcı kaydı için gerekli bilgileri taşır.
 * Validation annotation'ları ile input kontrolü sağlanır.
 * 
 * Request örneği:
 * {
 *   "email": "kullanici@example.com",
 *   "password": "Sifre123!",
 *   "confirmPassword": "Sifre123!",
 *   "role": "DONOR",
 *   "firstName": "Ahmet",
 *   "lastName": "Yılmaz",
 *   "acceptTerms": true,
 *   "acceptKvkk": true
 * }
 * 
 * @author Furkan
 * @version 1.0
 */
public class RegisterRequest {

    /**
     * E-posta adresi.
     * 
     * - Boş olamaz
     * - Geçerli e-posta formatında olmalı
     * - Maksimum 255 karakter
     */
    @NotBlank(message = "E-posta adresi zorunludur")
    @Email(message = "Geçerli bir e-posta adresi giriniz")
    @Size(max = 255, message = "E-posta adresi en fazla 255 karakter olabilir")
    private String email;

    /**
     * Şifre.
     * 
     * Şifre kuralları:
     * - En az 8 karakter
     * - En az 1 büyük harf
     * - En az 1 küçük harf
     * - En az 1 rakam
     * - En az 1 özel karakter (!@#$%^&*(),.?":{}|<>)
     */
    @NotBlank(message = "Şifre zorunludur")
    @Size(min = 8, max = 100, message = "Şifre en az 8, en fazla 100 karakter olmalıdır")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#$%^&*(),.?\":{}|<>]{8,}$",
        message = "Şifre en az 1 büyük harf, 1 küçük harf, 1 rakam ve 1 özel karakter içermelidir"
    )
    private String password;

    /**
     * Şifre onayı.
     * 
     * Password ile aynı olmalı.
     * Bu kontrol service katmanında yapılır.
     */
    @NotBlank(message = "Şifre onayı zorunludur")
    private String confirmPassword;

    /**
     * Kullanıcı rolü.
     * 
     * - DONOR: Bağışçı
     * - FOUNDATION: Vakıf/Dernek
     * - BENEFICIARY: Faydalanıcı
     * 
     * Not: ADMIN rolü kayıt sırasında seçilemez.
     */
    @NotNull(message = "Kullanıcı rolü zorunludur")
    private UserRole role;

    /**
     * Ad.
     * Profil için opsiyonel.
     */
    @Size(max = 100, message = "Ad en fazla 100 karakter olabilir")
    private String firstName;

    /**
     * Soyad.
     * Profil için opsiyonel.
     */
    @Size(max = 100, message = "Soyad en fazla 100 karakter olabilir")
    private String lastName;

    /**
     * Kullanım şartları kabul edildi mi?
     * 
     * Kayıt için zorunlu.
     */
    @NotNull(message = "Kullanım şartlarını kabul etmelisiniz")
    private Boolean acceptTerms;

    /**
     * KVKK aydınlatma metni kabul edildi mi?
     * 
     * Kayıt için zorunlu.
     */
    @NotNull(message = "KVKK aydınlatma metnini kabul etmelisiniz")
    private Boolean acceptKvkk;

    // ==================== CONSTRUCTOR ====================

    /**
     * Boş constructor.
     */
    public RegisterRequest() {
        // Boş constructor
    }

    // ==================== GETTER METODLARI ====================

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public UserRole getRole() {
        return role;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Boolean getAcceptTerms() {
        return acceptTerms;
    }

    public Boolean getAcceptKvkk() {
        return acceptKvkk;
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

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setAcceptTerms(Boolean acceptTerms) {
        this.acceptTerms = acceptTerms;
    }

    public void setAcceptKvkk(Boolean acceptKvkk) {
        this.acceptKvkk = acceptKvkk;
    }

    // ==================== YARDIMCI METODLAR ====================

    /**
     * Şifreler eşleşiyor mu kontrol eder.
     * 
     * @return Eşleşiyorsa true
     */
    public boolean passwordsMatch() {
        if (password == null || confirmPassword == null) {
            return false;
        }
        return password.equals(confirmPassword);
    }

    /**
     * Tüm kabul alanları onaylanmış mı kontrol eder.
     * 
     * @return Hepsi onaylanmışsa true
     */
    public boolean hasAllConsents() {
        boolean termsAccepted = acceptTerms != null && acceptTerms;
        boolean kvkkAccepted = acceptKvkk != null && acceptKvkk;
        return termsAccepted && kvkkAccepted;
    }

    /**
     * Rol geçerli mi kontrol eder.
     * ADMIN rolü kayıt sırasında seçilemez.
     * 
     * @return Geçerli ise true
     */
    public boolean isRoleValid() {
        if (role == null) {
            return false;
        }
        // ADMIN rolü kayıt sırasında seçilemez
        return role != UserRole.ADMIN;
    }

    @Override
    public String toString() {
        // GÜVENLİK: Şifre asla log'lanmaz
        return "RegisterRequest{" +
                "email='" + email + '\'' +
                ", role=" + role +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", acceptTerms=" + acceptTerms +
                ", acceptKvkk=" + acceptKvkk +
                '}';
    }
}
