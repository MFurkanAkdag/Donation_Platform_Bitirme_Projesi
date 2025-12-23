# PHASE 8.0: DONATION MODULE - EXTENDED

## Context and Background

You are working on the "Şeffaf Bağış Platformu" (Transparent Donation Platform), a graduation project for Fatih Sultan Mehmet Vakıf University.

**Project Stack:** Java 17, Spring Boot 3.x, PostgreSQL 15+, Redis, Maven

**Current Phase:** 8.0 - Donation Module - Extended (Recurring Donations, Bank Transfers)

**Previous Phases Completed:**
- Phase 1.0-6.0: All foundation modules ✅
- Phase 7.0: Donation Core Service & Controller ✅

---

## Objective

Extend the donation module with recurring donation subscriptions (monthly/weekly) and bank transfer (Havale/EFT) support with reference code matching system.

---

## What This Phase Will Solve

1. **Recurring Donations**: Users set up automatic monthly/weekly donations
2. **Card Token Storage**: Save card for recurring payments (via Iyzico in Phase 9)
3. **Bank Transfer Flow**: Generate reference codes for Havale/EFT matching
4. **Reference Expiry**: Auto-expire unused bank transfer references
5. **Manual Matching**: Admin can manually match bank transfers to donations

---

## Files to Create

### 1. DTOs - Request
**Location:** `src/main/java/com/seffafbagis/api/dto/request/donation/`

#### CreateRecurringDonationRequest.java
Fields:
- campaignId (optional, UUID - if null, general donation to organization)
- organizationId (required if campaignId is null)
- donationTypeId (optional, UUID)
- amount (required, positive)
- currency (default TRY)
- frequency (required: "weekly", "monthly", "yearly")

#### UpdateRecurringDonationRequest.java
Fields (all optional):
- amount
- frequency
- donationTypeId

#### InitiateBankTransferRequest.java
Fields:
- campaignId (required, UUID)
- donationTypeId (optional, UUID)
- amount (required, positive)
- senderName (optional)

#### MatchBankTransferRequest.java (Admin)
Fields:
- referenceCode (required)
- actualAmount (required) - may differ from expected
- senderName (optional)
- senderIban (optional)
- notes (optional)

---

### 2. DTOs - Response
**Location:** `src/main/java/com/seffafbagis/api/dto/response/donation/`

#### RecurringDonationResponse.java
Fields: id, campaignId, campaignTitle, organizationId, organizationName, donationTypeCode, amount, currency, frequency, nextPaymentDate, lastPaymentDate, totalDonated, paymentCount, status, createdAt

#### RecurringDonationListResponse.java
List of RecurringDonationResponse with summary (totalActive, totalMonthlyAmount)

#### BankTransferInfoResponse.java
Fields: referenceCode, bankName, branchName, accountHolder, iban, amount, expiresAt, instructions (text explaining how to make transfer)

#### BankTransferReferenceResponse.java
Fields: id, referenceCode, campaignTitle, expectedAmount, status, senderName, senderIban, createdAt, expiresAt, matchedDonationId

---

### 3. Services
**Location:** `src/main/java/com/seffafbagis/api/service/donation/`

#### RecurringDonationService.java

**User Methods:**
- createRecurringDonation(CreateRecurringDonationRequest) - create subscription
- getMyRecurringDonations() - list user's subscriptions
- getRecurringDonation(UUID id) - get details
- updateRecurringDonation(UUID id, UpdateRecurringDonationRequest)
- pauseRecurringDonation(UUID id) - status = "paused"
- resumeRecurringDonation(UUID id) - status = "active", recalculate next_payment_date
- cancelRecurringDonation(UUID id) - status = "cancelled"

**Internal Methods (for Scheduler in Phase 15):**
- getDueRecurringDonations(LocalDate date) - get subscriptions due for payment
- processRecurringPayment(UUID recurringDonationId) - create donation, process payment
- handlePaymentFailure(UUID recurringDonationId, String error) - increment failure count
- handlePaymentSuccess(UUID recurringDonationId, BigDecimal amount) - update stats

**Business Logic:**
1. When creating: set next_payment_date based on frequency
2. On pause: don't process payments
3. On resume: recalculate next_payment_date from today
4. On success: update last_payment_date, total_donated, payment_count, calculate next date
5. On failure: increment failure_count, if >= 3 then auto-pause and notify user
6. Card token stored after first successful payment (Phase 9)

**Next Payment Date Calculation:**
- Weekly: add 7 days
- Monthly: add 1 month (same day of month)
- Yearly: add 1 year

#### BankTransferService.java

**User Methods:**
- initiateBankTransfer(InitiateBankTransferRequest) - generate reference code and return bank info
- getMyBankTransferReferences() - list user's pending transfers
- getBankTransferStatus(String referenceCode) - check status
- cancelBankTransfer(String referenceCode) - cancel if still pending

**Admin Methods:**
- getPendingBankTransfers(Pageable) - list all pending for matching
- matchBankTransfer(MatchBankTransferRequest) - manually match to donation
- expireBankTransfer(String referenceCode) - manually expire

**Internal Methods (for Scheduler in Phase 15):**
- getExpiredReferences(LocalDateTime before) - get references past expiry
- markAsExpired(UUID referenceId) - update status to "expired"

**Business Logic:**
1. Generate unique reference code using Furkan's ReferenceCodeGenerator
2. Store bank account snapshot (JSONB) at creation time
3. Set expiry to 7 days from creation
4. On match:
   - Create Donation with status COMPLETED
   - Update reference status to "matched"
   - Link matched_donation_id
   - Update campaign stats
   - Generate receipt
   - Notify organization and donor

**Reference Code Format:** `SBP-YYYYMMDD-XXXXX`

---

### 4. Controllers
**Location:** `src/main/java/com/seffafbagis/api/controller/donation/`

#### RecurringDonationController.java

**User Endpoints (Authenticated):**
```
GET    /api/v1/recurring-donations/my           - My subscriptions
POST   /api/v1/recurring-donations              - Create subscription
GET    /api/v1/recurring-donations/{id}         - Get details
PUT    /api/v1/recurring-donations/{id}         - Update amount/frequency
PUT    /api/v1/recurring-donations/{id}/pause   - Pause
PUT    /api/v1/recurring-donations/{id}/resume  - Resume
DELETE /api/v1/recurring-donations/{id}         - Cancel
```

#### BankTransferController.java

**User Endpoints (Authenticated):**
```
POST   /api/v1/bank-transfers/initiate          - Get reference code & bank info
GET    /api/v1/bank-transfers/my                - My pending transfers
GET    /api/v1/bank-transfers/{code}            - Check status
DELETE /api/v1/bank-transfers/{code}            - Cancel
```

**Admin Endpoints:**
```
GET    /api/v1/admin/bank-transfers/pending     - Pending for matching
POST   /api/v1/admin/bank-transfers/match       - Manual match
PUT    /api/v1/admin/bank-transfers/{code}/expire - Manual expire
```

---

## Recurring Donation Flow

```
1. User creates recurring donation
           │
           ▼
2. First payment processed immediately (Phase 9)
           │
           ▼
3. Card token saved on success
           │
           ▼
4. Scheduler runs daily (Phase 15)
   - Find due subscriptions (next_payment_date <= today)
   - For each: create donation, charge saved card
           │
     ┌─────┴─────┐
     ▼           ▼
  Success     Failure
     │           │
     ▼           ▼
  Update      Increment failure_count
  stats       If >= 3: pause + notify
```

---

## Bank Transfer Flow

```
1. User requests bank transfer
           │
           ▼
2. POST /api/v1/bank-transfers/initiate
   - Generate unique reference code
   - Store bank account details snapshot
   - Set 7-day expiry
   - Return bank info + reference
           │
           ▼
3. User makes transfer at their bank
   (includes reference in description)
           │
           ▼
4. Organization checks bank statement
           │
           ▼
5. Admin matches via POST /admin/bank-transfers/match
   - Create completed donation
   - Update reference status
   - Generate receipt
   - Notify parties
           │
     OR    ▼
6. Reference expires after 7 days
   - Scheduler marks as expired
   - Notify user
```

---

## Bank Transfer Info Response Example

```json
{
  "referenceCode": "SBP-20240115-A7B3C",
  "bankName": "Türkiye İş Bankası",
  "branchName": "Kadıköy Şubesi",
  "accountHolder": "XYZ Vakfı",
  "iban": "TR33 0006 4000 0011 2345 6789 00",
  "amount": 500.00,
  "expiresAt": "2024-01-22T23:59:59",
  "instructions": "Lütfen havale/EFT yaparken açıklama kısmına 'SBP-20240115-A7B3C' referans kodunu yazınız. Bu kod 7 gün geçerlidir."
}
```

---

## Testing Requirements

### Unit Tests
- RecurringDonationServiceTest:
  - Test next payment date calculation for each frequency
  - Test pause/resume updates status correctly
  - Test failure count increment and auto-pause at 3

- BankTransferServiceTest:
  - Test reference code generation is unique
  - Test bank account snapshot stored correctly
  - Test matching creates completed donation

### Integration Tests
- Recurring donation lifecycle (create → process → update stats)
- Bank transfer matching flow

---

## Success Criteria

- [ ] All 4 request DTOs created
- [ ] All 4 response DTOs created
- [ ] RecurringDonationService with all methods
- [ ] Next payment date calculation correct for all frequencies
- [ ] Auto-pause after 3 failures implemented
- [ ] BankTransferService with all methods
- [ ] Reference code generation uses ReferenceCodeGenerator
- [ ] Bank account snapshot stored as JSONB
- [ ] Manual matching creates donation and updates stats
- [ ] All endpoints with proper authorization
- [ ] Admin endpoints protected
- [ ] All unit tests pass

---

## Result File Requirement

After completing this phase, create:
**Location:** `docs/Emir/step_results/phase_8.0_result.md`

Include:
1. Summary
2. Files created
3. API endpoints table
4. Recurring donation flow
5. Bank transfer flow
6. Reference code format confirmation
7. Testing results
8. Issues and resolutions
9. Next steps (Phase 9.0)
10. Success criteria checklist

---

## Important Notes

1. **Do NOT use ternary operators** - Use if-else
2. **Card token handling is Phase 9** - Just prepare the field
3. **Scheduler is Phase 15** - Just prepare the methods
4. **Use ReferenceCodeGenerator** from Furkan's utilities
5. **Bank account snapshot** preserves account info at transfer time

---

## Dependencies

From Furkan's work:
- ReferenceCodeGenerator
- SecurityUtils, ApiResponse

From previous phases:
- Campaign, Organization (Phase 2-5)
- DonationService (Phase 7)
- OrganizationBankAccount (Phase 2)
- RecurringDonation, BankTransferReference entities (Phase 6)

---

## Estimated Duration

2 days

---

## Next Phase

**Phase 9.0: Payment Module (Iyzico Integration)**
