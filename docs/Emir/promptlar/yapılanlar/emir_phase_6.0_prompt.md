# PHASE 6.0: DONATION MODULE - ENTITIES & REPOSITORY

## Context and Background

You are working on the "Şeffaf Bağış Platformu" (Transparent Donation Platform), a graduation project for Fatih Sultan Mehmet Vakıf University.

**Project Stack:** Java 17, Spring Boot 3.x, PostgreSQL 15+, Redis, Maven

**Current Phase:** 6.0 - Donation Module - Entities & Repository

**Previous Phases Completed:**
- Phase 1.0: Category & Donation Type Module ✅
- Phase 2.0: Organization Module - Entities & Repository ✅
- Phase 3.0: Organization Module - Service & Controller ✅
- Phase 4.0: Campaign Module - Entities & Repository ✅
- Phase 5.0: Campaign Module - Service & Controller ✅

---

## Objective

Create all Donation-related entities and repository interfaces. This includes the core donation record, payment transactions, digital receipts, recurring donation subscriptions, and bank transfer reference system.

---

## What This Phase Will Solve

1. **Donation Tracking**: Record all donations with amount, status, donor info
2. **Payment Transactions**: Store payment provider responses and details
3. **Digital Receipts**: Generate and store donation receipts
4. **Recurring Donations**: Monthly/weekly subscription donations
5. **Bank Transfers**: Reference code system for matching Havale/EFT payments

---

## Database Schema Reference

### donations table
```sql
CREATE TABLE donations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    campaign_id UUID NOT NULL REFERENCES campaigns(id),
    donor_id UUID REFERENCES users(id),       -- NULL for anonymous
    donation_type_id UUID REFERENCES donation_types(id),
    amount DECIMAL(12,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'TRY',
    status donation_status DEFAULT 'pending',
    is_anonymous BOOLEAN DEFAULT FALSE,
    donor_message VARCHAR(500),
    donor_display_name VARCHAR(100),
    ip_address INET,
    user_agent TEXT,
    source VARCHAR(20) DEFAULT 'web',
    refund_status VARCHAR(20) DEFAULT 'none',
    refund_reason TEXT,
    refund_requested_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
```

### transactions table
```sql
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    donation_id UUID NOT NULL REFERENCES donations(id),
    payment_method payment_method NOT NULL,
    payment_provider VARCHAR(50) NOT NULL,    -- 'iyzico', 'paytr'
    provider_transaction_id VARCHAR(255),
    provider_payment_id VARCHAR(255),
    amount DECIMAL(12,2) NOT NULL,
    fee_amount DECIMAL(12,2) DEFAULT 0,
    net_amount DECIMAL(12,2),
    refunded_amount DECIMAL(12,2),
    refunded_at TIMESTAMPTZ,
    installment_count INTEGER DEFAULT 1,
    currency VARCHAR(3) DEFAULT 'TRY',
    status VARCHAR(50) NOT NULL,
    error_code VARCHAR(50),
    error_message TEXT,
    card_last_four VARCHAR(4),
    card_brand VARCHAR(50),
    is_3d_secure BOOLEAN DEFAULT FALSE,
    raw_response JSONB,
    processed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
```

### donation_receipts table
```sql
CREATE TABLE donation_receipts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    donation_id UUID UNIQUE NOT NULL REFERENCES donations(id),
    receipt_number VARCHAR(50) UNIQUE NOT NULL,
    receipt_url VARCHAR(500),
    issued_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
```

### recurring_donations table
```sql
CREATE TABLE recurring_donations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    donor_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    campaign_id UUID REFERENCES campaigns(id),
    organization_id UUID REFERENCES organizations(id),
    donation_type_id UUID REFERENCES donation_types(id),
    amount DECIMAL(12,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'TRY',
    frequency VARCHAR(20) NOT NULL,           -- 'weekly', 'monthly', 'yearly'
    next_payment_date DATE NOT NULL,
    last_payment_date DATE,
    total_donated DECIMAL(12,2) DEFAULT 0,
    payment_count INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'active',      -- 'active', 'paused', 'cancelled'
    card_token VARCHAR(255),
    failure_count INTEGER DEFAULT 0,
    last_error_message TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
```

### bank_transfer_references table
```sql
CREATE TABLE bank_transfer_references (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reference_code VARCHAR(20) UNIQUE NOT NULL,
    campaign_id UUID REFERENCES campaigns(id),
    organization_id UUID REFERENCES organizations(id),
    bank_account_id UUID REFERENCES organization_bank_accounts(id),
    donor_id UUID REFERENCES users(id),
    expected_amount DECIMAL(12,2),
    donation_type_id UUID REFERENCES donation_types(id),
    sender_name VARCHAR(255),
    sender_iban VARCHAR(34),
    bank_account_snapshot JSONB,
    status VARCHAR(20) DEFAULT 'pending',     -- 'pending', 'matched', 'expired'
    matched_donation_id UUID REFERENCES donations(id),
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
```

### Enum types
```sql
CREATE TYPE donation_status AS ENUM ('pending', 'completed', 'failed', 'refunded');
CREATE TYPE payment_method AS ENUM ('credit_card', 'bank_transfer', 'mobile_payment');
```

---

## Files to Create

### 1. Enums
**Location:** `src/main/java/com/seffafbagis/api/enums/`

#### DonationStatus.java
Values: PENDING, COMPLETED, FAILED, REFUNDED

#### PaymentMethod.java
Values: CREDIT_CARD, BANK_TRANSFER, MOBILE_PAYMENT

---

### 2. Entities
**Location:** `src/main/java/com/seffafbagis/api/entity/donation/`

#### Donation.java
- Extend BaseEntity
- ManyToOne: Campaign, User (donor - nullable), DonationType
- OneToOne: Transaction, DonationReceipt

Fields: amount, currency, status, isAnonymous, donorMessage, donorDisplayName, ipAddress, userAgent, source, refundStatus, refundReason, refundRequestedAt

Note: Use `@Column(columnDefinition = "inet")` for ipAddress

#### Transaction.java
- Extend BaseEntity
- OneToOne with Donation

Fields: paymentMethod (enum), paymentProvider, providerTransactionId, providerPaymentId, amount, feeAmount, netAmount, refundedAmount, refundedAt, installmentCount, currency, status, errorCode, errorMessage, cardLastFour, cardBrand, is3dSecure, rawResponse (JSONB), processedAt

For JSONB field:
```java
@Type(JsonType.class)
@Column(columnDefinition = "jsonb")
private Map<String, Object> rawResponse;
```

#### DonationReceipt.java
- Extend BaseEntity
- OneToOne with Donation (unique constraint)

Fields: receiptNumber (unique), receiptUrl, issuedAt

#### RecurringDonation.java
- Extend BaseEntity
- ManyToOne: User (donor), Campaign (nullable), Organization (nullable), DonationType

Fields: amount, currency, frequency, nextPaymentDate, lastPaymentDate, totalDonated, paymentCount, status, cardToken, failureCount, lastErrorMessage

Frequency values: "weekly", "monthly", "yearly"
Status values: "active", "paused", "cancelled"

#### BankTransferReference.java
- Extend BaseEntity
- ManyToOne: Campaign (nullable), Organization (nullable), OrganizationBankAccount, User (donor - nullable), DonationType (nullable), Donation (matched - nullable)

Fields: referenceCode (unique), expectedAmount, senderName, senderIban, bankAccountSnapshot (JSONB), status, expiresAt

Status values: "pending", "matched", "expired"

---

### 3. Repositories
**Location:** `src/main/java/com/seffafbagis/api/repository/`

#### DonationRepository.java
Key methods:
- findByCampaignId(UUID campaignId, Pageable pageable)
- findByCampaignIdAndStatus(UUID campaignId, DonationStatus status)
- findByDonorId(UUID donorId, Pageable pageable)
- findByDonorIdOrderByCreatedAtDesc(UUID donorId)
- sumAmountByCampaignIdAndStatus(UUID campaignId, DonationStatus status) - @Query for SUM
- countByCampaignIdAndStatus(UUID campaignId, DonationStatus status)
- findByCampaignIdAndIsAnonymousFalseAndStatus(UUID campaignId, DonationStatus status, Pageable pageable) - for public donor list
- findTopDonationsByCampaignId(UUID campaignId, Pageable pageable) - order by amount DESC

#### TransactionRepository.java
- findByDonationId(UUID donationId)
- findByProviderTransactionId(String providerTransactionId)
- findByStatus(String status, Pageable pageable)
- findByPaymentProviderAndStatus(String provider, String status)

#### DonationReceiptRepository.java
- findByDonationId(UUID donationId)
- findByReceiptNumber(String receiptNumber)
- existsByReceiptNumber(String receiptNumber)
- findMaxReceiptNumberByYear(int year) - @Query for getting last receipt number of year

#### RecurringDonationRepository.java
- findByDonorId(UUID donorId)
- findByDonorIdAndStatus(UUID donorId, String status)
- findByStatus(String status)
- findByStatusAndNextPaymentDateLessThanEqual(String status, LocalDate date) - for scheduler
- findByCampaignId(UUID campaignId)
- findByOrganizationId(UUID organizationId)
- countByDonorIdAndStatus(UUID donorId, String status)

#### BankTransferReferenceRepository.java
- findByReferenceCode(String referenceCode)
- findByReferenceCodeAndStatus(String referenceCode, String status)
- findByStatus(String status)
- findByStatusAndExpiresAtBefore(String status, LocalDateTime dateTime) - for expiry scheduler
- findByDonorId(UUID donorId)
- findByCampaignId(UUID campaignId)
- existsByReferenceCode(String referenceCode)

---

## Entity Relationships Diagram

```
Campaign (1) ──── (N) Donation
                      │
    ┌─────────────────┼─────────────────┐
    │                 │                 │
    ▼                 ▼                 ▼
User (donor)    DonationType      Transaction (1:1)
                                       │
                                       │
                              DonationReceipt (1:1)


User (1) ──── (N) RecurringDonation ──── Campaign (optional)
                                    ──── Organization (optional)

BankTransferReference ──── Campaign (optional)
                      ──── Organization (optional)
                      ──── Donation (matched, optional)
```

---

## Important Implementation Notes

1. **Anonymous Donations**: donor_id can be NULL for anonymous donations
2. **JSONB Fields**: Use Hibernate Types for rawResponse and bankAccountSnapshot
3. **Decimal Precision**: Use BigDecimal for all monetary fields
4. **Receipt Number Format**: RCPT-YYYY-NNNNNN (e.g., RCPT-2024-000001)
5. **Reference Code Format**: SBP-YYYYMMDD-XXXXX (use Furkan's ReferenceCodeGenerator)
6. **Indexes**: Add indexes on status, campaign_id, donor_id, created_at

---

## Hibernate Types Dependency

For JSONB support, ensure this is in pom.xml:
```xml
<dependency>
    <groupId>io.hypersistence</groupId>
    <artifactId>hypersistence-utils-hibernate-63</artifactId>
    <version>3.7.0</version>
</dependency>
```

---

## Testing Requirements

### Repository Tests
**Location:** `src/test/java/com/seffafbagis/api/repository/`

- DonationRepositoryTest:
  - Test sumAmountByCampaignIdAndStatus returns correct sum
  - Test anonymous donations handled correctly

- RecurringDonationRepositoryTest:
  - Test findByStatusAndNextPaymentDateLessThanEqual for scheduler

- BankTransferReferenceRepositoryTest:
  - Test findByStatusAndExpiresAtBefore for expiry

### Entity Tests
- Test JSONB serialization/deserialization
- Test BigDecimal precision

---

## Success Criteria

- [ ] DonationStatus enum created
- [ ] PaymentMethod enum created
- [ ] Donation entity with all relationships
- [ ] Transaction entity with JSONB rawResponse
- [ ] DonationReceipt entity with unique receipt_number
- [ ] RecurringDonation entity
- [ ] BankTransferReference entity with JSONB bankAccountSnapshot
- [ ] All 5 repositories with custom query methods
- [ ] SUM query for donation amounts works
- [ ] JSONB fields serialize correctly
- [ ] Proper indexes defined
- [ ] Application starts without errors
- [ ] All repository tests pass

---

## Result File Requirement

After completing this phase, create:
**Location:** `docs/Emir/step_results/phase_6.0_result.md`

Include:
1. Summary
2. Files created
3. Entity relationships
4. JSONB implementation notes
5. Testing results
6. Issues and resolutions
7. Next steps (Phase 7.0)
8. Success criteria checklist

---

## Important Notes

1. **Do NOT use ternary operators** - Use if-else
2. **Use BigDecimal** for all monetary amounts
3. **Handle nullable relationships** for anonymous donations
4. **JSONB requires Hibernate Types** dependency
5. **No services/controllers in this phase** - Only entities and repositories

---

## Dependencies

From Furkan's work:
- BaseEntity
- User entity

From previous phases:
- Campaign (Phase 4)
- Organization, OrganizationBankAccount (Phase 2)
- DonationType (Phase 1)

---

## Estimated Duration

2 days

---

## Next Phase

**Phase 7.0: Donation Module - Service & Controller (Core)**
