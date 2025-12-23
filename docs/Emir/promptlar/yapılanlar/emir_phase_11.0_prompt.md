# PHASE 11.0: TRANSPARENCY SCORE MODULE

## Context and Background

You are working on the "Şeffaf Bağış Platformu" (Transparent Donation Platform), a graduation project for Fatih Sultan Mehmet Vakıf University.

**Project Stack:** Java 17, Spring Boot 3.x, PostgreSQL 15+, Redis, Maven

**Current Phase:** 11.0 - Transparency Score Module

**Previous Phases Completed:**
- Phase 1.0-9.0: Foundation, Donation, Payment modules ✅
- Phase 10.0: Evidence Module ✅

---

## Objective

Implement the transparency scoring system that rates organizations based on their reporting performance, evidence quality, and accountability. This score determines organization visibility and ability to create campaigns.

---

## What This Phase Will Solve

1. **Dynamic Scoring**: Calculate and update organization transparency scores
2. **Score History**: Track all score changes with reasons
3. **Thresholds**: Enforce minimum scores for campaign creation
4. **Public Display**: Show scores on organization profiles
5. **Incentivize Transparency**: Reward timely and quality reporting

---

## Database Schema Reference

### transparency_scores table
```sql
CREATE TABLE transparency_scores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID UNIQUE NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    current_score DECIMAL(5,2) DEFAULT 50.00,
    total_campaigns INTEGER DEFAULT 0,
    completed_campaigns INTEGER DEFAULT 0,
    on_time_reports INTEGER DEFAULT 0,
    late_reports INTEGER DEFAULT 0,
    approved_evidences INTEGER DEFAULT 0,
    rejected_evidences INTEGER DEFAULT 0,
    last_calculated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
```

### transparency_score_history table
```sql
CREATE TABLE transparency_score_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    previous_score DECIMAL(5,2),
    new_score DECIMAL(5,2) NOT NULL,
    change_reason VARCHAR(255) NOT NULL,
    campaign_id UUID REFERENCES campaigns(id),
    evidence_id UUID REFERENCES evidences(id),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
```

---

## Files to Create

### 1. Entities
**Location:** `src/main/java/com/seffafbagis/api/entity/transparency/`

#### TransparencyScore.java
- Extend BaseEntity
- OneToOne: Organization

Fields: currentScore, totalCampaigns, completedCampaigns, onTimeReports, lateReports, approvedEvidences, rejectedEvidences, lastCalculatedAt

#### TransparencyScoreHistory.java
- Extend BaseEntity
- ManyToOne: Organization, Campaign (nullable), Evidence (nullable)

Fields: previousScore, newScore, changeReason, createdAt

---

### 2. Repositories
**Location:** `src/main/java/com/seffafbagis/api/repository/`

#### TransparencyScoreRepository.java
- findByOrganizationId(UUID organizationId)
- findByCurrentScoreLessThan(BigDecimal threshold)
- findByCurrentScoreGreaterThanEqual(BigDecimal threshold)
- findTopByOrderByCurrentScoreDesc(Pageable pageable) - leaderboard

#### TransparencyScoreHistoryRepository.java
- findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId, Pageable pageable)
- findByOrganizationIdAndCreatedAtBetween(UUID orgId, LocalDateTime start, LocalDateTime end)
- findByCampaignId(UUID campaignId)
- findByEvidenceId(UUID evidenceId)

---

### 3. DTOs - Response
**Location:** `src/main/java/com/seffafbagis/api/dto/response/transparency/`

#### TransparencyScoreResponse.java
Fields: organizationId, organizationName, currentScore, scoreLevel (text like "Yüksek", "Orta", "Düşük"), totalCampaigns, completedCampaigns, onTimeReports, lateReports, approvedEvidences, rejectedEvidences, lastCalculatedAt

#### ScoreHistoryResponse.java
Fields: id, previousScore, newScore, changeAmount, changeReason, campaignTitle (if related), createdAt

#### ScoreHistoryListResponse.java
List with pagination

#### TransparencyLeaderboardResponse.java
Fields: rank, organizationId, organizationName, logoUrl, currentScore, completedCampaigns

---

### 4. Services
**Location:** `src/main/java/com/seffafbagis/api/service/transparency/`

#### TransparencyScoreService.java

**Public Methods:**
- getOrganizationScore(UUID organizationId)
- getScoreHistory(UUID organizationId, Pageable pageable)
- getLeaderboard(Pageable pageable) - top organizations by score

**Internal Methods (called by other services):**
- initializeScore(UUID organizationId) - create with base 50
- canCreateCampaign(UUID organizationId) - check score >= 40
- getScoreLevel(BigDecimal score) - return level text

**Score Update Methods:**
- onEvidenceApproved(UUID evidenceId, boolean onTime)
- onEvidenceRejected(UUID evidenceId)
- onEvidenceMissedDeadline(UUID campaignId)
- onCampaignCompleted(UUID campaignId)
- onCampaignCancelled(UUID campaignId)
- onReportUpheld(UUID reportId) - fraud report confirmed
- recalculateScore(UUID organizationId) - full recalculation

#### TransparencyScoreCalculator.java

**Calculation Logic:**

Base score: 50 points (new organizations start here)
Range: 0 - 100

**Positive Factors:**
| Action | Points |
|--------|--------|
| Evidence approved (on time) | +5 |
| Evidence approved (late but before deadline) | +3 |
| Evidence approved (after deadline) | +2 |
| Campaign completed successfully | +3 |
| Consistent reporting (per month streak) | +1 |

**Negative Factors:**
| Action | Points |
|--------|--------|
| Evidence rejected | -5 |
| Evidence not uploaded (deadline missed) | -10 |
| Late evidence upload | -3 |
| Campaign cancelled | -2 |
| Fraud report upheld | -15 |

**Thresholds:**
| Score Range | Level | Restrictions |
|-------------|-------|--------------|
| 80-100 | Çok Yüksek | Featured eligibility |
| 60-79 | Yüksek | No restrictions |
| 40-59 | Orta | No restrictions |
| 30-39 | Düşük | Cannot create new campaigns |
| 0-29 | Çok Düşük | Flagged for review, campaigns paused |

**Calculator Methods:**
- calculateScore(TransparencyScore stats) - compute from stats
- calculateChange(String reason) - get point change for reason
- applyChange(TransparencyScore score, BigDecimal change, String reason, UUID campaignId, UUID evidenceId)

---

### 5. Controller
**Location:** `src/main/java/com/seffafbagis/api/controller/transparency/`

#### TransparencyController.java

**Public Endpoints:**
```
GET /api/v1/transparency/organization/{id}    - Organization score (public)
GET /api/v1/transparency/organization/{id}/history - Score history
GET /api/v1/transparency/leaderboard          - Top organizations
```

**Organization Endpoints (FOUNDATION role):**
```
GET /api/v1/transparency/my                   - My organization's score
GET /api/v1/transparency/my/history           - My score history
GET /api/v1/transparency/my/can-create-campaign - Check if can create
```

**Admin Endpoints:**
```
GET  /api/v1/admin/transparency/low-score     - Organizations below threshold
POST /api/v1/admin/transparency/{orgId}/recalculate - Force recalculation
```

---

## Score Update Flow

```
Evidence Approved
       │
       ▼
EvidenceService.reviewEvidence()
       │
       ▼
TransparencyScoreService.onEvidenceApproved(evidenceId, onTime)
       │
       ▼
TransparencyScoreCalculator.applyChange(+5 or +3 or +2)
       │
       ▼
Save TransparencyScore
       │
       ▼
Create TransparencyScoreHistory entry
       │
       ▼
(Optional) Send notification if threshold crossed
```

---

## Integration with Previous Phases

### Phase 5 (Campaign Creation)
```java
// In CampaignService.createCampaign()
if (!transparencyScoreService.canCreateCampaign(organizationId)) {
    throw new BadRequestException("Transparency score too low to create campaigns");
}
```

### Phase 10 (Evidence Review)
```java
// In EvidenceService.reviewEvidence()
if (request.isApproved()) {
    boolean onTime = isBeforeDeadline(evidence);
    transparencyScoreService.onEvidenceApproved(evidence.getId(), onTime);
} else {
    transparencyScoreService.onEvidenceRejected(evidence.getId());
}
```

---

## Score History Change Reasons

Standard change_reason values:
- `EVIDENCE_APPROVED_ON_TIME`
- `EVIDENCE_APPROVED_LATE`
- `EVIDENCE_REJECTED`
- `EVIDENCE_DEADLINE_MISSED`
- `CAMPAIGN_COMPLETED`
- `CAMPAIGN_CANCELLED`
- `REPORT_UPHELD`
- `MONTHLY_CONSISTENCY_BONUS`
- `MANUAL_ADJUSTMENT`
- `INITIAL_SCORE`

---

## Testing Requirements

### Unit Tests
- TransparencyScoreServiceTest:
  - Test initializeScore creates with 50
  - Test canCreateCampaign returns correct for thresholds
  - Test score updates correctly for each action

- TransparencyScoreCalculatorTest:
  - Test point calculation for each action type
  - Test score stays within 0-100 bounds
  - Test threshold level determination

### Integration Tests
- Evidence approval → score update → history created
- Campaign creation blocked when score < 40

---

## Success Criteria

- [ ] TransparencyScore and TransparencyScoreHistory entities created
- [ ] Both repositories with custom queries
- [ ] All response DTOs created
- [ ] TransparencyScoreService with all methods
- [ ] TransparencyScoreCalculator with point logic
- [ ] Score initialization at 50 for new organizations
- [ ] All score update triggers working
- [ ] History entries created on each change
- [ ] Score bounds enforced (0-100)
- [ ] canCreateCampaign threshold check (>= 40)
- [ ] Leaderboard query working
- [ ] All endpoints with proper authorization
- [ ] All unit tests pass

---

## Result File Requirement

After completing this phase, create:
**Location:** `docs/Emir/step_results/phase_11.0_result.md`

Include:
1. Summary
2. Files created
3. Scoring algorithm details
4. Threshold table
5. Integration points with other phases
6. API endpoints table
7. Testing results
8. Issues and resolutions
9. Next steps (Phase 12.0)
10. Success criteria checklist

---

## Important Notes

1. **Do NOT use ternary operators** - Use if-else
2. **Score bounds** - Always keep between 0 and 100
3. **History is immutable** - Never delete history entries
4. **Integrate with Phase 5** - Add campaign creation check
5. **Integrate with Phase 10** - Add evidence review triggers

---

## Dependencies

From Furkan's work:
- SecurityUtils, ApiResponse
- AuditLogService

From previous phases:
- Organization (Phase 2)
- Campaign (Phase 4, 5)
- Evidence, EvidenceService (Phase 10)

---

## Estimated Duration

3 days

---

## Next Phase

**Phase 12.0: Application Module (Beneficiary Applications)**
