# PHASE 5.0: CAMPAIGN MODULE - SERVICE & CONTROLLER

## Context and Background

You are working on the "Şeffaf Bağış Platformu" (Transparent Donation Platform), a graduation project for Fatih Sultan Mehmet Vakıf University.

**Project Stack:** Java 17, Spring Boot 3.x, PostgreSQL 15+, Redis, Maven

**Current Phase:** 5.0 - Campaign Module - Service & Controller

**Previous Phases Completed:**
- Phase 1.0: Category & Donation Type Module ✅
- Phase 2.0: Organization Module - Entities & Repository ✅
- Phase 3.0: Organization Module - Service & Controller ✅
- Phase 4.0: Campaign Module - Entities & Repository ✅

---

## Objective

Implement business logic (services) and REST API (controllers) for campaign management. This includes campaign CRUD, status workflow, category/donation-type assignment, updates, images, and follower functionality. Also implement `ICampaignService` interface for Furkan's admin module.

---

## What This Phase Will Solve

1. **Campaign CRUD**: Organizations create, update, delete campaigns
2. **Status Workflow**: DRAFT → PENDING_APPROVAL → ACTIVE → COMPLETED
3. **Admin Approval**: Integration with admin module via interface
4. **Public Discovery**: List, search, filter campaigns by category/status
5. **Campaign Updates**: Post news and progress updates
6. **Image Gallery**: Manage campaign images
7. **Follow System**: Users follow campaigns for notifications

---

## Interface from Furkan's Work

**MUST Implement:** `ICampaignService` interface

```java
public interface ICampaignService {
    CampaignResponse getById(UUID id);
    void approve(UUID id, ApproveCampaignRequest request);
    void reject(UUID id, ApproveCampaignRequest request);
    Page<CampaignResponse> getPendingApprovals(Pageable pageable);
    Page<CampaignResponse> getAllCampaigns(Pageable pageable);
    CampaignDetailResponse getCampaignDetail(UUID id);
}
```

---

## Files to Create

### 1. DTOs - Request
**Location:** `src/main/java/com/seffafbagis/api/dto/request/campaign/`

#### CreateCampaignRequest.java
Fields (with validation):
- title (required, max 255)
- summary (max 500)
- description (required)
- coverImageUrl (max 500)
- targetAmount (required, positive)
- currency (default TRY)
- startDate, endDate
- evidenceDeadlineDays (default 15)
- isUrgent (default false)
- locationCity, locationDistrict
- beneficiaryCount
- categoryIds (required, at least one UUID)
- primaryCategoryId (required, UUID)
- donationTypeIds (at least one UUID)
- defaultBankAccountId (UUID)

#### UpdateCampaignRequest.java
- Same fields as create, all optional for partial update
- Cannot update if status is COMPLETED or CANCELLED

#### SubmitForApprovalRequest.java
- Optional notes field for admin

#### AddCampaignUpdateRequest.java
- title (required, max 255)
- content (required)
- imageUrl (optional)

#### AddCampaignImageRequest.java
- imageUrl (required)
- thumbnailUrl (optional)
- caption (max 255)
- displayOrder (optional)

#### CampaignSearchRequest.java
- keyword (optional)
- categorySlug (optional)
- donationTypeCode (optional)
- city (optional)
- isUrgent (optional)
- minAmount, maxAmount (optional)
- sortBy (optional: newest, ending_soon, most_funded, most_donors)

---

### 2. DTOs - Response
**Location:** `src/main/java/com/seffafbagis/api/dto/response/campaign/`

#### CampaignResponse.java
Basic fields: id, title, slug, summary, coverImageUrl, targetAmount, collectedAmount, donorCount, currency, status, startDate, endDate, isUrgent, isFeatured, locationCity, createdAt
Plus: organizationId, organizationName, organizationLogo, progressPercentage

#### CampaignDetailResponse.java
All CampaignResponse fields plus:
- description, beneficiaryCount, evidenceDeadlineDays
- approvedAt, completedAt
- categories (list), donationTypes (list)
- images (list), recentUpdates (list)
- organization summary
- transparencyScore (from organization)
- followerCount, isFollowedByCurrentUser

#### CampaignListResponse.java
Optimized for list/cards: id, title, slug, coverImageUrl, targetAmount, collectedAmount, progressPercentage, donorCount, isUrgent, isFeatured, organizationName, organizationLogo, daysRemaining

#### CampaignSummaryResponse.java
Minimal: id, title, slug, coverImageUrl, targetAmount, collectedAmount

#### CampaignUpdateResponse.java
id, title, content, imageUrl, createdByName, createdAt

#### CampaignImageResponse.java
id, imageUrl, thumbnailUrl, caption, displayOrder

#### CampaignStatsResponse.java
totalDonations, totalDonors, averageDonation, largestDonation, donationsByType (map), donationsByDay (list for chart)

#### CampaignProgressResponse.java
targetAmount, collectedAmount, progressPercentage, donorCount, daysRemaining, isCompleted

---

### 3. Mapper
**Location:** `src/main/java/com/seffafbagis/api/dto/mapper/CampaignMapper.java`

Methods to implement:
- Entity to Response conversions (all response types)
- Request to Entity (create, update)
- Calculate progressPercentage: (collectedAmount / targetAmount) * 100
- Calculate daysRemaining: days between now and endDate
- Handle category and donationType lists

---

### 4. Services
**Location:** `src/main/java/com/seffafbagis/api/service/campaign/`

#### CampaignService.java (implements ICampaignService)

**Public Methods:**
- getActiveCampaigns(Pageable) - status = ACTIVE
- getFeaturedCampaigns() - featured + ACTIVE
- getUrgentCampaigns() - urgent + ACTIVE
- getCampaignBySlug(String slug) - public detail
- getCampaignsByCategory(String categorySlug, Pageable)
- getCampaignsByOrganization(UUID orgId, Pageable) - only ACTIVE for public
- searchCampaigns(CampaignSearchRequest, Pageable)

**Organization Owner Methods:**
- getMyCampaigns(Pageable) - all statuses for owner
- getMyCampaign(UUID id) - owner's campaign detail
- createCampaign(CreateCampaignRequest) - creates as DRAFT
- updateCampaign(UUID id, UpdateCampaignRequest)
- deleteCampaign(UUID id) - only DRAFT can be deleted
- submitForApproval(UUID id) - DRAFT → PENDING_APPROVAL
- pauseCampaign(UUID id) - ACTIVE → PAUSED
- resumeCampaign(UUID id) - PAUSED → ACTIVE
- completeCampaign(UUID id) - manually mark as COMPLETED

**ICampaignService Implementation (Admin):**
- getById(UUID id)
- approve(UUID id, request) - PENDING_APPROVAL → ACTIVE
- reject(UUID id, request) - PENDING_APPROVAL → DRAFT with reason
- getPendingApprovals(Pageable)
- getAllCampaigns(Pageable)
- getCampaignDetail(UUID id)

**Internal Methods (called by DonationService later):**
- incrementDonationStats(UUID campaignId, BigDecimal amount) - update collectedAmount, donorCount
- checkAndUpdateCompletionStatus(UUID campaignId) - auto-complete if target reached

**Business Rules:**
1. Only APPROVED organizations can create campaigns
2. Organization transparency score must be >= 40 to create campaigns
3. Slug auto-generated from title using SlugGenerator
4. Cannot update COMPLETED or CANCELLED campaigns
5. Cannot delete non-DRAFT campaigns
6. At least one category required, one must be primary
7. At least one donation type required
8. startDate must be before endDate
9. targetAmount must be positive

#### CampaignUpdateService.java
- getUpdates(UUID campaignId, Pageable)
- addUpdate(UUID campaignId, AddCampaignUpdateRequest)
- deleteUpdate(UUID updateId) - only owner can delete

#### CampaignImageService.java
- getImages(UUID campaignId)
- addImage(UUID campaignId, AddCampaignImageRequest)
- deleteImage(UUID imageId)
- reorderImages(UUID campaignId, List<UUID> orderedImageIds)

#### CampaignFollowerService.java
- followCampaign(UUID campaignId)
- unfollowCampaign(UUID campaignId)
- isFollowing(UUID campaignId) - for current user
- getFollowersToNotify(UUID campaignId, boolean forUpdate) - returns users to notify
- getFollowedCampaigns(Pageable) - current user's followed campaigns

---

### 5. Controllers
**Location:** `src/main/java/com/seffafbagis/api/controller/campaign/`

#### CampaignController.java

**Public Endpoints (No Auth):**
```
GET  /api/v1/campaigns                     - List active campaigns
GET  /api/v1/campaigns/featured            - Featured campaigns
GET  /api/v1/campaigns/urgent              - Urgent campaigns  
GET  /api/v1/campaigns/{slug}              - Campaign detail by slug
GET  /api/v1/campaigns/category/{slug}     - By category
GET  /api/v1/campaigns/organization/{id}   - By organization
GET  /api/v1/campaigns/search              - Search with filters
GET  /api/v1/campaigns/{id}/stats          - Campaign statistics
GET  /api/v1/campaigns/{id}/updates        - Campaign updates
GET  /api/v1/campaigns/{id}/images         - Campaign gallery
```

**Owner Endpoints (FOUNDATION role):**
```
GET    /api/v1/campaigns/my                - My campaigns
POST   /api/v1/campaigns                   - Create campaign
PUT    /api/v1/campaigns/{id}              - Update campaign
DELETE /api/v1/campaigns/{id}              - Delete draft
POST   /api/v1/campaigns/{id}/submit       - Submit for approval
PUT    /api/v1/campaigns/{id}/pause        - Pause campaign
PUT    /api/v1/campaigns/{id}/resume       - Resume campaign
PUT    /api/v1/campaigns/{id}/complete     - Mark completed
POST   /api/v1/campaigns/{id}/updates      - Add update
DELETE /api/v1/campaigns/{id}/updates/{uid} - Delete update
POST   /api/v1/campaigns/{id}/images       - Add image
DELETE /api/v1/campaigns/{id}/images/{iid} - Delete image
PUT    /api/v1/campaigns/{id}/images/reorder - Reorder images
```

**User Endpoints (Authenticated):**
```
POST   /api/v1/campaigns/{id}/follow       - Follow campaign
DELETE /api/v1/campaigns/{id}/follow       - Unfollow campaign
GET    /api/v1/campaigns/following         - My followed campaigns
```

#### CampaignUpdateController.java
Handle update-specific endpoints if separating from main controller.

#### CampaignImageController.java
Handle image-specific endpoints if separating from main controller.

#### CampaignFollowerController.java
Handle follower-specific endpoints if separating from main controller.

---

## Campaign Status Workflow

```
                    ┌─────────┐
                    │  DRAFT  │ ← Initial state
                    └────┬────┘
                         │ submitForApproval()
                         ▼
                ┌─────────────────┐
                │PENDING_APPROVAL │
                └────────┬────────┘
                    ┌────┴────┐
            approve()│        │reject()
                    ▼         ▼
              ┌────────┐  ┌───────┐
              │ ACTIVE │  │ DRAFT │ (with rejection reason)
              └───┬────┘  └───────┘
                  │
        ┌─────────┼─────────┐
        │         │         │
   pause()    complete() cancel()
        │         │         │
        ▼         ▼         ▼
   ┌────────┐ ┌─────────┐ ┌──────────┐
   │ PAUSED │ │COMPLETED│ │CANCELLED │
   └───┬────┘ └─────────┘ └──────────┘
       │
   resume()
       │
       ▼
   ┌────────┐
   │ ACTIVE │
   └────────┘
```

---

## Testing Requirements

### Unit Tests
**Location:** `src/test/java/com/seffafbagis/api/service/campaign/`

- CampaignServiceTest:
  - Test createCampaign generates unique slug
  - Test createCampaign fails for non-approved organization
  - Test createCampaign fails if transparency score < 40
  - Test status transitions follow workflow
  - Test cannot delete non-DRAFT campaign
  - Test incrementDonationStats updates correctly

- CampaignFollowerServiceTest:
  - Test follow/unfollow toggles correctly
  - Test getFollowersToNotify returns correct users

### Integration Tests
- Full campaign lifecycle test
- Search functionality test

---

## Success Criteria

- [ ] All 6 request DTOs created with validation
- [ ] All 8 response DTOs created
- [ ] CampaignMapper handles all conversions and calculations
- [ ] CampaignService implements ICampaignService interface
- [ ] All status transitions implemented correctly
- [ ] Organization verification check before campaign creation
- [ ] Transparency score check (>= 40) before campaign creation
- [ ] Slug auto-generation with uniqueness
- [ ] CampaignUpdateService complete
- [ ] CampaignImageService complete with reorder
- [ ] CampaignFollowerService complete
- [ ] All controllers with proper authorization
- [ ] Swagger shows all endpoints
- [ ] All unit tests pass

---

## Result File Requirement

After completing this phase, create:
**Location:** `docs/Emir/step_results/phase_5.0_result.md`

Include:
1. Summary
2. Files created
3. API endpoints table
4. ICampaignService implementation confirmation
5. Testing results
6. Issues and resolutions
7. Next steps (Phase 6.0)
8. Success criteria checklist

---

## Important Notes

1. **Do NOT use ternary operators** - Use if-else
2. **Implement ICampaignService** - Required for Furkan's admin module
3. **Use SlugGenerator** from Furkan's utilities
4. **Check organization status** before allowing campaign creation
5. **Use SecurityUtils** to get current user
6. **Wrap responses in ApiResponse**

---

## Dependencies

From Furkan's work:
- ICampaignService interface
- SlugGenerator, SecurityUtils, ApiResponse
- AuditLogService for logging actions

From previous phases:
- Organization, OrganizationService (Phase 2, 3)
- Category, DonationType (Phase 1)
- Campaign entities and repositories (Phase 4)

---

## Estimated Duration

3 days

---

## Next Phase

**Phase 6.0: Donation Module - Entities & Repository**
