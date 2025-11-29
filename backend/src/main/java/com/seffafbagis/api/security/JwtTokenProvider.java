package com.seffafbagis.api.security;

import com.seffafbagis.api.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * JWT Token oluşturma ve doğrulama sınıfı.
 * 
 * Bu sınıf şunları sağlar:
 * - Access token oluşturma
 * - Refresh token oluşturma
 * - Token doğrulama
 * - Token'dan bilgi çıkarma
 * 
 * JWT Yapısı:
 * - Header: Algoritma bilgisi (HS256)
 * - Payload: Kullanıcı bilgileri (claims)
 * - Signature: İmza (secret key ile)
 * 
 * GÜVENLİK NOTLARI:
 * - Secret key en az 256 bit olmalı
 * - Token sürelerini kısa tutun
 * - Refresh token'ları veritabanında saklayın
 * 
 * @author Furkan
 * @version 1.0
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final JwtConfig jwtConfig;
    private final SecretKey secretKey;

    /**
     * Token içindeki rol claim adı.
     */
    private static final String CLAIM_ROLE = "role";

    /**
     * Token içindeki kullanıcı ID claim adı.
     */
    private static final String CLAIM_USER_ID = "userId";

    /**
     * Token tipi claim adı.
     */
    private static final String CLAIM_TOKEN_TYPE = "type";

    /**
     * Access token tipi.
     */
    private static final String TOKEN_TYPE_ACCESS = "access";

    /**
     * Refresh token tipi.
     */
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    /**
     * Constructor.
     * 
     * @param jwtConfig JWT yapılandırması
     */
    public JwtTokenProvider(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        this.secretKey = createSecretKey();
    }

    /**
     * Secret key oluşturur.
     * 
     * Base64 encoded string'den SecretKey oluşturur.
     * 
     * @return SecretKey
     */
    private SecretKey createSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtConfig.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ==================== TOKEN OLUŞTURMA ====================

    /**
     * Access token oluşturur.
     * 
     * @param userId Kullanıcı ID
     * @param email Kullanıcı e-posta
     * @param role Kullanıcı rolü
     * @return JWT access token
     */
    public String generateAccessToken(UUID userId, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, userId.toString());
        claims.put(CLAIM_ROLE, role);
        claims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS);

        return buildToken(claims, email, jwtConfig.getAccessTokenExpiration());
    }

    /**
     * Refresh token oluşturur.
     * 
     * @param userId Kullanıcı ID
     * @param email Kullanıcı e-posta
     * @return JWT refresh token
     */
    public String generateRefreshToken(UUID userId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, userId.toString());
        claims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH);

        return buildToken(claims, email, jwtConfig.getRefreshTokenExpiration());
    }

    /**
     * Token oluşturur.
     * 
     * @param claims Token içindeki bilgiler
     * @param subject Token sahibi (genellikle email)
     * @param expirationMs Token süresi (milisaniye)
     * @return JWT token
     */
    private String buildToken(Map<String, Object> claims, String subject, long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuer(jwtConfig.getIssuer())
                .audience().add(jwtConfig.getAudience()).and()
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    // ==================== TOKEN DOĞRULAMA ====================

    /**
     * Token'ın geçerli olup olmadığını kontrol eder.
     * 
     * @param token JWT token
     * @return Geçerli ise true
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;

        } catch (SignatureException e) {
            logger.error("Geçersiz JWT imzası: {}", e.getMessage());

        } catch (MalformedJwtException e) {
            logger.error("Geçersiz JWT formatı: {}", e.getMessage());

        } catch (ExpiredJwtException e) {
            logger.error("JWT token süresi dolmuş: {}", e.getMessage());

        } catch (UnsupportedJwtException e) {
            logger.error("Desteklenmeyen JWT token: {}", e.getMessage());

        } catch (IllegalArgumentException e) {
            logger.error("JWT claims boş: {}", e.getMessage());

        } catch (JwtException e) {
            logger.error("JWT doğrulama hatası: {}", e.getMessage());
        }

        return false;
    }

    /**
     * Token'ın access token olup olmadığını kontrol eder.
     * 
     * @param token JWT token
     * @return Access token ise true
     */
    public boolean isAccessToken(String token) {
        String tokenType = extractClaim(token, claims -> claims.get(CLAIM_TOKEN_TYPE, String.class));
        return TOKEN_TYPE_ACCESS.equals(tokenType);
    }

    /**
     * Token'ın refresh token olup olmadığını kontrol eder.
     * 
     * @param token JWT token
     * @return Refresh token ise true
     */
    public boolean isRefreshToken(String token) {
        String tokenType = extractClaim(token, claims -> claims.get(CLAIM_TOKEN_TYPE, String.class));
        return TOKEN_TYPE_REFRESH.equals(tokenType);
    }

    /**
     * Token'ın süresinin dolup dolmadığını kontrol eder.
     * 
     * @param token JWT token
     * @return Süresi dolmuşsa true
     */
    public boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        if (expiration == null) {
            return true;
        }
        return expiration.before(new Date());
    }

    // ==================== BİLGİ ÇIKARMA ====================

    /**
     * Token'dan e-posta adresini çıkarır.
     * 
     * @param token JWT token
     * @return E-posta adresi
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Token'dan kullanıcı ID'sini çıkarır.
     * 
     * @param token JWT token
     * @return Kullanıcı ID
     */
    public UUID extractUserId(String token) {
        String userIdStr = extractClaim(token, claims -> claims.get(CLAIM_USER_ID, String.class));
        if (userIdStr == null) {
            return null;
        }
        return UUID.fromString(userIdStr);
    }

    /**
     * Token'dan rolü çıkarır.
     * 
     * @param token JWT token
     * @return Kullanıcı rolü
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get(CLAIM_ROLE, String.class));
    }

    /**
     * Token'dan son kullanma tarihini çıkarır.
     * 
     * @param token JWT token
     * @return Son kullanma tarihi
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Token'dan oluşturulma tarihini çıkarır.
     * 
     * @param token JWT token
     * @return Oluşturulma tarihi
     */
    public Date extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }

    /**
     * Token'dan belirtilen claim'i çıkarır.
     * 
     * @param token JWT token
     * @param claimsResolver Claim çözümleyici fonksiyon
     * @param <T> Dönüş tipi
     * @return Claim değeri
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        if (claims == null) {
            return null;
        }
        return claimsResolver.apply(claims);
    }

    /**
     * Token'dan tüm claim'leri çıkarır.
     * 
     * @param token JWT token
     * @return Claims nesnesi
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (JwtException e) {
            logger.error("Claims çıkarma hatası: {}", e.getMessage());
            return null;
        }
    }

    // ==================== YARDIMCI METODLAR ====================

    /**
     * Token'ın kalan süresini saniye cinsinden döndürür.
     * 
     * @param token JWT token
     * @return Kalan süre (saniye)
     */
    public long getRemainingTimeInSeconds(String token) {
        Date expiration = extractExpiration(token);
        if (expiration == null) {
            return 0;
        }

        long remainingMs = expiration.getTime() - System.currentTimeMillis();
        if (remainingMs < 0) {
            return 0;
        }

        return remainingMs / 1000;
    }

    /**
     * Access token süresini milisaniye cinsinden döndürür.
     * 
     * @return Access token süresi (ms)
     */
    public long getAccessTokenExpirationMs() {
        return jwtConfig.getAccessTokenExpiration();
    }

    /**
     * Refresh token süresini milisaniye cinsinden döndürür.
     * 
     * @return Refresh token süresi (ms)
     */
    public long getRefreshTokenExpirationMs() {
        return jwtConfig.getRefreshTokenExpiration();
    }
}
