# PHASE 4.0: CAMPAIGN MODULE - ENTITIES & REPOSITORY

## Context and Background

You are working on the "Şeffaf Bağış Platformu" (Transparent Donation Platform), a graduation project for Fatih Sultan Mehmet Vakıf University.

**Project Stack:** Java 17, Spring Boot 3.x, PostgreSQL 15+, Redis, Maven

**Current Phase:** 4.0 - Campaign Module - Entities & Repository

**Previous Phases Completed:**
- Phase 1.0: Category & Donation Type Module ✅
- Phase 2.0: Organization Module - Entities & Repository ✅
- Phase 3.0: Organization Module - Service & Controller ✅

---

## Objective

Create all Campaign-related entities, enums, and repository interfaces. Campaigns are fundraising initiatives created by verified organizations to collect donations for specific causes.

---

## What This Phase Will Solve

1. **Campaign Creation**: Organizations need to create donation campaigns
2. **Multi-Category Support**: Campaigns can belong to multiple categories (many-to-many)
3. **Donation Type Support**: Campaigns accept specific donation types (Zakat, Fitr, etc.)
4. **Campaign Updates**: Organizations post news/updates about campaigns
5. **Campaign Gallery**: Multiple images per campaign
6. **Follower System**: Users can follow campaigns for notifications

---

## Database Schema Reference

### campaigns table
```sql
CREATE TABLE campaigns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    summary VARCHAR(500),
    description TEXT NOT NULL,
    cover_image_url VARCHAR(500),
    target_amount DECIMAL(12,2) NOT NULL,
    collected_amount DECIMAL(12,2) DEFAULT 0,
    donor_count INTEGER DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'TRY',
    status campaign_status DEFAULT 'draft',
    start_date TIMESTAMPTZ,
    end_date TIMESTAMPTZ,
    evidence_deadline_days INTEGER DEFAULT 15,
    is_urgent BOOLEAN DEFAULT FALSE,
    is_featured BOOLEAN DEFAULT FALSE,
    location_city VARCHAR(100),
    location_district VARCHAR(100),
    beneficiary_count INTEGER,
    created_by UUID REFERENCES users(id),
    approved_by UUID REFERENCES users(id),
    default_bank_account_id UUID REFERENCES organization_bank_accounts(id),
    approved_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
```

### campaign_categories (Many-to-Many)
```sql
CREATE TABLE campaign_categories (
    campaign_id UUID NOT NULL REFERENCES campaigns(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (campaign_id, category_id)
);
```

### campaign_donation_types (Many-to-Many)
```sql
CREATE TABLE campaign_donation_types (
    campaign_id UUID NOT NULL REFERENCES campaigns(id) ON DELETE CASCADE,
    donation_type_id UUID NOT NULL REFERENCES donation_types(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (campaign_id, donation_type_id)
);
```

### campaign_updates
```sql
CREATE TABLE campaign_updates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    campaign_id UUID NOT NULL REFERENCES campaigns(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    image_url VARCHAR(500),
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
```

### campaign_images
```sql
CREATE TABLE campaign_images (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    campaign_id UUID NOT NULL REFERENCES campaigns(id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500),
    caption VARCHAR(255),
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
```

### campaign_followers
```sql
CREATE TABLE campaign_followers (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    campaign_id UUID NOT NULL REFERENCES campaigns(id) ON DELETE CASCADE,
    notify_on_update BOOLEAN DEFAULT TRUE,
    notify_on_complete BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, campaign_id)
);
```

### campaign_status enum
```sql
CREATE TYPE campaign_status AS ENUM ('draft', 'pending_approval', 'active', 'paused', 'completed', 'cancelled');
```

---

## Files to Create

### 1. Enum
**Location:** `src/main/java/com/seffafbagis/api/enums/CampaignStatus.java`

Values: DRAFT, PENDING_APPROVAL, ACTIVE, PAUSED, COMPLETED, CANCELLED

---

### 2. Entities
**Location:** `src/main/java/com/seffafbagis/api/entity/campaign/`

#### Campaign.java
- Extend BaseEntity
- ManyToOne with Organization
- ManyToOne with User (createdBy, approvedBy)
- ManyToOne with OrganizationBankAccount (defaultBankAccount)
- OneToMany with CampaignCategory, CampaignDonationType, CampaignUpdate, CampaignImage
- ManyToMany with User through CampaignFollower

Key fields: title, slug, summary, description, coverImageUrl, targetAmount, collectedAmount, donorCount, currency, status, startDate, endDate, evidenceDeadlineDays, isUrgent, isFeatured, locationCity, locationDistrict, beneficiaryCount, approvedAt, completedAt

#### CampaignCategory.java (Composite Key)
- Embeddable ID class: CampaignCategoryId (campaignId, categoryId)
- ManyToOne with Campaign
- ManyToOne with Category
- Fields: isPrimary, createdAt

#### CampaignDonationType.java (Composite Key)
- Embeddable ID class: CampaignDonationTypeId (campaignId, donationTypeId)
- ManyToOne with Campaign
- ManyToOne with DonationType
- Field: createdAt

#### CampaignUpdate.java
- Extend BaseEntity
- ManyToOne with Campaign
- ManyToOne with User (createdBy)
- Fields: title, content, imageUrl, createdAt

#### CampaignImage.java
- Extend BaseEntity
- ManyToOne with Campaign
- Fields: imageUrl, thumbnailUrl, caption, displayOrder, createdAt

#### CampaignFollower.java (Composite Key)
- Embeddable ID class: CampaignFollowerId (userId, campaignId)
- ManyToOne with User
- ManyToOne with Campaign
- Fields: notifyOnUpdate, notifyOnComplete, createdAt

---

### 3. Embeddable ID Classes
**Location:** `src/main/java/com/seffafbagis/api/entity/campaign/`

Create these @Embeddable classes for composite keys:
- CampaignCategoryId.java
- CampaignDonationTypeId.java
- CampaignFollowerId.java

Each must implement Serializable and have equals/hashCode methods.

---

### 4. Repositories
**Location:** `src/main/java/com/seffafbagis/api/repository/`

#### CampaignRepository.java
Key methods:
- findBySlug(String slug)
- findByOrganizationId(UUID orgId)
- findByOrganizationIdAndStatus(UUID orgId, CampaignStatus status)
- findByStatus(CampaignStatus status, Pageable pageable)
- findByStatusOrderByIsFeaturedDesc(CampaignStatus status, Pageable pageable)
- findByIsFeaturedTrueAndStatus(CampaignStatus status)
- findByIsUrgentTrueAndStatus(CampaignStatus status)
- findByEndDateBeforeAndStatus(LocalDateTime date, CampaignStatus status)
- searchByKeyword(String keyword, CampaignStatus status, Pageable pageable) - JPQL query
- countByOrganizationIdAndStatus(UUID orgId, CampaignStatus status)

#### CampaignCategoryRepository.java
- findByCampaignId(UUID campaignId)
- findByCategoryId(UUID categoryId)
- findByCampaignIdAndIsPrimaryTrue(UUID campaignId)
- deleteByCampaignId(UUID campaignId)

#### CampaignDonationTypeRepository.java
- findByCampaignId(UUID campaignId)
- findByDonationTypeId(UUID donationTypeId)
- deleteByCampaignId(UUID campaignId)

#### CampaignUpdateRepository.java
- findByCampaignIdOrderByCreatedAtDesc(UUID campaignId)
- findByCampaignIdOrderByCreatedAtDesc(UUID campaignId, Pageable pageable)
- countByCampaignId(UUID campaignId)

#### CampaignImageRepository.java
- findByCampaignIdOrderByDisplayOrderAsc(UUID campaignId)
- countByCampaignId(UUID campaignId)
- findMaxDisplayOrderByCampaignId(UUID campaignId)

#### CampaignFollowerRepository.java
- findByCampaignId(UUID campaignId)
- findByUserId(UUID userId)
- existsByUserIdAndCampaignId(UUID userId, UUID campaignId)
- findByCampaignIdAndNotifyOnUpdateTrue(UUID campaignId)
- findByCampaignIdAndNotifyOnCompleteTrue(UUID campaignId)
- countByCampaignId(UUID campaignId)

---

## Entity Relationships Diagram

```
Organization (1) ──── (N) Campaign
                           │
    ┌──────────────────────┼──────────────────────┐
    │                      │                      │
    ▼                      ▼                      ▼
(N) CampaignCategory   (N) CampaignUpdate    (N) CampaignImage
    │                      
    ▼                      
(N) Category               

Campaign (N) ──── (M) DonationType  [via CampaignDonationType]
Campaign (N) ──── (M) User          [via CampaignFollower]
```

---

## Important Implementation Notes

1. **Composite Keys**: Use @EmbeddedId for CampaignCategory, CampaignDonationType, CampaignFollower
2. **Lazy Loading**: All ManyToOne relationships should be LAZY
3. **Cascade**: Campaign should cascade to updates and images (orphanRemoval = true)
4. **Indexes**: Add indexes on status, slug, organization_id, is_featured, is_urgent
5. **Slug Uniqueness**: Slug must be unique constraint

---

## Testing Requirements

### Repository Tests
**Location:** `src/test/java/com/seffafbagis/api/repository/`

- CampaignRepositoryTest: Test findBySlug, findByStatus, searchByKeyword
- CampaignCategoryRepositoryTest: Test composite key operations
- CampaignFollowerRepositoryTest: Test existsByUserIdAndCampaignId

### Entity Tests
- Test cascade operations work correctly
- Test composite key entities

---

## Success Criteria

- [ ] CampaignStatus enum created with all 6 values
- [ ] Campaign entity with all relationships
- [ ] CampaignCategory entity with composite key
- [ ] CampaignDonationType entity with composite key
- [ ] CampaignUpdate entity
- [ ] CampaignImage entity
- [ ] CampaignFollower entity with composite key
- [ ] All 3 embeddable ID classes created
- [ ] All 6 repositories with custom query methods
- [ ] Proper indexes defined
- [ ] Application starts without errors
- [ ] All repository tests pass

---

## Result File Requirement

After completing this phase, create:
**Location:** `docs/Emir/step_results/phase_4.0_result.md`

Include:
1. Summary of accomplishments
2. Files created list
3. Entity relationships description
4. Any database migrations added
5. Testing results
6. Issues encountered and resolutions
7. Next steps (Phase 5.0)
8. Completed success criteria checklist

---

## Important Notes

1. **Do NOT use ternary operators** - Use if-else for readability
2. **Use existing entities**: Organization (Phase 2), Category, DonationType (Phase 1), User (Furkan)
3. **Follow composite key pattern** for many-to-many with extra fields
4. **No services/controllers in this phase** - Only entities and repositories

---

## Dependencies

From previous phases:
- BaseEntity, User (Furkan)
- Organization, OrganizationBankAccount (Phase 2)
- Category, DonationType (Phase 1)

---

## Estimated Duration

2 days

---

## Next Phase

**Phase 5.0: Campaign Module - Service & Controller**
