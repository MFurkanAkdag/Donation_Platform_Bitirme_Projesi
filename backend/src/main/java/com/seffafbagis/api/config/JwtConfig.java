package com.seffafbagis.api.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Binds JWT related configuration from {@code app.jwt.*} properties in a
 * type-safe manner.
 */
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@Validated
public class JwtConfig {

    private final String secret;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final String tokenPrefix;
    private final String headerName;
    private final String issuer;
    private final String audience;

    public JwtConfig(
            @NotBlank @Size(min = 32, message = "JWT secret must be at least 32 characters long") String secret,
            @Min(value = 60000, message = "Access token expiration must be at least 1 minute") @DefaultValue("900000") long accessTokenExpiration,
            @Min(value = 3600000, message = "Refresh token expiration must be at least 1 hour") @DefaultValue("604800000") long refreshTokenExpiration,
            @NotBlank @DefaultValue("Bearer ") String tokenPrefix,
            @NotBlank @DefaultValue("Authorization") String headerName,
            @NotBlank @DefaultValue("seffaf-bagis-platform") String issuer,
            @NotBlank @DefaultValue("seffaf-bagis-clients") String audience) {
        this.secret = secret;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.tokenPrefix = tokenPrefix;
        this.headerName = headerName;
        this.issuer = issuer;
        this.audience = audience;
    }

    public String getSecret() {
        return secret;
    }

    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    public String getTokenPrefix() {
        return tokenPrefix;
    }

    public String getHeaderName() {
        return headerName;
    }

    public String getIssuer() {
        return issuer;
    }

    public String getAudience() {
        return audience;
    }

    public long getAccessTokenExpirationInSeconds() {
        return accessTokenExpiration / 1000;
    }

    public long getAccessTokenExpirationInMinutes() {
        return accessTokenExpiration / 60000;
    }

    public long getRefreshTokenExpirationInSeconds() {
        return refreshTokenExpiration / 1000;
    }

    public long getRefreshTokenExpirationInDays() {
        return refreshTokenExpiration / 86_400_000;
    }
}
