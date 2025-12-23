# PHASE 0: DATABASE MIGRATION - COMPLETE SCHEMA ENHANCEMENT

## CONTEXT AND BACKGROUND

You are working on "Şeffaf Bağış Platformu" (Transparent Donation Platform), a graduation project for Fatih Sultan Mehmet Vakıf University. This is a comprehensive donation platform for Turkey that emphasizes transparency and accountability in charitable giving.

### Project Overview
- **Technology Stack**: Java Spring Boot, PostgreSQL, Redis, Next.js
- **Purpose**: Enable foundations to create campaigns, donors to contribute, and maintain transparency through evidence uploads and scoring systems
- **Compliance Requirements**: KVKK (Turkish Personal Data Protection Law) compliant

### Current State
The database schema has been created with 36 tables following 5NF normalization. However, after a thorough real-world compatibility analysis, several critical gaps have been identified that must be addressed before proceeding with backend development.

---

## OBJECTIVE

Execute a comprehensive database migration that adds all missing columns and tables required for real-world operation. This migration must be created as a Flyway migration file following the project's established patterns.

---

## MIGRATION REQUIREMENTS

### 1. USERS TABLE - Security Enhancements

**Purpose**: Add brute force protection and security policy compliance fields.

**Why Needed**:
- After 5 failed login attempts, the account should be temporarily locked
- KVKK and security standards require tracking password change dates
- These fields are essential for the Auth module (Furkan's responsibility)

```sql
ALTER TABLE users
ADD COLUMN failed_login_attempts INTEGER DEFAULT 0,
ADD COLUMN locked_until TIMESTAMPTZ,
ADD COLUMN password_changed_at TIMESTAMPTZ;

COMMENT ON COLUMN users.failed_login_attempts IS 'Counter for consecutive failed login attempts - resets on successful login';
COMMENT ON COLUMN users.locked_until IS 'Account lockout expiry timestamp - NULL means not locked';
COMMENT ON COLUMN users.password_changed_at IS 'Last password change timestamp for security policy enforcement';
```

---

### 2. USER_SENSITIVE_DATA TABLE - KVKK Consent Fields

**Purpose**: Add marketing and third-party sharing consent tracking for KVKK compliance.

**Why Needed**:
- KVKK Articles 5 and 6 require explicit consent for data processing
- Marketing communications require separate consent
- Third-party data sharing requires explicit permission
- All consent dates must be recorded for audit purposes

```sql
ALTER TABLE user_sensitive_data
ADD COLUMN marketing_consent BOOLEAN DEFAULT FALSE,
ADD COLUMN marketing_consent_date TIMESTAMPTZ,
ADD COLUMN third_party_sharing_consent BOOLEAN DEFAULT FALSE,
ADD COLUMN third_party_sharing_consent_date TIMESTAMPTZ;

COMMENT ON COLUMN user_sensitive_data.marketing_consent IS 'User consent for marketing communications';
COMMENT ON COLUMN user_sensitive_data.marketing_consent_date IS 'Timestamp when marketing consent was given/withdrawn';
COMMENT ON COLUMN user_sensitive_data.third_party_sharing_consent IS 'User consent for sharing data with third parties';
COMMENT ON COLUMN user_sensitive_data.third_party_sharing_consent_date IS 'Timestamp when third-party sharing consent was given/withdrawn';
```

---

### 3. ORGANIZATIONS TABLE - Rejection Workflow

**Purpose**: Add fields to support the organization verification rejection and resubmission workflow.

**Why Needed**:
- Rejected organizations need to know the reason for rejection
- Admin panel needs to track resubmission attempts
- Better UX for foundation onboarding process

```sql
ALTER TABLE organizations
ADD COLUMN rejection_reason TEXT,
ADD COLUMN resubmission_count INTEGER DEFAULT 0,
ADD COLUMN last_resubmission_at TIMESTAMPTZ;

COMMENT ON COLUMN organizations.rejection_reason IS 'Explanation provided when verification is rejected';
COMMENT ON COLUMN organizations.resubmission_count IS 'Number of times organization has resubmitted for verification';
COMMENT ON COLUMN organizations.last_resubmission_at IS 'Timestamp of the most recent resubmission';
```

---

### 4. ORGANIZATION_BANK_ACCOUNTS TABLE - Complete Banking Details

**Purpose**: Add comprehensive bank account information required for bank transfer processing.

**Why Needed**:
- Turkish banking system requires bank code and branch code for transfers
- Account number separate from IBAN for legacy system compatibility
- Branch location helps with verification
- Account type distinguishes between commercial and personal accounts

```sql
ALTER TABLE organization_bank_accounts
ADD COLUMN bank_code VARCHAR(5),
ADD COLUMN branch_code VARCHAR(10),
ADD COLUMN account_number VARCHAR(30),
ADD COLUMN branch_city VARCHAR(100),
ADD COLUMN branch_district VARCHAR(100),
ADD COLUMN account_type VARCHAR(50) DEFAULT 'current';

COMMENT ON COLUMN organization_bank_accounts.bank_code IS 'Turkish bank code (e.g., 00046 for Akbank)';
COMMENT ON COLUMN organization_bank_accounts.branch_code IS 'Bank branch code';
COMMENT ON COLUMN organization_bank_accounts.account_number IS 'Account number (separate from IBAN for legacy compatibility)';
COMMENT ON COLUMN organization_bank_accounts.branch_city IS 'City where the bank branch is located';
COMMENT ON COLUMN organization_bank_accounts.branch_district IS 'District where the bank branch is located';
COMMENT ON COLUMN organization_bank_accounts.account_type IS 'Account type: current, savings, commercial';
```

---

### 5. CAMPAIGNS TABLE - Default Bank Account Association

**Purpose**: Allow campaigns to be associated with a specific bank account for bank transfers.

**Why Needed**:
- Organizations may have multiple bank accounts
- Each campaign might use a different designated account
- Bank transfer screen should show the correct IBAN for each campaign

```sql
ALTER TABLE campaigns
ADD COLUMN default_bank_account_id UUID REFERENCES organization_bank_accounts(id);

CREATE INDEX idx_campaigns_bank_account ON campaigns(default_bank_account_id);

COMMENT ON COLUMN campaigns.default_bank_account_id IS 'Default bank account for receiving bank transfer donations for this campaign';
```

---

### 6. DONATIONS TABLE - Source Tracking and Refund Management

**Purpose**: Add donation source tracking and refund workflow fields.

**Why Needed**:
- Analytics requires knowing donation source (web, mobile, API)
- Refund process needs dedicated status tracking
- Customer support needs refund reason documentation

```sql
ALTER TABLE donations
ADD COLUMN source VARCHAR(20) DEFAULT 'web',
ADD COLUMN refund_status VARCHAR(20) DEFAULT 'none',
ADD COLUMN refund_reason TEXT,
ADD COLUMN refund_requested_at TIMESTAMPTZ;

COMMENT ON COLUMN donations.source IS 'Donation source: web, mobile, api, partner';
COMMENT ON COLUMN donations.refund_status IS 'Refund status: none, pending, processing, completed, failed';
COMMENT ON COLUMN donations.refund_reason IS 'Reason provided for refund request';
COMMENT ON COLUMN donations.refund_requested_at IS 'Timestamp when refund was requested';
```

---

### 7. TRANSACTIONS TABLE - Refund and Installment Support

**Purpose**: Add refund tracking and installment payment support.

**Why Needed**:
- Refund amounts may differ from original (partial refunds)
- Turkish payment systems commonly support installment payments
- Need to track when refunds are processed

```sql
ALTER TABLE transactions
ADD COLUMN refunded_amount DECIMAL(12,2),
ADD COLUMN refunded_at TIMESTAMPTZ,
ADD COLUMN installment_count INTEGER DEFAULT 1;

COMMENT ON COLUMN transactions.refunded_amount IS 'Amount refunded (may be partial)';
COMMENT ON COLUMN transactions.refunded_at IS 'Timestamp when refund was processed';
COMMENT ON COLUMN transactions.installment_count IS 'Number of installments for credit card payments';
```

---

### 8. BANK_TRANSFER_REFERENCES TABLE - Complete Transfer Tracking

**Purpose**: Add bank account association and sender information for transfer matching.

**Why Needed**:
- Need to know which bank account the transfer should go to
- Sender information from bank statement helps with matching
- Snapshot preserves IBAN details at time of reference creation

```sql
ALTER TABLE bank_transfer_references
ADD COLUMN bank_account_id UUID REFERENCES organization_bank_accounts(id),
ADD COLUMN sender_name VARCHAR(255),
ADD COLUMN sender_iban VARCHAR(34),
ADD COLUMN bank_account_snapshot JSONB;

CREATE INDEX idx_bank_ref_account ON bank_transfer_references(bank_account_id);

COMMENT ON COLUMN bank_transfer_references.bank_account_id IS 'The organization bank account this transfer should be sent to';
COMMENT ON COLUMN bank_transfer_references.sender_name IS 'Sender name from bank statement (for matching)';
COMMENT ON COLUMN bank_transfer_references.sender_iban IS 'Sender IBAN if available from bank statement';
COMMENT ON COLUMN bank_transfer_references.bank_account_snapshot IS 'Snapshot of bank account details at time of reference creation';
```

---

### 9. EVIDENCES TABLE - Invoice Details

**Purpose**: Add invoice-specific fields for better financial documentation.

**Why Needed**:
- Transparency reports need exact spend dates
- Invoice numbers enable verification against original documents
- Required for proper financial auditing

```sql
ALTER TABLE evidences
ADD COLUMN spend_date DATE,
ADD COLUMN invoice_number VARCHAR(100);

COMMENT ON COLUMN evidences.spend_date IS 'Date when the expense was incurred';
COMMENT ON COLUMN evidences.invoice_number IS 'Invoice or receipt number for verification';
```

---

### 10. REPORTS TABLE - Priority and Assignment

**Purpose**: Add priority levels and admin assignment for report management.

**Why Needed**:
- Critical fraud reports need immediate attention
- Admin workflow requires assignment tracking
- Proper prioritization prevents important reports being overlooked

```sql
ALTER TABLE reports
ADD COLUMN priority VARCHAR(20) DEFAULT 'medium',
ADD COLUMN assigned_to UUID REFERENCES users(id),
ADD COLUMN assigned_at TIMESTAMPTZ;

CREATE INDEX idx_reports_priority ON reports(priority);
CREATE INDEX idx_reports_assigned ON reports(assigned_to);

COMMENT ON COLUMN reports.priority IS 'Report priority: low, medium, high, critical';
COMMENT ON COLUMN reports.assigned_to IS 'Admin user assigned to handle this report';
COMMENT ON COLUMN reports.assigned_at IS 'Timestamp when report was assigned';
```

---

### 11. AUDIT_LOGS TABLE - Request Tracing

**Purpose**: Add distributed tracing support for debugging and compliance.

**Why Needed**:
- Request ID enables log correlation across services
- Session ID links actions to specific user sessions
- Essential for KVKK audits and debugging

```sql
ALTER TABLE audit_logs
ADD COLUMN request_id VARCHAR(50),
ADD COLUMN session_id VARCHAR(255);

CREATE INDEX idx_audit_request ON audit_logs(request_id);

COMMENT ON COLUMN audit_logs.request_id IS 'Unique request identifier for distributed tracing';
COMMENT ON COLUMN audit_logs.session_id IS 'User session identifier for action correlation';
```

---

### 12. EMAIL_LOGS TABLE - Provider and Template Tracking

**Purpose**: Add email provider and template information for better email management.

**Why Needed**:
- Support for multiple email providers (Mailgun, SES, SMTP)
- Template tracking helps with debugging delivery issues
- Retry count enables monitoring of delivery problems

```sql
ALTER TABLE email_logs
ADD COLUMN provider VARCHAR(50),
ADD COLUMN template_name VARCHAR(100),
ADD COLUMN retry_count INTEGER DEFAULT 0;

COMMENT ON COLUMN email_logs.provider IS 'Email service provider: mailgun, ses, smtp';
COMMENT ON COLUMN email_logs.template_name IS 'Name of the email template used';
COMMENT ON COLUMN email_logs.retry_count IS 'Number of delivery retry attempts';
```

---

### 13. RECURRING_DONATIONS TABLE - Error Tracking

**Purpose**: Add error message tracking for failed recurring payments.

**Why Needed**:
- Customer support needs to know why payments failed
- Enables proactive outreach to donors with payment issues
- Helps identify systematic payment problems

```sql
ALTER TABLE recurring_donations
ADD COLUMN last_error_message TEXT;

COMMENT ON COLUMN recurring_donations.last_error_message IS 'Error message from the last failed payment attempt';
```

---

### 14. NEW TABLE: EMAIL_VERIFICATION_TOKENS

**Purpose**: Dedicated table for email verification tokens, separate from password reset tokens.

**Why Needed**:
- Email verification has different lifecycle than password reset
- Users may request multiple verification emails
- Separate table enables different expiry policies

```sql
CREATE TABLE email_verification_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    verified_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_email_verification_user ON email_verification_tokens(user_id);
CREATE INDEX idx_email_verification_hash ON email_verification_tokens(token_hash);

COMMENT ON TABLE email_verification_tokens IS 'Stores email verification tokens with separate lifecycle from password reset';
```

---

### 15. NEW TABLE: LOGIN_HISTORY

**Purpose**: Comprehensive login activity tracking for security monitoring.

**Why Needed**:
- Security auditing requires login history
- Detect suspicious login patterns (different locations, devices)
- KVKK compliance for data access logging
- Enable "active sessions" feature for users

```sql
CREATE TABLE login_history (
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

CREATE INDEX idx_login_history_user ON login_history(user_id);
CREATE INDEX idx_login_history_status ON login_history(login_status);
CREATE INDEX idx_login_history_created ON login_history(created_at);

COMMENT ON TABLE login_history IS 'Tracks all login attempts for security monitoring and KVKK compliance';
COMMENT ON COLUMN login_history.login_status IS 'Login result: success, failed, blocked';
COMMENT ON COLUMN login_history.device_type IS 'Device type: desktop, mobile, tablet';
COMMENT ON COLUMN login_history.failure_reason IS 'Reason for failed login: invalid_password, account_locked, account_suspended, etc.';
```

---

## COMPLETE MIGRATION FILE

Create the following Flyway migration file:

**File**: `backend/src/main/resources/db/migration/V16__add_realworld_compatibility_fields.sql`

```sql
-- ============================================================================
-- MIGRATION: V16__add_realworld_compatibility_fields.sql
-- PURPOSE: Add all missing columns and tables for real-world compatibility
-- DATE: 2024
-- AUTHOR: Furkan (Phase 0 - Database Enhancement)
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
```

---

## VERIFICATION STEPS

After executing this migration, verify the following:

### 1. Check All New Columns Exist

```sql
-- Verify users table columns
SELECT column_name, data_type, column_default 
FROM information_schema.columns 
WHERE table_name = 'users' 
AND column_name IN ('failed_login_attempts', 'locked_until', 'password_changed_at');

-- Verify user_sensitive_data table columns
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'user_sensitive_data' 
AND column_name LIKE '%consent%';

-- Verify organization_bank_accounts columns
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'organization_bank_accounts' 
AND column_name IN ('bank_code', 'branch_code', 'account_number', 'branch_city', 'branch_district', 'account_type');
```

### 2. Check New Tables Exist

```sql
-- Verify new tables
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN ('email_verification_tokens', 'login_history');
```

### 3. Check Indexes Exist

```sql
-- Verify new indexes
SELECT indexname, tablename 
FROM pg_indexes 
WHERE schemaname = 'public' 
AND indexname LIKE 'idx_%' 
AND indexname IN (
    'idx_campaigns_bank_account',
    'idx_bank_ref_account',
    'idx_reports_priority',
    'idx_reports_assigned',
    'idx_audit_request',
    'idx_email_verification_user',
    'idx_email_verification_hash',
    'idx_login_history_user',
    'idx_login_history_status',
    'idx_login_history_created'
);
```

### 4. Check Foreign Key Constraints

```sql
-- Verify foreign key constraints
SELECT
    tc.constraint_name,
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
AND tc.table_name IN ('campaigns', 'bank_transfer_references', 'reports', 'email_verification_tokens', 'login_history');
```

---

## RESULT FILE REQUIREMENTS

After completing this phase, create a result file at:
`docs/Furkan/step_results/phase_0_result.md`

The result file must include:

1. **Execution Status**: Success or failure for each section
2. **Tables Modified**: List of all 13 modified tables with their new columns
3. **Tables Created**: Confirmation of 2 new tables
4. **Indexes Created**: List of all 11 new indexes
5. **Verification Results**: Output of all verification queries
6. **Issues Encountered**: Any errors or warnings during migration
7. **Next Steps**: If any issues, what needs to be fixed before proceeding to Phase 1

---

## IMPORTANT NOTES

1. **Idempotent Migration**: All statements use `IF NOT EXISTS` or `ADD COLUMN IF NOT EXISTS` to ensure the migration can be safely re-run.

2. **No Data Loss**: This migration only adds columns and tables; it does not modify or delete existing data.

3. **Backward Compatible**: All new columns have sensible defaults, ensuring existing application code continues to work.

4. **Transaction Safety**: Flyway executes migrations within a transaction, so if any statement fails, all changes are rolled back.

5. **Documentation**: All columns and tables have COMMENT statements for documentation purposes.

---

## DEPENDENCIES

This migration has no dependencies on application code. It must be executed before:
- Phase 1: Config & Security Infrastructure
- Phase 2: Auth Module
- All subsequent phases

---

## ROLLBACK PLAN

If rollback is needed, create `V17__rollback_realworld_compatibility_fields.sql`:

Note: Rollback should only be used in development. In production, create a new forward migration to fix issues.

```sql
-- WARNING: This will remove columns and tables. Use only in development.
-- DROP TABLE IF EXISTS login_history;
-- DROP TABLE IF EXISTS email_verification_tokens;
-- Then ALTER TABLE ... DROP COLUMN for each added column
```

---

## SUCCESS CRITERIA

Phase 0 is considered successful when:

1. ✅ Migration file executes without errors
2. ✅ All 35 new columns are added to existing tables
3. ✅ Both new tables (email_verification_tokens, login_history) are created
4. ✅ All 11 new indexes are created
5. ✅ All foreign key constraints are properly established
6. ✅ All verification queries return expected results
7. ✅ Result file is created with complete documentation
