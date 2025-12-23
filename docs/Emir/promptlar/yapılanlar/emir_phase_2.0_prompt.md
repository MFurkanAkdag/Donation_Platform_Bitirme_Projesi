# PHASE 2.0: ORGANIZATION MODULE - ENTITIES & REPOSITORY

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

**Current Phase:** 2.0 - Organization Module - Entities & Repository

**Previous Phase Completed:** Phase 1.0 - Category & Donation Type Module
- DonationTypeCode enum
- Category and DonationType entities
- CategoryRepository and DonationTypeRepository
- CategoryService and DonationTypeService
- CategoryController and DonationTypeController
- Seed data migration

---

## Objective

Create all Organization-related entities, enums, and repository interfaces. Organizations represent foundations (vakıf) and associations (dernek) that create campaigns and receive donations. This phase establishes the data layer for organization management.

---

## What This Phase Will Solve

1. **Foundation/Association Registration**: Organizations need a data model for registration and verification
2. **Multiple Contacts**: Organizations can have multiple contact persons (primary, support, press)
3. **Document Storage**: Organizations must upload verification documents (tax certificate, authorization, etc.)
4. **Bank Account Management**: Organizations need to store multiple bank accounts for receiving donations
5. **Verification Workflow**: Track verification status (pending, in_review, approved, rejected)

---

## Database Schema Reference

From the existing `database_schema.sql`, the relevant tables are:

### organizations table
```sql
CREATE TABLE organizations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    organization_type organization_type NOT NULL,
    legal_name VARCHAR(255) NOT NULL,
    trade_name VARCHAR(255),
    tax_number VARCHAR(20) UNIQUE,
    derbis_number VARCHAR(50),                -- Dernekler için DERBİS no
    mersis_number VARCHAR(50),                -- Vakıflar için MERSİS no
    establishment_date DATE,
    description TEXT,
    mission_statement TEXT,
    logo_url VARCHAR(500),
    website_url VARCHAR(500),
    verification_status verification_status DEFAULT 'pending',
    verified_at TIMESTAMPTZ,
    verified_by UUID REFERENCES users(id),
    rejection_reason TEXT,
    resubmission_count INTEGER DEFAULT 0,
    last_resubmission_at TIMESTAMPTZ,
    is_featured BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
```

### organization_contacts table
```sql
CREATE TABLE organization_contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    contact_type VARCHAR(50) NOT NULL,        -- 'primary', 'support', 'press'
    contact_name VARCHAR(100),
    email VARCHAR(255),
    phone VARCHAR(20),
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    district VARCHAR(100),
    postal_code VARCHAR(10),
    country VARCHAR(100) DEFAULT 'Türkiye',
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
```

### organization_documents table
```sql
CREATE TABLE organization_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    document_type VARCHAR(100) NOT NULL,      -- 'tax_certificate', 'authorization', 'derbis_record'
    document_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_size INTEGER,
    mime_type VARCHAR(100),
    is_verified BOOLEAN DEFAULT FALSE,
    verified_at TIMESTAMPTZ,
    verified_by UUID REFERENCES users(id),
    expires_at DATE,
    uploaded_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
```

### organization_bank_accounts table
```sql
CREATE TABLE organization_bank_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    bank_name VARCHAR(100) NOT NULL,
    bank_code VARCHAR(5),
    branch_name VARCHAR(100),
    branch_code VARCHAR(10),
    branch_city VARCHAR(100),
    branch_district VARCHAR(100),
    account_holder VARCHAR(255) NOT NULL,
    account_number VARCHAR(30),
    iban VARCHAR(34) NOT NULL,
    currency VARCHAR(3) DEFAULT 'TRY',
    account_type VARCHAR(50) DEFAULT 'current',
    is_primary BOOLEAN DEFAULT FALSE,
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
```

### Enum types from schema
```sql
CREATE TYPE organization_type AS ENUM ('foundation', 'association', 'ngo');
CREATE TYPE verification_status AS ENUM ('pending', 'in_review', 'approved', 'rejected');
```

---

## Files to Create

### 1. Enums

**Location:** `src/main/java/com/seffafbagis/api/enums/`

#### OrganizationType.java
```java
public enum OrganizationType {
    FOUNDATION,    // Vakıf
    ASSOCIATION,   // Dernek
    NGO            // Sivil Toplum Kuruluşu
}
```

#### VerificationStatus.java
```java
public enum VerificationStatus {
    PENDING,       // Beklemede
    IN_REVIEW,     // İnceleniyor
    APPROVED,      // Onaylandı
    REJECTED       // Reddedildi
}
```

Both enums must use `@Enumerated(EnumType.STRING)` when mapped in entities.

---

### 2. Entities

**Location:** `src/main/java/com/seffafbagis/api/entity/organization/`

#### Organization.java

Key implementation details:
- Extend `BaseEntity` (from Furkan's work)
- One-to-One relationship with `User` entity (user_id is unique)
- One-to-Many relationship with `OrganizationContact`
- One-to-Many relationship with `OrganizationDocument`
- One-to-Many relationship with `OrganizationBankAccount`
- Many-to-One relationship with `User` for `verifiedBy`

Fields to include:
```java
@OneToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", nullable = false, unique = true)
private User user;

@Enumerated(EnumType.STRING)
@Column(name = "organization_type", nullable = false)
private OrganizationType organizationType;

@Column(name = "legal_name", nullable = false)
private String legalName;

@Column(name = "trade_name")
private String tradeName;

@Column(name = "tax_number", unique = true)
private String taxNumber;

@Column(name = "derbis_number")
private String derbisNumber;

@Column(name = "mersis_number")
private String mersisNumber;

@Column(name = "establishment_date")
private LocalDate establishmentDate;

@Column(columnDefinition = "TEXT")
private String description;

@Column(name = "mission_statement", columnDefinition = "TEXT")
private String missionStatement;

@Column(name = "logo_url")
private String logoUrl;

@Column(name = "website_url")
private String websiteUrl;

@Enumerated(EnumType.STRING)
@Column(name = "verification_status")
private VerificationStatus verificationStatus = VerificationStatus.PENDING;

@Column(name = "verified_at")
private LocalDateTime verifiedAt;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "verified_by")
private User verifiedBy;

@Column(name = "rejection_reason")
private String rejectionReason;

@Column(name = "resubmission_count")
private Integer resubmissionCount = 0;

@Column(name = "last_resubmission_at")
private LocalDateTime lastResubmissionAt;

@Column(name = "is_featured")
private Boolean isFeatured = false;

// Relationships
@OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
private List<OrganizationContact> contacts = new ArrayList<>();

@OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
private List<OrganizationDocument> documents = new ArrayList<>();

@OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
private List<OrganizationBankAccount> bankAccounts = new ArrayList<>();
```

Add table indexes:
```java
@Table(name = "organizations", indexes = {
    @Index(name = "idx_organizations_user_id", columnList = "user_id"),
    @Index(name = "idx_organizations_verification", columnList = "verification_status"),
    @Index(name = "idx_organizations_type", columnList = "organization_type")
})
```

---

#### OrganizationContact.java

Fields:
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "organization_id", nullable = false)
private Organization organization;

@Column(name = "contact_type", nullable = false)
private String contactType;  // 'primary', 'support', 'press'

@Column(name = "contact_name")
private String contactName;

@Column(name = "email")
private String email;

@Column(name = "phone")
private String phone;

@Column(name = "address_line1")
private String addressLine1;

@Column(name = "address_line2")
private String addressLine2;

@Column(name = "city")
private String city;

@Column(name = "district")
private String district;

@Column(name = "postal_code")
private String postalCode;

@Column(name = "country")
private String country = "Türkiye";

@Column(name = "is_primary")
private Boolean isPrimary = false;

@Column(name = "created_at")
private LocalDateTime createdAt;
```

Add `@PrePersist` for createdAt:
```java
@PrePersist
protected void onCreate() {
    this.createdAt = LocalDateTime.now();
}
```

---

#### OrganizationDocument.java

Fields:
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "organization_id", nullable = false)
private Organization organization;

@Column(name = "document_type", nullable = false)
private String documentType;  // 'tax_certificate', 'authorization', 'derbis_record', 'board_decision'

@Column(name = "document_name", nullable = false)
private String documentName;

@Column(name = "file_url", nullable = false)
private String fileUrl;

@Column(name = "file_size")
private Integer fileSize;

@Column(name = "mime_type")
private String mimeType;

@Column(name = "is_verified")
private Boolean isVerified = false;

@Column(name = "verified_at")
private LocalDateTime verifiedAt;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "verified_by")
private User verifiedBy;

@Column(name = "expires_at")
private LocalDate expiresAt;

@Column(name = "uploaded_at")
private LocalDateTime uploadedAt;
```

Add `@PrePersist`:
```java
@PrePersist
protected void onCreate() {
    this.uploadedAt = LocalDateTime.now();
}
```

---

#### OrganizationBankAccount.java

Fields:
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "organization_id", nullable = false)
private Organization organization;

@Column(name = "bank_name", nullable = false)
private String bankName;

@Column(name = "bank_code")
private String bankCode;

@Column(name = "branch_name")
private String branchName;

@Column(name = "branch_code")
private String branchCode;

@Column(name = "branch_city")
private String branchCity;

@Column(name = "branch_district")
private String branchDistrict;

@Column(name = "account_holder", nullable = false)
private String accountHolder;

@Column(name = "account_number")
private String accountNumber;

@Column(name = "iban", nullable = false)
private String iban;

@Column(name = "currency")
private String currency = "TRY";

@Column(name = "account_type")
private String accountType = "current";

@Column(name = "is_primary")
private Boolean isPrimary = false;

@Column(name = "is_verified")
private Boolean isVerified = false;

@Column(name = "created_at")
private LocalDateTime createdAt;
```

---

### 3. Repositories

**Location:** `src/main/java/com/seffafbagis/api/repository/`

#### OrganizationRepository.java

```java
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    
    // Find by user
    Optional<Organization> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);
    
    // Find by verification status
    List<Organization> findByVerificationStatus(VerificationStatus status);
    Page<Organization> findByVerificationStatus(VerificationStatus status, Pageable pageable);
    
    // Find approved (public listing)
    Page<Organization> findByVerificationStatusOrderByIsFeaturedDescCreatedAtDesc(
        VerificationStatus status, Pageable pageable);
    
    // Find featured
    List<Organization> findByIsFeaturedTrueAndVerificationStatus(VerificationStatus status);
    
    // Search
    @Query("SELECT o FROM Organization o WHERE o.verificationStatus = :status " +
           "AND (LOWER(o.legalName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(o.tradeName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Organization> searchByKeyword(@Param("keyword") String keyword, 
                                       @Param("status") VerificationStatus status,
                                       Pageable pageable);
    
    // Find by tax number (unique check)
    Optional<Organization> findByTaxNumber(String taxNumber);
    boolean existsByTaxNumber(String taxNumber);
    boolean existsByTaxNumberAndIdNot(String taxNumber, UUID id);
    
    // Find by organization type
    Page<Organization> findByOrganizationTypeAndVerificationStatus(
        OrganizationType type, VerificationStatus status, Pageable pageable);
    
    // Count statistics
    long countByVerificationStatus(VerificationStatus status);
}
```

---

#### OrganizationContactRepository.java

```java
public interface OrganizationContactRepository extends JpaRepository<OrganizationContact, UUID> {
    
    List<OrganizationContact> findByOrganizationIdOrderByIsPrimaryDescCreatedAtAsc(UUID organizationId);
    
    Optional<OrganizationContact> findByOrganizationIdAndIsPrimaryTrue(UUID organizationId);
    
    boolean existsByOrganizationIdAndIsPrimaryTrue(UUID organizationId);
    
    void deleteByOrganizationId(UUID organizationId);
    
    long countByOrganizationId(UUID organizationId);
}
```

---

#### OrganizationDocumentRepository.java

```java
public interface OrganizationDocumentRepository extends JpaRepository<OrganizationDocument, UUID> {
    
    List<OrganizationDocument> findByOrganizationIdOrderByUploadedAtDesc(UUID organizationId);
    
    List<OrganizationDocument> findByOrganizationIdAndDocumentType(UUID organizationId, String documentType);
    
    List<OrganizationDocument> findByOrganizationIdAndIsVerifiedFalse(UUID organizationId);
    
    boolean existsByOrganizationIdAndDocumentType(UUID organizationId, String documentType);
    
    // Find expiring documents
    @Query("SELECT d FROM OrganizationDocument d WHERE d.expiresAt IS NOT NULL " +
           "AND d.expiresAt <= :expiryDate AND d.isVerified = true")
    List<OrganizationDocument> findExpiringDocuments(@Param("expiryDate") LocalDate expiryDate);
    
    void deleteByOrganizationId(UUID organizationId);
    
    long countByOrganizationId(UUID organizationId);
    
    long countByOrganizationIdAndIsVerifiedTrue(UUID organizationId);
}
```

---

#### OrganizationBankAccountRepository.java

```java
public interface OrganizationBankAccountRepository extends JpaRepository<OrganizationBankAccount, UUID> {
    
    List<OrganizationBankAccount> findByOrganizationIdOrderByIsPrimaryDescCreatedAtAsc(UUID organizationId);
    
    Optional<OrganizationBankAccount> findByOrganizationIdAndIsPrimaryTrue(UUID organizationId);
    
    Optional<OrganizationBankAccount> findByIban(String iban);
    
    boolean existsByIban(String iban);
    
    boolean existsByIbanAndIdNot(String iban, UUID id);
    
    boolean existsByOrganizationIdAndIsPrimaryTrue(UUID organizationId);
    
    List<OrganizationBankAccount> findByOrganizationIdAndIsVerifiedTrue(UUID organizationId);
    
    void deleteByOrganizationId(UUID organizationId);
    
    long countByOrganizationId(UUID organizationId);
}
```

---

## Entity Relationships Diagram

```
User (1) ─────────────── (1) Organization
                              │
                              ├──── (N) OrganizationContact
                              │
                              ├──── (N) OrganizationDocument ──── (1) User (verifiedBy)
                              │
                              └──── (N) OrganizationBankAccount

Organization ──── (1) User (verifiedBy)
```

---

## Important Implementation Notes

### 1. User Entity Reference
The User entity already exists from Furkan's work. Import it:
```java
import com.seffafbagis.api.entity.user.User;
```

### 2. Lazy Loading
Use `FetchType.LAZY` for all relationships to prevent N+1 query problems.

### 3. Cascade Operations
- `CascadeType.ALL` for contacts, documents, bank accounts (owned by organization)
- `orphanRemoval = true` to delete children when removed from parent collection

### 4. Bidirectional Relationships
Ensure both sides of the relationship are properly maintained:
```java
// In Organization.java
public void addContact(OrganizationContact contact) {
    contacts.add(contact);
    contact.setOrganization(this);
}

public void removeContact(OrganizationContact contact) {
    contacts.remove(contact);
    contact.setOrganization(null);
}
```

### 5. Index Annotations
Add appropriate indexes for frequently queried columns.

---

## Database Migration Check

**IMPORTANT:** Before running the application, verify that the existing `database_schema.sql` has been executed. If there are any missing columns or differences, create a migration file:

**Location:** `src/main/resources/db/migration/V17__organization_schema_updates.sql`

Only create this file if modifications to the existing schema are needed.

---

## Testing Requirements

After implementation, create tests:

### Repository Tests
**Location:** `src/test/java/com/seffafbagis/api/repository/`

#### OrganizationRepositoryTest.java
- Test findByUserId returns correct organization
- Test findByVerificationStatus filters correctly
- Test searchByKeyword finds matching organizations
- Test existsByTaxNumber checks uniqueness

#### OrganizationContactRepositoryTest.java
- Test findByOrganizationId returns all contacts
- Test findByOrganizationIdAndIsPrimaryTrue returns primary contact

#### OrganizationDocumentRepositoryTest.java
- Test findByOrganizationIdAndDocumentType filters correctly
- Test findExpiringDocuments finds documents expiring soon

#### OrganizationBankAccountRepositoryTest.java
- Test findByIban returns correct account
- Test existsByIban checks uniqueness

### Entity Tests
**Location:** `src/test/java/com/seffafbagis/api/entity/organization/`

#### OrganizationEntityTest.java
- Test cascade operations work correctly
- Test orphan removal deletes children
- Test bidirectional relationship maintenance

---

## Success Criteria

Before completing this phase, verify:

- [ ] OrganizationType enum created with FOUNDATION, ASSOCIATION, NGO
- [ ] VerificationStatus enum created with PENDING, IN_REVIEW, APPROVED, REJECTED
- [ ] Organization entity with all fields and relationships
- [ ] OrganizationContact entity with organization relationship
- [ ] OrganizationDocument entity with organization and verifiedBy relationships
- [ ] OrganizationBankAccount entity with organization relationship
- [ ] OrganizationRepository with all query methods
- [ ] OrganizationContactRepository with all query methods
- [ ] OrganizationDocumentRepository with all query methods including expiring documents
- [ ] OrganizationBankAccountRepository with all query methods
- [ ] All entities properly extend BaseEntity (where applicable)
- [ ] Proper indexes defined on entities
- [ ] Cascade and orphan removal configured correctly
- [ ] Application starts without entity mapping errors
- [ ] All repository tests pass

---

## Result File Requirement

After completing this phase, you MUST create a result file at:

**Location:** `docs/Emir/step_results/phase_2.0_result.md`

The result file must include:

1. **Summary**: Brief description of what was accomplished
2. **Files Created**: List all files created with their paths
3. **Entity Relationships**: Diagram or description of how entities relate
4. **Database Changes**: Any migrations added (if schema modifications needed)
5. **Repository Methods**: List of custom query methods in each repository
6. **Testing Results**: Which tests were run and their status
7. **Issues Encountered**: Any problems faced and how they were resolved
8. **Next Steps**: What needs to be done in the next phase (Phase 3.0)
9. **Checklist**: Mark all success criteria as completed or note what's pending

Example format:
```markdown
# Phase 2.0 Result: Organization Module - Entities & Repository

## Summary
Created Organization module data layer with 4 entities and 4 repositories.

## Files Created
- `src/main/java/com/seffafbagis/api/enums/OrganizationType.java`
- `src/main/java/com/seffafbagis/api/enums/VerificationStatus.java`
- `src/main/java/com/seffafbagis/api/entity/organization/Organization.java`
- [... list all files]

## Entity Relationships
[Include relationship diagram]

## Database Changes
- No migrations needed (schema already exists)
OR
- Added migration: `V17__organization_schema_updates.sql` for [reason]

## Repository Methods
### OrganizationRepository
- findByUserId(UUID userId)
- findByVerificationStatus(VerificationStatus status)
- [... list all]

## Testing Results
- OrganizationRepositoryTest: ✅ All tests passed
- [... list all test results]

## Issues Encountered
[Any issues and resolutions]

## Next Steps
- Phase 3.0: Organization Module - Service & Controller

## Success Criteria Checklist
- [x] OrganizationType enum created
- [x] VerificationStatus enum created
- [... complete checklist]
```

---

## Important Notes

1. **Do NOT use ternary operators** - Use if-else statements for better readability
2. **Follow existing code style** - Match patterns from Furkan's completed work
3. **Use existing BaseEntity** - Already created by Furkan
4. **Check User entity import** - Ensure correct import path
5. **Lazy loading** - All relationships should use LAZY fetch type
6. **No service/controller in this phase** - Only entities and repositories

---

## Dependencies from Furkan's Work

You will need to use these existing components:
- `BaseEntity` from `entity/base/BaseEntity.java`
- `User` from `entity/user/User.java`

---

## Estimated Duration

2 days

---

## Next Phase

After completing this phase, proceed to:
**Phase 3.0: Organization Module - Service & Controller**
