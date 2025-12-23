# PHASE 12.0: APPLICATION MODULE (BENEFICIARY APPLICATIONS)

## Context and Background

You are working on the "Şeffaf Bağış Platformu" (Transparent Donation Platform), a graduation project for Fatih Sultan Mehmet Vakıf University.

**Project Stack:** Java 17, Spring Boot 3.x, PostgreSQL 15+, Redis, Maven

**Current Phase:** 12.0 - Application Module

**Previous Phases Completed:**
- Phase 1.0-9.0: Foundation, Donation, Payment modules ✅
- Phase 10.0-11.0: Evidence & Transparency Score modules ✅

---

## Objective

Implement the beneficiary application system where people in need can apply for aid. Organizations review applications and can link approved applicants to appropriate campaigns.

---

## What This Phase Will Solve

1. **Aid Applications**: Beneficiaries submit help requests
2. **Document Upload**: Support documents (ID, income proof, medical reports)
3. **Application Review**: Organizations/admins review and approve
4. **Campaign Matching**: Link approved applications to campaigns
5. **Status Tracking**: Applicants track their application status

---

## Database Schema Reference

### applications table
```sql
CREATE TABLE applications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    applicant_id UUID REFERENCES users(id),
    category_id UUID REFERENCES categories(id),
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    requested_amount DECIMAL(12,2),
    status application_status DEFAULT 'pending',
    location_city VARCHAR(100),
    location_district VARCHAR(100),
    household_size INTEGER,
    urgency_level INTEGER DEFAULT 1,          -- 1-5
    assigned_organization_id UUID REFERENCES organizations(id),
    assigned_campaign_id UUID REFERENCES campaigns(id),
    reviewed_by UUID REFERENCES users(id),
    reviewed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
```

### application_documents table
```sql
CREATE TABLE application_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    document_type VARCHAR(100) NOT NULL,      -- 'id_card', 'income_proof', 'medical_report'
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    is_verified BOOLEAN DEFAULT FALSE,
    uploaded_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
```

### application_status enum
```sql
CREATE TYPE application_status AS ENUM ('pending', 'in_review', 'approved', 'rejected', 'completed');
```

---

## Files to Create

### 1. Enum
**Location:** `src/main/java/com/seffafbagis/api/enums/ApplicationStatus.java`

Values: PENDING, IN_REVIEW, APPROVED, REJECTED, COMPLETED

---

### 2. Entities
**Location:** `src/main/java/com/seffafbagis/api/entity/application/`

#### Application.java
- Extend BaseEntity
- ManyToOne: User (applicant), Category, Organization (assigned), Campaign (assigned), User (reviewedBy)
- OneToMany: ApplicationDocument (cascade, orphanRemoval)

Fields: title, description, requestedAmount, status, locationCity, locationDistrict, householdSize, urgencyLevel (1-5), reviewedAt

#### ApplicationDocument.java
- Extend BaseEntity
- ManyToOne: Application

Fields: documentType, fileName, fileUrl, isVerified, uploadedAt

---

### 3. Repositories
**Location:** `src/main/java/com/seffafbagis/api/repository/`

#### ApplicationRepository.java
Key methods:
- findByApplicantId(UUID applicantId)
- findByApplicantIdOrderByCreatedAtDesc(UUID applicantId)
- findByStatus(ApplicationStatus status, Pageable pageable)
- findByStatusOrderByUrgencyLevelDescCreatedAtAsc(ApplicationStatus status, Pageable)
- findByCategoryId(UUID categoryId, Pageable pageable)
- findByAssignedOrganizationId(UUID organizationId, Pageable pageable)
- findByAssignedCampaignId(UUID campaignId)
- findByLocationCity(String city, Pageable pageable)
- countByStatus(ApplicationStatus status)
- countByAssignedOrganizationIdAndStatus(UUID orgId, ApplicationStatus status)

#### ApplicationDocumentRepository.java
- findByApplicationId(UUID applicationId)
- findByApplicationIdAndDocumentType(UUID applicationId, String documentType)
- countByApplicationId(UUID applicationId)
- deleteByApplicationId(UUID applicationId)

---

### 4. DTOs - Request
**Location:** `src/main/java/com/seffafbagis/api/dto/request/application/`

#### CreateApplicationRequest.java
Fields:
- categoryId (required, UUID)
- title (required, max 255)
- description (required)
- requestedAmount (optional, positive)
- locationCity (optional)
- locationDistrict (optional)
- householdSize (optional, 1-20)
- urgencyLevel (optional, 1-5, default 1)
- documents (optional) - List of:
  - documentType (required: id_card, income_proof, medical_report, utility_bill, other)
  - fileName, fileUrl

#### UpdateApplicationRequest.java
Same fields, all optional, only for PENDING status

#### ReviewApplicationRequest.java
Fields:
- status (required: IN_REVIEW, APPROVED, REJECTED)
- rejectionReason (required if REJECTED)
- assignedOrganizationId (optional, for routing)
- notes (optional)

#### AssignToCampaignRequest.java
Fields:
- campaignId (required, UUID)
- notes (optional)

#### CompleteApplicationRequest.java
Fields:
- completionNotes (optional)
- actualAmountProvided (optional)

---

### 5. DTOs - Response
**Location:** `src/main/java/com/seffafbagis/api/dto/response/application/`

#### ApplicationResponse.java
Fields: id, applicantId, categoryId, categoryName, title, description, requestedAmount, status, locationCity, locationDistrict, householdSize, urgencyLevel, createdAt

#### ApplicationDetailResponse.java
All ApplicationResponse fields plus:
- documents (list)
- assignedOrganizationId, assignedOrganizationName
- assignedCampaignId, assignedCampaignTitle
- reviewedAt, reviewedByName
- rejectionReason (if rejected)

#### ApplicationListResponse.java
List with summary: totalCount, pendingCount, approvedCount

#### ApplicationDocumentResponse.java
Fields: id, documentType, documentTypeName, fileName, fileUrl, isVerified, uploadedAt

#### ApplicationStatsResponse.java
For dashboard: totalApplications, pending, inReview, approved, rejected, completed, byCategory (map), byCity (map)

---

### 6. Mapper
**Location:** `src/main/java/com/seffafbagis/api/dto/mapper/ApplicationMapper.java`

Methods:
- toEntity(CreateApplicationRequest, User, Category)
- toResponse(Application)
- toDetailResponse(Application)
- toDocumentResponse(ApplicationDocument)

---

### 7. Services
**Location:** `src/main/java/com/seffafbagis/api/service/application/`

#### ApplicationService.java

**Applicant Methods:**
- createApplication(CreateApplicationRequest) - submit application
- getMyApplications() - list user's applications
- getMyApplication(UUID id) - detail view
- updateApplication(UUID id, UpdateApplicationRequest) - only PENDING
- cancelApplication(UUID id) - only PENDING
- addDocument(UUID applicationId, document data)
- removeDocument(UUID documentId)

**Organization Methods (FOUNDATION role):**
- getAssignedApplications(Pageable) - assigned to my organization
- assignToCampaign(UUID applicationId, AssignToCampaignRequest)
- completeApplication(UUID applicationId, CompleteApplicationRequest)

**Admin Methods:**
- getAllApplications(ApplicationStatus status, Pageable)
- getApplicationDetail(UUID id)
- reviewApplication(UUID id, ReviewApplicationRequest)
- assignToOrganization(UUID applicationId, UUID organizationId)
- getApplicationStats()

**Business Logic:**
1. User must be authenticated to apply
2. Only applicant can view/update their own applications
3. Only PENDING applications can be updated/cancelled
4. Admin or assigned organization can review
5. On APPROVED: can be assigned to organization/campaign
6. On assignment to campaign: applicant notified
7. COMPLETED when aid successfully delivered

#### ApplicationDocumentService.java

**Methods:**
- addDocument(UUID applicationId, document data)
- removeDocument(UUID documentId) - only for PENDING applications
- verifyDocument(UUID documentId) - admin marks as verified
- getDocuments(UUID applicationId)

---

### 8. Controller
**Location:** `src/main/java/com/seffafbagis/api/controller/application/`

#### ApplicationController.java

**Applicant Endpoints (Authenticated):**
```
POST   /api/v1/applications                 - Submit application
GET    /api/v1/applications/my              - My applications
GET    /api/v1/applications/my/{id}         - My application detail
PUT    /api/v1/applications/my/{id}         - Update application
DELETE /api/v1/applications/my/{id}         - Cancel application
POST   /api/v1/applications/my/{id}/documents - Add document
DELETE /api/v1/applications/my/{id}/documents/{docId} - Remove document
```

**Organization Endpoints (FOUNDATION role):**
```
GET  /api/v1/applications/assigned          - Assigned to my org
POST /api/v1/applications/{id}/assign-campaign - Assign to campaign
POST /api/v1/applications/{id}/complete     - Mark as completed
```

**Admin Endpoints:**
```
GET  /api/v1/admin/applications             - All applications (with filters)
GET  /api/v1/admin/applications/{id}        - Application detail
POST /api/v1/admin/applications/{id}/review - Review application
POST /api/v1/admin/applications/{id}/assign-organization - Assign to org
GET  /api/v1/admin/applications/stats       - Statistics
POST /api/v1/admin/applications/{id}/documents/{docId}/verify - Verify document
```

---

## Application Workflow

```
Applicant submits
       │
       ▼
   PENDING
       │
       ▼
Admin/Org reviews
       │
   ┌───┴───┐
   ▼       ▼
IN_REVIEW  REJECTED
   │          │
   ▼          ▼
Admin decides  End
   │
   ▼
APPROVED
   │
   ▼
Assign to Organization
   │
   ▼
Assign to Campaign (optional)
   │
   ▼
Aid delivered
   │
   ▼
COMPLETED
```

---

## Document Types

Standard document_type values:
- `id_card` - Kimlik fotokopisi
- `income_proof` - Gelir belgesi
- `medical_report` - Sağlık raporu
- `utility_bill` - Fatura (ikamet ispatı)
- `disability_card` - Engelli kartı
- `student_certificate` - Öğrenci belgesi
- `other` - Diğer

---

## Urgency Levels

| Level | Name | Description |
|-------|------|-------------|
| 1 | Normal | Standart başvuru |
| 2 | Orta | Birkaç hafta içinde ihtiyaç |
| 3 | Yüksek | Birkaç gün içinde ihtiyaç |
| 4 | Çok Yüksek | Acil durum |
| 5 | Kritik | Hayati aciliyet |

---

## Testing Requirements

### Unit Tests
- ApplicationServiceTest:
  - Test only applicant can view own applications
  - Test only PENDING can be updated
  - Test status transitions follow workflow
  - Test assignment to campaign works

- ApplicationDocumentServiceTest:
  - Test document add/remove
  - Test verify only by admin

### Integration Tests
- Full application lifecycle
- Assignment flow

---

## Success Criteria

- [ ] ApplicationStatus enum created
- [ ] Application and ApplicationDocument entities created
- [ ] Both repositories with custom queries
- [ ] All 5 request DTOs with validation
- [ ] All 5 response DTOs created
- [ ] ApplicationMapper handles all conversions
- [ ] ApplicationService with all methods
- [ ] ApplicationDocumentService with all methods
- [ ] Only applicant can manage own applications
- [ ] Status workflow enforced
- [ ] Organization assignment works
- [ ] Campaign assignment works
- [ ] Document verification by admin
- [ ] All endpoints with proper authorization
- [ ] All unit tests pass

---

## Result File Requirement

After completing this phase, create:
**Location:** `docs/Emir/step_results/phase_12.0_result.md`

Include:
1. Summary
2. Files created
3. Application workflow description
4. Document types list
5. Urgency levels table
6. API endpoints table
7. Testing results
8. Issues and resolutions
9. Next steps (Phase 13.0)
10. Success criteria checklist

---

## Important Notes

1. **Do NOT use ternary operators** - Use if-else
2. **Privacy** - Applicant data is sensitive, proper authorization critical
3. **FileStorageService** from Furkan handles document upload
4. **Notifications** will be added in Phase 13
5. **Anonymization** - Consider k-anonymity for public display

---

## Dependencies

From Furkan's work:
- FileStorageService
- SecurityUtils, ApiResponse
- User entity

From previous phases:
- Category (Phase 1)
- Organization (Phase 2)
- Campaign (Phase 4)

---

## Estimated Duration

2 days

---

## Next Phase

**Phase 13.0: Notification Module**
