# PHASE 10.0: EVIDENCE MODULE

## Context and Background

You are working on the "Şeffaf Bağış Platformu" (Transparent Donation Platform), a graduation project for Fatih Sultan Mehmet Vakıf University.

**Project Stack:** Java 17, Spring Boot 3.x, PostgreSQL 15+, Redis, Maven

**Current Phase:** 10.0 - Evidence Module

**Previous Phases Completed:**
- Phase 1.0-6.0: Foundation modules ✅
- Phase 7.0-9.0: Donation & Payment modules ✅

---

## Objective

Implement the evidence (kanıt) system where organizations upload spending proofs after campaigns complete. This is the core transparency mechanism - organizations must document how donations were spent with invoices, receipts, photos, and reports.

---

## What This Phase Will Solve

1. **Evidence Upload**: Organizations upload spending documentation
2. **Multi-Document Support**: Each evidence can have multiple files
3. **Admin Review**: Admin approves or rejects evidence
4. **Deadline Tracking**: Evidence must be uploaded within deadline
5. **Transparency Impact**: Evidence status affects transparency score (Phase 11)

---

## Database Schema Reference

### evidences table
```sql
CREATE TABLE evidences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    campaign_id UUID NOT NULL REFERENCES campaigns(id),
    evidence_type evidence_type NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    amount_spent DECIMAL(12,2),
    spend_date DATE,
    vendor_name VARCHAR(255),
    vendor_tax_number VARCHAR(20),
    invoice_number VARCHAR(100),
    status evidence_status DEFAULT 'pending',
    reviewed_by UUID REFERENCES users(id),
    reviewed_at TIMESTAMPTZ,
    rejection_reason TEXT,
    uploaded_by UUID REFERENCES users(id),
    uploaded_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
```

### evidence_documents table
```sql
CREATE TABLE evidence_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    evidence_id UUID NOT NULL REFERENCES evidences(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_size INTEGER,
    mime_type VARCHAR(100),
    is_primary BOOLEAN DEFAULT FALSE,
    uploaded_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
```

### Enum types
```sql
CREATE TYPE evidence_type AS ENUM ('invoice', 'receipt', 'photo', 'video', 'delivery_report', 'other');
CREATE TYPE evidence_status AS ENUM ('pending', 'approved', 'rejected');
```

---

## Files to Create

### 1. Enums
**Location:** `src/main/java/com/seffafbagis/api/enums/`

#### EvidenceType.java
Values: INVOICE, RECEIPT, PHOTO, VIDEO, DELIVERY_REPORT, OTHER

#### EvidenceStatus.java
Values: PENDING, APPROVED, REJECTED

---

### 2. Entities
**Location:** `src/main/java/com/seffafbagis/api/entity/evidence/`

#### Evidence.java
- Extend BaseEntity
- ManyToOne: Campaign, User (uploadedBy), User (reviewedBy)
- OneToMany: EvidenceDocument (cascade, orphanRemoval)

Fields: evidenceType, title, description, amountSpent, spendDate, vendorName, vendorTaxNumber, invoiceNumber, status, reviewedAt, rejectionReason, uploadedAt

#### EvidenceDocument.java
- Extend BaseEntity
- ManyToOne: Evidence

Fields: fileName, fileUrl, fileSize, mimeType, isPrimary, uploadedAt

---

### 3. Repositories
**Location:** `src/main/java/com/seffafbagis/api/repository/`

#### EvidenceRepository.java
Key methods:
- findByCampaignId(UUID campaignId)
- findByCampaignIdOrderByUploadedAtDesc(UUID campaignId)
- findByCampaignIdAndStatus(UUID campaignId, EvidenceStatus status)
- findByStatus(EvidenceStatus status, Pageable pageable)
- countByCampaignIdAndStatus(UUID campaignId, EvidenceStatus status)
- sumAmountSpentByCampaignIdAndStatus(UUID campaignId, EvidenceStatus status) - @Query
- findByUploadedBy(UUID userId, Pageable pageable)

#### EvidenceDocumentRepository.java
- findByEvidenceId(UUID evidenceId)
- findByEvidenceIdAndIsPrimaryTrue(UUID evidenceId)
- countByEvidenceId(UUID evidenceId)
- deleteByEvidenceId(UUID evidenceId)

---

### 4. DTOs - Request
**Location:** `src/main/java/com/seffafbagis/api/dto/request/evidence/`

#### CreateEvidenceRequest.java
Fields:
- campaignId (required, UUID)
- evidenceType (required)
- title (required, max 255)
- description (optional)
- amountSpent (required, positive)
- spendDate (required, not future)
- vendorName (optional, max 255)
- vendorTaxNumber (optional, max 20)
- invoiceNumber (optional, max 100)
- documents (required, at least one) - List of:
  - fileName, fileUrl, fileSize, mimeType, isPrimary

#### UpdateEvidenceRequest.java
Same fields, all optional except documents cannot be empty if provided

#### ReviewEvidenceRequest.java
Fields:
- approved (required, boolean)
- rejectionReason (required if approved = false, max 500)

---

### 5. DTOs - Response
**Location:** `src/main/java/com/seffafbagis/api/dto/response/evidence/`

#### EvidenceResponse.java
Fields: id, campaignId, campaignTitle, evidenceType, title, description, amountSpent, spendDate, vendorName, invoiceNumber, status, uploadedAt, uploadedByName

#### EvidenceDetailResponse.java
All EvidenceResponse fields plus:
- vendorTaxNumber
- documents (list)
- reviewedAt, reviewedByName, rejectionReason

#### EvidenceListResponse.java
List wrapper with summary: totalCount, pendingCount, approvedCount, rejectedCount, totalAmountApproved

#### EvidenceDocumentResponse.java
Fields: id, fileName, fileUrl, fileSize, mimeType, isPrimary, uploadedAt

#### CampaignEvidenceSummaryResponse.java
For campaign detail page: totalEvidences, approvedCount, pendingCount, totalAmountDocumented, deadline, daysRemaining, isOverdue

---

### 6. Mapper
**Location:** `src/main/java/com/seffafbagis/api/dto/mapper/EvidenceMapper.java`

Methods:
- toEntity(CreateEvidenceRequest, Campaign, User)
- toResponse(Evidence)
- toDetailResponse(Evidence)
- toDocumentResponse(EvidenceDocument)
- toSummaryResponse(Campaign, evidence stats)

---

### 7. Services
**Location:** `src/main/java/com/seffafbagis/api/service/evidence/`

#### EvidenceService.java

**Organization Methods:**
- uploadEvidence(CreateEvidenceRequest) - upload new evidence
- updateEvidence(UUID id, UpdateEvidenceRequest) - update pending evidence
- deleteEvidence(UUID id) - delete pending evidence only
- getMyEvidences(UUID campaignId) - organization's evidences for campaign
- getEvidenceDetail(UUID id)

**Admin Methods:**
- getPendingEvidences(Pageable) - all pending for review
- reviewEvidence(UUID id, ReviewEvidenceRequest) - approve or reject
- getEvidencesByStatus(EvidenceStatus status, Pageable)

**Public Methods:**
- getCampaignEvidences(UUID campaignId) - approved evidences for public view
- getCampaignEvidenceSummary(UUID campaignId) - summary stats

**Internal Methods (for TransparencyScore in Phase 11):**
- getEvidenceStats(UUID organizationId) - counts by status
- isDeadlineMissed(UUID campaignId) - check if deadline passed without evidence

**Business Logic:**
1. Only organization that owns campaign can upload evidence
2. Evidence can only be uploaded for COMPLETED campaigns
3. Cannot update/delete approved or rejected evidence
4. On approval: trigger transparency score update (Phase 11)
5. On rejection: allow resubmission, trigger score update
6. Track if evidence uploaded before deadline

#### EvidenceDocumentService.java

**Methods:**
- addDocument(UUID evidenceId, document data)
- removeDocument(UUID documentId)
- setPrimaryDocument(UUID documentId)

---

### 8. Controller
**Location:** `src/main/java/com/seffafbagis/api/controller/evidence/`

#### EvidenceController.java

**Public Endpoints:**
```
GET /api/v1/evidences/campaign/{campaignId}         - Approved evidences (public)
GET /api/v1/evidences/campaign/{campaignId}/summary - Evidence summary
```

**Organization Endpoints (FOUNDATION role):**
```
POST   /api/v1/evidences                    - Upload evidence
GET    /api/v1/evidences/my/{campaignId}    - My evidences for campaign
GET    /api/v1/evidences/{id}               - Evidence detail
PUT    /api/v1/evidences/{id}               - Update evidence
DELETE /api/v1/evidences/{id}               - Delete evidence
POST   /api/v1/evidences/{id}/documents     - Add document
DELETE /api/v1/evidences/{id}/documents/{docId} - Remove document
```

**Admin Endpoints:**
```
GET  /api/v1/admin/evidences/pending        - Pending for review
POST /api/v1/admin/evidences/{id}/review    - Approve or reject
GET  /api/v1/admin/evidences                - All evidences with filters
```

---

## Evidence Workflow

```
Campaign COMPLETED
        │
        ▼
Evidence deadline starts (campaign.evidence_deadline_days)
        │
        ▼
Organization uploads evidence(s)
        │
        ▼
Status = PENDING
        │
        ▼
Admin reviews
        │
   ┌────┴────┐
   ▼         ▼
APPROVED   REJECTED
   │         │
   ▼         ▼
Score +5   Score -5
           Can resubmit
           
If deadline passed without evidence:
   │
   ▼
Score -10 (handled by scheduler in Phase 15)
```

---

## Evidence Deadline Calculation

```java
// Campaign completion date + evidence_deadline_days
LocalDateTime deadline = campaign.getCompletedAt()
    .plusDays(campaign.getEvidenceDeadlineDays());

boolean isOverdue = LocalDateTime.now().isAfter(deadline);
long daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), deadline);
```

---

## Testing Requirements

### Unit Tests
- EvidenceServiceTest:
  - Test upload only for COMPLETED campaigns
  - Test cannot update approved evidence
  - Test deadline calculation
  - Test only campaign owner can upload

### Integration Tests
- Full evidence lifecycle (upload → review → approve)
- Rejection and resubmission flow

---

## Success Criteria

- [ ] EvidenceType and EvidenceStatus enums created
- [ ] Evidence and EvidenceDocument entities created
- [ ] Both repositories with custom queries
- [ ] All request DTOs with validation
- [ ] All response DTOs created
- [ ] EvidenceMapper handles all conversions
- [ ] EvidenceService with all methods
- [ ] Only campaign owner can upload evidence
- [ ] Only COMPLETED campaigns accept evidence
- [ ] Cannot modify approved/rejected evidence
- [ ] Admin review flow works
- [ ] Deadline calculation correct
- [ ] All endpoints with proper authorization
- [ ] All unit tests pass

---

## Result File Requirement

After completing this phase, create:
**Location:** `docs/Emir/step_results/phase_10.0_result.md`

Include:
1. Summary
2. Files created
3. API endpoints table
4. Evidence workflow description
5. Deadline calculation logic
6. Testing results
7. Issues and resolutions
8. Next steps (Phase 11.0)
9. Success criteria checklist

---

## Important Notes

1. **Do NOT use ternary operators** - Use if-else
2. **Transparency score update is Phase 11** - Just prepare the hooks
3. **FileStorageService** from Furkan handles actual file upload
4. **Only COMPLETED campaigns** can have evidence
5. **Deadline is campaign-specific** (evidence_deadline_days field)

---

## Dependencies

From Furkan's work:
- FileStorageService (for document storage)
- SecurityUtils, ApiResponse
- AuditLogService

From previous phases:
- Campaign, CampaignService (Phase 4, 5)
- Organization (Phase 2)

---

## Estimated Duration

3 days

---

## Next Phase

**Phase 11.0: Transparency Score Module**
