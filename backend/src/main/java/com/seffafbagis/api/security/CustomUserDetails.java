package com.seffafbagis.api.security;

import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Spring Security UserDetails implementasyonu.
 * 
 * Bu sınıf, User entity'sini Spring Security'nin anlayacağı
 * formata dönüştürür.
 * 
 * Spring Security bu sınıfı şunlar için kullanır:
 * - Authentication (kimlik doğrulama)
 * - Authorization (yetkilendirme)
 * - Kullanıcı durumu kontrolü (aktif, kilitli vb.)
 * 
 * @author Furkan
 * @version 1.0
 */
public class CustomUserDetails implements UserDetails {

    private final UUID id;
    private final String email;
    private final String password;
    private final String role;
    private final UserStatus status;
    private final boolean emailVerified;

    /**
     * Constructor - User entity'den oluşturur.
     * 
     * @param user User entity
     */
    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPasswordHash();
        this.role = user.getRole().name();
        this.status = user.getStatus();
        this.emailVerified = Boolean.TRUE.equals(user.getEmailVerified());
    }

    /**
     * Kullanıcı ID'sini döndürür.
     * 
     * @return Kullanıcı UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Kullanıcı rolünü döndürür.
     * 
     * @return Rol adı
     */
    public String getRole() {
        return role;
    }

    /**
     * E-posta doğrulanmış mı?
     * 
     * @return Doğrulanmışsa true
     */
    public boolean isEmailVerified() {
        return emailVerified;
    }

    // ==================== UserDetails INTERFACE METODLARI ====================

    /**
     * Kullanıcının yetkilerini (rollerini) döndürür.
     * 
     * Spring Security "ROLE_" prefix'i bekler.
     * Örn: ROLE_ADMIN, ROLE_DONOR, ROLE_FOUNDATION
     * 
     * @return Yetki listesi
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // "ROLE_" prefix'i ekliyoruz çünkü Spring Security bunu bekler
        // hasRole("ADMIN") kontrolü aslında hasAuthority("ROLE_ADMIN") demek
        String authorityName = "ROLE_" + role;
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(authorityName);
        return Collections.singletonList(authority);
    }

    /**
     * Şifreyi döndürür.
     * 
     * @return Hash'lenmiş şifre
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Kullanıcı adını döndürür.
     * 
     * Bizim sistemimizde e-posta kullanıcı adı olarak kullanılıyor.
     * 
     * @return E-posta adresi
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Hesabın süresi dolmuş mu?
     * 
     * Bizim sistemimizde hesap süresi dolma özelliği yok.
     * 
     * @return Her zaman true (süre dolmamış)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Hesap kilitli mi?
     * 
     * SUSPENDED durumundaki hesaplar kilitli sayılır.
     * 
     * @return Kilitli değilse true
     */
    @Override
    public boolean isAccountNonLocked() {
        // SUSPENDED durumundaki hesaplar kilitli
        if (status == UserStatus.SUSPENDED) {
            return false;
        }
        return true;
    }

    /**
     * Kimlik bilgileri (şifre) süresi dolmuş mu?
     * 
     * Bizim sistemimizde şifre süresi dolma özelliği yok.
     * 
     * @return Her zaman true (süre dolmamış)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Hesap aktif mi?
     * 
     * ACTIVE durumundaki hesaplar aktif sayılır.
     * E-posta doğrulaması da kontrol edilebilir.
     * 
     * @return Aktifse true
     */
    @Override
    public boolean isEnabled() {
        // Sadece ACTIVE durumundaki kullanıcılar etkin
        if (status != UserStatus.ACTIVE) {
            return false;
        }
        return true;
    }

    // ==================== EQUALS VE HASHCODE ====================

    /**
     * İki CustomUserDetails nesnesinin eşit olup olmadığını kontrol eder.
     * ID üzerinden karşılaştırma yapar.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CustomUserDetails other = (CustomUserDetails) obj;
        if (id == null) {
            return other.id == null;
        }
        return id.equals(other.id);
    }

    /**
     * Hash code hesaplar.
     */
    @Override
    public int hashCode() {
        if (id == null) {
            return 0;
        }
        return id.hashCode();
    }

    /**
     * String temsilini döndürür.
     */
    @Override
    public String toString() {
        return "CustomUserDetails{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", status=" + status +
                ", emailVerified=" + emailVerified +
                '}';
    }
}
