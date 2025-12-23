# PHASE 3.0: ORGANIZATION MODULE - SERVICE & CONTROLLER

## Context and Background

You are working on the "Şeffaf Bağış Platformu" (Transparent Donation Platform), a comprehensive donation platform for Turkey that emphasizes transparency and accountability in charitable giving. This is a graduation project for Fatih Sultan Mehmet Vakıf University.

**Project Stack:**
- Java 17
- Spring Boot 3.x
- PostgreSQL 15+
- Redis (for caching)
- Maven

**Team Structure:**
- Furkan (~58%): Infrastructure, Auth, User, Admin modules - COMPLETED
- Emir (~42%): Organization, Campaign, Donation, Payment, Evidence, Transparency, Application, Category, Notification, Report modules - IN PROGRESS

**Current Phase:** 3.0 - Organization Module - Service & Controller

**Previous Phases Completed:**
- Phase 1.0: Category & Donation Type Module
- Phase 2.0: Organization Module - Entities & Repository
  - OrganizationType and VerificationStatus enums
  - Organization, OrganizationContact, OrganizationDocument, OrganizationBankAccount entities
  - All four repositories with custom query methods

---

## Objective

Implement the business logic (services) and REST API (controllers) for organization management. This includes organization registration, verification workflow, document management, contact management, and bank account handling. Also implement the `IOrganizationService` interface required by Furkan's Admin module.

---

## What This Phase Will Solve

1. **Organization Registration**: Users with FOUNDATION role can create and manage their organization
2. **Verification Workflow**: Organizations submit for verification, admins approve/reject
3. **Document Upload**: Organizations upload required documents for verification
4. **Contact Management**: Organizations manage multiple contact persons
5. **Bank Account Management**: Organizations manage bank accounts for receiving donations
6. **Admin Integration**: Implement interface for Furkan's admin module to verify organizations

---

## Interface from Furkan's Work

Furkan has created an interface that you MUST implement:

**Location:** `src/main/java/com/seffafbagis/api/service/interfaces/IOrganizationService.java`

```java
public interface IOrganizationService {
    OrganizationResponse getById(UUID id);
    void verify(UUID id, VerifyOrganizationRequest request);
    void reject(UUID id, VerifyOrganizationRequest request);
    Page<OrganizationResponse> getPendingVerifications(Pageable pageable);
    Page<OrganizationResponse> getAllOrganizations(Pageable pageable);
    OrganizationDetailResponse getOrganizationDetail(UUID id);
}
```

Your `OrganizationService` class must implement this interface.

---

## Files to Create

### 1. DTOs - Request

**Location:** `src/main/java/com/seffafbagis/api/dto/request/organization/`

#### CreateOrganizationRequest.java
```java
@Data
public class CreateOrganizationRequest {
    
    @NotNull(message = "Organization type is required")
    private OrganizationType organizationType;
    
    @NotBlank(message = "Legal name is required")
    @Size(max = 255, message = "Legal name must not exceed 255 characters")
    private String legalName;
    
    @Size(max = 255, message = "Trade name must not exceed 255 characters")
    private String tradeName;
    
    @Size(max = 20, message = "Tax number must not exceed 20 characters")
    private String taxNumber;
    
    @Size(max = 50, message = "DERBİS number must not exceed 50 characters")
    private String derbisNumber;
    
    @Size(max = 50, message = "MERSİS number must not exceed 50 characters")
    private String mersisNumber;
    
    private LocalDate establishmentDate;
    
    private String description;
    
    private String missionStatement;
    
    @Size(max = 500, message = "Logo URL must not exceed 500 characters")
    private String logoUrl;
    
    @Size(max = 500, message = "Website URL must not exceed 500 characters")
    @Pattern(regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$", 
             message = "Invalid website URL format")
    private String websiteUrl;
}
```

#### UpdateOrganizationRequest.java
- Same fields as CreateOrganizationRequest but all optional (for partial updates)
- No @NotNull or @NotBlank annotations

#### AddContactRequest.java
```java
@Data
public class AddContactRequest {
    
    @NotBlank(message = "Contact type is required")
    @Pattern(regexp = "^(primary|support|press|other)$", message = "Invalid contact type")
    private String contactType;
    
    @Size(max = 100)
    private String contactName;
    
    @Email(message = "Invalid email format")
    @Size(max = 255)
    private String email;
    
    @Size(max = 20)
    private String phone;
    
    @Size(max = 255)
    private String addressLine1;
    
    @Size(max = 255)
    private String addressLine2;
    
    @Size(max = 100)
    private String city;
    
    @Size(max = 100)
    private String district;
    
    @Size(max = 10)
    private String postalCode;
    
    @Size(max = 100)
    private String country;
    
    private Boolean isPrimary = false;
}
```

#### UpdateContactRequest.java
- Same as AddContactRequest but all optional

#### AddDocumentRequest.java
```java
@Data
public class AddDocumentRequest {
    
    @NotBlank(message = "Document type is required")
    @Pattern(regexp = "^(tax_certificate|authorization|derbis_record|mersis_record|board_decision|other)$",
             message = "Invalid document type")
    private String documentType;
    
    @NotBlank(message = "Document name is required")
    @Size(max = 255)
    private String documentName;
    
    @NotBlank(message = "File URL is required")
    @Size(max = 500)
    private String fileUrl;
    
    private Integer fileSize;
    
    @Size(max = 100)
    private String mimeType;
    
    private LocalDate expiresAt;
}
```

#### AddBankAccountRequest.java
```java
@Data
public class AddBankAccountRequest {
    
    @NotBlank(message = "Bank name is required")
    @Size(max = 100)
    private String bankName;
    
    @Size(max = 5)
    private String bankCode;
    
    @Size(max = 100)
    private String branchName;
    
    @Size(max = 10)
    private String branchCode;
    
    @Size(max = 100)
    private String branchCity;
    
    @Size(max = 100)
    private String branchDistrict;
    
    @NotBlank(message = "Account holder is required")
    @Size(max = 255)
    private String accountHolder;
    
    @Size(max = 30)
    private String accountNumber;
    
    @NotBlank(message = "IBAN is required")
    @Size(min = 26, max = 34, message = "IBAN must be between 26 and 34 characters")
    @Pattern(regexp = "^TR[0-9]{24}$", message = "Invalid Turkish IBAN format")
    private String iban;
    
    @Size(max = 3)
    private String currency = "TRY";
    
    @Size(max = 50)
    private String accountType = "current";
    
    private Boolean isPrimary = false;
}
```

#### UpdateBankAccountRequest.java
- Same as AddBankAccountRequest but all optional except IBAN validation when provided

#### ResubmitVerificationRequest.java
```java
@Data
public class ResubmitVerificationRequest {
    
    private String additionalNotes;
    
    // List of newly uploaded document IDs to include in resubmission
    private List<UUID> newDocumentIds;
}
```

---

### 2. DTOs - Response

**Location:** `src/main/java/com/seffafbagis/api/dto/response/organization/`

#### OrganizationResponse.java
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponse {
    private UUID id;
    private UUID userId;
    private OrganizationType organizationType;
    private String legalName;
    private String tradeName;
    private String taxNumber;
    private String logoUrl;
    private String websiteUrl;
    private VerificationStatus verificationStatus;
    private Boolean isFeatured;
    private LocalDateTime createdAt;
    
    // For display
    private String organizationTypeName;
    private String verificationStatusName;
}
```

#### OrganizationDetailResponse.java
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDetailResponse {
    private UUID id;
    private UUID userId;
    private OrganizationType organizationType;
    private String legalName;
    private String tradeName;
    private String taxNumber;
    private String derbisNumber;
    private String mersisNumber;
    private LocalDate establishmentDate;
    private String description;
    private String missionStatement;
    private String logoUrl;
    private String websiteUrl;
    private VerificationStatus verificationStatus;
    private LocalDateTime verifiedAt;
    private String rejectionReason;
    private Integer resubmissionCount;
    private LocalDateTime lastResubmissionAt;
    private Boolean isFeatured;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Related data
    private List<OrganizationContactResponse> contacts;
    private List<OrganizationDocumentResponse> documents;
    private List<OrganizationBankAccountResponse> bankAccounts;
    
    // Statistics (for dashboard)
    private Long totalCampaigns;
    private Long activeCampaigns;
    private BigDecimal totalDonationsReceived;
    
    // Transparency score (will be populated later when that module is complete)
    private BigDecimal transparencyScore;
}
```

#### OrganizationListResponse.java
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationListResponse {
    private UUID id;
    private OrganizationType organizationType;
    private String legalName;
    private String tradeName;
    private String logoUrl;
    private VerificationStatus verificationStatus;
    private Boolean isFeatured;
    private BigDecimal transparencyScore;
    private Long campaignCount;
    private LocalDateTime createdAt;
}
```

#### OrganizationSummaryResponse.java
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationSummaryResponse {
    private UUID id;
    private String legalName;
    private String logoUrl;
    private BigDecimal transparencyScore;
    private Boolean isVerified;
}
```

#### OrganizationContactResponse.java
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationContactResponse {
    private UUID id;
    private String contactType;
    private String contactName;
    private String email;
    private String phone;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String district;
    private String postalCode;
    private String country;
    private Boolean isPrimary;
    private LocalDateTime createdAt;
}
```

#### OrganizationDocumentResponse.java
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDocumentResponse {
    private UUID id;
    private String documentType;
    private String documentName;
    private String fileUrl;
    private Integer fileSize;
    private String mimeType;
    private Boolean isVerified;
    private LocalDateTime verifiedAt;
    private LocalDate expiresAt;
    private LocalDateTime uploadedAt;
    
    // Computed
    private Boolean isExpired;
    private Boolean isExpiringSoon; // Within 30 days
}
```

#### OrganizationBankAccountResponse.java
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationBankAccountResponse {
    private UUID id;
    private String bankName;
    private String bankCode;
    private String branchName;
    private String branchCode;
    private String branchCity;
    private String branchDistrict;
    private String accountHolder;
    private String accountNumber;
    private String iban;
    private String maskedIban; // TR** **** **** **** **** **34
    private String currency;
    private String accountType;
    private Boolean isPrimary;
    private Boolean isVerified;
    private LocalDateTime createdAt;
}
```

---

### 3. Mapper

**Location:** `src/main/java/com/seffafbagis/api/dto/mapper/OrganizationMapper.java`

```java
@Component
public class OrganizationMapper {
    
    // Organization mappings
    public Organization toEntity(CreateOrganizationRequest request, User user);
    public void updateEntity(Organization entity, UpdateOrganizationRequest request);
    public OrganizationResponse toResponse(Organization entity);
    public OrganizationDetailResponse toDetailResponse(Organization entity);
    public OrganizationListResponse toListResponse(Organization entity);
    public OrganizationSummaryResponse toSummaryResponse(Organization entity);
    public List<OrganizationResponse> toResponseList(List<Organization> entities);
    public List<OrganizationListResponse> toListResponseList(List<Organization> entities);
    
    // Contact mappings
    public OrganizationContact toEntity(AddContactRequest request, Organization organization);
    public void updateEntity(OrganizationContact entity, UpdateContactRequest request);
    public OrganizationContactResponse toResponse(OrganizationContact entity);
    public List<OrganizationContactResponse> toContactResponseList(List<OrganizationContact> entities);
    
    // Document mappings
    public OrganizationDocument toEntity(AddDocumentRequest request, Organization organization);
    public OrganizationDocumentResponse toResponse(OrganizationDocument entity);
    public List<OrganizationDocumentResponse> toDocumentResponseList(List<OrganizationDocument> entities);
    
    // Bank account mappings
    public OrganizationBankAccount toEntity(AddBankAccountRequest request, Organization organization);
    public void updateEntity(OrganizationBankAccount entity, UpdateBankAccountRequest request);
    public OrganizationBankAccountResponse toResponse(OrganizationBankAccount entity);
    public List<OrganizationBankAccountResponse> toBankAccountResponseList(List<OrganizationBankAccount> entities);
    
    // Helper methods
    private String maskIban(String iban);
    private boolean isExpired(LocalDate expiresAt);
    private boolean isExpiringSoon(LocalDate expiresAt);
}
```

Implementation notes:
- Use `maskIban()` to return masked IBAN like `TR** **** **** **** **** **34`
- `isExpired()` checks if expiresAt is before today
- `isExpiringSoon()` checks if expiresAt is within 30 days

---

### 4. Services

**Location:** `src/main/java/com/seffafbagis/api/service/organization/`

#### OrganizationService.java

**IMPORTANT:** This service MUST implement `IOrganizationService` interface from Furkan's work.

```java
@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationService implements IOrganizationService {
    
    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService; // From Furkan
    
    // ========== Public Methods ==========
    
    /**
     * Get all approved organizations for public listing
     */
    public Page<OrganizationListResponse> getApprovedOrganizations(Pageable pageable);
    
    /**
     * Get featured organizations
     */
    public List<OrganizationSummaryResponse> getFeaturedOrganizations();
    
    /**
     * Search organizations by keyword
     */
    public Page<OrganizationListResponse> searchOrganizations(String keyword, Pageable pageable);
    
    /**
     * Get organization by ID (public view - only if approved)
     */
    public OrganizationDetailResponse getOrganizationPublicDetail(UUID id);
    
    // ========== Owner Methods (FOUNDATION role) ==========
    
    /**
     * Get current user's organization
     */
    public OrganizationDetailResponse getMyOrganization();
    
    /**
     * Create organization for current user
     */
    public OrganizationResponse createOrganization(CreateOrganizationRequest request);
    
    /**
     * Update current user's organization
     */
    public OrganizationResponse updateOrganization(UpdateOrganizationRequest request);
    
    /**
     * Submit organization for verification
     */
    public OrganizationResponse submitForVerification();
    
    /**
     * Resubmit after rejection
     */
    public OrganizationResponse resubmitVerification(ResubmitVerificationRequest request);
    
    // ========== IOrganizationService Implementation (Admin) ==========
    
    @Override
    public OrganizationResponse getById(UUID id);
    
    @Override
    public void verify(UUID id, VerifyOrganizationRequest request);
    
    @Override
    public void reject(UUID id, VerifyOrganizationRequest request);
    
    @Override
    public Page<OrganizationResponse> getPendingVerifications(Pageable pageable);
    
    @Override
    public Page<OrganizationResponse> getAllOrganizations(Pageable pageable);
    
    @Override
    public OrganizationDetailResponse getOrganizationDetail(UUID id);
    
    // ========== Helper Methods ==========
    
    private Organization getCurrentUserOrganization();
    private void validateCanSubmitForVerification(Organization org);
    private void validateCanResubmit(Organization org);
    private User getCurrentUser();
}
```

**Business Logic:**

1. **createOrganization:**
   - Get current user from SecurityContext
   - Check user has FOUNDATION role
   - Check user doesn't already have an organization
   - Check tax number uniqueness
   - Create organization with status PENDING
   - Create audit log

2. **submitForVerification:**
   - Check organization has required documents
   - Check organization has at least one contact
   - Check organization has at least one bank account
   - Update status to IN_REVIEW
   - Create audit log

3. **verify (admin):**
   - Update status to APPROVED
   - Set verifiedAt and verifiedBy
   - Clear rejection reason
   - Create audit log
   - (Future: Send notification to organization)

4. **reject (admin):**
   - Update status to REJECTED
   - Set rejection reason
   - Create audit log
   - (Future: Send notification to organization)

5. **resubmitVerification:**
   - Check status is REJECTED
   - Check resubmission count < 3
   - Increment resubmission count
   - Update lastResubmissionAt
   - Update status to IN_REVIEW
   - Create audit log

---

#### OrganizationContactService.java

```java
@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationContactService {
    
    private final OrganizationContactRepository contactRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper mapper;
    
    public List<OrganizationContactResponse> getContacts(UUID organizationId);
    
    public OrganizationContactResponse addContact(AddContactRequest request);
    
    public OrganizationContactResponse updateContact(UUID contactId, UpdateContactRequest request);
    
    public void deleteContact(UUID contactId);
    
    public OrganizationContactResponse setPrimaryContact(UUID contactId);
    
    // Helper
    private Organization getCurrentUserOrganization();
    private void validateOwnership(OrganizationContact contact);
}
```

**Business Logic:**
- Only organization owner can manage contacts
- When setting primary, unset previous primary
- Cannot delete last contact

---

#### OrganizationDocumentService.java

```java
@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationDocumentService {
    
    private final OrganizationDocumentRepository documentRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper mapper;
    
    public List<OrganizationDocumentResponse> getDocuments(UUID organizationId);
    
    public OrganizationDocumentResponse addDocument(AddDocumentRequest request);
    
    public void deleteDocument(UUID documentId);
    
    public List<OrganizationDocumentResponse> getUnverifiedDocuments(UUID organizationId);
    
    // Admin methods
    public void verifyDocument(UUID documentId, UUID adminUserId);
    
    // Helper
    private Organization getCurrentUserOrganization();
    private void validateOwnership(OrganizationDocument document);
}
```

**Business Logic:**
- Only organization owner can add/delete documents
- Cannot delete verified documents
- Admin can verify documents
- Track who verified and when

---

#### OrganizationBankAccountService.java

```java
@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationBankAccountService {
    
    private final OrganizationBankAccountRepository bankAccountRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper mapper;
    private final IbanValidator ibanValidator; // From Furkan
    
    public List<OrganizationBankAccountResponse> getBankAccounts(UUID organizationId);
    
    public OrganizationBankAccountResponse addBankAccount(AddBankAccountRequest request);
    
    public OrganizationBankAccountResponse updateBankAccount(UUID accountId, UpdateBankAccountRequest request);
    
    public void deleteBankAccount(UUID accountId);
    
    public OrganizationBankAccountResponse setPrimaryBankAccount(UUID accountId);
    
    // Helper
    private Organization getCurrentUserOrganization();
    private void validateOwnership(OrganizationBankAccount account);
    private void validateIbanUniqueness(String iban, UUID excludeId);
}
```

**Business Logic:**
- Only organization owner can manage bank accounts
- IBAN must be unique across all organizations
- Use IbanValidator from Furkan's validators
- Cannot delete primary bank account if it's the only verified one
- When setting primary, unset previous primary

---

### 5. Controllers

**Location:** `src/main/java/com/seffafbagis/api/controller/organization/`

#### OrganizationController.java

```java
@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
@Tag(name = "Organizations", description = "Organization management endpoints")
public class OrganizationController {
    
    private final OrganizationService organizationService;
    
    // ========== Public Endpoints ==========
    
    @GetMapping
    @Operation(summary = "List approved organizations")
    public ResponseEntity<ApiResponse<Page<OrganizationListResponse>>> getOrganizations(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
            organizationService.getApprovedOrganizations(pageable)));
    }
    
    @GetMapping("/featured")
    @Operation(summary = "Get featured organizations")
    public ResponseEntity<ApiResponse<List<OrganizationSummaryResponse>>> getFeatured() {
        return ResponseEntity.ok(ApiResponse.success(
            organizationService.getFeaturedOrganizations()));
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search organizations")
    public ResponseEntity<ApiResponse<Page<OrganizationListResponse>>> search(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
            organizationService.searchOrganizations(keyword, pageable)));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get organization detail")
    public ResponseEntity<ApiResponse<OrganizationDetailResponse>> getOrganization(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
            organizationService.getOrganizationPublicDetail(id)));
    }
    
    // ========== Owner Endpoints (FOUNDATION role) ==========
    
    @GetMapping("/my")
    @PreAuthorize("hasRole('FOUNDATION')")
    @Operation(summary = "Get my organization")
    public ResponseEntity<ApiResponse<OrganizationDetailResponse>> getMyOrganization() {
        return ResponseEntity.ok(ApiResponse.success(
            organizationService.getMyOrganization()));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('FOUNDATION')")
    @Operation(summary = "Create organization")
    public ResponseEntity<ApiResponse<OrganizationResponse>> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(organizationService.createOrganization(request)));
    }
    
    @PutMapping("/my")
    @PreAuthorize("hasRole('FOUNDATION')")
    @Operation(summary = "Update my organization")
    public ResponseEntity<ApiResponse<OrganizationResponse>> updateOrganization(
            @Valid @RequestBody UpdateOrganizationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
            organizationService.updateOrganization(request)));
    }
    
    @PostMapping("/my/submit-verification")
    @PreAuthorize("hasRole('FOUNDATION')")
    @Operation(summary = "Submit for verification")
    public ResponseEntity<ApiResponse<OrganizationResponse>> submitForVerification() {
        return ResponseEntity.ok(ApiResponse.success(
            organizationService.submitForVerification()));
    }
    
    @PostMapping("/my/resubmit")
    @PreAuthorize("hasRole('FOUNDATION')")
    @Operation(summary = "Resubmit after rejection")
    public ResponseEntity<ApiResponse<OrganizationResponse>> resubmitVerification(
            @Valid @RequestBody ResubmitVerificationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
            organizationService.resubmitVerification(request)));
    }
    
    // ========== Contact Sub-Endpoints ==========
    
    @GetMapping("/my/contacts")
    @PreAuthorize("hasRole('FOUNDATION')")
    public ResponseEntity<ApiResponse<List<OrganizationContactResponse>>> getContacts();
    
    @PostMapping("/my/contacts")
    @PreAuthorize("hasRole('FOUNDATION')")
    public ResponseEntity<ApiResponse<OrganizationContactResponse>> addContact(
            @Valid @RequestBody AddContactRequest request);
    
    @PutMapping("/my/contacts/{contactId}")
    @PreAuthorize("hasRole('FOUNDATION')")
    public ResponseEntity<ApiResponse<OrganizationContactResponse>> updateContact(
            @PathVariable UUID contactId,
            @Valid @RequestBody UpdateContactRequest request);
    
    @DeleteMapping("/my/contacts/{contactId}")
    @PreAuthorize("hasRole('FOUNDATION')")
    public ResponseEntity<ApiResponse<Void>> deleteContact(@PathVariable UUID contactId);
    
    @PutMapping("/my/contacts/{contactId}/primary")
    @PreAuthorize("hasRole('FOUNDATION')")
    public ResponseEntity<ApiResponse<OrganizationContactResponse>> setPrimaryContact(
            @PathVariable UUID contactId);
}
```

---

#### OrganizationDocumentController.java

```java
@RestController
@RequestMapping("/api/v1/organizations/my/documents")
@RequiredArgsConstructor
@PreAuthorize("hasRole('FOUNDATION')")
@Tag(name = "Organization Documents", description = "Organization document management")
public class OrganizationDocumentController {
    
    private final OrganizationDocumentService documentService;
    
    @GetMapping
    @Operation(summary = "List my organization's documents")
    public ResponseEntity<ApiResponse<List<OrganizationDocumentResponse>>> getDocuments();
    
    @PostMapping
    @Operation(summary = "Upload document")
    public ResponseEntity<ApiResponse<OrganizationDocumentResponse>> addDocument(
            @Valid @RequestBody AddDocumentRequest request);
    
    @DeleteMapping("/{documentId}")
    @Operation(summary = "Delete document")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable UUID documentId);
}
```

---

#### OrganizationBankAccountController.java

```java
@RestController
@RequestMapping("/api/v1/organizations/my/bank-accounts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('FOUNDATION')")
@Tag(name = "Organization Bank Accounts", description = "Organization bank account management")
public class OrganizationBankAccountController {
    
    private final OrganizationBankAccountService bankAccountService;
    
    @GetMapping
    @Operation(summary = "List my organization's bank accounts")
    public ResponseEntity<ApiResponse<List<OrganizationBankAccountResponse>>> getBankAccounts();
    
    @PostMapping
    @Operation(summary = "Add bank account")
    public ResponseEntity<ApiResponse<OrganizationBankAccountResponse>> addBankAccount(
            @Valid @RequestBody AddBankAccountRequest request);
    
    @PutMapping("/{accountId}")
    @Operation(summary = "Update bank account")
    public ResponseEntity<ApiResponse<OrganizationBankAccountResponse>> updateBankAccount(
            @PathVariable UUID accountId,
            @Valid @RequestBody UpdateBankAccountRequest request);
    
    @DeleteMapping("/{accountId}")
    @Operation(summary = "Delete bank account")
    public ResponseEntity<ApiResponse<Void>> deleteBankAccount(@PathVariable UUID accountId);
    
    @PutMapping("/{accountId}/primary")
    @Operation(summary = "Set as primary bank account")
    public ResponseEntity<ApiResponse<OrganizationBankAccountResponse>> setPrimaryBankAccount(
            @PathVariable UUID accountId);
}
```

---

## API Endpoints Summary

### Public Endpoints (No Auth)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1/organizations | List approved organizations |
| GET | /api/v1/organizations/featured | Get featured organizations |
| GET | /api/v1/organizations/search | Search organizations |
| GET | /api/v1/organizations/{id} | Get organization detail |

### Organization Owner Endpoints (FOUNDATION role)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1/organizations/my | Get my organization |
| POST | /api/v1/organizations | Create organization |
| PUT | /api/v1/organizations/my | Update organization |
| POST | /api/v1/organizations/my/submit-verification | Submit for verification |
| POST | /api/v1/organizations/my/resubmit | Resubmit after rejection |
| GET | /api/v1/organizations/my/contacts | List contacts |
| POST | /api/v1/organizations/my/contacts | Add contact |
| PUT | /api/v1/organizations/my/contacts/{id} | Update contact |
| DELETE | /api/v1/organizations/my/contacts/{id} | Delete contact |
| PUT | /api/v1/organizations/my/contacts/{id}/primary | Set primary |
| GET | /api/v1/organizations/my/documents | List documents |
| POST | /api/v1/organizations/my/documents | Add document |
| DELETE | /api/v1/organizations/my/documents/{id} | Delete document |
| GET | /api/v1/organizations/my/bank-accounts | List bank accounts |
| POST | /api/v1/organizations/my/bank-accounts | Add bank account |
| PUT | /api/v1/organizations/my/bank-accounts/{id} | Update bank account |
| DELETE | /api/v1/organizations/my/bank-accounts/{id} | Delete bank account |
| PUT | /api/v1/organizations/my/bank-accounts/{id}/primary | Set primary |

---

## Verification Workflow

```
┌─────────────────┐
│    PENDING      │ ← Initial state after creation
└────────┬────────┘
         │ submitForVerification()
         ▼
┌─────────────────┐
│   IN_REVIEW     │ ← Waiting for admin review
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌────────┐ ┌────────┐
│APPROVED│ │REJECTED│
└────────┘ └───┬────┘
               │ resubmitVerification() (max 3 times)
               ▼
         ┌─────────────────┐
         │   IN_REVIEW     │
         └─────────────────┘
```

---

## Required Documents for Verification

Organizations must upload these documents before submitting for verification:
1. Tax Certificate (Vergi Levhası) - `tax_certificate`
2. Authorization Certificate (Yetki Belgesi) - `authorization`
3. DERBİS Record (for associations) OR MERSİS Record (for foundations) - `derbis_record` / `mersis_record`

---

## Testing Requirements

### Unit Tests
**Location:** `src/test/java/com/seffafbagis/api/service/organization/`

1. `OrganizationServiceTest.java`
   - Test createOrganization creates with PENDING status
   - Test createOrganization fails if user already has organization
   - Test submitForVerification fails without required documents
   - Test submitForVerification changes status to IN_REVIEW
   - Test verify changes status to APPROVED
   - Test reject changes status to REJECTED
   - Test resubmitVerification increments count
   - Test resubmitVerification fails after 3 attempts

2. `OrganizationContactServiceTest.java`
   - Test addContact creates contact
   - Test setPrimaryContact unsets previous primary
   - Test deleteContact fails if last contact

3. `OrganizationBankAccountServiceTest.java`
   - Test addBankAccount validates IBAN
   - Test IBAN uniqueness check works
   - Test setPrimaryBankAccount works

### Integration Tests
**Location:** `src/test/java/com/seffafbagis/api/integration/`

1. `OrganizationIntegrationTest.java`
   - Test full verification workflow
   - Test public endpoints return only approved
   - Test owner endpoints require FOUNDATION role
   - Test search functionality

---

## Success Criteria

Before completing this phase, verify:

- [ ] All 8 request DTOs created with proper validation
- [ ] All 7 response DTOs created with builders
- [ ] OrganizationMapper handles all conversions
- [ ] OrganizationService implements IOrganizationService interface
- [ ] All verification workflow methods implemented
- [ ] OrganizationContactService complete
- [ ] OrganizationDocumentService complete
- [ ] OrganizationBankAccountService complete
- [ ] OrganizationController with all public and owner endpoints
- [ ] OrganizationDocumentController complete
- [ ] OrganizationBankAccountController complete
- [ ] IBAN validation using Furkan's IbanValidator
- [ ] Audit logging on important actions
- [ ] All unit tests pass
- [ ] All integration tests pass
- [ ] Swagger UI shows all endpoints correctly
- [ ] Authorization working (FOUNDATION role required for owner endpoints)

---

## Result File Requirement

After completing this phase, you MUST create a result file at:

**Location:** `docs/Emir/step_results/phase_3.0_result.md`

The result file must include:

1. **Summary**: Brief description of what was accomplished
2. **Files Created**: List all files created with their paths
3. **API Endpoints**: Complete list of all endpoints
4. **Interface Implementation**: Confirm IOrganizationService is implemented
5. **Testing Results**: Which tests were run and their status
6. **Issues Encountered**: Any problems faced and how they were resolved
7. **Next Steps**: What needs to be done in the next phase
8. **Checklist**: Mark all success criteria as completed or note what's pending

---

## Important Notes

1. **Do NOT use ternary operators** - Use if-else statements for better readability
2. **Follow existing code style** - Match patterns from Furkan's completed work
3. **Implement IOrganizationService** - This is required for Furkan's admin module
4. **Use SecurityUtils** - Get current user from Furkan's SecurityUtils
5. **Use ApiResponse** - Wrap all responses in Furkan's ApiResponse
6. **Use IbanValidator** - From Furkan's validators
7. **Use AuditLogService** - Log important actions

---

## Dependencies from Furkan's Work

You will need to use these existing components:
- `IOrganizationService` from `service/interfaces/IOrganizationService.java`
- `ApiResponse` from `dto/response/common/ApiResponse.java`
- `SecurityUtils` from `security/SecurityUtils.java`
- `IbanValidator` from `validator/IbanValidator.java`
- `AuditLogService` from `service/audit/AuditLogService.java`
- `ResourceNotFoundException` from `exception/ResourceNotFoundException.java`
- `BadRequestException` from `exception/BadRequestException.java`
- `ForbiddenException` from `exception/ForbiddenException.java`
- `ConflictException` from `exception/ConflictException.java`
- `User` entity from `entity/user/User.java`
- `UserRepository` from `repository/UserRepository.java`

---

## Estimated Duration

3 days

---

## Next Phase

After completing this phase, proceed to:
**Phase 4.0: Campaign Module - Entities & Repository**
