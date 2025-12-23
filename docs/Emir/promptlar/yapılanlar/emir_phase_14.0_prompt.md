# PHASE 14.0: REPORT MODULE (FRAUD & COMPLAINT REPORTS)

## Context and Background

You are working on the "Şeffaf Bağış Platformu" (Transparent Donation Platform), a graduation project for Fatih Sultan Mehmet Vakıf University.

**Project Stack:** Java 17, Spring Boot 3.x, PostgreSQL 15+, Redis, Maven

**Current Phase:** 14.0 - Report Module

**Previous Phases Completed:**
- Phase 1.0-12.0: All foundation modules ✅
- Phase 13.0: Notification Module ✅

---

## Objective

Implement a reporting system where users can report fraudulent campaigns, suspicious organizations, or inappropriate content. Admins review and resolve these reports with appropriate actions.

---

## What This Phase Will Solve

1. **Fraud Reporting**: Report suspicious campaigns or organizations
2. **Content Moderation**: Report inappropriate content
3. **Admin Review**: Queue and workflow for report handling
4. **Priority System**: Urgent reports get faster attention
5. **Resolution Tracking**: Track how reports are resolved
6. **Transparency Impact**: Upheld fraud reports affect transparency score

---

## Database Schema Reference

### reports table
```sql
CREATE TABLE reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reporter_id UUID REFERENCES users(id),               -- NULL for anonymous
    report_type VARCHAR(50) NOT NULL,                    -- 'fraud', 'inappropriate', 'spam', 'other'
    entity_type VARCHAR(50) NOT NULL,                    -- 'campaign', 'organization', 'user'
    entity_id UUID NOT NULL,
    reason VARCHAR(255) NOT NULL,
    description TEXT,
    evidence_urls TEXT[],                                -- Evidence links
    priority VARCHAR(20) DEFAULT 'medium',
    status VARCHAR(20) DEFAULT 'pending',                -- 'pending', 'investigating', 'resolved', 'dismissed'
    resolution_notes TEXT,
    resolved_by UUID REFERENCES users(id),
    resolved_at TIMESTAMPTZ,
    assigned_to UUID REFERENCES users(id),
    assigned_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
```

---

## Files to Create

### 1. Enums
**Location:** `src/main/java/com/seffafbagis/api/enums/`

#### ReportType.java
Values: FRAUD, INAPPROPRIATE, SPAM, MISLEADING, OTHER

#### ReportStatus.java
Values: PENDING, INVESTIGATING, RESOLVED, DISMISSED

#### ReportPriority.java
Values: LOW, MEDIUM, HIGH, CRITICAL

#### ReportEntityType.java
Values: CAMPAIGN, ORGANIZATION, USER

---

### 2. Entity
**Location:** `src/main/java/com/seffafbagis/api/entity/report/`

#### Report.java
- Extend BaseEntity
- ManyToOne: User (reporter, nullable), User (resolvedBy), User (assignedTo)

Fields: reportType, entityType, entityId, reason, description, evidenceUrls (List<String>), priority, status, resolutionNotes, resolvedAt, assignedAt

For PostgreSQL TEXT[] array:
```java
@Type(ListArrayType.class)
@Column(columnDefinition = "text[]")
private List<String> evidenceUrls;
```

---

### 3. Repository
**Location:** `src/main/java/com/seffafbagis/api/repository/ReportRepository.java`

Key methods:
- findByStatus(ReportStatus status, Pageable pageable)
- findByStatusOrderByPriorityDescCreatedAtAsc(ReportStatus status, Pageable) - priority queue
- findByEntityTypeAndEntityId(ReportEntityType entityType, UUID entityId)
- findByReporterId(UUID reporterId, Pageable pageable)
- findByAssignedTo(UUID adminId, Pageable pageable)
- findByReportTypeAndStatus(ReportType type, ReportStatus status, Pageable)
- countByStatus(ReportStatus status)
- countByEntityTypeAndEntityIdAndStatus(ReportEntityType type, UUID entityId, ReportStatus status)
- existsByReporterIdAndEntityTypeAndEntityIdAndStatusNot(UUID reporterId, ReportEntityType type, UUID entityId, ReportStatus dismissed) - prevent duplicate reports

---

### 4. DTOs - Request
**Location:** `src/main/java/com/seffafbagis/api/dto/request/report/`

#### CreateReportRequest.java
Fields:
- reportType (required)
- entityType (required)
- entityId (required, UUID)
- reason (required, max 255)
- description (optional, max 2000)
- evidenceUrls (optional, list of valid URLs)
- isAnonymous (optional, default false)

#### AssignReportRequest.java
Fields:
- assignedTo (required, UUID of admin user)
- notes (optional)

#### ResolveReportRequest.java
Fields:
- resolution (required: RESOLVED or DISMISSED)
- resolutionNotes (required, max 1000)
- takeAction (optional, boolean) - If true, take action on reported entity
- actionType (required if takeAction: WARN, SUSPEND, BAN, REMOVE_CONTENT)

#### UpdateReportPriorityRequest.java
Fields:
- priority (required: LOW, MEDIUM, HIGH, CRITICAL)
- reason (optional)

---

### 5. DTOs - Response
**Location:** `src/main/java/com/seffafbagis/api/dto/response/report/`

#### ReportResponse.java
Fields: id, reportType, reportTypeName, entityType, entityTypeName, entityId, entityName (campaign title, org name, or username), reason, priority, priorityName, status, statusName, createdAt, isAnonymous

#### ReportDetailResponse.java
All ReportResponse fields plus:
- reporterName (if not anonymous)
- description
- evidenceUrls
- assignedToName
- assignedAt
- resolvedByName
- resolvedAt
- resolutionNotes

#### ReportListResponse.java
List with summary: totalCount, pendingCount, investigatingCount, resolvedCount, dismissedCount

#### ReportStatsResponse.java
For admin dashboard: byType (map), byPriority (map), byStatus (map), averageResolutionTimeHours, todayNewCount, todayResolvedCount

---

### 6. Mapper
**Location:** `src/main/java/com/seffafbagis/api/dto/mapper/ReportMapper.java`

Methods:
- toEntity(CreateReportRequest, User reporter)
- toResponse(Report)
- toDetailResponse(Report)
- getEntityName(ReportEntityType type, UUID entityId) - fetch name from respective service

---

### 7. Service
**Location:** `src/main/java/com/seffafbagis/api/service/report/ReportService.java`

**User Methods:**
- createReport(CreateReportRequest) - submit a report
- getMyReports(Pageable) - user's submitted reports
- getMyReport(UUID reportId) - detail of own report

**Admin Methods:**
- getPendingReports(Pageable) - priority-ordered queue
- getReportsByStatus(ReportStatus status, Pageable)
- getReportsByType(ReportType type, Pageable)
- getReportDetail(UUID reportId)
- assignReport(UUID reportId, AssignReportRequest)
- startInvestigation(UUID reportId) - move to INVESTIGATING
- resolveReport(UUID reportId, ResolveReportRequest)
- updatePriority(UUID reportId, UpdateReportPriorityRequest)
- getMyAssignedReports(Pageable) - reports assigned to current admin
- getReportStats()
- getEntityReports(ReportEntityType type, UUID entityId) - all reports for an entity

**Business Logic:**
1. Prevent duplicate reports (same reporter, same entity, not dismissed)
2. Anonymous reports allowed (reporter_id = null)
3. Auto-assign priority based on report type:
   - FRAUD → HIGH
   - INAPPROPRIATE → MEDIUM
   - SPAM → LOW
   - OTHER → MEDIUM
4. On RESOLVED with takeAction:
   - If CAMPAIGN: pause or cancel campaign
   - If ORGANIZATION: update verification status or suspend
   - If USER: warn or suspend user
5. On FRAUD report upheld against organization:
   - Call transparencyScoreService.onReportUpheld(reportId)
6. Notify reporter when report is resolved
7. Track resolution time for metrics

**Helper Methods:**
- validateEntityExists(ReportEntityType type, UUID entityId)
- getEntityName(ReportEntityType type, UUID entityId)
- calculateAutoPriority(ReportType type)
- executeAction(Report report, String actionType)

---

### 8. Controller
**Location:** `src/main/java/com/seffafbagis/api/controller/report/`

#### ReportController.java

**User Endpoints (Authenticated):**
```
POST /api/v1/reports                    - Submit a report
GET  /api/v1/reports/my                 - My submitted reports
GET  /api/v1/reports/my/{id}            - My report detail
```

**Public Endpoint:**
```
POST /api/v1/reports/anonymous          - Anonymous report (no auth required)
```

**Admin Endpoints:**
```
GET    /api/v1/admin/reports                    - All reports (with filters)
GET    /api/v1/admin/reports/pending            - Pending queue (priority ordered)
GET    /api/v1/admin/reports/assigned           - My assigned reports
GET    /api/v1/admin/reports/{id}               - Report detail
POST   /api/v1/admin/reports/{id}/assign        - Assign to admin
POST   /api/v1/admin/reports/{id}/investigate   - Start investigation
POST   /api/v1/admin/reports/{id}/resolve       - Resolve report
PUT    /api/v1/admin/reports/{id}/priority      - Update priority
GET    /api/v1/admin/reports/stats              - Report statistics
GET    /api/v1/admin/reports/entity/{type}/{id} - Reports for entity
```

---

## Report Workflow

```
User submits report
        │
        ▼
    PENDING
   (in queue)
        │
        ▼
Admin assigns to self or another admin
        │
        ▼
  INVESTIGATING
        │
   ┌────┴────┐
   ▼         ▼
RESOLVED   DISMISSED
   │         │
   ▼         ▼
Action?    No action
   │
   ▼
Execute action (suspend, ban, etc.)
   │
   ▼
If FRAUD on Org → Score -15
```

---

## Report Type Names (Turkish)

| Type | Turkish Name | Auto Priority |
|------|--------------|---------------|
| FRAUD | Dolandırıcılık | HIGH |
| INAPPROPRIATE | Uygunsuz İçerik | MEDIUM |
| SPAM | Spam | LOW |
| MISLEADING | Yanıltıcı Bilgi | MEDIUM |
| OTHER | Diğer | MEDIUM |

---

## Report Status Names (Turkish)

| Status | Turkish Name |
|--------|--------------|
| PENDING | Beklemede |
| INVESTIGATING | İnceleniyor |
| RESOLVED | Çözüldü |
| DISMISSED | Reddedildi |

---

## Priority Names (Turkish)

| Priority | Turkish Name |
|----------|--------------|
| LOW | Düşük |
| MEDIUM | Orta |
| HIGH | Yüksek |
| CRITICAL | Kritik |

---

## Action Types

When resolving a report with action:

| Action | Description | Target |
|--------|-------------|--------|
| WARN | Send warning notification | User, Organization |
| SUSPEND | Temporarily suspend | User, Organization, Campaign |
| BAN | Permanently ban | User |
| REMOVE_CONTENT | Remove/hide content | Campaign |

---

## Integration with Transparency Score

When a FRAUD report is upheld (resolved, not dismissed) against an organization:

```java
// In ReportService.resolveReport()
if (request.getResolution() == ReportStatus.RESOLVED 
    && report.getReportType() == ReportType.FRAUD
    && report.getEntityType() == ReportEntityType.ORGANIZATION) {
    
    transparencyScoreService.onReportUpheld(report.getId());
}
```

This triggers -15 points in the transparency score (from Phase 11).

---

## Duplicate Report Prevention

```java
public void createReport(CreateReportRequest request) {
    UUID reporterId = SecurityUtils.getCurrentUserId();
    
    // Check for existing active report from same user
    boolean exists = reportRepository.existsByReporterIdAndEntityTypeAndEntityIdAndStatusNot(
        reporterId,
        request.getEntityType(),
        request.getEntityId(),
        ReportStatus.DISMISSED
    );
    
    if (exists) {
        throw new BadRequestException("You already have an active report for this entity");
    }
    
    // Continue with report creation
}
```

---

## Testing Requirements

### Unit Tests
- ReportServiceTest:
  - Test duplicate prevention
  - Test auto-priority assignment
  - Test status transitions
  - Test action execution

### Integration Tests
- Full report lifecycle (create → investigate → resolve)
- Fraud report → transparency score impact

---

## Success Criteria

- [ ] All 4 enums created (ReportType, ReportStatus, ReportPriority, ReportEntityType)
- [ ] Report entity with TEXT[] array support
- [ ] Repository with all custom queries
- [ ] All 4 request DTOs with validation
- [ ] All 4 response DTOs created
- [ ] ReportMapper with entity name resolution
- [ ] ReportService with all methods
- [ ] Duplicate report prevention working
- [ ] Auto-priority assignment
- [ ] Status workflow enforced
- [ ] Action execution on resolve
- [ ] Transparency score integration for fraud
- [ ] Anonymous reporting supported
- [ ] All endpoints with proper authorization
- [ ] All unit tests pass

---

## Result File Requirement

After completing this phase, create:
**Location:** `docs/Emir/step_results/phase_14.0_result.md`

Include:
1. Summary
2. Files created
3. Report types and priorities table
4. Report workflow description
5. Action types and effects
6. Integration with transparency score
7. API endpoints table
8. Testing results
9. Issues and resolutions
10. Next steps (Phase 15.0)
11. Success criteria checklist

---

## Important Notes

1. **Do NOT use ternary operators** - Use if-else
2. **Anonymous reports** - reporter_id can be null
3. **TEXT[] array** - Use Hibernate Types ListArrayType
4. **Priority queue** - Order by priority DESC, createdAt ASC
5. **Action execution** - Careful with suspend/ban operations

---

## Dependencies

From Furkan's work:
- SecurityUtils, ApiResponse
- AuditLogService (log all admin actions)

From previous phases:
- Campaign, CampaignService (Phase 4, 5)
- Organization, OrganizationService (Phase 2, 3)
- TransparencyScoreService (Phase 11)
- NotificationService (Phase 13)

---

## Estimated Duration

2 days

---

## Next Phase

**Phase 15.0: Scheduler Module (Automated Tasks)**
