# EMIR BACKEND DEVELOPMENT - COMPLETE PHASE BREAKDOWN

## PROJECT OVERVIEW

**Project**: Şeffaf Bağış Platformu (Transparent Donation Platform)  
**Developer**: Emir Kaan Oğşarim  
**Responsibility**: ~42% of Backend (Organization, Campaign, Donation, Payment, Evidence, Transparency, Application, Category, Notification, Report, Scheduler, Events)  
**Technology Stack**: Java 17, Spring Boot 3.x, PostgreSQL, Redis, Iyzico Payment Gateway

---

## DEPENDENCIES FROM FURKAN'S WORK

Emir's phases depend on the following components completed by Furkan:

| Component | Description | Status |
|-----------|-------------|--------|
| BaseEntity | Common entity fields (id, createdAt, updatedAt) | ✅ Completed |
| SecurityConfig | Spring Security configuration | ✅ Completed |
| JwtTokenProvider | JWT token operations | ✅ Completed |
| GlobalExceptionHandler | Centralized exception handling | ✅ Completed |
| ApiResponse, ErrorResponse, PageResponse | Common response DTOs | ✅ Completed |
| UserRepository, User Entity | User management | ✅ Completed |
| EncryptionService | AES-256 encryption | ✅ Completed |
| SlugGenerator | URL-friendly slug generation | ✅ Completed |
| ReferenceCodeGenerator | Bank transfer reference codes | ✅ Completed |
| AuditLogService | Audit logging | ✅ Completed |
| EmailService | Email sending service | ✅ Completed |
| IOrganizationService Interface | Interface for admin module | ✅ Completed |
| ICampaignService Interface | Interface for admin module | ✅ Completed |

---

## PHASE SUMMARY TABLE

| Phase | Name | Description | Estimated Duration | Dependencies |
|-------|------|-------------|-------------------|--------------|
| 1.0 | Category & Donation Type Module | Base categories and donation types | 2 days | Furkan Phase 1-4 |
| 2.0 | Organization Module - Entities & Repository | Organization entities and data access | 2 days | Phase 1.0 |
| 3.0 | Organization Module - Service & Controller | Organization business logic and API | 3 days | Phase 2.0 |
| 4.0 | Campaign Module - Entities & Repository | Campaign entities and data access | 2 days | Phase 3.0 |
| 5.0 | Campaign Module - Service & Controller | Campaign business logic and API | 3 days | Phase 4.0 |
| 6.0 | Donation Module - Entities & Repository | Donation entities and data access | 2 days | Phase 5.0 |
| 7.0 | Donation Module - Service & Controller (Core) | Core donation operations | 3 days | Phase 6.0 |
| 8.0 | Donation Module - Extended | Recurring donations, Bank transfers | 2 days | Phase 7.0 |
| 9.0 | Payment Module (Iyzico Integration) | Payment gateway integration | 4 days | Phase 7.0 |
| 10.0 | Evidence Module | Evidence upload and management | 3 days | Phase 5.0 |
| 11.0 | Transparency Score Module | Transparency scoring algorithm | 3 days | Phase 10.0 |
| 12.0 | Application Module | Aid applications management | 2 days | Phase 1.0 |
| 13.0 | Notification Module | User notifications | 2 days | Phase 5.0 |
| 14.0 | Report Module | Fraud/complaint reports | 2 days | Phase 3.0 |
| 15.0 | Scheduler Module | Scheduled tasks | 2 days | Phase 8.0, 10.0, 11.0 |
| 16.0 | Event System & Integration | Event-driven architecture | 2 days | All Previous |
| 17.0 | Integration Testing & Final Polish | Testing and documentation | 3 days | All Phases |

**Total Estimated Duration**: 42 days (~8-9 weeks)

---

## DEPENDENCY GRAPH

```
Furkan's Completed Work (Foundation)
            ↓
    Phase 1.0 (Category & Donation Type)
            ↓
    Phase 2.0 (Organization Entities)
            ↓
    Phase 3.0 (Organization Service/Controller)
            ↓
    Phase 4.0 (Campaign Entities)
            ↓
    Phase 5.0 (Campaign Service/Controller)
        ↓           ↓           ↓
Phase 6.0      Phase 10.0   Phase 13.0
(Donation)    (Evidence)   (Notification)
    ↓              ↓
Phase 7.0      Phase 11.0
(Donation Core) (Transparency)
    ↓
Phase 8.0 (Donation Extended)
    ↓
Phase 9.0 (Payment/Iyzico)

Phase 12.0 (Application) ← depends on Phase 1.0
Phase 14.0 (Report) ← depends on Phase 3.0

Phase 15.0 (Scheduler) ← depends on Phase 8.0, 10.0, 11.0
Phase 16.0 (Events) ← depends on all previous
Phase 17.0 (Testing) ← depends on all
```

---

## DETAILED PHASE BREAKDOWN

---

# PHASE 1.0: CATEGORY & DONATION TYPE MODULE

## Overview
This phase creates the foundational Category and DonationType entities that other modules (Campaign, Donation, Application) depend on. Categories organize campaigns (Education, Health, Food, etc.), while DonationTypes define religious/social donation rules (Zakat, Fitr, Sadaka, etc.).

## Problems Being Solved
- Campaigns need categorization for filtering and discovery
- Donations need type classification for religious compliance (Zakat rules, Fitr calculations)
- Categories support hierarchical structure (parent-child)
- System needs predefined donation types with rules

## Components to Create

### 1.1 Enum Types
**Location**: `src/main/java/com/seffafbagis/api/enums/`

| Enum | Purpose |
|------|---------|
| `DonationTypeCode.java` | ZEKAT, FITRE, SADAKA, KURBAN, GENEL, AFET |

### 1.2 Entities
**Location**: `src/main/java/com/seffafbagis/api/entity/category/`

| Entity | Purpose |
|--------|---------|
| `Category.java` | Campaign categories with hierarchy |
| `DonationType.java` | Donation type definitions with rules |

### 1.3 Repositories
**Location**: `src/main/java/com/seffafbagis/api/repository/`

| Repository | Purpose |
|------------|---------|
| `CategoryRepository.java` | Category CRUD and tree operations |
| `DonationTypeRepository.java` | Donation type access |

### 1.4 DTOs
**Location**: `src/main/java/com/seffafbagis/api/dto/`

| DTO | Purpose |
|-----|---------|
| `request/category/CreateCategoryRequest.java` | Category creation |
| `request/category/UpdateCategoryRequest.java` | Category update |
| `response/category/CategoryResponse.java` | Category data |
| `response/category/CategoryTreeResponse.java` | Hierarchical category |
| `response/category/DonationTypeResponse.java` | Donation type data |

### 1.5 Mapper
**Location**: `src/main/java/com/seffafbagis/api/dto/mapper/`

| Class | Purpose |
|-------|---------|
| `CategoryMapper.java` | Category entity to DTO conversion |

### 1.6 Services
**Location**: `src/main/java/com/seffafbagis/api/service/category/`

| Class | Purpose |
|-------|---------|
| `CategoryService.java` | Category business logic |
| `DonationTypeService.java` | Donation type operations |

### 1.7 Controllers
**Location**: `src/main/java/com/seffafbagis/api/controller/category/`

| Class | Purpose |
|-------|---------|
| `CategoryController.java` | Category REST endpoints |
| `DonationTypeController.java` | Donation type endpoints |

## API Endpoints
```
GET    /api/v1/categories                  - List all active categories
GET    /api/v1/categories/tree             - Get category tree structure
GET    /api/v1/categories/{id}             - Get category by ID
GET    /api/v1/categories/slug/{slug}      - Get category by slug
POST   /api/v1/categories                  - Create category (ADMIN)
PUT    /api/v1/categories/{id}             - Update category (ADMIN)
DELETE /api/v1/categories/{id}             - Deactivate category (ADMIN)

GET    /api/v1/donation-types              - List all donation types
GET    /api/v1/donation-types/{code}       - Get by type code
GET    /api/v1/donation-types/active       - List active types only
```

## Database Seed Data
Initial categories and donation types must be seeded:
- Categories: Eğitim, Sağlık, Gıda, Barınma, Afet Yardımı, Çocuk, Yaşlı, Engelli
- Donation Types: Zekat, Fitre, Sadaka, Kurban, Genel, Afet

## Expected Outputs
- Category and DonationType entities mapped correctly
- Hierarchical category structure working
- Donation type rules defined
- All endpoints returning correct data
- Initial seed data loaded

## Success Criteria
- [ ] Categories support parent-child hierarchy
- [ ] Slug generated automatically for categories
- [ ] Donation types have rule definitions
- [ ] Category tree returns proper nested structure
- [ ] Admin-only endpoints protected
- [ ] Deactivating category doesn't delete it

## Files to Create (Count: 13)
```
src/main/java/com/seffafbagis/api/
├── enums/
│   └── DonationTypeCode.java
├── entity/category/
│   ├── Category.java
│   └── DonationType.java
├── repository/
│   ├── CategoryRepository.java
│   └── DonationTypeRepository.java
├── dto/
│   ├── request/category/
│   │   ├── CreateCategoryRequest.java
│   │   └── UpdateCategoryRequest.java
│   ├── response/category/
│   │   ├── CategoryResponse.java
│   │   ├── CategoryTreeResponse.java
│   │   └── DonationTypeResponse.java
│   └── mapper/
│       └── CategoryMapper.java
├── service/category/
│   ├── CategoryService.java
│   └── DonationTypeService.java
└── controller/category/
    ├── CategoryController.java
    └── DonationTypeController.java
```

## Testing Requirements
- Unit tests for CategoryService
- Unit tests for DonationTypeService
- Integration test for category tree structure
- Test seed data loads correctly

## Result File
After completing this phase, create:
`docs/Emir/step_results/phase_1.0_result.md`

---

# PHASE 2.0: ORGANIZATION MODULE - ENTITIES & REPOSITORY

## Overview
This phase creates all Organization-related entities, enums, and repository interfaces. Organizations represent foundations and associations that create campaigns and receive donations.

## Problems Being Solved
- Foundations/associations need registration and verification
- Organizations have multiple contacts, documents, and bank accounts
- Verification status must be tracked
- Documents have expiry dates and need validation
- Bank accounts must store IBAN and branch info

## Components to Create

### 2.1 Enum Types
**Location**: `src/main/java/com/seffafbagis/api/enums/`

| Enum | Purpose |
|------|---------|
| `OrganizationType.java` | FOUNDATION, ASSOCIATION, NGO |
| `VerificationStatus.java` | PENDING, IN_REVIEW, APPROVED, REJECTED |

### 2.2 Entities
**Location**: `src/main/java/com/seffafbagis/api/entity/organization/`

| Entity | Purpose |
|--------|---------|
| `Organization.java` | Main organization entity |
| `OrganizationContact.java` | Contact information (multiple per org) |
| `OrganizationDocument.java` | Uploaded documents (tax cert, etc.) |
| `OrganizationBankAccount.java` | Bank account details |

### 2.3 Repositories
**Location**: `src/main/java/com/seffafbagis/api/repository/`

| Repository | Purpose |
|------------|---------|
| `OrganizationRepository.java` | Organization CRUD + search |
| `OrganizationContactRepository.java` | Contact operations |
| `OrganizationDocumentRepository.java` | Document operations |
| `OrganizationBankAccountRepository.java` | Bank account operations |

## Entity Relationships
```
Organization (1) ──── (1) User (foreign key to users table)
     │
     ├─── (N) OrganizationContact
     │
     ├─── (N) OrganizationDocument
     │
     └─── (N) OrganizationBankAccount
```

## Key Entity Fields

### Organization
- id, user_id, organization_type, legal_name, trade_name
- tax_number, derbis_number, mersis_number
- establishment_date, description, mission_statement
- logo_url, website_url
- verification_status, verified_at, verified_by
- rejection_reason, resubmission_count
- is_featured, created_at, updated_at

### OrganizationContact
- id, organization_id, contact_type, contact_name
- email, phone, address_line1, address_line2
- city, district, postal_code, country
- is_primary

### OrganizationDocument
- id, organization_id, document_type, document_name
- file_url, file_size, mime_type
- is_verified, verified_at, verified_by
- expires_at, uploaded_at

### OrganizationBankAccount
- id, organization_id, bank_name, bank_code
- branch_name, branch_code, branch_city, branch_district
- account_holder, account_number, iban
- currency, account_type, is_primary, is_verified

## Expected Outputs
- All entities mapped to database tables
- Repositories provide query methods
- Proper indexes created
- Foreign key constraints working

## Success Criteria
- [ ] All entities compile without errors
- [ ] Relationships properly defined with JPA annotations
- [ ] Organization links to User correctly
- [ ] Multiple contacts/documents/accounts per organization work
- [ ] Repository custom queries function correctly

## Files to Create (Count: 10)
```
src/main/java/com/seffafbagis/api/
├── enums/
│   ├── OrganizationType.java
│   └── VerificationStatus.java
├── entity/organization/
│   ├── Organization.java
│   ├── OrganizationContact.java
│   ├── OrganizationDocument.java
│   └── OrganizationBankAccount.java
└── repository/
    ├── OrganizationRepository.java
    ├── OrganizationContactRepository.java
    ├── OrganizationDocumentRepository.java
    └── OrganizationBankAccountRepository.java
```

## Testing Requirements
- Entity mapping tests
- Repository query tests
- Relationship cascade tests
- Index verification

## Result File
After completing this phase, create:
`docs/Emir/step_results/phase_2.0_result.md`

---

# PHASE 3.0: ORGANIZATION MODULE - SERVICE & CONTROLLER

## Overview
This phase implements the business logic and REST API for organization management, including registration, verification workflow, document management, and bank account handling.

## Problems Being Solved
- Users with FOUNDATION role need to register organizations
- Organizations need document upload for verification
- Admin needs to verify/reject organizations
- Organizations need to manage bank accounts
- Contact information needs CRUD operations

## Components to Create

### 3.1 DTOs - Request
**Location**: `src/main/java/com/seffafbagis/api/dto/request/organization/`

| DTO | Purpose |
|-----|---------|
| `CreateOrganizationRequest.java` | Initial registration |
| `UpdateOrganizationRequest.java` | Profile updates |
| `AddContactRequest.java` | Add contact info |
| `UpdateContactRequest.java` | Update contact |
| `AddDocumentRequest.java` | Document upload metadata |
| `AddBankAccountRequest.java` | Bank account details |
| `UpdateBankAccountRequest.java` | Bank account update |
| `ResubmitVerificationRequest.java` | Resubmission after rejection |

### 3.2 DTOs - Response
**Location**: `src/main/java/com/seffafbagis/api/dto/response/organization/`

| DTO | Purpose |
|-----|---------|
| `OrganizationResponse.java` | Basic org info |
| `OrganizationDetailResponse.java` | Full org details |
| `OrganizationListResponse.java` | List item |
| `OrganizationSummaryResponse.java` | Summary for cards |
| `OrganizationContactResponse.java` | Contact details |
| `OrganizationDocumentResponse.java` | Document info |
| `OrganizationBankAccountResponse.java` | Bank account info |

### 3.3 Mapper
**Location**: `src/main/java/com/seffafbagis/api/dto/mapper/`

| Class | Purpose |
|-------|---------|
| `OrganizationMapper.java` | All organization mappings |

### 3.4 Services
**Location**: `src/main/java/com/seffafbagis/api/service/organization/`

| Class | Purpose |
|-------|---------|
| `OrganizationService.java` | Main organization logic (implements IOrganizationService) |
| `OrganizationContactService.java` | Contact management |
| `OrganizationDocumentService.java` | Document handling |
| `OrganizationBankAccountService.java` | Bank account operations |

### 3.5 Controllers
**Location**: `src/main/java/com/seffafbagis/api/controller/organization/`

| Class | Purpose |
|-------|---------|
| `OrganizationController.java` | Main org endpoints |
| `OrganizationDocumentController.java` | Document endpoints |
| `OrganizationBankAccountController.java` | Bank account endpoints |

## API Endpoints

### Public Endpoints
```
GET    /api/v1/organizations                     - List verified organizations
GET    /api/v1/organizations/{id}                - Get organization detail
GET    /api/v1/organizations/featured            - Get featured organizations
GET    /api/v1/organizations/search              - Search organizations
```

### Organization Owner Endpoints (FOUNDATION role)
```
GET    /api/v1/organizations/my                  - Get own organization
POST   /api/v1/organizations                     - Create organization
PUT    /api/v1/organizations/my                  - Update own organization
POST   /api/v1/organizations/my/resubmit         - Resubmit for verification

GET    /api/v1/organizations/my/contacts         - List contacts
POST   /api/v1/organizations/my/contacts         - Add contact
PUT    /api/v1/organizations/my/contacts/{id}    - Update contact
DELETE /api/v1/organizations/my/contacts/{id}    - Delete contact

GET    /api/v1/organizations/my/documents        - List documents
POST   /api/v1/organizations/my/documents        - Upload document
DELETE /api/v1/organizations/my/documents/{id}   - Delete document

GET    /api/v1/organizations/my/bank-accounts    - List bank accounts
POST   /api/v1/organizations/my/bank-accounts    - Add bank account
PUT    /api/v1/organizations/my/bank-accounts/{id} - Update bank account
DELETE /api/v1/organizations/my/bank-accounts/{id} - Delete bank account
```

## Verification Workflow
```
1. User registers with FOUNDATION role
2. Creates organization (status: PENDING)
3. Uploads required documents
4. Submits for verification (status: IN_REVIEW)
5. Admin reviews documents
6. Admin approves → status: APPROVED, organization can create campaigns
   OR Admin rejects → status: REJECTED with reason
7. If rejected, organization can fix issues and resubmit (max 3 times)
```

## Required Documents
- Tax Certificate (Vergi Levhası)
- Authorization Certificate (Yetki Belgesi)
- DERBİS/MERSİS Record
- Board Decision (Yönetim Kurulu Kararı)
- Bank Account Confirmation

## Expected Outputs
- Organization registration working
- Document upload and management
- Bank account CRUD operations
- Verification workflow complete
- Search and filtering working

## Success Criteria
- [ ] FOUNDATION user can create organization
- [ ] Organization linked to user correctly
- [ ] Document upload stores metadata
- [ ] Bank account IBAN validated
- [ ] Only APPROVED organizations visible publicly
- [ ] Rejection reason stored and displayed
- [ ] Resubmission count tracked
- [ ] IOrganizationService interface implemented

## Files to Create (Count: 22)
```
src/main/java/com/seffafbagis/api/
├── dto/
│   ├── request/organization/
│   │   ├── CreateOrganizationRequest.java
│   │   ├── UpdateOrganizationRequest.java
│   │   ├── AddContactRequest.java
│   │   ├── UpdateContactRequest.java
│   │   ├── AddDocumentRequest.java
│   │   ├── AddBankAccountRequest.java
│   │   ├── UpdateBankAccountRequest.java
│   │   └── ResubmitVerificationRequest.java
│   ├── response/organization/
│   │   ├── OrganizationResponse.java
│   │   ├── OrganizationDetailResponse.java
│   │   ├── OrganizationListResponse.java
│   │   ├── OrganizationSummaryResponse.java
│   │   ├── OrganizationContactResponse.java
│   │   ├── OrganizationDocumentResponse.java
│   │   └── OrganizationBankAccountResponse.java
│   └── mapper/
│       └── OrganizationMapper.java
├── service/organization/
│   ├── OrganizationService.java
│   ├── OrganizationContactService.java
│   ├── OrganizationDocumentService.java
│   └── OrganizationBankAccountService.java
└── controller/organization/
    ├── OrganizationController.java
    ├── OrganizationDocumentController.java
    └── OrganizationBankAccountController.java
```

## Testing Requirements
- Service unit tests
- Controller integration tests
- Verification workflow test
- Document upload test
- Authorization tests (only owner can modify)

## Result File
After completing this phase, create:
`docs/Emir/step_results/phase_3.0_result.md`

---

# PHASE 4.0: CAMPAIGN MODULE - ENTITIES & REPOSITORY

## Overview
This phase creates Campaign-related entities, enums, and repositories. Campaigns are fundraising initiatives created by verified organizations.

## Problems Being Solved
- Organizations need to create donation campaigns
- Campaigns have categories and donation types (many-to-many)
- Campaign status lifecycle needs tracking
- Campaign updates (news) for donors
- Campaign images (gallery) for presentation
- Campaign followers for notifications

## Components to Create

### 4.1 Enum Types
**Location**: `src/main/java/com/seffafbagis/api/enums/`

| Enum | Purpose |
|------|---------|
| `CampaignStatus.java` | DRAFT, PENDING_APPROVAL, ACTIVE, PAUSED, COMPLETED, CANCELLED |

### 4.2 Entities
**Location**: `src/main/java/com/seffafbagis/api/entity/campaign/`

| Entity | Purpose |
|--------|---------|
| `Campaign.java` | Main campaign entity |
| `CampaignCategory.java` | Many-to-many with Category |
| `CampaignDonationType.java` | Many-to-many with DonationType |
| `CampaignUpdate.java` | Campaign news/updates |
| `CampaignImage.java` | Campaign gallery images |
| `CampaignFollower.java` | Users following campaign |

### 4.3 Repositories
**Location**: `src/main/java/com/seffafbagis/api/repository/`

| Repository | Purpose |
|------------|---------|
| `CampaignRepository.java` | Campaign CRUD + search |
| `CampaignCategoryRepository.java` | Category relationships |
| `CampaignDonationTypeRepository.java` | Donation type relationships |
| `CampaignUpdateRepository.java` | Update operations |
| `CampaignImageRepository.java` | Image operations |
| `CampaignFollowerRepository.java` | Follower operations |

## Entity Relationships
```
Campaign (N) ──── (1) Organization
    │
    ├─── (N) CampaignCategory ──── (N) Category
    │
    ├─── (N) CampaignDonationType ──── (N) DonationType
    │
    ├─── (N) CampaignUpdate
    │
    ├─── (N) CampaignImage
    │
    └─── (N) CampaignFollower ──── (N) User
```

## Key Entity Fields

### Campaign
- id, organization_id, title, slug, summary, description
- cover_image_url, target_amount, collected_amount, donor_count
- currency, status, start_date, end_date
- evidence_deadline_days, is_urgent, is_featured
- location_city, location_district, beneficiary_count
- created_by, approved_by, approved_at, completed_at
- default_bank_account_id

### CampaignCategory (Composite Key)
- campaign_id, category_id, is_primary

### CampaignDonationType (Composite Key)
- campaign_id, donation_type_id

### CampaignUpdate
- id, campaign_id, title, content, image_url, created_by

### CampaignImage
- id, campaign_id, image_url, thumbnail_url, caption, display_order

### CampaignFollower (Composite Key)
- user_id, campaign_id, notify_on_update, notify_on_complete

## Repository Custom Queries
```java
// CampaignRepository
List<Campaign> findByOrganizationIdAndStatus(UUID orgId, CampaignStatus status);
Page<Campaign> findByStatusAndCategoriesSlug(CampaignStatus status, String slug, Pageable pageable);
List<Campaign> findByIsFeaturedTrueAndStatus(CampaignStatus status);
Optional<Campaign> findBySlug(String slug);
List<Campaign> findByEndDateBeforeAndStatus(LocalDateTime date, CampaignStatus status);

// CampaignFollowerRepository
boolean existsByUserIdAndCampaignId(UUID userId, UUID campaignId);
List<CampaignFollower> findByCampaignIdAndNotifyOnUpdateTrue(UUID campaignId);
```

## Expected Outputs
- All entities mapped correctly
- Many-to-many relationships working
- Repository queries functional
- Composite keys properly defined

## Success Criteria
- [ ] Campaign links to Organization correctly
- [ ] Many-to-many with Category works
- [ ] Many-to-many with DonationType works
- [ ] Composite key entities compile
- [ ] Slug field unique constraint
- [ ] Custom queries return correct results

## Files to Create (Count: 13)
```
src/main/java/com/seffafbagis/api/
├── enums/
│   └── CampaignStatus.java
├── entity/campaign/
│   ├── Campaign.java
│   ├── CampaignCategory.java
│   ├── CampaignDonationType.java
│   ├── CampaignUpdate.java
│   ├── CampaignImage.java
│   └── CampaignFollower.java
└── repository/
    ├── CampaignRepository.java
    ├── CampaignCategoryRepository.java
    ├── CampaignDonationTypeRepository.java
    ├── CampaignUpdateRepository.java
    ├── CampaignImageRepository.java
    └── CampaignFollowerRepository.java
```

## Testing Requirements
- Entity mapping tests
- Many-to-many relationship tests
- Composite key tests
- Custom query tests

## Result File
After completing this phase, create:
`docs/Emir/step_results/phase_4.0_result.md`

---

# PHASE 5.0: CAMPAIGN MODULE - SERVICE & CONTROLLER

## Overview
This phase implements campaign business logic, REST API, and integrates with Furkan's admin approval interface (ICampaignService).

## Problems Being Solved
- Organizations need to create/manage campaigns
- Campaigns need approval workflow
- Public needs to discover and filter campaigns
- Users need to follow campaigns
- Campaign updates need publishing
- Campaign images need gallery management

## Components to Create

### 5.1 DTOs - Request
**Location**: `src/main/java/com/seffafbagis/api/dto/request/campaign/`

| DTO | Purpose |
|-----|---------|
| `CreateCampaignRequest.java` | New campaign data |
| `UpdateCampaignRequest.java` | Campaign modifications |
| `SubmitForApprovalRequest.java` | Submit draft for approval |
| `AddCampaignUpdateRequest.java` | Post campaign update |
| `AddCampaignImageRequest.java` | Add gallery image |
| `CampaignSearchRequest.java` | Search/filter criteria |

### 5.2 DTOs - Response
**Location**: `src/main/java/com/seffafbagis/api/dto/response/campaign/`

| DTO | Purpose |
|-----|---------|
| `CampaignResponse.java` | Basic campaign info |
| `CampaignDetailResponse.java` | Full campaign with all relations |
| `CampaignListResponse.java` | List item for cards |
| `CampaignSummaryResponse.java` | Minimal summary |
| `CampaignUpdateResponse.java` | Campaign update/news |
| `CampaignImageResponse.java` | Gallery image |
| `CampaignStatsResponse.java` | Campaign statistics |
| `CampaignProgressResponse.java` | Progress towards goal |

### 5.3 Mapper
**Location**: `src/main/java/com/seffafbagis/api/dto/mapper/`

| Class | Purpose |
|-------|---------|
| `CampaignMapper.java` | All campaign mappings |

### 5.4 Services
**Location**: `src/main/java/com/seffafbagis/api/service/campaign/`

| Class | Purpose |
|-------|---------|
| `CampaignService.java` | Main campaign logic (implements ICampaignService) |
| `CampaignUpdateService.java` | Campaign news management |
| `CampaignImageService.java` | Gallery management |
| `CampaignFollowerService.java` | Following functionality |

### 5.5 Controllers
**Location**: `src/main/java/com/seffafbagis/api/controller/campaign/`

| Class | Purpose |
|-------|---------|
| `CampaignController.java` | Main campaign endpoints |
| `CampaignUpdateController.java` | Update endpoints |
| `CampaignImageController.java` | Image endpoints |
| `CampaignFollowerController.java` | Follow endpoints |

## API Endpoints

### Public Endpoints
```
GET    /api/v1/campaigns                        - List active campaigns
GET    /api/v1/campaigns/{slug}                 - Get campaign by slug
GET    /api/v1/campaigns/featured               - Featured campaigns
GET    /api/v1/campaigns/urgent                 - Urgent campaigns
GET    /api/v1/campaigns/category/{categorySlug} - By category
GET    /api/v1/campaigns/organization/{orgId}   - By organization
GET    /api/v1/campaigns/search                 - Search campaigns
GET    /api/v1/campaigns/{id}/updates           - Campaign updates
GET    /api/v1/campaigns/{id}/images            - Campaign gallery
GET    /api/v1/campaigns/{id}/stats             - Campaign statistics
```

### Organization Owner Endpoints
```
GET    /api/v1/campaigns/my                     - My organization's campaigns
POST   /api/v1/campaigns                        - Create campaign (DRAFT)
PUT    /api/v1/campaigns/{id}                   - Update campaign
DELETE /api/v1/campaigns/{id}                   - Delete draft campaign
POST   /api/v1/campaigns/{id}/submit            - Submit for approval
PUT    /api/v1/campaigns/{id}/pause             - Pause active campaign
PUT    /api/v1/campaigns/{id}/resume            - Resume paused campaign
PUT    /api/v1/campaigns/{id}/complete          - Mark as completed

POST   /api/v1/campaigns/{id}/updates           - Add campaign update
PUT    /api/v1/campaigns/{id}/updates/{updateId} - Edit update
DELETE /api/v1/campaigns/{id}/updates/{updateId} - Delete update

POST   /api/v1/campaigns/{id}/images            - Add gallery image
PUT    /api/v1/campaigns/{id}/images/reorder    - Reorder images
DELETE /api/v1/campaigns/{id}/images/{imageId}  - Remove image
```

### User Endpoints (Authenticated)
```
POST   /api/v1/campaigns/{id}/follow            - Follow campaign
DELETE /api/v1/campaigns/{id}/follow            - Unfollow campaign
GET    /api/v1/campaigns/following              - My followed campaigns
```

## Campaign Status Workflow
```
DRAFT → PENDING_APPROVAL → ACTIVE → COMPLETED
                ↓              ↓
            REJECTED        PAUSED → ACTIVE
                               ↓
                           CANCELLED
```

## Slug Generation
- Use SlugGenerator from Furkan's utilities
- Auto-generate from title on creation
- Ensure uniqueness by appending number if needed

## Expected Outputs
- Full campaign CRUD operations
- Search and filtering working
- Follow functionality working
- Update/image management working
- Status transitions enforced

## Success Criteria
- [ ] Only APPROVED organizations can create campaigns
- [ ] Slug auto-generated and unique
- [ ] DRAFT campaigns not visible publicly
- [ ] Status transitions follow workflow
- [ ] Following creates notification preferences
- [ ] collected_amount updates via donation service
- [ ] ICampaignService interface implemented

## Files to Create (Count: 20)
```
src/main/java/com/seffafbagis/api/
├── dto/
│   ├── request/campaign/
│   │   ├── CreateCampaignRequest.java
│   │   ├── UpdateCampaignRequest.java
│   │   ├── SubmitForApprovalRequest.java
│   │   ├── AddCampaignUpdateRequest.java
│   │   ├── AddCampaignImageRequest.java
│   │   └── CampaignSearchRequest.java
│   ├── response/campaign/
│   │   ├── CampaignResponse.java
│   │   ├── CampaignDetailResponse.java
│   │   ├── CampaignListResponse.java
│   │   ├── CampaignSummaryResponse.java
│   │   ├── CampaignUpdateResponse.java
│   │   ├── CampaignImageResponse.java
│   │   ├── CampaignStatsResponse.java
│   │   └── CampaignProgressResponse.java
│   └── mapper/
│       └── CampaignMapper.java
├── service/campaign/
│   ├── CampaignService.java
│   ├── CampaignUpdateService.java
│   ├── CampaignImageService.java
│   └── CampaignFollowerService.java
└── controller/campaign/
    ├── CampaignController.java
    ├── CampaignUpdateController.java
    ├── CampaignImageController.java
    └── CampaignFollowerController.java
```

## Testing Requirements
- Service unit tests for all operations
- Controller integration tests
- Status transition tests
- Authorization tests
- Search functionality tests

## Result File
After completing this phase, create:
`docs/Emir/step_results/phase_5.0_result.md`

---

# PHASE 6.0: DONATION MODULE - ENTITIES & REPOSITORY

## Overview
This phase creates Donation-related entities for tracking donations, transactions, receipts, recurring donations, and bank transfer references.

## Problems Being Solved
- Donations need tracking with amount, status, type
- Payment transactions need separate record
- Digital receipts need generation
- Recurring donations (monthly/weekly) support
- Bank transfer (Havale/EFT) donations need reference codes

## Components to Create

### 6.1 Enum Types
**Location**: `src/main/java/com/seffafbagis/api/enums/`

| Enum | Purpose |
|------|---------|
| `DonationStatus.java` | PENDING, COMPLETED, FAILED, REFUNDED |
| `PaymentMethod.java` | CREDIT_CARD, BANK_TRANSFER, MOBILE_PAYMENT |

### 6.2 Entities
**Location**: `src/main/java/com/seffafbagis/api/entity/donation/`

| Entity | Purpose |
|--------|---------|
| `Donation.java` | Main donation record |
| `Transaction.java` | Payment provider transaction |
| `DonationReceipt.java` | Digital receipt |
| `RecurringDonation.java` | Recurring donation subscription |
| `BankTransferReference.java` | Bank transfer matching reference |

### 6.3 Repositories
**Location**: `src/main/java/com/seffafbagis/api/repository/`

| Repository | Purpose |
|------------|---------|
| `DonationRepository.java` | Donation CRUD + queries |
| `TransactionRepository.java` | Transaction operations |
| `DonationReceiptRepository.java` | Receipt operations |
| `RecurringDonationRepository.java` | Recurring donation operations |
| `BankTransferReferenceRepository.java` | Bank transfer matching |

## Entity Relationships
```
Donation (N) ──── (1) Campaign
    │
    ├─── (N) ──── (1) User (donor, optional for anonymous)
    │
    ├─── (N) ──── (1) DonationType
    │
    ├─── (1) ──── (1) Transaction
    │
    └─── (1) ──── (1) DonationReceipt

RecurringDonation (N) ──── (1) User
                   │
                   └─── (N) Donation (generated donations)

BankTransferReference (1) ──── (1) Donation (after matching)
```

## Key Entity Fields

### Donation
- id, campaign_id, donor_id (nullable), donation_type_id
- amount, currency, status, is_anonymous
- donor_message, donor_display_name
- ip_address, user_agent, source
- refund_status, refund_reason, refund_requested_at
- created_at, updated_at

### Transaction
- id, donation_id, payment_method, payment_provider
- provider_transaction_id, provider_payment_id
- amount, fee_amount, net_amount
- installment_count, currency, status
- error_code, error_message
- card_last_four, card_brand, is_3d_secure
- raw_response (JSONB), processed_at

### DonationReceipt
- id, donation_id, receipt_number, receipt_url, issued_at

### RecurringDonation
- id, donor_id, campaign_id (nullable), organization_id
- donation_type_id, amount, currency, frequency
- next_payment_date, last_payment_date
- total_donated, payment_count, status
- card_token, failure_count, last_error_message

### BankTransferReference
- id, reference_code, campaign_id, organization_id
- bank_account_id, donor_id, expected_amount
- donation_type_id, sender_name, sender_iban
- bank_account_snapshot (JSONB), status
- matched_donation_id, expires_at

## Repository Custom Queries
```java
// DonationRepository
List<Donation> findByCampaignIdAndStatus(UUID campaignId, DonationStatus status);
List<Donation> findByDonorIdOrderByCreatedAtDesc(UUID donorId);
BigDecimal sumAmountByCampaignIdAndStatus(UUID campaignId, DonationStatus status);

// RecurringDonationRepository
List<RecurringDonation> findByStatusAndNextPaymentDateBefore(String status, LocalDate date);

// BankTransferReferenceRepository
Optional<BankTransferReference> findByReferenceCodeAndStatus(String code, String status);
List<BankTransferReference> findByStatusAndExpiresAtBefore(String status, LocalDateTime date);
```

## Expected Outputs
- All donation entities mapped
- Transaction linked to donation
- Receipt generation ready
- Recurring donation subscription model ready
- Bank transfer reference system ready

## Success Criteria
- [ ] Donation-Campaign relationship works
- [ ] Anonymous donations (null donor_id) supported
- [ ] Transaction stores payment provider response
- [ ] Receipt number unique
- [ ] Recurring donation frequency types work
- [ ] Bank transfer reference code unique

## Files to Create (Count: 12)
```
src/main/java/com/seffafbagis/api/
├── enums/
│   ├── DonationStatus.java
│   └── PaymentMethod.java
├── entity/donation/
│   ├── Donation.java
│   ├── Transaction.java
│   ├── DonationReceipt.java
│   ├── RecurringDonation.java
│   └── BankTransferReference.java
└── repository/
    ├── DonationRepository.java
    ├── TransactionRepository.java
    ├── DonationReceiptRepository.java
    ├── RecurringDonationRepository.java
    └── BankTransferReferenceRepository.java
```

## Testing Requirements
- Entity relationship tests
- Repository query tests
- Amount calculation tests
- Reference code uniqueness tests

## Result File
After completing this phase, create:
`docs/Emir/step_results/phase_6.0_result.md`

---

# PHASE 7.0: DONATION MODULE - SERVICE & CONTROLLER (CORE)

## Overview
This phase implements core donation functionality: creating donations, processing payments, generating receipts, and updating campaign statistics.

## Problems Being Solved
- Users need to donate to campaigns
- Payment processing integration needed
- Digital receipts need automatic generation
- Campaign collected_amount needs updating
- Donation history for users
- Anonymous donations need handling

## Components to Create

### 7.1 DTOs - Request
**Location**: `src/main/java/com/seffafbagis/api/dto/request/donation/`

| DTO | Purpose |
|-----|---------|
| `CreateDonationRequest.java` | New donation data |
| `ProcessPaymentRequest.java` | Payment details |
| `RefundRequest.java` | Refund request |

### 7.2 DTOs - Response
**Location**: `src/main/java/com/seffafbagis/api/dto/response/donation/`

| DTO | Purpose |
|-----|---------|
| `DonationResponse.java` | Donation data |
| `DonationDetailResponse.java` | Full donation details |
| `DonationListResponse.java` | List for history |
| `DonationReceiptResponse.java` | Receipt data |
| `DonorListResponse.java` | Campaign donor list |

### 7.3 Mapper
**Location**: `src/main/java/com/seffafbagis/api/dto/mapper/`

| Class | Purpose |
|-------|---------|
| `DonationMapper.java` | Donation mappings |

### 7.4 Services
**Location**: `src/main/java/com/seffafbagis/api/service/donation/`

| Class | Purpose |
|-------|---------|
| `DonationService.java` | Core donation operations |
| `DonationReceiptService.java` | Receipt generation |

### 7.5 Controllers
**Location**: `src/main/java/com/seffafbagis/api/controller/donation/`

| Class | Purpose |
|-------|---------|
| `DonationController.java` | Donation endpoints |

## API Endpoints

### Public Endpoints
```
GET    /api/v1/donations/campaign/{campaignId}/donors - Public donor list
```

### Donation Flow Endpoints
```
POST   /api/v1/donations                    - Create donation (initiate)
POST   /api/v1/donations/{id}/process       - Process payment
GET    /api/v1/donations/{id}               - Get donation details
GET    /api/v1/donations/{id}/receipt       - Get receipt (PDF)
```

### User Endpoints (Authenticated)
```
GET    /api/v1/donations/my                 - My donation history
GET    /api/v1/donations/my/{id}            - My donation detail
POST   /api/v1/donations/{id}/refund        - Request refund
```

### Organization Endpoints
```
GET    /api/v1/donations/organization       - Organization's received donations
GET    /api/v1/donations/campaign/{id}      - Campaign donations
```

## Donation Flow
```
1. User selects campaign, amount, donation type
2. CreateDonationRequest → DonationService.create()
3. Donation created with status PENDING
4. User provides payment info
5. ProcessPaymentRequest → PaymentService.process() (Phase 9)
6. Transaction created with provider response
7. If successful:
   - Donation status → COMPLETED
   - Campaign collected_amount updated
   - Campaign donor_count incremented
   - Receipt generated
   - Notification sent to organization
   - Notification sent to donor
8. If failed:
   - Donation status → FAILED
   - Error logged
```

## Receipt Number Generation
Format: `RCPT-YYYY-NNNNNN`
- YYYY: Year
- NNNNNN: Sequential number (padded)

## Expected Outputs
- Donation creation working
- Receipt generation working
- Campaign stats updated
- Donation history available
- Anonymous donations working

## Success Criteria
- [ ] Donation created with correct campaign link
- [ ] Anonymous donations hide donor info in public list
- [ ] Receipt number unique and sequential
- [ ] Campaign collected_amount accurate
- [ ] Donation history paginated
- [ ] Only campaign organization sees donor details

## Files to Create (Count: 11)
```
src/main/java/com/seffafbagis/api/
├── dto/
│   ├── request/donation/
│   │   ├── CreateDonationRequest.java
│   │   ├── ProcessPaymentRequest.java
│   │   └── RefundRequest.java
│   ├── response/donation/
│   │   ├── DonationResponse.java
│   │   ├── DonationDetailResponse.java
│   │   ├── DonationListResponse.java
│   │   ├── DonationReceiptResponse.java
│   │   └── DonorListResponse.java
│   └── mapper/
│       └── DonationMapper.java
├── service/donation/
│   ├── DonationService.java
│   └── DonationReceiptService.java
└── controller/donation/
    └── DonationController.java
```

## Testing Requirements
- Donation creation tests
- Receipt generation tests
- Campaign update tests
- Anonymous donation tests
- Authorization tests

## Result File
After completing this phase, create:
`docs/Emir/step_results/phase_7.0_result.md`

---

# PHASE 8.0: DONATION MODULE - EXTENDED

## Overview
This phase extends donations with recurring donation subscriptions and bank transfer (Havale/EFT) support.

## Problems Being Solved
- Users want monthly/weekly recurring donations
- Some users prefer bank transfer over credit card
- Bank transfers need reference codes for matching
- Recurring payments need automatic processing
- Failed recurring payments need retry logic

## Components to Create

### 8.1 DTOs - Request
**Location**: `src/main/java/com/seffafbagis/api/dto/request/donation/`

| DTO | Purpose |
|-----|---------|
| `CreateRecurringDonationRequest.java` | New recurring donation |
| `UpdateRecurringDonationRequest.java` | Modify recurring donation |
| `InitiateBankTransferRequest.java` | Start bank transfer flow |
| `MatchBankTransferRequest.java` | Manual matching by admin |

### 8.2 DTOs - Response
**Location**: `src/main/java/com/seffafbagis/api/dto/response/donation/`

| DTO | Purpose |
|-----|---------|
| `RecurringDonationResponse.java` | Recurring donation data |
| `RecurringDonationListResponse.java` | List of recurring donations |
| `BankTransferInfoResponse.java` | Bank transfer instructions |
| `BankTransferReferenceResponse.java` | Reference details |

### 8.3 Services
**Location**: `src/main/java/com/seffafbagis/api/service/donation/`

| Class | Purpose |
|-------|---------|
| `RecurringDonationService.java` | Recurring donation logic |
| `BankTransferService.java` | Bank transfer operations |

### 8.4 Controllers
**Location**: `src/main/java/com/seffafbagis/api/controller/donation/`

| Class | Purpose |
|-------|---------|
| `RecurringDonationController.java` | Recurring donation endpoints |
| `BankTransferController.java` | Bank transfer endpoints |

## API Endpoints

### Recurring Donation Endpoints
```
GET    /api/v1/recurring-donations/my              - My recurring donations
POST   /api/v1/recurring-donations                  - Create recurring donation
PUT    /api/v1/recurring-donations/{id}             - Update (amount, frequency)
PUT    /api/v1/recurring-donations/{id}/pause       - Pause recurring
PUT    /api/v1/recurring-donations/{id}/resume      - Resume recurring
DELETE /api/v1/recurring-donations/{id}             - Cancel recurring
```

### Bank Transfer Endpoints
```
POST   /api/v1/bank-transfers/initiate             - Get reference code & bank info
GET    /api/v1/bank-transfers/{referenceCode}      - Get transfer status
POST   /api/v1/bank-transfers/{referenceCode}/confirm - User confirms sent (optional)

# Admin endpoints
GET    /api/v1/admin/bank-transfers/pending        - Pending transfers
POST   /api/v1/admin/bank-transfers/match          - Manual match
```

## Recurring Donation Flow
```
1. User creates recurring donation with card info
2. Card token saved (via Iyzico)
3. Scheduler runs daily to check next_payment_date
4. For due payments:
   - Create new Donation
   - Process payment using saved card
   - If success: update total_donated, payment_count, next_payment_date
   - If fail: increment failure_count, set last_error_message
   - If failure_count >= 3: pause subscription, notify user
```

## Bank Transfer Flow
```
1. User requests bank transfer for campaign
2. System generates unique reference code (using ReferenceCodeGenerator)
3. System returns bank account info + reference code
4. User makes transfer with reference in description
5. Organization checks bank statement, enters reference code
6. System matches and creates donation
7. OR: Admin manually matches unmatched transfers
8. Reference expires after 7 days if unused
```

## Reference Code Format
Using Furkan's ReferenceCodeGenerator:
`SBP-YYYYMMDD-XXXXX` (e.g., SBP-20240115-A7B3C)

## Expected Outputs
- Recurring donation CRUD working
- Card token storage working
- Bank transfer reference generation
- Manual matching by admin
- Reference expiry handling

## Success Criteria
- [ ] Recurring donation creates with card token
- [ ] Pause/resume works correctly
- [ ] Bank transfer reference unique
- [ ] Reference code in valid format
- [ ] Expired references handled
- [ ] Admin can manually match transfers

## Files to Create (Count: 10)
```
src/main/java/com/seffafbagis/api/
├── dto/
│   ├── request/donation/
│   │   ├── CreateRecurringDonationRequest.java
│   │   ├── UpdateRecurringDonationRequest.java
│   │   ├── InitiateBankTransferRequest.java
│   │   └── MatchBankTransferRequest.java
│   └── response/donation/
│       ├── RecurringDonationResponse.java
│       ├── RecurringDonationListResponse.java
│       ├── BankTransferInfoResponse.java
│       └── BankTransferReferenceResponse.java
├── service/donation/
│   ├── RecurringDonationService.java
│   └── BankTransferService.java
└── controller/donation/
    ├── RecurringDonationController.java
    └── BankTransferController.java
```

## Testing Requirements
- Recurring donation lifecycle tests
- Card token storage tests
- Bank transfer reference tests
- Expiry handling tests
- Manual matching tests

## Result File
After completing this phase, create:
`docs/Emir/step_results/phase_8.0_result.md`

---

# PHASE 9.0: PAYMENT MODULE (IYZICO INTEGRATION)

## Overview
This phase integrates Iyzico payment gateway for credit card processing, 3D Secure payments, and card token storage for recurring donations.

## Problems Being Solved
- Credit card payments need processing
- 3D Secure required for Turkey regulations
- Card info must never be stored locally
- Refunds need processing
- Recurring payments need saved card tokens
- Transaction fees need tracking

## Components to Create

### 9.1 DTOs - Request
**Location**: `src/main/java/com/seffafbagis/api/dto/request/payment/`

| DTO | Purpose |
|-----|---------|
| `PaymentRequest.java` | Payment initiation |
| `CardInfoRequest.java` | Card details (for Iyzico) |
| `ThreeDSCallbackRequest.java` | 3DS callback data |
| `RefundPaymentRequest.java` | Refund request |
| `SaveCardRequest.java` | Save card for recurring |

### 9.2 DTOs - Response
**Location**: `src/main/java/com/seffafbagis/api/dto/response/payment/`

| DTO | Purpose |
|-----|---------|
| `PaymentResultResponse.java` | Payment result |
| `ThreeDSInitResponse.java` | 3DS redirect info |
| `RefundResultResponse.java` | Refund result |
| `SavedCardResponse.java` | Saved card info (masked) |
| `TransactionResponse.java` | Transaction details |

### 9.3 Mapper
**Location**: `src/main/java/com/seffafbagis/api/dto/mapper/`

| Class | Purpose |
|-------|---------|
| `PaymentMapper.java` | Payment related mappings |

### 9.4 Payment Exception
**Location**: `src/main/java/com/seffafbagis/api/exception/`

| Class | Purpose |
|-------|---------|
| `PaymentException.java` | Payment-specific errors |

### 9.5 Services
**Location**: `src/main/java/com/seffafbagis/api/service/payment/`

| Class | Purpose |
|-------|---------|
| `PaymentService.java` | Payment orchestration |
| `IyzicoService.java` | Iyzico API integration |
| `TransactionService.java` | Transaction management |

### 9.6 Controllers
**Location**: `src/main/java/com/seffafbagis/api/controller/payment/`

| Class | Purpose |
|-------|---------|
| `PaymentController.java` | Payment endpoints |

## API Endpoints
```
POST   /api/v1/payments/initialize            - Initialize payment (returns 3DS HTML or direct result)
POST   /api/v1/payments/callback/3ds          - 3DS callback endpoint
GET    /api/v1/payments/{transactionId}       - Get transaction status
POST   /api/v1/payments/{transactionId}/refund - Request refund

# Card management for recurring
POST   /api/v1/payments/cards                 - Save card
GET    /api/v1/payments/cards                 - List saved cards
DELETE /api/v1/payments/cards/{cardToken}     - Delete saved card
```

## Payment Flow (3D Secure)
```
1. PaymentRequest → PaymentService.initialize()
2. IyzicoService.initiate3DSPayment()
3. Return HTML form for 3DS redirect
4. User completes 3DS at bank
5. Bank redirects to callback URL with result
6. PaymentService.handle3DSCallback()
7. If success:
   - Transaction status → SUCCESS
   - Donation status → COMPLETED
   - DonationService.complete() called
8. If failed:
   - Transaction status → FAILED
   - Donation status → FAILED
   - Error logged
```

## Iyzico Configuration
```yaml
iyzico:
  api-key: ${IYZICO_API_KEY}
  secret-key: ${IYZICO_SECRET_KEY}
  base-url: https://sandbox-api.iyzipay.com  # sandbox
  # base-url: https://api.iyzipay.com        # production
  callback-url: ${BASE_URL}/api/v1/payments/callback/3ds
```

## Transaction Recording
Store in Transaction entity:
- Provider transaction ID
- Provider payment ID
- Amount, fee, net amount
- Card last four, brand
- 3D Secure status
- Raw response (JSONB for debugging)
- Error code/message if failed

## Security Considerations
- NEVER store full card numbers
- Only store masked card info (last 4 digits)
- Use Iyzico card tokens for recurring
- Log transaction IDs, not card data
- Validate callback signature from Iyzico

## Expected Outputs
- Credit card payments working
- 3D Secure flow complete
- Refunds processing
- Card token storage for recurring
- Transaction logging complete

## Success Criteria
- [ ] Sandbox payments succeed
- [ ] 3D Secure redirect works
- [ ] Callback processes correctly
- [ ] Transaction logged with all details
- [ ] Refund creates reversal transaction
- [ ] Card token saved for recurring
- [ ] No sensitive card data logged

## Files to Create (Count: 15)
```
src/main/java/com/seffafbagis/api/
├── dto/
│   ├── request/payment/
│   │   ├── PaymentRequest.java
│   │   ├── CardInfoRequest.java
│   │   ├── ThreeDSCallbackRequest.java
│   │   ├── RefundPaymentRequest.java
│   │   └── SaveCardRequest.java
│   ├── response/payment/
│   │   ├── PaymentResultResponse.java
│   │   ├── ThreeDSInitResponse.java
│   │   ├── RefundResultResponse.java
│   │   ├── SavedCardResponse.java
│   │   └── TransactionResponse.java
│   └── mapper/
│       └── PaymentMapper.java
├── exception/
│   └── PaymentException.java
├── service/payment/
│   ├── PaymentService.java
│   ├── IyzicoService.java
│   └── TransactionService.java
└── controller/payment/
    └── PaymentController.java
```

## External Dependencies
Add to pom.xml:
```xml
<dependency>
    <groupId>com.iyzipay</groupId>
    <artifactId>iyzipay-java</artifactId>
    <version>2.0.131</version>
</dependency>
```

## Testing Requirements
- Iyzico sandbox integration tests
- 3DS flow tests (with mock)
- Refund tests
- Card token tests
- Error handling tests

## Result File
After completing this phase, create:
`docs/Emir/step_results/phase_9.0_result.md`

---

# PHASE 10.0: EVIDENCE MODULE

## Overview
This phase implements the evidence (kanıt) system for transparency. Organizations upload spending evidence (invoices, receipts, photos) after campaigns complete.

## Problems Being Solved
- Organizations must prove how donations were spent
- Evidence needs approval by admin
- Multiple documents per evidence
- Evidence deadline tracking
- Transparency score affected by evidence

## Components to Create

### 10.1 Enum Types
**Location**: `src/main/java/com/seffafbagis/api/enums/`

| Enum | Purpose |
|------|---------|
| `EvidenceType.java` | INVOICE, RECEIPT, PHOTO, VIDEO, DELIVERY_REPORT, OTHER |
| `EvidenceStatus.java` | PENDING, APPROVED, REJECTED |

### 10.2 Entities
**Location**: `src/main/java/com/seffafbagis/api/entity/evidence/`

| Entity | Purpose |
|--------|---------|
| `Evidence.java` | Evidence record |
| `EvidenceDocument.java` | Evidence files |

### 10.3 Repositories
**Location**: `src/main/java/com/seffafbagis/api/repository/`

| Repository | Purpose |
|------------|---------|
| `EvidenceRepository.java` | Evidence CRUD |
| `EvidenceDocumentRepository.java` | Document operations |

### 10.4 DTOs
**Location**: `src/main/java/com/seffafbagis/api/dto/`

| DTO | Purpose |
|-----|---------|
| `request/evidence/CreateEvidenceRequest.java` | New evidence |
| `request/evidence/AddEvidenceDocumentRequest.java` | Add document |
| `request/evidence/ReviewEvidenceRequest.java` | Admin review |
| `response/evidence/EvidenceResponse.java` | Evidence data |
| `response/evidence/EvidenceDetailResponse.java` | Full evidence |
| `response/evidence/EvidenceDocumentResponse.java` | Document info |
| `response/evidence/EvidenceListResponse.java` | List view |

### 10.5 Mapper
**Location**: `src/main/java/com/seffafbagis/api/dto/mapper/`

| Class | Purpose |
|-------|---------|
| `EvidenceMapper.java` | Evidence mappings |

### 10.6 Services
**Location**: `src/main/java/com/seffafbagis/api/service/evidence/`

| Class | Purpose |
|-------|---------|
| `EvidenceService.java` | Evidence business logic |
| `EvidenceDocumentService.java` | Document handling |

### 10.7 Controller
**Location**: `src/main/java/com/seffafbagis/api/controller/evidence/`

| Class | Purpose |
|-------|---------|
| `EvidenceController.java` | Evidence endpoints |

## API Endpoints

### Public Endpoints
```
GET    /api/v1/evidences/campaign/{campaignId}     - Campaign evidences (approved only)
GET    /api/v1/evidences/{id}                       - Evidence detail
```

### Organization Endpoints
```
GET    /api/v1/evidences/my                         - My organization's evidences
POST   /api/v1/evidences                            - Create evidence
PUT    /api/v1/evidences/{id}                       - Update evidence
DELETE /api/v1/evidences/{id}                       - Delete evidence (if pending)
POST   /api/v1/evidences/{id}/documents             - Add document
DELETE /api/v1/evidences/{id}/documents/{docId}     - Remove document
```

### Admin Endpoints
```
GET    /api/v1/admin/evidences/pending              - Pending evidences
POST   /api/v1/admin/evidences/{id}/approve         - Approve evidence
POST   /api/v1/admin/evidences/{id}/reject          - Reject evidence
```

## Evidence Fields
- campaign_id, evidence_type, title, description
- amount_spent (how much was spent)
- spend_date, vendor_name, vendor_tax_number
- invoice_number, status
- reviewed_by, reviewed_at, rejection_reason

## Evidence Workflow
```
1. Campaign completes (collected_amount reached or end_date passed)
2. Organization has X days (evidence_deadline_days) to upload evidence
3. Organization creates Evidence with amount spent, vendor info
4. Organization uploads supporting documents
5. Admin reviews evidence
6. If approved:
   - Evidence status → APPROVED
   - Transparency score updated (+points)
7. If rejected:
   - Evidence status → REJECTED with reason
   - Organization can fix and resubmit
8. If deadline missed:
   - Transparency score decreases (-points)
   - Reminder notifications sent
```

## Expected Outputs
- Evidence CRUD working
- Document upload working
- Admin approval workflow
- Campaign evidence listing
- Rejection with reason

## Success Criteria
- [ ] Evidence links to campaign correctly
- [ ] Multiple documents per evidence work
- [ ] Only organization owner can create evidence
- [ ] Admin can approve/reject
- [ ] Rejected evidence can be resubmitted
- [ ] Public sees only approved evidences
- [ ] amount_spent tracked for transparency

## Files to Create (Count: 14)
```
src/main/java/com/seffafbagis/api/
├── enums/
│   ├── EvidenceType.java
│   └── EvidenceStatus.java
├── entity/evidence/
│   ├── Evidence.java
│   └── EvidenceDocument.java
├── repository/
│   ├── EvidenceRepository.java
│   └── EvidenceDocumentRepository.java
├── dto/
│   ├── request/evidence/
│   │   ├── CreateEvidenceRequest.java
│   │   ├── AddEvidenceDocumentRequest.java
│   │   └── ReviewEvidenceRequest.java
│   ├── response/evidence/
│   │   ├── EvidenceResponse.java
│   │   ├── EvidenceDetailResponse.java
│   │   ├── EvidenceDocumentResponse.java
│   │   └── EvidenceListResponse.java
│   └── mapper/
│       └── EvidenceMapper.java
├── service/evidence/
│   ├── EvidenceService.java
│   └── EvidenceDocumentService.java
└── controller/evidence/
    └── EvidenceController.java
```

## Testing Requirements
- Evidence creation tests
- Document upload tests
- Admin approval tests
- Deadline tracking tests
- Authorization tests

## Result File
After completing this phase, create:
`docs/Emir/step_results/phase_10.0_result.md`

---

# PHASE 11.0: TRANSPARENCY SCORE MODULE

## Overview
This phase implements the transparency scoring algorithm that rates organizations based on their reporting performance and accountability.

## Problems Being Solved
- Organizations need accountability rating
- Score based on evidence uploads, timing, approvals
- Score history for trend analysis
- Low score should restrict campaign creation
- Score displayed on organization profile

## Components to Create

### 11.1 Entities
**Location**: `src/main/java/com/seffafbagis/api/entity/transparency/`

| Entity | Purpose |
|--------|---------|
| `TransparencyScore.java` | Current score and stats |
| `TransparencyScoreHistory.java` | Score change history |

### 11.2 Repositories
**Location**: `src/main/java/com/seffafbagis/api/repository/`

| Repository | Purpose |
|------------|---------|
| `TransparencyScoreRepository.java` | Score operations |
| `TransparencyScoreHistoryRepository.java` | History queries |

### 11.3 DTOs
**Location**: `src/main/java/com/seffafbagis/api/dto/response/transparency/`

| DTO | Purpose |
|-----|---------|
| `TransparencyScoreResponse.java` | Current score |
| `TransparencyScoreDetailResponse.java` | Score with breakdown |
| `ScoreHistoryResponse.java` | Score change history |
| `ScoreTrendResponse.java` | Trend analysis |

### 11.4 Service
**Location**: `src/main/java/com/seffafbagis/api/service/transparency/`

| Class | Purpose |
|-------|---------|
| `TransparencyScoreService.java` | Score operations |
| `TransparencyScoreCalculator.java` | Scoring algorithm |

### 11.5 Controller
**Location**: `src/main/java/com/seffafbagis/api/controller/transparency/`

| Class | Purpose |
|-------|---------|
| `TransparencyController.java` | Score endpoints |

## API Endpoints
```
GET    /api/v1/transparency/organization/{orgId}   - Get organization score
GET    /api/v1/transparency/organization/{orgId}/history - Score history
GET    /api/v1/transparency/organization/{orgId}/breakdown - Score breakdown

GET    /api/v1/transparency/my                      - My organization's score
GET    /api/v1/transparency/leaderboard             - Top organizations by score
```

## Scoring Algorithm

### Base Score: 50 points (new organizations)

### Positive Factors (Increase Score)
```
Evidence approved on time:         +5 points
Evidence approved (late):          +2 points
Campaign completed successfully:   +3 points
Consistent reporting streak:       +1 point per month
High donor satisfaction:           +2 points
Document verification:             +2 points
```

### Negative Factors (Decrease Score)
```
Evidence rejected:                 -5 points
Evidence not uploaded by deadline: -10 points
Evidence uploaded late:            -3 points
Campaign cancelled:                -2 points
Complaint upheld:                  -5 points
```

### Score Bounds
- Minimum: 0
- Maximum: 100
- New organization starts at: 50

### Threshold Effects
```
Score < 40: Cannot create new campaigns
Score < 30: Warning displayed on profile
Score < 20: Organization flagged for review
```

## TransparencyScore Fields
- organization_id, current_score
- total_campaigns, completed_campaigns
- on_time_reports, late_reports
- approved_evidences, rejected_evidences
- last_calculated_at

## TransparencyScoreHistory Fields
- organization_id, previous_score, new_score
- change_reason (enum or string)
- campaign_id (if related)
- evidence_id (if related)
- created_at

## Score Update Triggers
Called by:
- EvidenceService (on approve/reject)
- CampaignService (on complete/cancel)
- ReportService (on complaint resolution)
- Scheduler (for missed deadlines)

## Expected Outputs
- Score calculation working
- Score history tracked
- Score displayed on organization
- Low score restricts campaigns
- Leaderboard shows top organizations

## Success Criteria
- [ ] New organization starts with 50
- [ ] Evidence approval increases score
- [ ] Evidence rejection decreases score
- [ ] Score never goes below 0 or above 100
- [ ] History tracks all changes
- [ ] Organizations with score < 40 cannot create campaigns

## Files to Create (Count: 10)
```
src/main/java/com/seffafbagis/api/
├── entity/transparency/
│   ├── TransparencyScore.java
│   └── TransparencyScoreHistory.java
├── repository/
│   ├── TransparencyScoreRepository.java
│   └── TransparencyScoreHistoryRepository.java
├── dto/response/transparency/
│   ├── TransparencyScoreResponse.java
│   ├── TransparencyScoreDetailResponse.java
│   ├── ScoreHistoryResponse.java
│   └── ScoreTrendResponse.java
├── service/transparency/
│   ├── TransparencyScoreService.java
│   └── TransparencyScoreCalculator.java
└── controller/transparency/
    └── TransparencyController.java
```

## Testing Requirements
- Score calculation tests
- Score bounds tests
- History tracking tests
- Threshold enforcement tests

## Result File
After completing this phase, create:
`docs/Emir/step_results/phase_11.0_result.md`

---

# PHASE 12.0: APPLICATION MODULE

## Overview
This phase implements the aid application system where beneficiaries can request help and organizations can review/approve applications.

## Problems Being Solved
- Beneficiaries need to apply for aid
- Applications need document upload
- Applications need status workflow
- Organizations need to review applications
- Applications can be linked to campaigns

## Components to Create

### 12.1 Enum Type
**Location**: `src/main/java/com/seffafbagis/api/enums/`

| Enum | Purpose |
|------|---------|
| `ApplicationStatus.java` | PENDING, IN_REVIEW, APPROVED, REJECTED, COMPLETED |

### 12.2 Entities
**Location**: `src/main/java/com/seffafbagis/api/entity/application/`

| Entity | Purpose |
|--------|---------|
| `Application.java` | Aid application |
| `ApplicationDocument.java` | Supporting documents |

### 12.3 Repositories
**Location**: `src/main/java/com/seffafbagis/api/repository/`

| Repository | Purpose |
|------------|---------|
| `ApplicationRepository.java` | Application CRUD |
| `ApplicationDocumentRepository.java` | Document operations |

### 12.4 DTOs
**Location**: `src/main/java/com/seffafbagis/api/dto/`

| DTO | Purpose |
|-----|---------|
| `request/application/CreateApplicationRequest.java` | New application |
| `request/application/UpdateApplicationRequest.java` | Update application |
| `request/application/AddApplicationDocumentRequest.java` | Add document |
| `request/application/ReviewApplicationRequest.java` | Review action |
| `response/application/ApplicationResponse.java` | Application data |
| `response/application/ApplicationDetailResponse.java` | Full details |
| `response/application/ApplicationListResponse.java` | List view |

### 12.5 Mapper
**Location**: `src/main/java/com/seffafbagis/api/dto/mapper/`

| Class | Purpose |
|-------|---------|
| `ApplicationMapper.java` | Application mappings |

### 12.6 Services
**Location**: `src/main/java/com/seffafbagis/api/service/application/`

| Class | Purpose |
|-------|---------|
| `ApplicationService.java` | Application logic |
| `ApplicationDocumentService.java` | Document handling |

### 12.7 Controllers
**Location**: `src/main/java/com/seffafbagis/api/controller/application/`

| Class | Purpose |
|-------|---------|
| `ApplicationController.java` | Application endpoints |
| `ApplicationDocumentController.java` | Document endpoints |

## API Endpoints

### Beneficiary Endpoints
```
GET    /api/v1/applications/my                 - My applications
POST   /api/v1/applications                    - Create application
PUT    /api/v1/applications/{id}               - Update application
DELETE /api/v1/applications/{id}               - Withdraw application
POST   /api/v1/applications/{id}/documents     - Add document
DELETE /api/v1/applications/{id}/documents/{docId} - Remove document
```

### Organization Endpoints
```
GET    /api/v1/applications/organization       - Applications for review
GET    /api/v1/applications/{id}               - Application detail
PUT    /api/v1/applications/{id}/review        - Review application
PUT    /api/v1/applications/{id}/assign-campaign - Link to campaign
```

### Admin Endpoints
```
GET    /api/v1/admin/applications              - All applications
PUT    /api/v1/admin/applications/{id}/assign-organization - Assign to org
```

## Application Fields
- applicant_id, category_id, title, description
- requested_amount, location_city, location_district
- household_size, urgency_level (1-5)
- status, assigned_organization_id, assigned_campaign_id
- reviewed_by, reviewed_at

## Application Workflow
```
1. Beneficiary creates application
2. Application status: PENDING
3. Admin/Organization reviews
4. Status → IN_REVIEW
5. Organization approves → APPROVED, assigns campaign
   OR Organization rejects → REJECTED
6. Aid delivered → COMPLETED
```

## Expected Outputs
- Application CRUD working
- Document upload working
- Status workflow complete
- Organization can review applications
- Admin can assign to organizations

## Success Criteria
- [ ] BENEFICIARY role can create applications
- [ ] Documents uploaded correctly
- [ ] Status transitions follow workflow
- [ ] Organization sees only assigned applications
- [ ] Application can link to campaign

## Files to Create (Count: 14)
```
src/main/java/com/seffafbagis/api/
├── enums/
│   └── ApplicationStatus.java
├── entity/application/
│   ├── Application.java
│   └── ApplicationDocument.java
├── repository/
│   ├── ApplicationRepository.java
│   └── ApplicationDocumentRepository.java
├── dto/
│   ├── request/application/
│   │   ├── CreateApplicationRequest.java
│   │   ├── UpdateApplicationRequest.java
│   │   ├── AddApplicationDocumentRequest.java
│   │   └── ReviewApplicationRequest.java
│   ├── response/application/
│   │   ├── ApplicationResponse.java
│   │   ├── ApplicationDetailResponse.java
│   │   └── ApplicationListResponse.java
│   └── mapper/
│       └── ApplicationMapper.java
├── service/application/
│   ├── ApplicationService.java
│   └── ApplicationDocumentService.java
└── controller/application/
    ├── ApplicationController.java
    └── ApplicationDocumentController.java
```

## Testing Requirements
- Application creation tests
- Status workflow tests
- Document upload tests
- Authorization tests

## Result File
After completing this phase, create:
`docs/Emir/step_results/phase_12.0_result.md`

---

# PHASE 13.0: NOTIFICATION MODULE

## Overview
This phase implements the notification system for alerting users about donations, campaign updates, evidence requirements, and system events.

## Problems Being Solved
- Users need notification of donations
- Organizations need campaign update notifications
- Evidence deadline reminders needed
- Score change notifications needed
- Users have notification preferences

## Components to Create

### 13.1 Enum Type
**Location**: `src/main/java/com/seffafbagis/api/enums/`

| Enum | Purpose |
|------|---------|
| `NotificationType.java` | DONATION_RECEIVED, CAMPAIGN_UPDATE, EVIDENCE_REQUIRED, SCORE_CHANGE, SYSTEM |

### 13.2 Entity
**Location**: `src/main/java/com/seffafbagis/api/entity/notification/`

| Entity | Purpose |
|--------|---------|
| `Notification.java` | User notifications |

### 13.3 Repository
**Location**: `src/main/java/com/seffafbagis/api/repository/`

| Repository | Purpose |
|------------|---------|
| `NotificationRepository.java` | Notification operations |

### 13.4 DTOs
**Location**: `src/main/java/com/seffafbagis/api/dto/`

| DTO | Purpose |
|-----|---------|
| `response/notification/NotificationResponse.java` | Notification data |
| `response/notification/NotificationListResponse.java` | List with count |

### 13.5 Services
**Location**: `src/main/java/com/seffafbagis/api/service/notification/`

| Class | Purpose |
|-------|---------|
| `NotificationService.java` | Notification logic |
| `EmailNotificationService.java` | Email sending (uses Furkan's EmailService) |

### 13.6 Controller
**Location**: `src/main/java/com/seffafbagis/api/controller/notification/`

| Class | Purpose |
|-------|---------|
| `NotificationController.java` | Notification endpoints |

## API Endpoints
```
GET    /api/v1/notifications                   - My notifications
GET    /api/v1/notifications/unread            - Unread count
PUT    /api/v1/notifications/{id}/read         - Mark as read
PUT    /api/v1/notifications/read-all          - Mark all as read
DELETE /api/v1/notifications/{id}              - Delete notification
```

## Notification Triggers
```
- Donation completed → Notify donor, organization owner
- Campaign update posted → Notify followers
- Campaign completed → Notify followers, organization
- Evidence deadline approaching → Notify organization
- Evidence deadline missed → Notify organization, admin
- Evidence approved/rejected → Notify organization
- Transparency score changed → Notify organization
- Application status changed → Notify applicant
- Report filed → Notify admin
- Report resolved → Notify reporter
```

## Notification Fields
- user_id, type, title, message
- data (JSONB - campaign_id, donation_id, etc.)
- is_read, read_at, created_at

## Email Integration
Based on user preferences:
- If email_notifications = true → Send email via EmailService
- Always create in-app notification

## Expected Outputs
- In-app notifications working
- Email notifications sent
- Unread count accurate
- Mark as read working
- User preferences respected

## Success Criteria
- [ ] Notifications created for all events
- [ ] Unread count correct
- [ ] Mark as read updates correctly
- [ ] Email sent if preference enabled
- [ ] JSONB data stored correctly

## Files to Create (Count: 8)
```
src/main/java/com/seffafbagis/api/
├── enums/
│   └── NotificationType.java
├── entity/notification/
│   └── Notification.java
├── repository/
│   └── NotificationRepository.java
├── dto/response/notification/
│   ├── NotificationResponse.java
│   └── NotificationListResponse.java
├── service/notification/
│   ├── NotificationService.java
│   └── EmailNotificationService.java
└── controller/notification/
    └── NotificationController.java
```

## Testing Requirements
- Notification creation tests
- Read/unread tests
- Email sending tests
- Preference respect tests

## Result File
After completing this phase, create:
`docs/Emir/step_results/phase_13.0_result.md`

---

# PHASE 14.0: REPORT MODULE

## Overview
This phase implements the report/complaint system for fraud reporting and inappropriate content flagging.

## Problems Being Solved
- Users need to report fraudulent campaigns
- Users need to report inappropriate organizations
- Admins need to review and resolve reports
- Report resolution needs tracking
- Priority-based queue for admins

## Components to Create

### 14.1 Enum Types
**Location**: `src/main/java/com/seffafbagis/api/enums/`

| Enum | Purpose |
|------|---------|
| `ReportType.java` | FRAUD, INAPPROPRIATE, SPAM, OTHER |
| `ReportStatus.java` | PENDING, INVESTIGATING, RESOLVED, DISMISSED |

### 14.2 Entity
**Location**: `src/main/java/com/seffafbagis/api/entity/report/`

| Entity | Purpose |
|--------|---------|
| `Report.java` | Report record |

### 14.3 Repository
**Location**: `src/main/java/com/seffafbagis/api/repository/`

| Repository | Purpose |
|------------|---------|
| `ReportRepository.java` | Report operations |

### 14.4 DTOs
**Location**: `src/main/java/com/seffafbagis/api/dto/`

| DTO | Purpose |
|-----|---------|
| `request/report/CreateReportRequest.java` | New report |
| `request/report/ResolveReportRequest.java` | Admin resolution |
| `response/report/ReportResponse.java` | Report data |
| `response/report/ReportDetailResponse.java` | Full details |
| `response/report/ReportListResponse.java` | List view |

### 14.5 Service
**Location**: `src/main/java/com/seffafbagis/api/service/report/`

| Class | Purpose |
|-------|---------|
| `ReportService.java` | Report business logic |

### 14.6 Controller
**Location**: `src/main/java/com/seffafbagis/api/controller/report/`

| Class | Purpose |
|-------|---------|
| `ReportController.java` | Report endpoints |

## API Endpoints

### User Endpoints
```
POST   /api/v1/reports                         - Create report
GET    /api/v1/reports/my                      - My submitted reports
```

### Admin Endpoints
```
GET    /api/v1/admin/reports                   - All reports
GET    /api/v1/admin/reports/pending           - Pending reports
GET    /api/v1/admin/reports/{id}              - Report details
PUT    /api/v1/admin/reports/{id}/assign       - Assign to admin
PUT    /api/v1/admin/reports/{id}/status       - Update status
PUT    /api/v1/admin/reports/{id}/resolve      - Resolve report
```

## Report Fields
- reporter_id (nullable for anonymous)
- report_type, entity_type, entity_id
- reason, description, evidence_urls (array)
- priority (low, medium, high)
- status, resolution_notes
- assigned_to, assigned_at
- resolved_by, resolved_at

## Report Resolution Actions
When fraud confirmed:
- Suspend organization/campaign
- Decrease transparency score
- Notify affected donors
- Log in audit

## Expected Outputs
- Report creation working
- Anonymous reporting supported
- Admin queue working
- Resolution workflow complete
- Priority sorting

## Success Criteria
- [ ] Reports created correctly
- [ ] Anonymous reports work
- [ ] Priority affects queue order
- [ ] Resolution updates status
- [ ] Notification sent to reporter

## Files to Create (Count: 10)
```
src/main/java/com/seffafbagis/api/
├── enums/
│   ├── ReportType.java
│   └── ReportStatus.java
├── entity/report/
│   └── Report.java
├── repository/
│   └── ReportRepository.java
├── dto/
│   ├── request/report/
│   │   ├── CreateReportRequest.java
│   │   └── ResolveReportRequest.java
│   └── response/report/
│       ├── ReportResponse.java
│       ├── ReportDetailResponse.java
│       └── ReportListResponse.java
├── service/report/
│   └── ReportService.java
└── controller/report/
    └── ReportController.java
```

## Testing Requirements
- Report creation tests
- Anonymous reporting tests
- Admin workflow tests
- Resolution tests

## Result File
After completing this phase, create:
`docs/Emir/step_results/phase_14.0_result.md`

---

# PHASE 15.0: SCHEDULER MODULE

## Overview
This phase implements scheduled tasks for recurring payments, evidence reminders, bank transfer expiry, and transparency score recalculation.

## Problems Being Solved
- Recurring donations need daily processing
- Bank transfer references need expiry
- Evidence deadlines need reminders
- Transparency scores need periodic recalculation
- Campaign end dates need checking

## Components to Create

### 15.1 Configuration
**Location**: `src/main/java/com/seffafbagis/api/config/`

| Class | Purpose |
|-------|---------|
| `SchedulerConfig.java` | Enable scheduling, thread pool |

### 15.2 Schedulers
**Location**: `src/main/java/com/seffafbagis/api/scheduler/`

| Class | Purpose |
|-------|---------|
| `RecurringDonationScheduler.java` | Process recurring payments |
| `BankTransferExpiryScheduler.java` | Expire unused references |
| `EvidenceReminderScheduler.java` | Send deadline reminders |
| `TransparencyScoreScheduler.java` | Recalculate scores |
| `CampaignStatusScheduler.java` | Check campaign end dates |

## Scheduler Details

### RecurringDonationScheduler
```
Schedule: Daily at 06:00
Tasks:
1. Find recurring donations where next_payment_date <= today
2. For each: process payment
3. If success: update next_payment_date, total_donated, payment_count
4. If fail: increment failure_count, notify user
5. If failure_count >= 3: pause subscription, notify user
```

### BankTransferExpiryScheduler
```
Schedule: Every hour
Tasks:
1. Find references where status = PENDING and expires_at < now
2. Mark as EXPIRED
3. Notify user that reference expired
```

### EvidenceReminderScheduler
```
Schedule: Daily at 09:00
Tasks:
1. Find campaigns completed with no evidence, within deadline period
2. Calculate days remaining to deadline
3. Send reminder at 7 days, 3 days, 1 day before deadline
4. On deadline day: mark as missed, decrease transparency score
```

### TransparencyScoreScheduler
```
Schedule: Daily at 00:00
Tasks:
1. Recalculate scores for organizations with activity in last 24h
2. Check for missed evidence deadlines
3. Apply penalties for overdue reports
4. Create history records for changes
```

### CampaignStatusScheduler
```
Schedule: Every 6 hours
Tasks:
1. Find campaigns where status = ACTIVE and end_date < now
2. Update status to COMPLETED
3. Trigger evidence deadline countdown
4. Notify organization
```

## Expected Outputs
- Recurring payments processed automatically
- Expired references cleaned up
- Reminders sent on schedule
- Scores updated nightly
- Campaigns auto-complete on end date

## Success Criteria
- [ ] Schedulers run at configured times
- [ ] Recurring payments process correctly
- [ ] References expire correctly
- [ ] Reminders sent at right intervals
- [ ] Score changes logged in history
- [ ] Campaigns complete automatically

## Files to Create (Count: 6)
```
src/main/java/com/seffafbagis/api/
├── config/
│   └── SchedulerConfig.java
└── scheduler/
    ├── RecurringDonationScheduler.java
    ├── BankTransferExpiryScheduler.java
    ├── EvidenceReminderScheduler.java
    ├── TransparencyScoreScheduler.java
    └── CampaignStatusScheduler.java
```

## Testing Requirements
- Scheduler execution tests
- Date calculation tests
- Notification trigger tests
- Error handling tests

## Result File
After completing this phase, create:
`docs/Emir/step_results/phase_15.0_result.md`

---

# PHASE 16.0: EVENT SYSTEM & INTEGRATION

## Overview
This phase implements event-driven architecture for decoupling modules and enabling asynchronous operations.

## Problems Being Solved
- Modules need decoupled communication
- Donation completion triggers multiple actions
- Campaign completion triggers evidence workflow
- Evidence approval triggers score update
- Events enable future extensibility

## Components to Create

### 16.1 Events
**Location**: `src/main/java/com/seffafbagis/api/event/`

| Event | Purpose |
|-------|---------|
| `DonationCreatedEvent.java` | New donation created |
| `DonationCompletedEvent.java` | Donation payment successful |
| `CampaignCreatedEvent.java` | New campaign created |
| `CampaignCompletedEvent.java` | Campaign reached goal or ended |
| `CampaignApprovedEvent.java` | Campaign approved by admin |
| `EvidenceUploadedEvent.java` | Evidence submitted |
| `EvidenceApprovedEvent.java` | Evidence approved |
| `EvidenceRejectedEvent.java` | Evidence rejected |
| `OrganizationVerifiedEvent.java` | Organization approved |
| `TransparencyScoreChangedEvent.java` | Score changed |
| `ReportCreatedEvent.java` | New report filed |

### 16.2 Event Listeners
**Location**: `src/main/java/com/seffafbagis/api/event/listener/`

| Listener | Purpose |
|----------|---------|
| `DonationEventListener.java` | Handle donation events |
| `CampaignEventListener.java` | Handle campaign events |
| `EvidenceEventListener.java` | Handle evidence events |
| `NotificationEventListener.java` | Create notifications from events |
| `AuditEventListener.java` | Log events for audit |

## Event Flow Examples

### Donation Completed
```
DonationCompletedEvent
    ↓
    ├── NotificationEventListener → Create notifications
    ├── CampaignEventListener → Update campaign stats
    ├── AuditEventListener → Log audit record
    └── (Future: AnalyticsEventListener)
```

### Evidence Approved
```
EvidenceApprovedEvent
    ↓
    ├── TransparencyScoreService → Update score
    ├── NotificationEventListener → Notify organization
    └── AuditEventListener → Log audit record
```

### Campaign Completed
```
CampaignCompletedEvent
    ↓
    ├── NotificationEventListener → Notify followers
    ├── EvidenceReminderService → Schedule reminders
    └── AuditEventListener → Log completion
```

## Implementation Approach
Using Spring's ApplicationEventPublisher:
```java
// Publishing
applicationEventPublisher.publishEvent(new DonationCompletedEvent(donation));

// Listening
@EventListener
@Async
public void handleDonationCompleted(DonationCompletedEvent event) {
    // Handle event
}
```

## Expected Outputs
- All events defined
- Event publishing from services
- Listeners handle appropriately
- Notifications created via events
- Audit logs created via events

## Success Criteria
- [ ] Events decouple modules
- [ ] Async listeners work correctly
- [ ] Notifications created from events
- [ ] No direct dependencies between unrelated services
- [ ] Error in listener doesn't break main flow

## Files to Create (Count: 16)
```
src/main/java/com/seffafbagis/api/
└── event/
    ├── DonationCreatedEvent.java
    ├── DonationCompletedEvent.java
    ├── CampaignCreatedEvent.java
    ├── CampaignCompletedEvent.java
    ├── CampaignApprovedEvent.java
    ├── EvidenceUploadedEvent.java
    ├── EvidenceApprovedEvent.java
    ├── EvidenceRejectedEvent.java
    ├── OrganizationVerifiedEvent.java
    ├── TransparencyScoreChangedEvent.java
    ├── ReportCreatedEvent.java
    └── listener/
        ├── DonationEventListener.java
        ├── CampaignEventListener.java
        ├── EvidenceEventListener.java
        ├── NotificationEventListener.java
        └── AuditEventListener.java
```

## Testing Requirements
- Event publishing tests
- Listener invocation tests
- Async execution tests
- Error handling tests

## Result File
After completing this phase, create:
`docs/Emir/step_results/phase_16.0_result.md`

---

# PHASE 17.0: INTEGRATION TESTING & FINAL POLISH

## Overview
This phase focuses on comprehensive integration testing, bug fixes, documentation, and preparing for production.

## Problems Being Solved
- All modules need integration testing
- Edge cases need handling
- API documentation needs completion
- Performance needs verification
- Code quality needs review

## Tasks to Complete

### 17.1 Integration Tests
**Location**: `src/test/java/com/seffafbagis/api/integration/`

| Test Class | Purpose |
|------------|---------|
| `OrganizationIntegrationTest.java` | Full organization lifecycle |
| `CampaignIntegrationTest.java` | Campaign creation to completion |
| `DonationFlowIntegrationTest.java` | Complete donation flow |
| `PaymentIntegrationTest.java` | Iyzico integration |
| `EvidenceWorkflowTest.java` | Evidence upload and approval |
| `TransparencyScoreTest.java` | Score calculation scenarios |
| `ApplicationWorkflowTest.java` | Application lifecycle |
| `NotificationTest.java` | Notification delivery |
| `SchedulerTest.java` | Scheduled tasks |
| `EventSystemTest.java` | Event publishing/handling |

### 17.2 Test Scenarios
```
Full Donation Flow:
1. Register user → Verify email
2. Register foundation → Create organization → Upload documents
3. Admin verifies organization
4. Create campaign → Submit for approval
5. Admin approves campaign
6. Donor donates with credit card
7. 3D Secure completed
8. Receipt generated
9. Campaign stats updated
10. Notifications sent
11. Campaign completes
12. Organization uploads evidence
13. Admin approves evidence
14. Transparency score updated
```

### 17.3 Documentation
**Location**: `docs/`

| Document | Purpose |
|----------|---------|
| `API_ORGANIZATION.md` | Organization endpoints |
| `API_CAMPAIGN.md` | Campaign endpoints |
| `API_DONATION.md` | Donation endpoints |
| `API_PAYMENT.md` | Payment endpoints |
| `TRANSPARENCY.md` | Scoring algorithm |
| `WORKFLOWS.md` | Business workflows |

### 17.4 Performance Testing
- Load test donation endpoint
- Verify response times < 500ms
- Check N+1 query issues
- Verify Redis caching effectiveness

### 17.5 Code Quality
- Fix any SonarQube issues
- Ensure consistent error handling
- Verify logging is appropriate
- Check for security vulnerabilities

## Expected Outputs
- All integration tests passing
- Documentation complete
- Performance acceptable
- No critical bugs

## Success Criteria
- [ ] All integration tests pass
- [ ] Full donation flow works end-to-end
- [ ] API documentation complete
- [ ] Response times acceptable
- [ ] No security vulnerabilities
- [ ] Code review complete

## Files to Create (Count: 16+)
```
src/test/java/com/seffafbagis/api/integration/
├── OrganizationIntegrationTest.java
├── CampaignIntegrationTest.java
├── DonationFlowIntegrationTest.java
├── PaymentIntegrationTest.java
├── EvidenceWorkflowTest.java
├── TransparencyScoreTest.java
├── ApplicationWorkflowTest.java
├── NotificationTest.java
├── SchedulerTest.java
└── EventSystemTest.java

docs/
├── API_ORGANIZATION.md
├── API_CAMPAIGN.md
├── API_DONATION.md
├── API_PAYMENT.md
├── TRANSPARENCY.md
└── WORKFLOWS.md
```

## Testing Requirements
- Execute all test suites
- Manual API testing with Postman
- Load testing with JMeter or similar
- Security scan

## Result File
After completing this phase, create:
`docs/Emir/step_results/phase_17.0_result.md`

---

## COMPLETE FILE COUNT SUMMARY

| Phase | Files to Create |
|-------|-----------------|
| Phase 1.0 | 13 |
| Phase 2.0 | 10 |
| Phase 3.0 | 22 |
| Phase 4.0 | 13 |
| Phase 5.0 | 20 |
| Phase 6.0 | 12 |
| Phase 7.0 | 11 |
| Phase 8.0 | 10 |
| Phase 9.0 | 15 |
| Phase 10.0 | 14 |
| Phase 11.0 | 10 |
| Phase 12.0 | 14 |
| Phase 13.0 | 8 |
| Phase 14.0 | 10 |
| Phase 15.0 | 6 |
| Phase 16.0 | 16 |
| Phase 17.0 | 16+ |
| **TOTAL** | **~200 files** |

---

## ADDITIONAL NOTES

### Database Migration Needs
During development, if any new columns or tables are needed beyond the existing schema, create migration files:
- Location: `src/main/resources/db/migration/`
- Naming: `V{number}__{description}.sql`
- Always increment version numbers

### Interface Implementation
Emir must implement the interfaces defined by Furkan:
- `IOrganizationService` - in OrganizationService.java
- `ICampaignService` - in CampaignService.java

### FileStorageService Usage
For file uploads (documents, images, evidences), use Furkan's FileStorageService:
```java
@Autowired
private FileStorageService fileStorageService;

String fileUrl = fileStorageService.store(file, "organization-documents");
```

### Testing on Each Phase
After completing each phase:
1. Run unit tests: `mvn test`
2. Run integration tests: `mvn verify`
3. Test API endpoints manually
4. Document any issues in result file
5. Create migration if schema changes needed

### Coordination with Furkan
Before implementing:
- Phase 3.0: Confirm IOrganizationService interface
- Phase 5.0: Confirm ICampaignService interface
- Phase 9.0: Coordinate on payment error handling
- Phase 13.0: Coordinate on EmailService usage

---

## PROMPT CREATION NOTES

When creating prompts for each phase:
1. Include all context from this document
2. Reference database schema
3. Specify exact file locations
4. Include testing requirements
5. Require result file creation
6. Specify no code in prompt (implementation by AI)
7. Include success criteria checklist

---

## READY FOR PHASE PROMPTS

With this phase breakdown complete, proceed to create individual prompts for each phase, starting with Phase 1.0: Category & Donation Type Module.
