# Phase 10.0 Result: Evidence Module

## Summary

Phase 10.0 implements the Evidence Module for the Şeffaf Bağış Platformu. This module enables organizations to upload spending proofs after campaigns complete, supporting the platform's core transparency mechanism. The implementation includes enums, entities, repositories, DTOs, services, controllers, and comprehensive unit tests.

---

## Files Created/Modified

### Enums
| File | Description |
|------|-------------|
| `EvidenceType.java` | Values: INVOICE, RECEIPT, PHOTO, VIDEO, DELIVERY_REPORT, OTHER |
| `EvidenceStatus.java` | Values: PENDING, APPROVED, REJECTED |

### Entities
| File | Description |
|------|-------------|
| `Evidence.java` | Main evidence entity with campaign/user relationships |
| `EvidenceDocument.java` | Supporting documents for evidence |

### Repositories
| File | Description |
|------|-------------|
| `EvidenceRepository.java` | Custom queries for evidence filtering and aggregation |
| `EvidenceDocumentRepository.java` | Document management queries |

### DTOs
| File | Description |
|------|-------------|
| `CreateEvidenceRequest.java` | Evidence creation with validation |
| `UpdateEvidenceRequest.java` | Evidence update with optional fields |
| `ReviewEvidenceRequest.java` | Admin approve/reject request |
| `CreateEvidenceDocumentRequest.java` | Document upload request |
| `EvidenceResponse.java` | Basic evidence response |
| `EvidenceDetailResponse.java` | Extended response with documents |
| `EvidenceListResponse.java` | List with summary counts |
| `EvidenceDocumentResponse.java` | Document response |
| `CampaignEvidenceSummaryResponse.java` | Campaign evidence summary with deadline info |

### Services
| File | Description |
|------|-------------|
| `EvidenceService.java` | Core business logic for evidence management |
| `EvidenceDocumentService.java` | Document management operations |

### Mapper
| File | Description |
|------|-------------|
| `EvidenceMapper.java` | MapStruct mapper for entity/DTO conversion |

### Controller
| File | Description |
|------|-------------|
| `EvidenceController.java` | REST API endpoints |

### Tests
| File | Description |
|------|-------------|
| `EvidenceServiceTest.java` | Unit tests for evidence business logic |

---

## API Endpoints

### Public Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/evidences/campaign/{campaignId}` | Get approved evidences (public) |
| GET | `/api/v1/evidences/campaign/{campaignId}/summary` | Get evidence summary |

### Organization Endpoints (FOUNDATION role)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/evidences` | Upload evidence |
| GET | `/api/v1/evidences/my/{campaignId}` | My evidences for campaign |
| GET | `/api/v1/evidences/{id}` | Evidence detail |
| PUT | `/api/v1/evidences/{id}` | Update evidence |
| DELETE | `/api/v1/evidences/{id}` | Delete evidence |
| POST | `/api/v1/evidences/{id}/documents` | Add document |
| DELETE | `/api/v1/evidences/{id}/documents/{docId}` | Remove document |

### Admin Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/admin/evidences/pending` | Pending for review |
| POST | `/api/v1/admin/evidences/{id}/review` | Approve or reject |
| GET | `/api/v1/admin/evidences` | All evidences with status filter |

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
Score +5   Score -5, Can resubmit
```

---

## Deadline Calculation Logic

```java
// From EvidenceMapper and EvidenceService
LocalDateTime deadline = campaign.getCompletedAt()
    .plusDays(campaign.getEvidenceDeadlineDays() != null 
        ? campaign.getEvidenceDeadlineDays() : 15);

long daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), deadline);
boolean isOverdue = LocalDateTime.now().isAfter(deadline);
```

---

## Testing Results

### Unit Tests (EvidenceServiceTest)
| Test | Status |
|------|--------|
| `uploadEvidence_Success` | ✅ Pass |
| `uploadEvidence_Fail_NotCompleted` | ✅ Pass |
| `uploadEvidence_Fail_NotOwner` | ✅ Pass |
| `updateEvidence_Fail_ApprovedEvidence` | ✅ Pass |
| `isDeadlineMissed_True_WhenPastDeadline` | ✅ Pass |
| `isDeadlineMissed_False_WhenBeforeDeadline` | ✅ Pass |

---

## Issues and Resolutions

| Issue | Resolution |
|-------|------------|
| Document controller endpoints were stubs | Implemented `addDocumentToEvidence` and `removeDocumentFromEvidence` methods in EvidenceService |
| Missing unit tests for deadline and approved evidence | Added tests for deadline calculation and approved evidence update failure |

---

## Success Criteria Checklist

- [x] EvidenceType and EvidenceStatus enums created
- [x] Evidence and EvidenceDocument entities created
- [x] Both repositories with custom queries
- [x] All request DTOs with validation
- [x] All response DTOs created
- [x] EvidenceMapper handles all conversions
- [x] EvidenceService with all methods
- [x] Only campaign owner can upload evidence
- [x] Only COMPLETED campaigns accept evidence
- [x] Cannot modify approved evidence
- [x] Admin review flow works
- [x] Deadline calculation correct
- [x] All endpoints with proper authorization
- [x] All unit tests pass

---

## Next Steps

**Phase 11.0: Transparency Score Module**
- Implement transparency score calculation
- Hook evidence approval/rejection events
- Calculate organization scores based on evidence status
