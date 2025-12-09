package com.seffafbagis.api.entity.auth;

import com.seffafbagis.api.entity.base.BaseEntity;
import com.seffafbagis.api.entity.user.User;
import jakarta.persistence.*;

/**
 * Login history entity for tracking user authentication attempts.
 * 
 * Records login attempts (successful and failed) with device and location
 * information
 * for security monitoring and audit trails.
 * 
 * @author Furkan
 * @version 1.0
 */
@Entity
@Table(name = "login_history")
public class LoginHistory extends BaseEntity {

    /**
     * User who attempted to login.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Status of the login attempt.
     * Values: 'success', 'failed', 'blocked'
     */
    @Column(name = "login_status", nullable = false, length = 20)
    private String loginStatus;

    /**
     * IP address of the login attempt.
     * Supports both IPv4 (15 chars) and IPv6 (45 chars).
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * User agent string from HTTP request.
     * Contains browser and OS information.
     */
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    /**
     * Device type from user agent.
     * Values: 'desktop', 'mobile', 'tablet'
     */
    @Column(name = "device_type", length = 50)
    private String deviceType;

    /**
     * Country of login based on IP geolocation.
     */
    @Column(name = "location_country", length = 100)
    private String locationCountry;

    /**
     * City of login based on IP geolocation.
     */
    @Column(name = "location_city", length = 100)
    private String locationCity;

    /**
     * Reason for failed login attempt.
     * Values: 'invalid_password', 'account_locked', 'account_suspended',
     * 'email_not_verified'
     * Only populated if loginStatus is 'failed' or 'blocked'.
     */
    @Column(name = "failure_reason", length = 100)
    private String failureReason;

    // ==================== CONSTRUCTORS ====================

    public LoginHistory() {
    }

    public LoginHistory(User user, String loginStatus, String ipAddress, String userAgent, String deviceType,
            String locationCountry, String locationCity, String failureReason) {
        this.user = user;
        this.loginStatus = loginStatus;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.deviceType = deviceType;
        this.locationCountry = locationCountry;
        this.locationCity = locationCity;
        this.failureReason = failureReason;
    }

    // ==================== GETTERS AND SETTERS ====================

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getLoginStatus() {
        return loginStatus;
    }

    public void setLoginStatus(String loginStatus) {
        this.loginStatus = loginStatus;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getLocationCountry() {
        return locationCountry;
    }

    public void setLocationCountry(String locationCountry) {
        this.locationCountry = locationCountry;
    }

    public String getLocationCity() {
        return locationCity;
    }

    public void setLocationCity(String locationCity) {
        this.locationCity = locationCity;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    // ==================== METHODS ====================

    /**
     * Checks if this login attempt was successful.
     * 
     * @return true if loginStatus is 'success'
     */
    public boolean isSuccessful() {
        return "success".equals(loginStatus);
    }

    /**
     * Checks if this login attempt failed.
     * 
     * @return true if loginStatus is 'failed'
     */
    public boolean isFailed() {
        return "failed".equals(loginStatus);
    }

    /**
     * Checks if this login was blocked.
     * 
     * @return true if loginStatus is 'blocked'
     */
    public boolean isBlocked() {
        return "blocked".equals(loginStatus);
    }
}
