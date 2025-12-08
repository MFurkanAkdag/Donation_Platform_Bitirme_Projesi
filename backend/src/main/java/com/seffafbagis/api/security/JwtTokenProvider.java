package com.seffafbagis.api.security;

import com.seffafbagis.api.config.JwtConfig;
import com.seffafbagis.api.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

/**
 * Generates, validates, and parses JWT tokens used by the platform.
 */
@Component
public class JwtTokenProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenProvider.class);

    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TOKEN_TYPE = "type";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    private final JwtConfig jwtConfig;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtConfig.getSecret()));
    }

    /**
     * Builds an access token containing user identity information.
     */
    public String generateAccessToken(CustomUserDetails userDetails) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtConfig.getAccessTokenExpiration());

        return Jwts.builder()
                .subject(userDetails.getId().toString())
                .claim(CLAIM_EMAIL, userDetails.getUsername())
                .claim(CLAIM_ROLE, userDetails.getRole().name())
                .claim(CLAIM_TOKEN_TYPE, "access")
                .issuer(jwtConfig.getIssuer())
                .audience().add(jwtConfig.getAudience()).and()
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }

    /**
     * Builds an access token with user UUID, email, and role.
     * Overloaded variant for cases where CustomUserDetails is not available.
     */
    public String generateAccessToken(UUID userId, String email, String roleName) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtConfig.getAccessTokenExpiration());

        return Jwts.builder()
                .subject(userId.toString())
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_ROLE, roleName)
                .claim(CLAIM_TOKEN_TYPE, "access")
                .issuer(jwtConfig.getIssuer())
                .audience().add(jwtConfig.getAudience()).and()
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }

    /**
     * Builds a refresh token with minimal claims.
     */
    public String generateRefreshToken(CustomUserDetails userDetails) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtConfig.getRefreshTokenExpiration());

        return Jwts.builder()
                .subject(userDetails.getId().toString())
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH)
                .issuer(jwtConfig.getIssuer())
                .audience().add(jwtConfig.getAudience()).and()
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }

    /**
     * Builds a refresh token with user UUID.
     * Overloaded variant for cases where CustomUserDetails is not available.
     */
    public String generateRefreshToken(UUID userId, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtConfig.getRefreshTokenExpiration());

        return Jwts.builder()
                .subject(userId.toString())
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH)
                .issuer(jwtConfig.getIssuer())
                .audience().add(jwtConfig.getAudience()).and()
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }

    /**
     * Validates structure, signature, and expiration of a token.
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            LOGGER.debug("JWT expired: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            LOGGER.debug("JWT malformed: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            LOGGER.debug("JWT unsupported: {}", ex.getMessage());
        } catch (SecurityException ex) {
            LOGGER.debug("JWT signature invalid: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            LOGGER.debug("JWT empty: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Returns raw claims for the token.
     */
    public Claims getClaims(String token) {
        return parseClaims(token);
    }

    public UUID getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return UUID.fromString(claims.getSubject());
    }

    public String getEmailFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get(CLAIM_EMAIL, String.class);
    }

    public String extractEmail(String token) {
        return getEmailFromToken(token);
    }

    public String getRoleFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get(CLAIM_ROLE, String.class);
    }

    public Date getExpirationFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.getExpiration();
    }

    public boolean isRefreshToken(String token) {
        Claims claims = parseClaims(token);
        String type = claims.get(CLAIM_TOKEN_TYPE, String.class);
        return TOKEN_TYPE_REFRESH.equals(type);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
