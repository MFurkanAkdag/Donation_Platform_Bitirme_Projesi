package com.seffafbagis.api.enums;

/**
 * Enumeration of all auditable actions in the system.
 * Used for categorizing audit logs and filtering.
 */
public enum AuditAction {
    // Authentication
    USER_LOGIN("User logged in"),
    USER_LOGOUT("User logged out"),
    USER_REGISTER("User registered"),
    LOGIN_FAILED("Login attempt failed"),
    LOGIN_BLOCKED("Login blocked due to lockout"),

    // Password
    PASSWORD_CHANGE("Password changed"),
    PASSWORD_RESET_REQUEST("Password reset requested"),
    PASSWORD_RESET_COMPLETE("Password reset completed"),

    // Email
    EMAIL_VERIFICATION_SENT("Email verification sent"),
    EMAIL_VERIFIED("Email verified"),

    // User Management
    USER_PROFILE_UPDATE("User profile updated"),
    USER_PREFERENCES_UPDATE("User preferences updated"),
    USER_STATUS_CHANGE("User status changed"),
    USER_ROLE_CHANGE("User role changed"),
    USER_DELETE("User deleted"),
    USER_UNLOCK("User account unlocked"),

    // Sensitive Data (KVKK)
    SENSITIVE_DATA_ACCESS("Sensitive data accessed"),
    SENSITIVE_DATA_UPDATE("Sensitive data updated"),
    SENSITIVE_DATA_DELETE("Sensitive data deleted"),
    SENSITIVE_DATA_EXPORT("Sensitive data exported"),
    CONSENT_UPDATE("Consent settings updated"),

    // Organization
    ORGANIZATION_VERIFY("Organization verified"),
    ORGANIZATION_REJECT("Organization rejected"),

    // Campaign
    CAMPAIGN_APPROVE("Campaign approved"),
    CAMPAIGN_REJECT("Campaign rejected"),

    // Admin
    ADMIN_ACTION("Admin action performed"),
    SETTING_CREATE("System setting created"),
    SETTING_UPDATE("System setting updated"),
    SETTING_DELETE("System setting deleted"),

    // Reports
    REPORT_ASSIGN("Report assigned"),
    REPORT_RESOLVE("Report resolved");

    private final String description;

    AuditAction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
