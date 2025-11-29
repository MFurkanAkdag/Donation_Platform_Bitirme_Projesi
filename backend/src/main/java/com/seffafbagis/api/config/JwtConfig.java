package com.seffafbagis.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * JWT (JSON Web Token) yapılandırma sınıfı.
 * 
 * application.yml'daki app.jwt altındaki değerleri okur.
 * 
 * GÜVENLİK NOTLARI:
 * - Secret key en az 256 bit (32 karakter) olmalı
 * - Secret key'i asla kod içinde hard-coded tutmayın
 * - Environment variable veya secret manager kullanın
 * 
 * @author Furkan
 * @version 1.0
 */
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@Validated
public class JwtConfig {

    /**
     * JWT imzalama için gizli anahtar.
     * 
     * ÖNEMLİ:
     * - En az 32 karakter (256 bit) olmalı
     * - Rastgele ve tahmin edilemez olmalı
     * - Production'da environment variable kullanın
     */
    @NotBlank(message = "JWT secret boş olamaz")
    @Size(min = 32, message = "JWT secret en az 32 karakter olmalı")
    private String secret;

    /**
     * Access token geçerlilik süresi (milisaniye).
     * 
     * Önerilen değerler:
     * - 15 dakika = 900000 ms
     * - 30 dakika = 1800000 ms
     * - 1 saat = 3600000 ms
     * 
     * Kısa tutmak güvenlik açısından daha iyidir.
     */
    @Min(value = 60000, message = "Access token süresi en az 1 dakika olmalı")
    private long accessTokenExpiration = 900000; // 15 dakika

    /**
     * Refresh token geçerlilik süresi (milisaniye).
     * 
     * Önerilen değerler:
     * - 7 gün = 604800000 ms
     * - 30 gün = 2592000000 ms
     * 
     * Access token'dan uzun olmalı.
     */
    @Min(value = 3600000, message = "Refresh token süresi en az 1 saat olmalı")
    private long refreshTokenExpiration = 604800000; // 7 gün

    /**
     * Token'ı oluşturan kurum/uygulama.
     * Token doğrulama sırasında kontrol edilir.
     */
    private String issuer = "seffaf-bagis-api";

    /**
     * Token'ın hedef kitlesi.
     * Hangi uygulamalar için geçerli olduğunu belirtir.
     */
    private String audience = "seffaf-bagis-web";

    // ==================== GETTER VE SETTER METODLARI ====================

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public void setAccessTokenExpiration(long accessTokenExpiration) {
        this.accessTokenExpiration = accessTokenExpiration;
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    public void setRefreshTokenExpiration(long refreshTokenExpiration) {
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    // ==================== YARDIMCI METODLAR ====================

    /**
     * Access token süresini saniye cinsinden döndürür.
     * 
     * @return Access token süresi (saniye)
     */
    public long getAccessTokenExpirationInSeconds() {
        return accessTokenExpiration / 1000;
    }

    /**
     * Refresh token süresini saniye cinsinden döndürür.
     * 
     * @return Refresh token süresi (saniye)
     */
    public long getRefreshTokenExpirationInSeconds() {
        return refreshTokenExpiration / 1000;
    }

    /**
     * Access token süresini dakika cinsinden döndürür.
     * 
     * @return Access token süresi (dakika)
     */
    public long getAccessTokenExpirationInMinutes() {
        return accessTokenExpiration / 60000;
    }

    /**
     * Refresh token süresini gün cinsinden döndürür.
     * 
     * @return Refresh token süresi (gün)
     */
    public long getRefreshTokenExpirationInDays() {
        return refreshTokenExpiration / 86400000;
    }
}
