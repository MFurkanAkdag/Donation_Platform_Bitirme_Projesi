-- ============================================================================
-- MIGRATION: V16__add_realworld_compatibility_fields.sql
-- PURPOSE: Add all missing columns and tables for real-world compatibility
-- AUTHOR: Furkan (Database Enhancement)
-- ============================================================================

-- ============================================================================
-- SECTION 1: USERS TABLE - Security Enhancements
-- ============================================================================

ALTER TABLE users
ADD COLUMN IF NOT EXISTS failed_login_attempts INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS locked_until TIMESTAMPTZ,
ADD COLUMN IF NOT EXISTS password_changed_at TIMESTAMPTZ;

COMMENT ON COLUMN users.failed_login_attempts IS 'Counter for consecutive failed login attempts - resets on successful login';
COMMENT ON COLUMN users.locked_until IS 'Account lockout expiry timestamp - NULL means not locked';
COMMENT ON COLUMN users.password_changed_at IS 'Last password change timestamp for security policy enforcement';


-- ============================================================================
-- SECTION 2: USER_SENSITIVE_DATA TABLE - KVKK Consent Fields
-- ============================================================================

ALTER TABLE user_sensitive_data
ADD COLUMN IF NOT EXISTS marketing_consent BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS marketing_consent_date TIMESTAMPTZ,
ADD COLUMN IF NOT EXISTS third_party_sharing_consent BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS third_party_sharing_consent_date TIMESTAMPTZ;

COMMENT ON COLUMN user_sensitive_data.marketing_consent IS 'User consent for marketing communications';
COMMENT ON COLUMN user_sensitive_data.marketing_consent_date IS 'Timestamp when marketing consent was given/withdrawn';
COMMENT ON COLUMN user_sensitive_data.third_party_sharing_consent IS 'User consent for sharing data with third parties';
COMMENT ON COLUMN user_sensitive_data.third_party_sharing_consent_date IS 'Timestamp when third-party sharing consent was given/withdrawn';


-- ============================================================================
-- SECTION 3: ORGANIZATIONS TABLE - Rejection Workflow
-- ============================================================================

ALTER TABLE organizations
ADD COLUMN IF NOT EXISTS rejection_reason TEXT,
ADD COLUMN IF NOT EXISTS resubmission_count INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS last_resubmission_at TIMESTAMPTZ;

COMMENT ON COLUMN organizations.rejection_reason IS 'Explanation provided when verification is rejected';
COMMENT ON COLUMN organizations.resubmission_count IS 'Number of times organization has resubmitted for verification';
COMMENT ON COLUMN organizations.last_resubmission_at IS 'Timestamp of the most recent resubmission';


-- ============================================================================
-- SECTION 4: ORGANIZATION_BANK_ACCOUNTS TABLE - Complete Banking Details
-- ============================================================================

ALTER TABLE organization_bank_accounts
ADD COLUMN IF NOT EXISTS bank_code VARCHAR(5),
ADD COLUMN IF NOT EXISTS branch_code VARCHAR(10),
ADD COLUMN IF NOT EXISTS account_number VARCHAR(30),
ADD COLUMN IF NOT EXISTS branch_city VARCHAR(100),
ADD COLUMN IF NOT EXISTS branch_district VARCHAR(100),
ADD COLUMN IF NOT EXISTS account_type VARCHAR(50) DEFAULT 'current';

COMMENT ON COLUMN organization_bank_accounts.bank_code IS 'Turkish bank code (e.g., 00046 for Akbank)';
COMMENT ON COLUMN organization_bank_accounts.branch_code IS 'Bank branch code';
COMMENT ON COLUMN organization_bank_accounts.account_number IS 'Account number (separate from IBAN for legacy compatibility)';
COMMENT ON COLUMN organization_bank_accounts.branch_city IS 'City where the bank branch is located';
COMMENT ON COLUMN organization_bank_accounts.branch_district IS 'District where the bank branch is located';
COMMENT ON COLUMN organization_bank_accounts.account_type IS 'Account type: current, savings, commercial';


-- ============================================================================
-- SECTION 5: CAMPAIGNS TABLE - Default Bank Account Association
-- ============================================================================

ALTER TABLE campaigns
ADD COLUMN IF NOT EXISTS default_bank_account_id UUID REFERENCES organization_bank_accounts(id);

CREATE INDEX IF NOT EXISTS idx_campaigns_bank_account ON campaigns(default_bank_account_id);

COMMENT ON COLUMN campaigns.default_bank_account_id IS 'Default bank account for receiving bank transfer donations for this campaign';


-- ============================================================================
-- SECTION 6: DONATIONS TABLE - Source Tracking and Refund Management
-- ============================================================================

ALTER TABLE donations
ADD COLUMN IF NOT EXISTS source VARCHAR(20) DEFAULT 'web',
ADD COLUMN IF NOT EXISTS refund_status VARCHAR(20) DEFAULT 'none',
ADD COLUMN IF NOT EXISTS refund_reason TEXT,
ADD COLUMN IF NOT EXISTS refund_requested_at TIMESTAMPTZ;

COMMENT ON COLUMN donations.source IS 'Donation source: web, mobile, api, partner';
COMMENT ON COLUMN donations.refund_status IS 'Refund status: none, pending, processing, completed, failed';
COMMENT ON COLUMN donations.refund_reason IS 'Reason provided for refund request';
COMMENT ON COLUMN donations.refund_requested_at IS 'Timestamp when refund was requested';


-- ============================================================================
-- SECTION 7: TRANSACTIONS TABLE - Refund and Installment Support
-- ============================================================================

ALTER TABLE transactions
ADD COLUMN IF NOT EXISTS refunded_amount DECIMAL(12,2),
ADD COLUMN IF NOT EXISTS refunded_at TIMESTAMPTZ,
ADD COLUMN IF NOT EXISTS installment_count INTEGER DEFAULT 1;

COMMENT ON COLUMN transactions.refunded_amount IS 'Amount refunded (may be partial)';
COMMENT ON COLUMN transactions.refunded_at IS 'Timestamp when refund was processed';
COMMENT ON COLUMN transactions.installment_count IS 'Number of installments for credit card payments';


-- ============================================================================
-- SECTION 8: BANK_TRANSFER_REFERENCES TABLE - Complete Transfer Tracking
-- ============================================================================

ALTER TABLE bank_transfer_references
ADD COLUMN IF NOT EXISTS bank_account_id UUID REFERENCES organization_bank_accounts(id),
ADD COLUMN IF NOT EXISTS sender_name VARCHAR(255),
ADD COLUMN IF NOT EXISTS sender_iban VARCHAR(34),
ADD COLUMN IF NOT EXISTS bank_account_snapshot JSONB;

CREATE INDEX IF NOT EXISTS idx_bank_ref_account ON bank_transfer_references(bank_account_id);

COMMENT ON COLUMN bank_transfer_references.bank_account_id IS 'The organization bank account this transfer should be sent to';
COMMENT ON COLUMN bank_transfer_references.sender_name IS 'Sender name from bank statement (for matching)';
COMMENT ON COLUMN bank_transfer_references.sender_iban IS 'Sender IBAN if available from bank statement';
COMMENT ON COLUMN bank_transfer_references.bank_account_snapshot IS 'Snapshot of bank account details at time of reference creation';


-- ============================================================================
-- SECTION 9: EVIDENCES TABLE - Invoice Details
-- ============================================================================

ALTER TABLE evidences
ADD COLUMN IF NOT EXISTS spend_date DATE,
ADD COLUMN IF NOT EXISTS invoice_number VARCHAR(100);

COMMENT ON COLUMN evidences.spend_date IS 'Date when the expense was incurred';
COMMENT ON COLUMN evidences.invoice_number IS 'Invoice or receipt number for verification';


-- ============================================================================
-- SECTION 10: REPORTS TABLE - Priority and Assignment
-- ============================================================================

ALTER TABLE reports
ADD COLUMN IF NOT EXISTS priority VARCHAR(20) DEFAULT 'medium',
ADD COLUMN IF NOT EXISTS assigned_to UUID REFERENCES users(id),
ADD COLUMN IF NOT EXISTS assigned_at TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS idx_reports_priority ON reports(priority);
CREATE INDEX IF NOT EXISTS idx_reports_assigned ON reports(assigned_to);

COMMENT ON COLUMN reports.priority IS 'Report priority: low, medium, high, critical';
COMMENT ON COLUMN reports.assigned_to IS 'Admin user assigned to handle this report';
COMMENT ON COLUMN reports.assigned_at IS 'Timestamp when report was assigned';


-- ============================================================================
-- SECTION 11: AUDIT_LOGS TABLE - Request Tracing
-- ============================================================================

ALTER TABLE audit_logs
ADD COLUMN IF NOT EXISTS request_id VARCHAR(50),
ADD COLUMN IF NOT EXISTS session_id VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_audit_request ON audit_logs(request_id);

COMMENT ON COLUMN audit_logs.request_id IS 'Unique request identifier for distributed tracing';
COMMENT ON COLUMN audit_logs.session_id IS 'User session identifier for action correlation';


-- ============================================================================
-- SECTION 12: EMAIL_LOGS TABLE - Provider and Template Tracking
-- ============================================================================

ALTER TABLE email_logs
ADD COLUMN IF NOT EXISTS provider VARCHAR(50),
ADD COLUMN IF NOT EXISTS template_name VARCHAR(100),
ADD COLUMN IF NOT EXISTS retry_count INTEGER DEFAULT 0;

COMMENT ON COLUMN email_logs.provider IS 'Email service provider: mailgun, ses, smtp';
COMMENT ON COLUMN email_logs.template_name IS 'Name of the email template used';
COMMENT ON COLUMN email_logs.retry_count IS 'Number of delivery retry attempts';


-- ============================================================================
-- SECTION 13: RECURRING_DONATIONS TABLE - Error Tracking
-- ============================================================================

ALTER TABLE recurring_donations
ADD COLUMN IF NOT EXISTS last_error_message TEXT;

COMMENT ON COLUMN recurring_donations.last_error_message IS 'Error message from the last failed payment attempt';


-- ============================================================================
-- SECTION 14: NEW TABLE - EMAIL_VERIFICATION_TOKENS
-- ============================================================================

CREATE TABLE IF NOT EXISTS email_verification_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    verified_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_email_verification_user ON email_verification_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_email_verification_hash ON email_verification_tokens(token_hash);

COMMENT ON TABLE email_verification_tokens IS 'Stores email verification tokens with separate lifecycle from password reset';


-- ============================================================================
-- SECTION 15: NEW TABLE - LOGIN_HISTORY
-- ============================================================================

CREATE TABLE IF NOT EXISTS login_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    login_status VARCHAR(20) NOT NULL,
    ip_address INET,
    user_agent TEXT,
    device_type VARCHAR(50),
    location_country VARCHAR(100),
    location_city VARCHAR(100),
    failure_reason VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_login_history_user ON login_history(user_id);
CREATE INDEX IF NOT EXISTS idx_login_history_status ON login_history(login_status);
CREATE INDEX IF NOT EXISTS idx_login_history_created ON login_history(created_at);

COMMENT ON TABLE login_history IS 'Tracks all login attempts for security monitoring and KVKK compliance';
COMMENT ON COLUMN login_history.login_status IS 'Login result: success, failed, blocked';
COMMENT ON COLUMN login_history.device_type IS 'Device type: desktop, mobile, tablet';
COMMENT ON COLUMN login_history.failure_reason IS 'Reason for failed login: invalid_password, account_locked, account_suspended, etc.';


-- ============================================================================
-- MIGRATION COMPLETE
-- ============================================================================
-- Summary of changes:
-- - Modified 13 existing tables with new columns
-- - Created 2 new tables (email_verification_tokens, login_history)
-- - Added 11 new indexes for query optimization
-- - Total new columns: 35
-- ============================================================================
