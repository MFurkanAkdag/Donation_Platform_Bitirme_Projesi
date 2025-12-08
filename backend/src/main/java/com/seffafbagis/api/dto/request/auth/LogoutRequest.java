package com.seffafbagis.api.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * User logout request DTO.
 * 
 * Contains refresh token to revoke and optional flag to logout from all devices.
 * 
 * Example request:
 * {
 *   "refreshToken": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
 *   "logoutAllDevices": false
 * }
 * 
 * @author Furkan
 * @version 1.0
 */
public class LogoutRequest {

    /**
     * Refresh token to revoke.
     * The token will be marked as revoked in database.
     */
    @NotBlank(message = "Refresh token cannot be blank")
    @Schema(
        description = "Refresh token to revoke",
        example = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String refreshToken;

    /**
     * Flag to logout from all devices.
     * If true, all refresh tokens for the user will be revoked.
     * Optional, defaults to false.
     */
    @Schema(
        description = "Logout from all devices flag",
        example = "false",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Boolean logoutAllDevices;

    // ==================== CONSTRUCTORS ====================

    /**
     * Default constructor.
     */
    public LogoutRequest() {
        this.logoutAllDevices = false;
    }

    /**
     * Constructor with refresh token.
     * 
     * @param refreshToken Token to revoke
     */
    public LogoutRequest(String refreshToken) {
        this.refreshToken = refreshToken;
        this.logoutAllDevices = false;
    }

    /**
     * Full constructor.
     * 
     * @param refreshToken Token to revoke
     * @param logoutAllDevices Logout all devices flag
     */
    public LogoutRequest(String refreshToken, Boolean logoutAllDevices) {
        this.refreshToken = refreshToken;
        this.logoutAllDevices = logoutAllDevices != null ? logoutAllDevices : false;
    }

    // ==================== GETTERS ====================

    public String getRefreshToken() {
        return refreshToken;
    }

    public Boolean getLogoutAllDevices() {
        return logoutAllDevices != null ? logoutAllDevices : false;
    }

    // ==================== SETTERS ====================

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setLogoutAllDevices(Boolean logoutAllDevices) {
        this.logoutAllDevices = logoutAllDevices;
    }

    // ==================== toString ====================

    @Override
    public String toString() {
        // SECURITY: Mask token value in logs
        String maskedToken = "***";
        if (refreshToken != null && refreshToken.length() > 20) {
            maskedToken = refreshToken.substring(0, 10) + "..." + refreshToken.substring(refreshToken.length() - 5);
        }
        return "LogoutRequest{" +
                "refreshToken='" + maskedToken + '\'' +
                ", logoutAllDevices=" + logoutAllDevices +
                "}";
    }
}
