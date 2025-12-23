# PHASE 7.0: DONATION MODULE - SERVICE & CONTROLLER (CORE)

## Context and Background

You are working on the "Şeffaf Bağış Platformu" (Transparent Donation Platform), a graduation project for Fatih Sultan Mehmet Vakıf University.

**Project Stack:** Java 17, Spring Boot 3.x, PostgreSQL 15+, Redis, Maven

**Current Phase:** 7.0 - Donation Module - Service & Controller (Core)

**Previous Phases Completed:**
- Phase 1.0-3.0: Category, Organization modules ✅
- Phase 4.0-5.0: Campaign module ✅
- Phase 6.0: Donation Entities & Repository ✅

---

## Objective

Implement core donation functionality: creating donations, handling payment flow integration point, generating digital receipts, updating campaign statistics, and providing donation history. Payment processing (Iyzico) will be completed in Phase 9.

---

## What This Phase Will Solve

1. **Donation Creation**: Users donate to campaigns (authenticated or anonymous)
2. **Receipt Generation**: Automatic digital receipt with unique number
3. **Campaign Stats Update**: Increment collected_amount and donor_count
4. **Donation History**: Users view their donation history
5. **Public Donor List**: Show non-anonymous donors on campaign page
6. **Refund Requests**: Users can request refunds

---

## Files to Create

### 1. DTOs - Request
**Location:** `src/main/java/com/seffafbagis/api/dto/request/donation/`

#### CreateDonationRequest.java
Fields:
- campaignId (required, UUID)
- donationTypeId (optional, UUID)
- amount (required, positive, min value from system settings)
- currency (default TRY)
- isAnonymous (default false)
- donorMessage (max 500)
- donorDisplayName (max 100, used if not anonymous)

#### ProcessPaymentRequest.java
- donationId (required, UUID)
- paymentMethod (required: CREDIT_CARD, BANK_TRANSFER)
- Card details will be handled in Phase 9

#### RefundRequest.java
- donationId (required, UUID)
- reason (required, max 500)

---

### 2. DTOs - Response
**Location:** `src/main/java/com/seffafbagis/api/dto/response/donation/`

#### DonationResponse.java
Fields: id, campaignId, campaignTitle, campaignSlug, donationTypeCode, donationTypeName, amount, currency, status, isAnonymous, donorMessage, donorDisplayName, createdAt

#### DonationDetailResponse.java
All DonationResponse fields plus: refundStatus, refundReason, transaction details (if exists), receipt info (if exists)

#### DonationListResponse.java
Paginated list wrapper with donations and summary stats (totalAmount, count)

#### DonationReceiptResponse.java
Fields: id, donationId, receiptNumber, receiptUrl, issuedAt, donorName, campaignTitle, organizationName, amount, donationDate

#### DonorListResponse.java
For public campaign donor list: donorDisplayName (or "Anonim Bağışçı"), amount, message, createdAt

---

### 3. Mapper
**Location:** `src/main/java/com/seffafbagis/api/dto/mapper/DonationMapper.java`

Methods:
- toEntity(CreateDonationRequest, Campaign, User, DonationType)
- toResponse(Donation)
- toDetailResponse(Donation)
- toDonorListResponse(Donation) - handle anonymous display
- toReceiptResponse(DonationReceipt, Donation)

---

### 4. Services
**Location:** `src/main/java/com/seffafbagis/api/service/donation/`

#### DonationService.java

**Public/User Methods:**
- createDonation(CreateDonationRequest) - creates with PENDING status
- completeDonation(UUID donationId) - called after payment success, updates stats
- failDonation(UUID donationId, String errorMessage) - called after payment failure
- getDonationById(UUID id) - get donation details
- getMyDonations(Pageable) - current user's donation history
- getMyDonationDetail(UUID id) - with receipt and transaction
- requestRefund(RefundRequest) - create refund request

**Campaign Related:**
- getCampaignDonors(UUID campaignId, Pageable) - public donor list (non-anonymous only)
- getCampaignDonations(UUID campaignId, Pageable) - for organization owner

**Internal Methods (called by PaymentService in Phase 9):**
- updateDonationStatus(UUID donationId, DonationStatus status)
- linkTransaction(UUID donationId, Transaction transaction)

**Business Logic:**
1. Validate campaign is ACTIVE
2. Validate amount >= minimum (from system settings)
3. If authenticated, link donor_id; if anonymous or not logged in, donor_id = null
4. On completeDonation:
   - Update status to COMPLETED
   - Call CampaignService.incrementDonationStats()
   - Generate receipt via DonationReceiptService
   - Create notification for organization
   - Create notification for donor (if not anonymous)
5. Refund only allowed within 14 days and status = COMPLETED

#### DonationReceiptService.java

**Methods:**
- generateReceipt(Donation donation) - create receipt with unique number
- getReceiptByDonation(UUID donationId)
- getReceiptByNumber(String receiptNumber)
- generateReceiptPdf(UUID receiptId) - optional, generate PDF

**Receipt Number Generation:**
Format: `RCPT-YYYY-NNNNNN`
- Query max receipt number for current year
- Increment by 1
- Pad with zeros to 6 digits
- Example: RCPT-2024-000001, RCPT-2024-000002

---

### 5. Controllers
**Location:** `src/main/java/com/seffafbagis/api/controller/donation/`

#### DonationController.java

**Public Endpoints:**
```
GET  /api/v1/donations/campaign/{campaignId}/donors  - Public donor list
```

**Authenticated User Endpoints:**
```
POST /api/v1/donations                    - Create donation (initiate)
GET  /api/v1/donations/my                 - My donation history
GET  /api/v1/donations/my/{id}            - My donation detail
GET  /api/v1/donations/{id}/receipt       - Get receipt
POST /api/v1/donations/{id}/refund        - Request refund
```

**Organization Endpoints (FOUNDATION role):**
```
GET  /api/v1/donations/organization       - Donations to my organization
GET  /api/v1/donations/campaign/{id}      - Donations to specific campaign
```

**Note:** Anonymous donations (without login) will be handled separately or via a guest checkout flow.

---

## Donation Flow

```
1. User selects campaign, enters amount
           │
           ▼
2. POST /api/v1/donations (CreateDonationRequest)
           │
           ▼
3. DonationService.createDonation()
   - Validate campaign is ACTIVE
   - Validate amount >= minimum
   - Create Donation with status = PENDING
   - Return donation ID
           │
           ▼
4. Frontend redirects to payment (Phase 9)
           │
           ▼
5. Payment Success → DonationService.completeDonation()
   - Status = COMPLETED
   - Update campaign stats
   - Generate receipt
   - Send notifications
           │
           ▼
6. Payment Failed → DonationService.failDonation()
   - Status = FAILED
   - Log error
```

---

## Campaign Stats Update

When donation completes, call CampaignService method (from Phase 5):

```java
// In CampaignService (already exists from Phase 5)
public void incrementDonationStats(UUID campaignId, BigDecimal amount) {
    Campaign campaign = findById(campaignId);
    campaign.setCollectedAmount(campaign.getCollectedAmount().add(amount));
    campaign.setDonorCount(campaign.getDonorCount() + 1);
    campaignRepository.save(campaign);
    
    // Check if target reached
    if (campaign.getCollectedAmount().compareTo(campaign.getTargetAmount()) >= 0) {
        // Optionally auto-complete or notify
    }
}
```

---

## Testing Requirements

### Unit Tests
**Location:** `src/test/java/com/seffafbagis/api/service/donation/`

- DonationServiceTest:
  - Test createDonation validates campaign is ACTIVE
  - Test createDonation with minimum amount validation
  - Test completeDonation updates campaign stats
  - Test anonymous donation has null donor_id
  - Test refund request validates 14-day window

- DonationReceiptServiceTest:
  - Test receipt number generation is sequential
  - Test receipt number format is correct

### Integration Tests
- Full donation flow (create → complete → verify stats)
- Donor list excludes anonymous donations

---

## Success Criteria

- [ ] All 3 request DTOs created with validation
- [ ] All 5 response DTOs created
- [ ] DonationMapper handles all conversions
- [ ] DonationService.createDonation creates PENDING donation
- [ ] DonationService.completeDonation updates stats and generates receipt
- [ ] DonationReceiptService generates sequential receipt numbers
- [ ] Campaign collected_amount updates correctly
- [ ] Anonymous donations handled (null donor_id, display "Anonim Bağışçı")
- [ ] Refund request validates time window
- [ ] All endpoints working with proper authorization
- [ ] All unit tests pass

---

## Result File Requirement

After completing this phase, create:
**Location:** `docs/Emir/step_results/phase_7.0_result.md`

Include:
1. Summary
2. Files created
3. API endpoints table
4. Donation flow description
5. Receipt number format confirmation
6. Testing results
7. Issues and resolutions
8. Next steps (Phase 8.0)
9. Success criteria checklist

---

## Important Notes

1. **Do NOT use ternary operators** - Use if-else
2. **Payment processing is Phase 9** - This phase only creates the donation record
3. **Use CampaignService** from Phase 5 for stats update
4. **Receipt numbers must be unique and sequential per year**
5. **Handle both authenticated and anonymous donations**

---

## Dependencies

From Furkan's work:
- SecurityUtils, ApiResponse, AuditLogService
- NotificationService (or prepare interface for Phase 13)

From previous phases:
- Campaign, CampaignService (Phase 4, 5)
- DonationType (Phase 1)
- Donation entities and repositories (Phase 6)

---

## Estimated Duration

3 days

---

## Next Phase

**Phase 8.0: Donation Module - Extended (Recurring Donations, Bank Transfers)**
