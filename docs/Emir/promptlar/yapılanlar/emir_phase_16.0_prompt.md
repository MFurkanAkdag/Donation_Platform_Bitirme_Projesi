# PHASE 16.0: EVENT SYSTEM & INTEGRATION

## Context and Background

You are working on the "Şeffaf Bağış Platformu" (Transparent Donation Platform), a graduation project for Fatih Sultan Mehmet Vakıf University.

**Project Stack:** Java 17, Spring Boot 3.x, PostgreSQL 15+, Redis, Maven

**Current Phase:** 16.0 - Event System & Integration

**Previous Phases Completed:**
- Phase 1.0-14.0: All foundation modules ✅
- Phase 15.0: Scheduler Module ✅

---

## Objective

Implement a Spring Event-driven architecture to decouple services and enable reactive processing. This allows actions in one module to trigger responses in others without tight coupling, improving maintainability and testability.

---

## What This Phase Will Solve

1. **Service Decoupling**: Remove direct service-to-service calls
2. **Async Processing**: Handle secondary actions asynchronously
3. **Event Sourcing Lite**: Track important domain events
4. **Cross-Module Communication**: Clean integration between modules
5. **Extensibility**: Easy to add new listeners without modifying publishers

---

## Spring Events Overview

Spring's event system uses:
- **ApplicationEvent**: Base class for events
- **ApplicationEventPublisher**: Publishes events
- **@EventListener**: Methods that handle events
- **@Async**: For asynchronous event handling

---

## Files to Create

### Location: `src/main/java/com/seffafbagis/api/event/`

---

### 1. Event Classes

#### Base Event
**File:** `BaseEvent.java`
```java
public abstract class BaseEvent {
    private final UUID eventId;
    private final LocalDateTime occurredAt;
    private final UUID triggeredBy; // User who triggered the event
    
    protected BaseEvent(UUID triggeredBy) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = LocalDateTime.now();
        this.triggeredBy = triggeredBy;
    }
    
    // Getters
}
```

---

#### Donation Events
**File:** `DonationCreatedEvent.java`
```java
public class DonationCreatedEvent extends BaseEvent {
    private final UUID donationId;
    private final UUID campaignId;
    private final UUID donorId; // nullable for anonymous
    private final BigDecimal amount;
    private final String donationType;
    private final boolean isAnonymous;
    
    // Constructor, getters
}
```

**File:** `DonationCompletedEvent.java`
```java
public class DonationCompletedEvent extends BaseEvent {
    private final UUID donationId;
    private final UUID campaignId;
    private final UUID organizationId;
    private final UUID donorId;
    private final BigDecimal amount;
    private final UUID transactionId;
    
    // Constructor, getters
}
```

**File:** `DonationFailedEvent.java`
```java
public class DonationFailedEvent extends BaseEvent {
    private final UUID donationId;
    private final UUID campaignId;
    private final UUID donorId;
    private final String failureReason;
    
    // Constructor, getters
}
```

**File:** `DonationRefundedEvent.java`
```java
public class DonationRefundedEvent extends BaseEvent {
    private final UUID donationId;
    private final UUID campaignId;
    private final BigDecimal refundAmount;
    private final String refundReason;
    
    // Constructor, getters
}
```

---

#### Campaign Events
**File:** `CampaignCreatedEvent.java`
```java
public class CampaignCreatedEvent extends BaseEvent {
    private final UUID campaignId;
    private final UUID organizationId;
    private final String campaignTitle;
    private final BigDecimal targetAmount;
    
    // Constructor, getters
}
```

**File:** `CampaignApprovedEvent.java`
```java
public class CampaignApprovedEvent extends BaseEvent {
    private final UUID campaignId;
    private final UUID organizationId;
    private final UUID approvedBy;
    
    // Constructor, getters
}
```

**File:** `CampaignRejectedEvent.java`
```java
public class CampaignRejectedEvent extends BaseEvent {
    private final UUID campaignId;
    private final UUID organizationId;
    private final String rejectionReason;
    
    // Constructor, getters
}
```

**File:** `CampaignCompletedEvent.java`
```java
public class CampaignCompletedEvent extends BaseEvent {
    private final UUID campaignId;
    private final UUID organizationId;
    private final BigDecimal collectedAmount;
    private final int donorCount;
    private final LocalDateTime evidenceDeadline;
    
    // Constructor, getters
}
```

**File:** `CampaignStatusChangedEvent.java`
```java
public class CampaignStatusChangedEvent extends BaseEvent {
    private final UUID campaignId;
    private final CampaignStatus previousStatus;
    private final CampaignStatus newStatus;
    
    // Constructor, getters
}
```

---

#### Evidence Events
**File:** `EvidenceUploadedEvent.java`
```java
public class EvidenceUploadedEvent extends BaseEvent {
    private final UUID evidenceId;
    private final UUID campaignId;
    private final UUID organizationId;
    private final BigDecimal amountDocumented;
    
    // Constructor, getters
}
```

**File:** `EvidenceApprovedEvent.java`
```java
public class EvidenceApprovedEvent extends BaseEvent {
    private final UUID evidenceId;
    private final UUID campaignId;
    private final UUID organizationId;
    private final boolean wasOnTime;
    
    // Constructor, getters
}
```

**File:** `EvidenceRejectedEvent.java`
```java
public class EvidenceRejectedEvent extends BaseEvent {
    private final UUID evidenceId;
    private final UUID campaignId;
    private final UUID organizationId;
    private final String rejectionReason;
    
    // Constructor, getters
}
```

---

#### Organization Events
**File:** `OrganizationVerifiedEvent.java`
```java
public class OrganizationVerifiedEvent extends BaseEvent {
    private final UUID organizationId;
    private final UUID userId;
    
    // Constructor, getters
}
```

**File:** `OrganizationRejectedEvent.java`
```java
public class OrganizationRejectedEvent extends BaseEvent {
    private final UUID organizationId;
    private final String rejectionReason;
    
    // Constructor, getters
}
```

---

#### Application Events
**File:** `ApplicationSubmittedEvent.java`
```java
public class ApplicationSubmittedEvent extends BaseEvent {
    private final UUID applicationId;
    private final UUID applicantId;
    private final String categoryName;
    
    // Constructor, getters
}
```

**File:** `ApplicationStatusChangedEvent.java`
```java
public class ApplicationStatusChangedEvent extends BaseEvent {
    private final UUID applicationId;
    private final UUID applicantId;
    private final ApplicationStatus previousStatus;
    private final ApplicationStatus newStatus;
    
    // Constructor, getters
}
```

---

#### Score Events
**File:** `TransparencyScoreChangedEvent.java`
```java
public class TransparencyScoreChangedEvent extends BaseEvent {
    private final UUID organizationId;
    private final BigDecimal previousScore;
    private final BigDecimal newScore;
    private final String changeReason;
    
    // Constructor, getters
}
```

---

### 2. Event Listeners

#### Location: `src/main/java/com/seffafbagis/api/event/listener/`

---

#### DonationEventListener.java

Handles donation-related events:

```java
@Component
@Slf4j
public class DonationEventListener {
    
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final CampaignService campaignService;
    private final AuditLogService auditLogService;
    
    @Async
    @EventListener
    public void handleDonationCompleted(DonationCompletedEvent event) {
        log.info("Handling DonationCompletedEvent for donation: {}", event.getDonationId());
        
        // 1. Update campaign statistics
        campaignService.updateCampaignStats(event.getCampaignId(), event.getAmount());
        
        // 2. Notify organization
        notificationService.notifyDonationReceived(event);
        
        // 3. Send receipt email to donor (if not anonymous)
        if (event.getDonorId() != null) {
            emailService.sendDonationReceiptEmail(event.getDonationId());
        }
        
        // 4. Notify campaign followers
        notificationService.notifyCampaignFollowers(event.getCampaignId(), 
            "Yeni bağış alındı: " + event.getAmount() + " TL");
        
        // 5. Log audit
        auditLogService.log("donation.completed", "donation", event.getDonationId());
    }
    
    @Async
    @EventListener
    public void handleDonationFailed(DonationFailedEvent event) {
        log.warn("Handling DonationFailedEvent for donation: {}", event.getDonationId());
        
        // Notify donor of failure
        if (event.getDonorId() != null) {
            notificationService.notifyDonationFailed(event);
        }
        
        // Log for monitoring
        auditLogService.log("donation.failed", "donation", event.getDonationId());
    }
    
    @Async
    @EventListener
    public void handleDonationRefunded(DonationRefundedEvent event) {
        log.info("Handling DonationRefundedEvent for donation: {}", event.getDonationId());
        
        // Update campaign statistics (decrease)
        campaignService.decreaseCampaignStats(event.getCampaignId(), event.getRefundAmount());
        
        // Notify donor
        notificationService.notifyDonationRefunded(event);
        
        // Log audit
        auditLogService.log("donation.refunded", "donation", event.getDonationId());
    }
}
```

---

#### CampaignEventListener.java

```java
@Component
@Slf4j
public class CampaignEventListener {
    
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final TransparencyScoreService transparencyScoreService;
    private final AuditLogService auditLogService;
    
    @Async
    @EventListener
    public void handleCampaignApproved(CampaignApprovedEvent event) {
        log.info("Handling CampaignApprovedEvent for campaign: {}", event.getCampaignId());
        
        // Notify organization
        notificationService.notifyCampaignApproved(event.getCampaignId());
        
        // Send email
        emailService.sendCampaignApprovalEmail(event.getOrganizationId(), 
            event.getCampaignId(), true, null);
        
        // Log
        auditLogService.log("campaign.approved", "campaign", event.getCampaignId());
    }
    
    @Async
    @EventListener
    public void handleCampaignRejected(CampaignRejectedEvent event) {
        log.info("Handling CampaignRejectedEvent for campaign: {}", event.getCampaignId());
        
        notificationService.notifyCampaignRejected(event.getCampaignId(), 
            event.getRejectionReason());
        
        emailService.sendCampaignApprovalEmail(event.getOrganizationId(), 
            event.getCampaignId(), false, event.getRejectionReason());
    }
    
    @Async
    @EventListener
    public void handleCampaignCompleted(CampaignCompletedEvent event) {
        log.info("Handling CampaignCompletedEvent for campaign: {}", event.getCampaignId());
        
        // Update transparency score
        transparencyScoreService.onCampaignCompleted(event.getCampaignId());
        
        // Notify organization about evidence deadline
        notificationService.notifyEvidenceRequired(event.getCampaignId(), 
            calculateDaysRemaining(event.getEvidenceDeadline()));
        
        // Notify all donors and followers
        notificationService.notifyCampaignCompleted(event.getCampaignId());
        
        // Log
        auditLogService.log("campaign.completed", "campaign", event.getCampaignId());
    }
    
    private int calculateDaysRemaining(LocalDateTime deadline) {
        return (int) ChronoUnit.DAYS.between(LocalDateTime.now(), deadline);
    }
}
```

---

#### EvidenceEventListener.java

```java
@Component
@Slf4j
public class EvidenceEventListener {
    
    private final TransparencyScoreService transparencyScoreService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;
    
    @Async
    @EventListener
    public void handleEvidenceApproved(EvidenceApprovedEvent event) {
        log.info("Handling EvidenceApprovedEvent for evidence: {}", event.getEvidenceId());
        
        // Update transparency score
        transparencyScoreService.onEvidenceApproved(event.getEvidenceId(), event.isWasOnTime());
        
        // Notify organization
        notificationService.notifyEvidenceApproved(event.getEvidenceId());
        
        // Log
        auditLogService.log("evidence.approved", "evidence", event.getEvidenceId());
    }
    
    @Async
    @EventListener
    public void handleEvidenceRejected(EvidenceRejectedEvent event) {
        log.info("Handling EvidenceRejectedEvent for evidence: {}", event.getEvidenceId());
        
        // Update transparency score (negative)
        transparencyScoreService.onEvidenceRejected(event.getEvidenceId());
        
        // Notify organization
        notificationService.notifyEvidenceRejected(event.getEvidenceId(), 
            event.getRejectionReason());
        
        // Log
        auditLogService.log("evidence.rejected", "evidence", event.getEvidenceId());
    }
}
```

---

#### OrganizationEventListener.java

```java
@Component
@Slf4j
public class OrganizationEventListener {
    
    private final TransparencyScoreService transparencyScoreService;
    private final NotificationService notificationService;
    private final EmailService emailService;
    
    @Async
    @EventListener
    public void handleOrganizationVerified(OrganizationVerifiedEvent event) {
        log.info("Handling OrganizationVerifiedEvent for org: {}", event.getOrganizationId());
        
        // Initialize transparency score
        transparencyScoreService.initializeScore(event.getOrganizationId());
        
        // Notify and send email
        notificationService.notifySystem(event.getUserId(), 
            "Kuruluş Onaylandı", 
            "Tebrikler! Kuruluşunuz onaylandı ve artık kampanya oluşturabilirsiniz.");
        
        emailService.sendVerificationSuccessEmail(event.getOrganizationId());
    }
    
    @Async
    @EventListener
    public void handleOrganizationRejected(OrganizationRejectedEvent event) {
        log.info("Handling OrganizationRejectedEvent for org: {}", event.getOrganizationId());
        
        // Notify with rejection reason
        notificationService.notifySystem(event.getTriggeredBy(), 
            "Kuruluş Başvurusu Reddedildi", 
            "Başvurunuz reddedildi. Sebep: " + event.getRejectionReason());
    }
}
```

---

#### ApplicationEventListener.java

```java
@Component
@Slf4j
public class ApplicationEventListener {
    
    private final NotificationService notificationService;
    
    @Async
    @EventListener
    public void handleApplicationStatusChanged(ApplicationStatusChangedEvent event) {
        log.info("Handling ApplicationStatusChangedEvent for app: {}", event.getApplicationId());
        
        notificationService.notifyApplicationUpdate(event.getApplicationId());
    }
}
```

---

#### TransparencyScoreEventListener.java

```java
@Component
@Slf4j
public class TransparencyScoreEventListener {
    
    private final NotificationService notificationService;
    private final CampaignService campaignService;
    
    @Async
    @EventListener
    public void handleScoreChanged(TransparencyScoreChangedEvent event) {
        log.info("Handling TransparencyScoreChangedEvent for org: {}", event.getOrganizationId());
        
        // Notify organization
        notificationService.notifyScoreChange(event.getOrganizationId(), 
            event.getPreviousScore(), event.getNewScore());
        
        // Check if score dropped below threshold
        BigDecimal threshold = new BigDecimal("40");
        if (event.getNewScore().compareTo(threshold) < 0 
            && event.getPreviousScore().compareTo(threshold) >= 0) {
            
            // Score dropped below campaign creation threshold
            log.warn("Organization {} score dropped below threshold", event.getOrganizationId());
            
            // Could pause active campaigns or restrict new campaign creation
            // This is handled by campaign creation validation, but we can notify
            notificationService.notifySystem(event.getTriggeredBy(),
                "Şeffaflık Skoru Uyarısı",
                "Şeffaflık skorunuz " + threshold + " altına düştü. " +
                "Yeni kampanya oluşturma yetkiniz geçici olarak askıya alındı.");
        }
    }
}
```

---

### 3. Event Publishing Integration

Update existing services to publish events:

#### In DonationService (Phase 7)
```java
@Service
public class DonationService {
    
    private final ApplicationEventPublisher eventPublisher;
    
    public void completeDonation(UUID donationId) {
        // ... existing logic ...
        
        // Publish event
        DonationCompletedEvent event = new DonationCompletedEvent(
            SecurityUtils.getCurrentUserId(),
            donation.getId(),
            donation.getCampaign().getId(),
            donation.getCampaign().getOrganization().getId(),
            donation.getDonor() != null ? donation.getDonor().getId() : null,
            donation.getAmount(),
            transaction.getId()
        );
        eventPublisher.publishEvent(event);
    }
}
```

#### In CampaignService (Phase 5)
```java
@Service
public class CampaignService {
    
    private final ApplicationEventPublisher eventPublisher;
    
    public void approveCampaign(UUID campaignId, UUID approvedBy) {
        // ... existing logic ...
        
        // Publish event
        CampaignApprovedEvent event = new CampaignApprovedEvent(
            approvedBy,
            campaign.getId(),
            campaign.getOrganization().getId(),
            approvedBy
        );
        eventPublisher.publishEvent(event);
    }
}
```

---

### 4. Async Configuration

**File:** `src/main/java/com/seffafbagis/api/config/AsyncConfig.java`

```java
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("EventAsync-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
    
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, objects) -> {
            log.error("Async exception in method {}: {}", method.getName(), throwable.getMessage());
        };
    }
}
```

---

## Event Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        SERVICE LAYER                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  DonationService ──publish──▶ DonationCompletedEvent           │
│                                      │                          │
│                                      ▼                          │
│                           ┌─────────────────────┐               │
│                           │  EVENT DISPATCHER   │               │
│                           └─────────────────────┘               │
│                                      │                          │
│                    ┌─────────────────┼─────────────────┐        │
│                    ▼                 ▼                 ▼        │
│           ┌───────────────┐ ┌───────────────┐ ┌───────────────┐ │
│           │ Notification  │ │   Campaign    │ │    Audit      │ │
│           │   Service     │ │   Service     │ │    Service    │ │
│           └───────────────┘ └───────────────┘ └───────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Events Summary Table

| Event | Publisher | Listeners | Actions Triggered |
|-------|-----------|-----------|-------------------|
| DonationCompletedEvent | DonationService | DonationEventListener | Update stats, notify, email, audit |
| DonationFailedEvent | PaymentService | DonationEventListener | Notify donor, audit |
| DonationRefundedEvent | DonationService | DonationEventListener | Update stats, notify, audit |
| CampaignApprovedEvent | CampaignService | CampaignEventListener | Notify, email, audit |
| CampaignRejectedEvent | CampaignService | CampaignEventListener | Notify, email |
| CampaignCompletedEvent | CampaignService | CampaignEventListener | Score update, notify, audit |
| EvidenceApprovedEvent | EvidenceService | EvidenceEventListener | Score update, notify, audit |
| EvidenceRejectedEvent | EvidenceService | EvidenceEventListener | Score update, notify, audit |
| OrganizationVerifiedEvent | OrganizationService | OrganizationEventListener | Init score, notify, email |
| TransparencyScoreChangedEvent | TransparencyScoreService | TransparencyScoreEventListener | Notify, check threshold |
| ApplicationStatusChangedEvent | ApplicationService | ApplicationEventListener | Notify applicant |

---

## Testing Requirements

### Unit Tests
- Test event creation with all fields
- Test listener method invocation
- Mock event publisher in service tests

### Integration Tests
- Full event flow: action → event → listener → side effects
- Test async execution

---

## Success Criteria

- [ ] BaseEvent class with common fields
- [ ] All 16 event classes created
- [ ] All 6 event listener classes created
- [ ] AsyncConfig for async event handling
- [ ] DonationService publishes events
- [ ] CampaignService publishes events
- [ ] EvidenceService publishes events
- [ ] OrganizationService publishes events
- [ ] TransparencyScoreService publishes events
- [ ] ApplicationService publishes events
- [ ] All listeners handle events correctly
- [ ] Error handling in async methods
- [ ] Proper logging in all listeners
- [ ] All unit tests pass

---

## Result File Requirement

After completing this phase, create:
**Location:** `docs/Emir/step_results/phase_16.0_result.md`

Include:
1. Summary
2. Files created
3. Events summary table
4. Event flow diagram
5. Integration changes to existing services
6. Async configuration details
7. Testing results
8. Issues and resolutions
9. Next steps (Phase 17.0)
10. Success criteria checklist

---

## Important Notes

1. **Do NOT use ternary operators** - Use if-else
2. **@Async annotation** - Ensures non-blocking execution
3. **Error handling** - Listeners should not throw exceptions that break flow
4. **Logging** - Log all event handling for debugging
5. **Update existing services** - Add event publishing

---

## Dependencies

From Furkan's work:
- AuditLogService
- SecurityUtils

From previous phases:
- All services that will publish events
- NotificationService, EmailService (Phase 13)
- TransparencyScoreService (Phase 11)

---

## Estimated Duration

2 days

---

## Next Phase

**Phase 17.0: Integration Testing & Final Polish**
