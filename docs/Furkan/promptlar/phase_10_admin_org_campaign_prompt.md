# PHASE 10: ADMIN MODULE - ORGANIZATION & CAMPAIGN MANAGEMENT

## CONTEXT AND BACKGROUND

You are working on "Şeffaf Bağış Platformu" (Transparent Donation Platform), a graduation project for Fatih Sultan Mehmet Vakıf University. This is a comprehensive donation platform for Turkey that emphasizes transparency and accountability in charitable giving.

### Project Overview
- **Technology Stack**: Java 17, Spring Boot 3.x, PostgreSQL 15+, Redis, Maven
- **Purpose**: Enable foundations to create campaigns, donors to contribute, and maintain transparency through evidence uploads and scoring systems
- **Compliance Requirements**: KVKK (Turkish Personal Data Protection Law) compliant
- **Developer**: Furkan (responsible for Infrastructure, Auth, User, Admin modules - ~58% of backend)
- **Teammate**: Emir (responsible for Organization, Campaign, Donation, Payment modules - ~42% of backend)

### Current State
- Phase 0-8: Core infrastructure and user management complete
- Phase 9: Admin user management complete
- Organization and Campaign entities will be created by Emir
- Admin functionality for org/campaign approval needs to be implemented
- Interface-based design required for parallel development

### What This Phase Accomplishes
This phase implements admin functionality for organization verification and campaign approval. Since Organization and Campaign entities are Emir's responsibility, this phase uses **interface-based design** to define contracts that Emir's services will implement. This allows parallel development without blocking either developer.

---

## OBJECTIVE

Create the admin organization and campaign management module including:
1. Service interfaces that Emir will implement
2. Admin DTOs for organization/campaign management
3. Admin services for verification and approval workflows
4. Admin controllers with REST endpoints
5. Report/complaint management system

---

## IMPORTANT RULES

### Code Style Requirements
- Use clear, readable code - prefer if-else statements over ternary operators
- Follow SOLID principles
- Add comprehensive comments in English
- Use meaningful variable and method names
- No complex one-liners - prioritize readability over brevity

### Interface-Based Design Requirements
- Define clear interfaces for Organization and Campaign services
- Emir will implement these interfaces
- Use dependency injection for loose coupling
- Document expected behavior in interface JavaDocs
- Handle cases where implementation is not yet available

### Admin Workflow Requirements
- Organization verification requires document review
- Campaign approval requires content review
- Rejections must include reason
- All decisions must be audit logged
- Email notifications for approval/rejection

---

## DETAILED REQUIREMENTS

### 1. Service Interfaces (For Emir to Implement)

#### 1.1 IOrganizationService.java
**Location**: `src/main/java/com/seffafbagis/api/service/interfaces/IOrganizationService.java`

**Purpose**: Contract for organization operations that admin module depends on

**Interface Methods**:

```java
/**
 * Service interface for organization operations.
 * Implementation will be provided by Emir.
 */
public interface IOrganizationService {

    /**
     * Get organization by ID
     * @param id Organization UUID
     * @return OrganizationResponse DTO
     * @throws ResourceNotFoundException if not found
     */
    OrganizationResponse getById(UUID id);

    /**
     * Get all organizations with pagination
     * @param pageable Pagination parameters
     * @return Page of OrganizationResponse
     */
    Page<OrganizationResponse> getAll(Pageable pageable);

    /**
     * Get organizations pending verification
     * @param pageable Pagination parameters
     * @return Page of organizations with status PENDING
     */
    Page<OrganizationResponse> getPendingVerifications(Pageable pageable);

    /**
     * Get organizations by verification status
     * @param status Verification status
     * @param pageable Pagination parameters
     * @return Page of matching organizations
     */
    Page<OrganizationResponse> getByVerificationStatus(String status, Pageable pageable);

    /**
     * Update organization verification status
     * @param id Organization ID
     * @param status New status (VERIFIED, REJECTED)
     * @param reason Reason for decision (required for rejection)
     * @param adminId Admin making the decision
     */
    void updateVerificationStatus(UUID id, String status, String reason, UUID adminId);

    /**
     * Get organization statistics for dashboard
     * @return OrganizationStatistics DTO
     */
    OrganizationStatistics getStatistics();

    /**
     * Check if organization exists
     * @param id Organization ID
     * @return true if exists
     */
    boolean existsById(UUID id);
}
```

---

#### 1.2 ICampaignService.java
**Location**: `src/main/java/com/seffafbagis/api/service/interfaces/ICampaignService.java`

**Purpose**: Contract for campaign operations that admin module depends on

**Interface Methods**:

```java
/**
 * Service interface for campaign operations.
 * Implementation will be provided by Emir.
 */
public interface ICampaignService {

    /**
     * Get campaign by ID
     * @param id Campaign UUID
     * @return CampaignResponse DTO
     * @throws ResourceNotFoundException if not found
     */
    CampaignResponse getById(UUID id);

    /**
     * Get all campaigns with pagination
     * @param pageable Pagination parameters
     * @return Page of CampaignResponse
     */
    Page<CampaignResponse> getAll(Pageable pageable);

    /**
     * Get campaigns pending approval
     * @param pageable Pagination parameters
     * @return Page of campaigns with status PENDING_APPROVAL
     */
    Page<CampaignResponse> getPendingApprovals(Pageable pageable);

    /**
     * Get campaigns by status
     * @param status Campaign status
     * @param pageable Pagination parameters
     * @return Page of matching campaigns
     */
    Page<CampaignResponse> getByStatus(String status, Pageable pageable);

    /**
     * Update campaign approval status
     * @param id Campaign ID
     * @param status New status (ACTIVE, REJECTED)
     * @param reason Reason for decision (required for rejection)
     * @param adminId Admin making the decision
     */
    void updateApprovalStatus(UUID id, String status, String reason, UUID adminId);

    /**
     * Get campaign statistics for dashboard
     * @return CampaignStatistics DTO
     */
    CampaignStatistics getStatistics();

    /**
     * Check if campaign exists
     * @param id Campaign ID
     * @return true if exists
     */
    boolean existsById(UUID id);

    /**
     * Get campaigns by organization
     * @param organizationId Organization ID
     * @param pageable Pagination parameters
     * @return Page of campaigns for the organization
     */
    Page<CampaignResponse> getByOrganizationId(UUID organizationId, Pageable pageable);
}
```

---

#### 1.3 Placeholder Response DTOs
**Location**: `src/main/java/com/seffafbagis/api/dto/response/organization/` and `campaign/`

These DTOs define the expected structure. Emir will ensure his implementations return these:

**OrganizationResponse.java** (placeholder structure):
```java
public class OrganizationResponse {
    private UUID id;
    private String name;
    private String description;
    private String verificationStatus; // PENDING, VERIFIED, REJECTED
    private String rejectionReason;
    private LocalDateTime verifiedAt;
    private UUID verifiedBy;
    private LocalDateTime createdAt;
    // ... other fields as needed
}
```

**CampaignResponse.java** (placeholder structure):
```java
public class CampaignResponse {
    private UUID id;
    private UUID organizationId;
    private String title;
    private String description;
    private String status; // DRAFT, PENDING_APPROVAL, ACTIVE, REJECTED, COMPLETED
    private String rejectionReason;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDateTime approvedAt;
    private UUID approvedBy;
    private LocalDateTime createdAt;
    // ... other fields as needed
}
```

**OrganizationStatistics.java**:
```java
public class OrganizationStatistics {
    private Long totalOrganizations;
    private Long pendingVerifications;
    private Long verifiedOrganizations;
    private Long rejectedOrganizations;
}
```

**CampaignStatistics.java**:
```java
public class CampaignStatistics {
    private Long totalCampaigns;
    private Long pendingApprovals;
    private Long activeCampaigns;
    private Long completedCampaigns;
    private Long rejectedCampaigns;
    private BigDecimal totalRaisedAmount;
}
```

---

### 2. Admin DTOs - Request

#### 2.1 VerifyOrganizationRequest.java
**Location**: `src/main/java/com/seffafbagis/api/dto/request/admin/VerifyOrganizationRequest.java`

**Purpose**: Request body for organization verification decision

**Fields**:

| Field | Type | Validation | Description |
|-------|------|------------|-------------|
| decision | String | @NotBlank, @Pattern(regexp="^(APPROVE\|REJECT)$") | Approval decision |
| reason | String | Required if REJECT, @Size(max=1000) | Reason for decision |
| notes | String | @Size(max=2000) | Internal admin notes |
| notifyOrganization | Boolean | Default true | Send email notification |

---

#### 2.2 ApproveCampaignRequest.java
**Location**: `src/main/java/com/seffafbagis/api/dto/request/admin/ApproveCampaignRequest.java`

**Purpose**: Request body for campaign approval decision

**Fields**:

| Field | Type | Validation | Description |
|-------|------|------------|-------------|
| decision | String | @NotBlank, @Pattern(regexp="^(APPROVE\|REJECT)$") | Approval decision |
| reason | String | Required if REJECT, @Size(max=1000) | Reason for decision |
| notes | String | @Size(max=2000) | Internal admin notes |
| notifyOrganization | Boolean | Default true | Send email notification |

---

#### 2.3 ResolveReportRequest.java
**Location**: `src/main/java/com/seffafbagis/api/dto/request/admin/ResolveReportRequest.java`

**Purpose**: Request body for resolving user reports/complaints

**Fields**:

| Field | Type | Validation | Description |
|-------|------|------------|-------------|
| resolution | String | @NotBlank, @Pattern(regexp="^(RESOLVED\|DISMISSED\|ACTION_TAKEN)$") | Resolution type |
| resolutionNotes | String | @NotBlank, @Size(max=2000) | Resolution details |
| actionTaken | String | @Size(max=1000) | Description of action taken |
| notifyReporter | Boolean | Default true | Notify the person who reported |

---

#### 2.4 AssignReportRequest.java
**Location**: `src/main/java/com/seffafbagis/api/dto/request/admin/AssignReportRequest.java`

**Purpose**: Request body for assigning report to admin

**Fields**:

| Field | Type | Validation | Description |
|-------|------|------------|-------------|
| assigneeId | UUID | @NotNull | Admin user ID to assign to |
| priority | String | @Pattern(regexp="^(LOW\|MEDIUM\|HIGH\|CRITICAL)$") | Report priority |
| notes | String | @Size(max=500) | Assignment notes |

---

### 3. Admin DTOs - Response

#### 3.1 AdminOrganizationResponse.java
**Location**: `src/main/java/com/seffafbagis/api/dto/response/admin/AdminOrganizationResponse.java`

**Purpose**: Organization details for admin view

**Fields**:

| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Organization ID |
| name | String | Organization name |
| description | String | Description |
| email | String | Contact email |
| phone | String | Contact phone |
| website | String | Website URL |
| verificationStatus | String | PENDING, VERIFIED, REJECTED |
| rejectionReason | String | If rejected |
| resubmissionCount | Integer | Number of resubmissions |
| verifiedAt | LocalDateTime | Verification timestamp |
| verifiedBy | AdminUserSummary | Admin who verified |
| documents | List<DocumentInfo> | Uploaded verification documents |
| bankAccounts | List<BankAccountInfo> | Bank account info |
| campaignCount | Integer | Number of campaigns |
| totalRaised | BigDecimal | Total donations received |
| createdAt | LocalDateTime | Registration date |

**AdminUserSummary Nested Class**:
- id: UUID
- email: String
- fullName: String

**DocumentInfo Nested Class**:
- id: UUID
- documentType: String
- fileName: String
- uploadedAt: LocalDateTime

**BankAccountInfo Nested Class**:
- id: UUID
- bankName: String
- ibanMasked: String (show only last 4 digits)
- isVerified: Boolean

---

#### 3.2 AdminCampaignResponse.java
**Location**: `src/main/java/com/seffafbagis/api/dto/response/admin/AdminCampaignResponse.java`

**Purpose**: Campaign details for admin view

**Fields**:

| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Campaign ID |
| organizationId | UUID | Organization ID |
| organizationName | String | Organization name |
| title | String | Campaign title |
| description | String | Campaign description |
| status | String | Campaign status |
| rejectionReason | String | If rejected |
| targetAmount | BigDecimal | Goal amount |
| currentAmount | BigDecimal | Raised amount |
| donorCount | Integer | Number of donors |
| startDate | LocalDate | Campaign start |
| endDate | LocalDate | Campaign end |
| approvedAt | LocalDateTime | Approval timestamp |
| approvedBy | AdminUserSummary | Admin who approved |
| transparencyScore | Integer | Transparency score |
| evidenceCount | Integer | Number of evidence uploads |
| reportCount | Integer | Number of reports against |
| createdAt | LocalDateTime | Creation date |

---

#### 3.3 ReportResponse.java
**Location**: `src/main/java/com/seffafbagis/api/dto/response/admin/ReportResponse.java`

**Purpose**: Report/complaint details for admin

**Fields**:

| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Report ID |
| reporterId | UUID | User who reported |
| reporterEmail | String | Reporter's email |
| reportType | String | Type of report |
| targetType | String | ORGANIZATION, CAMPAIGN, USER |
| targetId | UUID | ID of reported entity |
| targetName | String | Name of reported entity |
| reason | String | Report reason |
| description | String | Detailed description |
| status | String | PENDING, ASSIGNED, RESOLVED, DISMISSED |
| priority | String | LOW, MEDIUM, HIGH, CRITICAL |
| assignedTo | AdminUserSummary | Assigned admin |
| assignedAt | LocalDateTime | Assignment timestamp |
| resolution | String | Resolution type |
| resolutionNotes | String | Resolution details |
| resolvedBy | AdminUserSummary | Admin who resolved |
| resolvedAt | LocalDateTime | Resolution timestamp |
| createdAt | LocalDateTime | Report creation date |

---

### 4. Admin Services

#### 4.1 AdminOrganizationService.java
**Location**: `src/main/java/com/seffafbagis/api/service/admin/AdminOrganizationService.java`

**Purpose**: Admin operations for organization management

**Dependencies**:
- IOrganizationService (interface - Emir's implementation)
- EmailService
- AuditLogService

**Methods**:

---

**`getAllOrganizations(Pageable pageable)`**

**Purpose**: Get all organizations with pagination

**Flow**:
1. Call organizationService.getAll(pageable)
2. Map to AdminOrganizationResponse
3. Return PageResponse

**Returns**: PageResponse<AdminOrganizationResponse>

---

**`getPendingVerifications(Pageable pageable)`**

**Purpose**: Get organizations awaiting verification

**Flow**:
1. Call organizationService.getPendingVerifications(pageable)
2. Map to AdminOrganizationResponse
3. Return PageResponse

**Returns**: PageResponse<AdminOrganizationResponse>

---

**`getOrganizationById(UUID id)`**

**Purpose**: Get detailed organization information

**Flow**:
1. Call organizationService.getById(id)
2. Enrich with additional admin-specific data
3. Map to AdminOrganizationResponse
4. Return response

**Returns**: AdminOrganizationResponse

---

**`verifyOrganization(UUID id, VerifyOrganizationRequest request, UUID adminId)`**

**Purpose**: Approve or reject organization verification

**Flow**:
1. Validate request (reason required if REJECT)
2. Determine new status based on decision:
   - APPROVE → status = "VERIFIED"
   - REJECT → status = "REJECTED"
3. Call organizationService.updateVerificationStatus()
4. Create audit log entry
5. If notifyOrganization is true, send email
6. Return updated organization info

**Returns**: AdminOrganizationResponse

---

**`getOrganizationStatistics()`**

**Purpose**: Get organization statistics for dashboard

**Flow**:
1. Call organizationService.getStatistics()
2. Return OrganizationStatistics

**Returns**: OrganizationStatistics

---

#### 4.2 AdminCampaignService.java
**Location**: `src/main/java/com/seffafbagis/api/service/admin/AdminCampaignService.java`

**Purpose**: Admin operations for campaign management

**Dependencies**:
- ICampaignService (interface - Emir's implementation)
- IOrganizationService
- EmailService
- AuditLogService

**Methods**:

---

**`getAllCampaigns(Pageable pageable)`**

**Purpose**: Get all campaigns with pagination

**Flow**:
1. Call campaignService.getAll(pageable)
2. Map to AdminCampaignResponse
3. Return PageResponse

**Returns**: PageResponse<AdminCampaignResponse>

---

**`getPendingApprovals(Pageable pageable)`**

**Purpose**: Get campaigns awaiting approval

**Flow**:
1. Call campaignService.getPendingApprovals(pageable)
2. Map to AdminCampaignResponse
3. Return PageResponse

**Returns**: PageResponse<AdminCampaignResponse>

---

**`getCampaignById(UUID id)`**

**Purpose**: Get detailed campaign information

**Flow**:
1. Call campaignService.getById(id)
2. Enrich with organization info
3. Map to AdminCampaignResponse
4. Return response

**Returns**: AdminCampaignResponse

---

**`approveCampaign(UUID id, ApproveCampaignRequest request, UUID adminId)`**

**Purpose**: Approve or reject campaign

**Flow**:
1. Validate request (reason required if REJECT)
2. Determine new status:
   - APPROVE → status = "ACTIVE"
   - REJECT → status = "REJECTED"
3. Call campaignService.updateApprovalStatus()
4. Create audit log entry
5. If notifyOrganization is true, send email
6. Return updated campaign info

**Returns**: AdminCampaignResponse

---

**`getCampaignsByOrganization(UUID organizationId, Pageable pageable)`**

**Purpose**: Get all campaigns for an organization

**Flow**:
1. Call campaignService.getByOrganizationId(organizationId, pageable)
2. Map to AdminCampaignResponse
3. Return PageResponse

**Returns**: PageResponse<AdminCampaignResponse>

---

**`getCampaignStatistics()`**

**Purpose**: Get campaign statistics for dashboard

**Flow**:
1. Call campaignService.getStatistics()
2. Return CampaignStatistics

**Returns**: CampaignStatistics

---

#### 4.3 AdminReportService.java
**Location**: `src/main/java/com/seffafbagis/api/service/admin/AdminReportService.java`

**Purpose**: Admin operations for report/complaint management

**Dependencies**:
- ReportRepository
- UserRepository
- EmailService
- AuditLogService

**Methods**:

---

**`getAllReports(Pageable pageable)`**

**Purpose**: Get all reports with pagination

**Flow**:
1. Call reportRepository.findAll(pageable)
2. Map to ReportResponse
3. Return PageResponse

**Returns**: PageResponse<ReportResponse>

---

**`getPendingReports(Pageable pageable)`**

**Purpose**: Get unresolved reports

**Flow**:
1. Call reportRepository.findByStatusIn(["PENDING", "ASSIGNED"], pageable)
2. Map to ReportResponse
3. Return PageResponse

**Returns**: PageResponse<ReportResponse>

---

**`getReportsByPriority(String priority, Pageable pageable)`**

**Purpose**: Get reports filtered by priority

**Flow**:
1. Call reportRepository.findByPriority(priority, pageable)
2. Map to ReportResponse
3. Return PageResponse

**Returns**: PageResponse<ReportResponse>

---

**`getReportById(UUID id)`**

**Purpose**: Get detailed report information

**Flow**:
1. Find report by ID
2. Enrich with reporter info, target info
3. Map to ReportResponse
4. Return response

**Returns**: ReportResponse

---

**`assignReport(UUID id, AssignReportRequest request, UUID adminId)`**

**Purpose**: Assign report to an admin

**Flow**:
1. Find report by ID
2. Validate assignee is an ADMIN
3. Update report:
   - Set assignedTo
   - Set assignedAt
   - Set priority if provided
   - Set status to "ASSIGNED"
4. Create audit log entry
5. Send notification to assignee
6. Return updated report

**Returns**: ReportResponse

---

**`resolveReport(UUID id, ResolveReportRequest request, UUID adminId)`**

**Purpose**: Resolve a report

**Flow**:
1. Find report by ID
2. Update report:
   - Set resolution
   - Set resolutionNotes
   - Set resolvedBy
   - Set resolvedAt
   - Set status to resolution type
3. Create audit log entry
4. If notifyReporter is true, send email to reporter
5. Return updated report

**Returns**: ReportResponse

---

**`getReportStatistics()`**

**Purpose**: Get report statistics for dashboard

**Returns**: ReportStatistics with counts by status and priority

---

### 5. Admin Controllers

#### 5.1 AdminOrganizationController.java
**Location**: `src/main/java/com/seffafbagis/api/controller/admin/AdminOrganizationController.java`

**Purpose**: REST endpoints for admin organization management

**Class Annotations**:
- @RestController
- @RequestMapping("/api/v1/admin/organizations")
- @Tag(name = "Admin - Organizations", description = "Admin organization management")
- @PreAuthorize("hasRole('ADMIN')")

**Endpoints**:

---

**GET /api/v1/admin/organizations**
- List all organizations (paginated)
- Returns: PageResponse<AdminOrganizationResponse>

**GET /api/v1/admin/organizations/pending**
- List pending verifications
- Returns: PageResponse<AdminOrganizationResponse>

**GET /api/v1/admin/organizations/{id}**
- Get organization details
- Returns: AdminOrganizationResponse

**PUT /api/v1/admin/organizations/{id}/verify**
- Approve or reject organization
- Body: VerifyOrganizationRequest
- Returns: AdminOrganizationResponse

**GET /api/v1/admin/organizations/{id}/campaigns**
- Get organization's campaigns
- Returns: PageResponse<AdminCampaignResponse>

**GET /api/v1/admin/organizations/statistics**
- Get organization statistics
- Returns: OrganizationStatistics

---

#### 5.2 AdminCampaignController.java
**Location**: `src/main/java/com/seffafbagis/api/controller/admin/AdminCampaignController.java`

**Purpose**: REST endpoints for admin campaign management

**Class Annotations**:
- @RestController
- @RequestMapping("/api/v1/admin/campaigns")
- @Tag(name = "Admin - Campaigns", description = "Admin campaign management")
- @PreAuthorize("hasRole('ADMIN')")

**Endpoints**:

---

**GET /api/v1/admin/campaigns**
- List all campaigns (paginated)
- Returns: PageResponse<AdminCampaignResponse>

**GET /api/v1/admin/campaigns/pending**
- List pending approvals
- Returns: PageResponse<AdminCampaignResponse>

**GET /api/v1/admin/campaigns/{id}**
- Get campaign details
- Returns: AdminCampaignResponse

**PUT /api/v1/admin/campaigns/{id}/approve**
- Approve or reject campaign
- Body: ApproveCampaignRequest
- Returns: AdminCampaignResponse

**GET /api/v1/admin/campaigns/statistics**
- Get campaign statistics
- Returns: CampaignStatistics

---

#### 5.3 AdminReportController.java
**Location**: `src/main/java/com/seffafbagis/api/controller/admin/AdminReportController.java`

**Purpose**: REST endpoints for admin report management

**Class Annotations**:
- @RestController
- @RequestMapping("/api/v1/admin/reports")
- @Tag(name = "Admin - Reports", description = "Admin report/complaint management")
- @PreAuthorize("hasRole('ADMIN')")

**Endpoints**:

---

**GET /api/v1/admin/reports**
- List all reports (paginated)
- Returns: PageResponse<ReportResponse>

**GET /api/v1/admin/reports/pending**
- List pending reports
- Returns: PageResponse<ReportResponse>

**GET /api/v1/admin/reports/priority/{priority}**
- List reports by priority
- Returns: PageResponse<ReportResponse>

**GET /api/v1/admin/reports/{id}**
- Get report details
- Returns: ReportResponse

**PUT /api/v1/admin/reports/{id}/assign**
- Assign report to admin
- Body: AssignReportRequest
- Returns: ReportResponse

**PUT /api/v1/admin/reports/{id}/resolve**
- Resolve report
- Body: ResolveReportRequest
- Returns: ReportResponse

**GET /api/v1/admin/reports/statistics**
- Get report statistics
- Returns: ReportStatistics

---

## FILE STRUCTURE

After completing this phase, the following files should exist:

```
src/main/java/com/seffafbagis/api/
├── service/interfaces/
│   ├── IOrganizationService.java
│   └── ICampaignService.java
├── dto/
│   ├── request/admin/
│   │   ├── VerifyOrganizationRequest.java
│   │   ├── ApproveCampaignRequest.java
│   │   ├── ResolveReportRequest.java
│   │   └── AssignReportRequest.java
│   └── response/admin/
│       ├── AdminOrganizationResponse.java
│       ├── AdminCampaignResponse.java
│       └── ReportResponse.java
├── service/admin/
│   ├── AdminOrganizationService.java
│   ├── AdminCampaignService.java
│   └── AdminReportService.java
└── controller/admin/
    ├── AdminOrganizationController.java
    ├── AdminCampaignController.java
    └── AdminReportController.java
```

**Total Files**: 14

---

## INTERFACE-BASED DEVELOPMENT PATTERN

```
┌─────────────────────────────────────────────────────────────────────┐
│                 INTERFACE-BASED DEVELOPMENT                          │
└─────────────────────────────────────────────────────────────────────┘

Furkan's Code (Admin Module)              Emir's Code (Organization Module)
         │                                           │
         │                                           │
         ▼                                           ▼
┌─────────────────────┐                   ┌─────────────────────┐
│ AdminOrganization   │                   │ OrganizationService │
│ Service             │                   │ (Implementation)    │
│                     │                   │                     │
│ Uses interface:     │                   │ Implements:         │
│ IOrganizationService│──────────────────>│ IOrganizationService│
│                     │                   │                     │
└─────────────────────┘                   └─────────────────────┘

Benefits:
1. Parallel development - no blocking
2. Clear contracts between modules
3. Easy testing with mocks
4. Loose coupling
```

---

## TESTING REQUIREMENTS

After implementation, verify:

### 1. Organization Management Tests

**Test: List Pending Verifications**
- Get pending organizations
- Verify only PENDING status returned

**Test: Verify Organization - Approve**
- Approve pending organization
- Verify status changed to VERIFIED
- Verify audit log created

**Test: Verify Organization - Reject**
- Reject with reason
- Verify status changed to REJECTED
- Verify reason stored

**Test: Rejection Requires Reason**
- Try to reject without reason
- Verify 400 Bad Request

### 2. Campaign Management Tests

**Test: List Pending Approvals**
- Get pending campaigns
- Verify only PENDING_APPROVAL status returned

**Test: Approve Campaign**
- Approve pending campaign
- Verify status changed to ACTIVE
- Verify audit log created

**Test: Reject Campaign**
- Reject with reason
- Verify status changed to REJECTED
- Verify reason stored

### 3. Report Management Tests

**Test: List Reports**
- Get all reports
- Verify pagination works

**Test: Assign Report**
- Assign to admin
- Verify assignedTo set
- Verify status changed to ASSIGNED

**Test: Resolve Report**
- Resolve with notes
- Verify resolution stored
- Verify status updated

### 4. Interface Tests

**Test: Mock Implementation**
- Use mock implementation of IOrganizationService
- Verify admin service works correctly

---

## SUCCESS CRITERIA

Phase 10 is considered successful when:

1. ✅ All 14 files are created in correct locations
2. ✅ Interfaces clearly define contracts for Emir
3. ✅ Organization verification workflow works
4. ✅ Campaign approval workflow works
5. ✅ Report management workflow works
6. ✅ Rejection requires reason
7. ✅ All actions are audit logged
8. ✅ Email notifications sent appropriately
9. ✅ Admin endpoints require ADMIN role
10. ✅ Code compiles even without Emir's implementations (using interfaces)

---

## RESULT FILE REQUIREMENTS

After completing this phase, create a result file at:
`docs/Furkan/step_results/phase_10_result.md`

The result file must include:

1. **Execution Status**: Success or Failure
2. **Files Created**: List all 14 files with their paths
3. **Interface Definitions**:
   - List of methods in IOrganizationService
   - List of methods in ICampaignService
4. **Organization Workflow Tests**:
   - Verification approve/reject results
5. **Campaign Workflow Tests**:
   - Approval approve/reject results
6. **Report Management Tests**:
   - Assignment and resolution results
7. **Integration Notes**:
   - Notes for Emir on implementing interfaces
8. **Issues Encountered**: Any problems and how they were resolved
9. **Notes for Next Phase**: Observations relevant to Phase 11

---

## NOTES FOR EMIR

When implementing the interfaces:

1. **IOrganizationService Implementation**:
   - Implement all methods defined in the interface
   - Use @Service annotation
   - OrganizationResponse DTO should match expected structure
   - Handle ResourceNotFoundException properly

2. **ICampaignService Implementation**:
   - Implement all methods defined in the interface
   - Use @Service annotation
   - CampaignResponse DTO should match expected structure
   - Handle ResourceNotFoundException properly

3. **Testing**:
   - Admin module tests will use mocks initially
   - Integration tests after implementation

---

## API ENDPOINTS SUMMARY

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1/admin/organizations | List all organizations |
| GET | /api/v1/admin/organizations/pending | List pending verifications |
| GET | /api/v1/admin/organizations/{id} | Get organization details |
| PUT | /api/v1/admin/organizations/{id}/verify | Verify organization |
| GET | /api/v1/admin/organizations/{id}/campaigns | Get org campaigns |
| GET | /api/v1/admin/organizations/statistics | Get org statistics |
| GET | /api/v1/admin/campaigns | List all campaigns |
| GET | /api/v1/admin/campaigns/pending | List pending approvals |
| GET | /api/v1/admin/campaigns/{id} | Get campaign details |
| PUT | /api/v1/admin/campaigns/{id}/approve | Approve campaign |
| GET | /api/v1/admin/campaigns/statistics | Get campaign statistics |
| GET | /api/v1/admin/reports | List all reports |
| GET | /api/v1/admin/reports/pending | List pending reports |
| GET | /api/v1/admin/reports/{id} | Get report details |
| PUT | /api/v1/admin/reports/{id}/assign | Assign report |
| PUT | /api/v1/admin/reports/{id}/resolve | Resolve report |

---

## NEXT PHASE PREVIEW

Phase 11 (System Settings & Favorites) will create:
- System settings management (platform configuration)
- User favorites feature (favorite organizations)
- Settings caching with Redis
