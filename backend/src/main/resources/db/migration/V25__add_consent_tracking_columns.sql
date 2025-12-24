-- Add missing consent tracking columns to user_sensitive_data table

-- Consent version for KVKK compliance tracking
ALTER TABLE user_sensitive_data
    ADD COLUMN consent_version VARCHAR(10);

-- IP address from which consent was given (for audit trail)
ALTER TABLE user_sensitive_data
    ADD COLUMN consent_ip_address VARCHAR(45);
