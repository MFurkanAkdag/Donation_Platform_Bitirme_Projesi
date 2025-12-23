# PHASE 15.0: SCHEDULER MODULE (AUTOMATED TASKS)

## Context and Background

You are working on the "Şeffaf Bağış Platformu" (Transparent Donation Platform), a graduation project for Fatih Sultan Mehmet Vakıf University.

**Project Stack:** Java 17, Spring Boot 3.x, PostgreSQL 15+, Redis, Maven

**Current Phase:** 15.0 - Scheduler Module

**Previous Phases Completed:**
- Phase 1.0-13.0: All foundation modules ✅
- Phase 14.0: Report Module ✅

---

## Objective

Implement scheduled tasks (cron jobs) that automate recurring operations like processing recurring donations, expiring bank transfers, sending evidence reminders, and recalculating transparency scores.

---

## What This Phase Will Solve

1. **Recurring Donations**: Auto-process monthly/weekly donations
2. **Bank Transfer Expiry**: Expire unused transfer references
3. **Evidence Reminders**: Notify organizations about deadlines
4. **Transparency Score Updates**: Penalize missed deadlines
5. **Campaign Auto-Complete**: Complete campaigns past end date
6. **Cleanup Tasks**: Archive old data, clean expired tokens

---

## Spring Scheduling Configuration

Enable scheduling in main application or config:

```java
@Configuration
@EnableScheduling
public class SchedulerConfig {
    // Optional: Configure task executor for parallel execution
}
```

In `application.yml`:
```yaml
spring:
  task:
    scheduling:
      pool:
        size: 4
```

---

## Files to Create

### Location: `src/main/java/com/seffafbagis/api/scheduler/`

---

### 1. RecurringDonationScheduler.java

**Purpose:** Process recurring donations on their scheduled dates

**Cron:** Daily at 09:00 AM Turkey time

```java
@Scheduled(cron = "0 0 9 * * ?", zone = "Europe/Istanbul")
```

**Logic:**
```
1. Find all active recurring donations where next_payment_date <= today
2. For each recurring donation:
   a. Check if campaign is still active (if campaign-specific)
   b. Attempt payment using saved card token
   c. If successful:
      - Create donation record
      - Update last_payment_date
      - Calculate and set next_payment_date
      - Increment payment_count
      - Add to total_donated
      - Reset failure_count to 0
   d. If failed:
      - Increment failure_count
      - If failure_count >= 3: pause recurring donation
      - Store last_error_message
      - Notify user of payment failure
3. Log all processed donations
```

**Methods:**
- processRecurringDonations() - main scheduled method
- processSingleRecurringDonation(RecurringDonation rd) - process one
- calculateNextPaymentDate(RecurringDonation rd) - based on frequency

---

### 2. BankTransferExpiryScheduler.java

**Purpose:** Expire bank transfer references that weren't matched

**Cron:** Every hour

```java
@Scheduled(cron = "0 0 * * * ?", zone = "Europe/Istanbul")
```

**Logic:**
```
1. Find all pending bank transfer references where expires_at < now
2. For each expired reference:
   a. Update status to 'expired'
   b. Notify donor (if donor_id exists) that reference expired
3. Log count of expired references
```

**Methods:**
- expireBankTransfers() - main scheduled method

---

### 3. EvidenceReminderScheduler.java

**Purpose:** Send reminders for evidence upload deadlines

**Cron:** Daily at 10:00 AM

```java
@Scheduled(cron = "0 0 10 * * ?", zone = "Europe/Istanbul")
```

**Logic:**
```
1. Find all COMPLETED campaigns where:
   - No evidence uploaded yet OR
   - Has pending evidence (not enough to cover collected amount)
   
2. Calculate days until deadline for each
   
3. Send reminders at these intervals:
   - 7 days before deadline: First reminder
   - 3 days before deadline: Urgent reminder
   - 1 day before deadline: Final warning
   - On deadline day: Last chance
   
4. For campaigns past deadline with no/insufficient evidence:
   a. Mark campaign as requiring evidence review
   b. Apply transparency score penalty (-10)
   c. Notify organization of penalty
```

**Methods:**
- sendEvidenceReminders() - main scheduled method
- checkCampaignEvidence(Campaign campaign) - check if enough evidence
- calculateDeadline(Campaign campaign) - completedAt + evidence_deadline_days
- sendReminder(Campaign campaign, int daysRemaining) - send notification + email

---

### 4. TransparencyScoreScheduler.java

**Purpose:** Recalculate transparency scores and apply time-based adjustments

**Cron:** Daily at 02:00 AM (low traffic time)

```java
@Scheduled(cron = "0 0 2 * * ?", zone = "Europe/Istanbul")
```

**Logic:**
```
1. Apply monthly consistency bonus:
   - Find organizations with 100% on-time evidence submissions in past month
   - Award +1 point for consistency
   
2. Check for campaigns with missed deadlines:
   - Find COMPLETED campaigns past evidence deadline
   - Where total approved evidence amount < collected amount
   - Apply -10 penalty if not already applied
   
3. Optional: Full score recalculation
   - Can be triggered manually for data integrity
   
4. Log all score changes
```

**Methods:**
- updateTransparencyScores() - main scheduled method
- applyConsistencyBonus() - reward consistent organizations
- penalizeMissedDeadlines() - apply deadline penalties
- fullRecalculation(UUID organizationId) - recalculate from scratch

---

### 5. CampaignStatusScheduler.java

**Purpose:** Auto-complete campaigns and handle status transitions

**Cron:** Every 6 hours

```java
@Scheduled(cron = "0 0 */6 * * ?", zone = "Europe/Istanbul")
```

**Logic:**
```
1. Auto-complete successful campaigns:
   - Find ACTIVE campaigns where collected_amount >= target_amount
   - Update status to COMPLETED
   - Set completed_at
   - Notify organization
   - Notify followers

2. Handle expired campaigns:
   - Find ACTIVE campaigns where end_date < now
   - If collected_amount >= 80% of target: COMPLETED
   - Otherwise: Extend end_date by 7 days (max 2 extensions)
   - If already extended twice: COMPLETED (partial)
   - Notify organization

3. Log all status changes
```

**Methods:**
- updateCampaignStatuses() - main scheduled method
- completeCampaign(Campaign campaign) - handle completion
- handleExpiredCampaign(Campaign campaign) - extend or complete

---

### 6. CleanupScheduler.java

**Purpose:** Clean up old data and maintain database hygiene

**Cron:** Weekly on Sunday at 03:00 AM

```java
@Scheduled(cron = "0 0 3 * * SUN", zone = "Europe/Istanbul")
```

**Logic:**
```
1. Delete expired refresh tokens
   - Where expires_at < now - 7 days
   
2. Delete used/expired password reset tokens
   - Where used_at IS NOT NULL OR expires_at < now
   
3. Delete old notifications
   - Where is_read = true AND created_at < now - 90 days
   
4. Archive old audit logs (optional)
   - Move to archive table if older than 1 year
   
5. Clean failed email logs
   - Where status = 'failed' AND retry_count >= 5 AND sent_at < now - 30 days

6. Log cleanup statistics
```

**Methods:**
- performCleanup() - main scheduled method
- cleanExpiredTokens()
- cleanOldNotifications()
- cleanFailedEmails()

---

### 7. EmailRetryScheduler.java

**Purpose:** Retry failed email sends

**Cron:** Every 30 minutes

```java
@Scheduled(cron = "0 */30 * * * ?", zone = "Europe/Istanbul")
```

**Logic:**
```
1. Find failed emails where retry_count < 5
2. For each:
   a. Attempt to resend
   b. If successful: update status to 'sent'
   c. If failed: increment retry_count
3. Log retry results
```

**Methods:**
- retryFailedEmails() - main scheduled method

---

## Scheduler Summary Table

| Scheduler | Cron | Time | Purpose |
|-----------|------|------|---------|
| RecurringDonationScheduler | `0 0 9 * * ?` | Daily 09:00 | Process recurring donations |
| BankTransferExpiryScheduler | `0 0 * * * ?` | Hourly | Expire old bank transfers |
| EvidenceReminderScheduler | `0 0 10 * * ?` | Daily 10:00 | Send evidence reminders |
| TransparencyScoreScheduler | `0 0 2 * * ?` | Daily 02:00 | Update transparency scores |
| CampaignStatusScheduler | `0 0 */6 * * ?` | Every 6 hours | Update campaign statuses |
| CleanupScheduler | `0 0 3 * * SUN` | Sunday 03:00 | Clean old data |
| EmailRetryScheduler | `0 */30 * * * ?` | Every 30 min | Retry failed emails |

---

## Error Handling

Each scheduler should handle errors gracefully:

```java
@Scheduled(cron = "0 0 9 * * ?", zone = "Europe/Istanbul")
public void processRecurringDonations() {
    log.info("Starting recurring donation processing");
    
    try {
        List<RecurringDonation> donations = findDueRecurringDonations();
        int successCount = 0;
        int failureCount = 0;
        
        for (RecurringDonation rd : donations) {
            try {
                processSingleRecurringDonation(rd);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to process recurring donation {}: {}", 
                    rd.getId(), e.getMessage());
                failureCount++;
                // Continue with next donation
            }
        }
        
        log.info("Recurring donation processing completed. Success: {}, Failed: {}", 
            successCount, failureCount);
            
    } catch (Exception e) {
        log.error("Critical error in recurring donation scheduler: {}", e.getMessage());
        // Send alert to admin
    }
}
```

---

## Logging Requirements

Each scheduler should log:
- Start time
- Number of items processed
- Success/failure counts
- End time and duration
- Any errors with stack traces

Use SLF4J:
```java
private static final Logger log = LoggerFactory.getLogger(RecurringDonationScheduler.class);
```

---

## Concurrency Considerations

1. **Prevent overlapping runs:**
```java
@Scheduled(cron = "...")
@SchedulerLock(name = "recurringDonationScheduler", 
               lockAtMostFor = "PT1H", 
               lockAtLeastFor = "PT5M")
public void processRecurringDonations() {
    // ...
}
```

2. **Add ShedLock dependency** (optional but recommended):
```xml
<dependency>
    <groupId>net.javacrumbs.shedlock</groupId>
    <artifactId>shedlock-spring</artifactId>
    <version>5.10.0</version>
</dependency>
<dependency>
    <groupId>net.javacrumbs.shedlock</groupId>
    <artifactId>shedlock-provider-jdbc-template</artifactId>
    <version>5.10.0</version>
</dependency>
```

---

## Testing Schedulers

Unit tests should test the logic, not the scheduling:

```java
@ExtendWith(MockitoExtension.class)
class RecurringDonationSchedulerTest {
    
    @Mock
    private RecurringDonationRepository recurringDonationRepository;
    
    @Mock
    private PaymentService paymentService;
    
    @InjectMocks
    private RecurringDonationScheduler scheduler;
    
    @Test
    void shouldProcessDueRecurringDonations() {
        // Given
        RecurringDonation rd = createTestRecurringDonation();
        when(recurringDonationRepository.findByStatusAndNextPaymentDateLessThanEqual(
            any(), any())).thenReturn(List.of(rd));
        when(paymentService.processRecurringPayment(any())).thenReturn(true);
        
        // When
        scheduler.processRecurringDonations();
        
        // Then
        verify(paymentService).processRecurringPayment(rd);
        verify(recurringDonationRepository).save(any());
    }
    
    @Test
    void shouldPauseAfterThreeFailures() {
        // Given
        RecurringDonation rd = createTestRecurringDonation();
        rd.setFailureCount(2); // Already failed twice
        when(recurringDonationRepository.findByStatusAndNextPaymentDateLessThanEqual(
            any(), any())).thenReturn(List.of(rd));
        when(paymentService.processRecurringPayment(any())).thenReturn(false);
        
        // When
        scheduler.processRecurringDonations();
        
        // Then
        if (rd.getFailureCount() >= 3) {
            assertEquals("paused", rd.getStatus());
        }
    }
}
```

---

## Configuration Options

Add to `application.yml`:

```yaml
scheduler:
  recurring-donation:
    enabled: true
    max-retries: 3
  bank-transfer:
    enabled: true
    expiry-hours: 168  # 7 days
  evidence-reminder:
    enabled: true
    reminder-days: [7, 3, 1, 0]
  cleanup:
    enabled: true
    notification-retention-days: 90
    audit-log-retention-days: 365
```

Create configuration class:
```java
@Configuration
@ConfigurationProperties(prefix = "scheduler")
public class SchedulerProperties {
    // Properties for each scheduler
}
```

---

## Testing Requirements

### Unit Tests
- RecurringDonationSchedulerTest:
  - Test successful payment processing
  - Test failure handling and pause logic
  - Test next payment date calculation

- EvidenceReminderSchedulerTest:
  - Test reminder timing (7, 3, 1, 0 days)
  - Test deadline penalty application

- CampaignStatusSchedulerTest:
  - Test auto-completion at 100%
  - Test expiry handling and extension

### Integration Tests
- Full recurring donation cycle
- Evidence deadline → penalty flow

---

## Success Criteria

- [ ] All 7 scheduler classes created
- [ ] Correct cron expressions with Turkey timezone
- [ ] RecurringDonationScheduler processes due payments
- [ ] BankTransferExpiryScheduler expires old references
- [ ] EvidenceReminderScheduler sends reminders at correct intervals
- [ ] TransparencyScoreScheduler applies penalties and bonuses
- [ ] CampaignStatusScheduler handles completion and expiry
- [ ] CleanupScheduler removes old data
- [ ] EmailRetryScheduler retries failed emails
- [ ] Error handling prevents cascade failures
- [ ] Proper logging in all schedulers
- [ ] Configuration properties created
- [ ] All unit tests pass

---

## Result File Requirement

After completing this phase, create:
**Location:** `docs/Emir/step_results/phase_15.0_result.md`

Include:
1. Summary
2. Files created
3. Scheduler summary table with cron expressions
4. Each scheduler's logic explanation
5. Configuration options
6. Error handling approach
7. Testing results
8. Issues and resolutions
9. Next steps (Phase 16.0)
10. Success criteria checklist

---

## Important Notes

1. **Do NOT use ternary operators** - Use if-else
2. **Timezone** - Always use Europe/Istanbul
3. **Error isolation** - One failure shouldn't stop others
4. **Logging** - Essential for debugging scheduled tasks
5. **Idempotency** - Schedulers should be safe to re-run

---

## Dependencies

From Furkan's work:
- SecurityUtils (for audit logging)
- Token repositories (for cleanup)

From previous phases:
- RecurringDonationService, BankTransferService (Phase 8)
- PaymentService, IyzicoService (Phase 9)
- EvidenceService (Phase 10)
- TransparencyScoreService (Phase 11)
- NotificationService, EmailService (Phase 13)
- CampaignService (Phase 5)

---

## Estimated Duration

2 days

---

## Next Phase

**Phase 16.0: Event System & Integration**
