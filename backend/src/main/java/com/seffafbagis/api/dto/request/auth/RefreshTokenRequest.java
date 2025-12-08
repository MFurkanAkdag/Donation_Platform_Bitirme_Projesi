package com.seffafbagis.api.dto.request.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Refresh token request DTO.
 * 
 * Access token yenilemek için refresh token gönderilir.
 * 
 * Request örneği:
 * {
 *   "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
 * }
 * 
 * @author Furkan
 * @version 1.0
 */


public class RefreshTokenRequest {

    /**
     * Refresh token.
     */
    @NotBlank(message = "Refresh token zorunludur")
    private String refreshToken;

    // ==================== CONSTRUCTOR ====================

    /**
     * Boş constructor.
     */
    public RefreshTokenRequest() {
        // Boş constructor
    }

    /**
     * Token ile constructor.
     * 
     * @param refreshToken Refresh token
     */
    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // ==================== GETTER METODLARI ====================

    public String getRefreshToken() {
        return refreshToken;
    }

    // ==================== SETTER METODLARI ====================

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public String toString() {
        // GÜVENLİK: Token değerini kısalt
        String maskedToken = "***";
        if (refreshToken != null && refreshToken.length() > 20) {
            maskedToken = refreshToken.substring(0, 10) + "..." + refreshToken.substring(refreshToken.length() - 5);
        }
        return "RefreshTokenRequest{" +
                "refreshToken='" + maskedToken + '\'' +
                '}';
    }
}
