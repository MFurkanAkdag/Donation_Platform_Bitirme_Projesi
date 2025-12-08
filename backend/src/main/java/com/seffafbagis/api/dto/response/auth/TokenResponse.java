package com.seffafbagis.api.dto.response.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Token refresh response DTO.
 * 
 * Contains new access and refresh tokens after token refresh operation.
 * Used as response for POST /api/v1/auth/refresh endpoint.
 * 
 * Example response:
 * {
 *   "accessToken": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
 *   "refreshToken": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
 *   "tokenType": "Bearer",
 *   "expiresIn": 900
 * }
 * 
 * @author Furkan
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenResponse {

    /**
     * New access token.
     * JWT token for authenticating API requests.
     * Expires in 15 minutes (900 seconds).
     */
    @Schema(
        description = "New JWT access token",
        example = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String accessToken;

    /**
     * New refresh token.
     * JWT token for refreshing access tokens.
     * Expires in 7 days (rotated on each refresh).
     */
    @Schema(
        description = "New JWT refresh token (rotated)",
        example = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String refreshToken;

    /**
     * Token type.
     * Always "Bearer" for JWT authentication.
     */
    @Schema(
        description = "Token type, always Bearer",
        example = "Bearer",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String tokenType;

    /**
     * Access token expiration time in seconds.
     * Client should refresh token before this duration elapses.
     */
    @Schema(
        description = "Access token expiration in seconds",
        example = "900",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private long expiresIn;

    // ==================== CONSTRUCTORS ====================

    /**
     * Default constructor.
     */
    public TokenResponse() {
        this.tokenType = "Bearer";
    }

    /**
     * Constructor with tokens.
     * 
     * @param accessToken New access token
     * @param refreshToken New refresh token
     * @param expiresIn Expiration time in seconds
     */
    public TokenResponse(String accessToken, String refreshToken, long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.tokenType = "Bearer";
    }

    /**
     * Constructor with all fields.
     * 
     * @param accessToken New access token
     * @param refreshToken New refresh token
     * @param tokenType Token type (usually "Bearer")
     * @param expiresIn Expiration time in seconds
     */
    public TokenResponse(String accessToken, String refreshToken, String tokenType, long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType != null ? tokenType : "Bearer";
        this.expiresIn = expiresIn;
    }

    // ==================== FACTORY METHODS ====================

    /**
     * Creates TokenResponse from tokens and expiration time.
     * 
     * @param accessToken New access token
     * @param refreshToken New refresh token
     * @param expiresInSeconds Expiration time in seconds
     * @return TokenResponse
     */
    public static TokenResponse of(String accessToken, String refreshToken, long expiresInSeconds) {
        return new TokenResponse(accessToken, refreshToken, expiresInSeconds);
    }

    // ==================== GETTERS ====================

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

    // ==================== SETTERS ====================

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

    // ==================== toString ====================

    @Override
    public String toString() {
        // SECURITY: Never log tokens in production
        return "TokenResponse{" +
                "tokenType='" + tokenType + '\'' +
                ", expiresIn=" + expiresIn +
                "}";
    }
}
