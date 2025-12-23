# PHASE 13.0: NOTIFICATION MODULE

## Context and Background

You are working on the "Şeffaf Bağış Platformu" (Transparent Donation Platform), a graduation project for Fatih Sultan Mehmet Vakıf University.

**Project Stack:** Java 17, Spring Boot 3.x, PostgreSQL 15+, Redis, Maven

**Current Phase:** 13.0 - Notification Module

**Previous Phases Completed:**
- Phase 1.0-9.0: Foundation, Donation, Payment modules ✅
- Phase 10.0-12.0: Evidence, Transparency, Application modules ✅

---

## Objective

Implement a comprehensive notification system that keeps users informed about donations, campaign updates, evidence status, and system events through in-app notifications and email.

---

## What This Phase Will Solve

1. **In-App Notifications**: Real-time notifications within the platform
2. **Email Notifications**: Transactional emails for important events
3. **User Preferences**: Respect user notification settings
4. **Read/Unread Tracking**: Track notification status
5. **Notification Types**: Different types for different events

---

## Database Schema Reference

### notifications table
```sql
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type notification_type NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    data JSONB,                               -- Extra data (campaign_id, donation_id, etc.)
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
```

### email_logs table
```sql
CREATE TABLE email_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    email_to VARCHAR(255) NOT NULL,
    email_type VARCHAR(100) NOT NULL,
    provider VARCHAR(50),
    template_name VARCHAR(100),
    retry_count INTEGER DEFAULT 0,
    subject VARCHAR(255) NOT NULL,
    status VARCHAR(50) DEFAULT 'sent',        -- 'sent', 'failed', 'bounced'
    provider_message_id VARCHAR(255),
    error_message TEXT,
    sent_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
```

### notification_type enum
```sql
CREATE TYPE notification_type AS ENUM (
    'donation_received', 
    'campaign_update', 
    'evidence_required', 
    'score_change', 
    'system'
);
```

---

## Files to Create

### 1. Enum
**Location:** `src/main/java/com/seffafbagis/api/enums/NotificationType.java`

Values:
- DONATION_RECEIVED - New donation received
- DONATION_COMPLETED - Donation completed successfully
- CAMPAIGN_UPDATE - Campaign has new update
- CAMPAIGN_COMPLETED - Campaign reached goal
- CAMPAIGN_APPROVED - Campaign approved by admin
- CAMPAIGN_REJECTED - Campaign rejected by admin
- EVIDENCE_REQUIRED - Evidence upload deadline approaching
- EVIDENCE_APPROVED - Evidence approved
- EVIDENCE_REJECTED - Evidence rejected
- SCORE_CHANGE - Transparency score changed
- APPLICATION_UPDATE - Application status changed
- SYSTEM - System announcements

---

### 2. Entities
**Location:** `src/main/java/com/seffafbagis/api/entity/notification/`

#### Notification.java
- Extend BaseEntity
- ManyToOne: User

Fields: type, title, message, data (Map<String, Object> with @Type), isRead, readAt, createdAt

Use Hibernate Types for JSONB:
```java
@Type(JsonType.class)
@Column(columnDefinition = "jsonb")
private Map<String, Object> data;
```

#### EmailLog.java
- Extend BaseEntity
- ManyToOne: User (nullable for system emails)

Fields: emailTo, emailType, provider, templateName, retryCount, subject, status, providerMessageId, errorMessage, sentAt

---

### 3. Repositories
**Location:** `src/main/java/com/seffafbagis/api/repository/`

#### NotificationRepository.java
Key methods:
- findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable)
- findByUserIdAndIsReadFalseOrderByCreatedAtDesc(UUID userId)
- findByUserIdAndType(UUID userId, NotificationType type, Pageable pageable)
- countByUserIdAndIsReadFalse(UUID userId)
- markAsRead(UUID notificationId) - @Modifying @Query
- markAllAsReadByUserId(UUID userId) - @Modifying @Query
- deleteByUserIdAndCreatedAtBefore(UUID userId, LocalDateTime date) - cleanup old

#### EmailLogRepository.java
- findByUserId(UUID userId, Pageable pageable)
- findByEmailType(String emailType, Pageable pageable)
- findByStatus(String status, Pageable pageable)
- findByStatusAndRetryCountLessThan(String status, int maxRetry)
- countByEmailTypeAndSentAtBetween(String type, LocalDateTime start, LocalDateTime end)

---

### 4. DTOs - Response
**Location:** `src/main/java/com/seffafbagis/api/dto/response/notification/`

#### NotificationResponse.java
Fields: id, type, typeName (Turkish), title, message, data, isRead, createdAt, timeAgo (relative time string)

#### NotificationListResponse.java
List with: unreadCount, notifications

#### NotificationCountResponse.java
Fields: total, unread

#### EmailLogResponse.java (Admin only)
Fields: id, emailTo, emailType, subject, status, sentAt, errorMessage

---

### 5. Services
**Location:** `src/main/java/com/seffafbagis/api/service/notification/`

#### NotificationService.java

**User Methods:**
- getMyNotifications(Pageable pageable) - paginated list
- getUnreadNotifications() - all unread
- getUnreadCount() - count only
- markAsRead(UUID notificationId)
- markAllAsRead()
- deleteNotification(UUID notificationId)
- deleteOldNotifications(int daysOld) - cleanup

**Internal Methods (called by other services):**
- createNotification(UUID userId, NotificationType type, String title, String message, Map<String, Object> data)
- notifyDonationReceived(Donation donation)
- notifyDonationCompleted(Donation donation)
- notifyCampaignUpdate(CampaignUpdate update)
- notifyCampaignCompleted(Campaign campaign)
- notifyCampaignApproved(Campaign campaign)
- notifyCampaignRejected(Campaign campaign, String reason)
- notifyEvidenceRequired(Campaign campaign, int daysRemaining)
- notifyEvidenceApproved(Evidence evidence)
- notifyEvidenceRejected(Evidence evidence, String reason)
- notifyScoreChange(UUID organizationId, BigDecimal oldScore, BigDecimal newScore)
- notifyApplicationUpdate(Application application)
- notifySystem(UUID userId, String title, String message)
- notifyAllUsers(String title, String message) - broadcast

**Helper Methods:**
- checkUserPreferences(UUID userId, NotificationType type) - respect preferences
- getTypeNameInTurkish(NotificationType type)
- calculateTimeAgo(LocalDateTime createdAt)

#### EmailService.java

**Methods:**
- sendEmail(String to, String subject, String templateName, Map<String, Object> variables)
- sendWelcomeEmail(User user)
- sendDonationReceiptEmail(Donation donation, DonationReceipt receipt)
- sendPasswordResetEmail(User user, String resetLink)
- sendEvidenceReminderEmail(Organization org, Campaign campaign, int daysRemaining)
- sendVerificationSuccessEmail(Organization org)
- sendCampaignApprovalEmail(Organization org, Campaign campaign, boolean approved, String reason)

**Internal:**
- logEmail(EmailLog log)
- retryFailedEmails() - called by scheduler

**Email Templates Location:** `src/main/resources/templates/email/`
- welcome.html
- donation-receipt.html
- password-reset.html
- evidence-reminder.html
- verification-success.html
- campaign-approval.html

---

### 6. Controller
**Location:** `src/main/java/com/seffafbagis/api/controller/notification/`

#### NotificationController.java

**User Endpoints (Authenticated):**
```
GET    /api/v1/notifications              - My notifications (paginated)
GET    /api/v1/notifications/unread       - Unread notifications
GET    /api/v1/notifications/count        - Unread count
PUT    /api/v1/notifications/{id}/read    - Mark as read
PUT    /api/v1/notifications/read-all     - Mark all as read
DELETE /api/v1/notifications/{id}         - Delete notification
```

**Admin Endpoints:**
```
GET  /api/v1/admin/notifications/email-logs - Email logs
POST /api/v1/admin/notifications/broadcast  - Send to all users
POST /api/v1/admin/notifications/retry-failed - Retry failed emails
```

---

## Notification Type Names (Turkish)

| Type | Turkish Name |
|------|--------------|
| DONATION_RECEIVED | Bağış Alındı |
| DONATION_COMPLETED | Bağış Tamamlandı |
| CAMPAIGN_UPDATE | Kampanya Güncellemesi |
| CAMPAIGN_COMPLETED | Kampanya Tamamlandı |
| CAMPAIGN_APPROVED | Kampanya Onaylandı |
| CAMPAIGN_REJECTED | Kampanya Reddedildi |
| EVIDENCE_REQUIRED | Kanıt Yükleme Hatırlatması |
| EVIDENCE_APPROVED | Kanıt Onaylandı |
| EVIDENCE_REJECTED | Kanıt Reddedildi |
| SCORE_CHANGE | Şeffaflık Skoru Değişti |
| APPLICATION_UPDATE | Başvuru Güncellendi |
| SYSTEM | Sistem Bildirimi |

---

## Notification Data Examples

### Donation Received
```java
Map<String, Object> data = Map.of(
    "donationId", donation.getId(),
    "campaignId", campaign.getId(),
    "campaignTitle", campaign.getTitle(),
    "amount", donation.getAmount(),
    "donorName", donation.isAnonymous() ? "Anonim" : donor.getDisplayName()
);
```

### Campaign Completed
```java
Map<String, Object> data = Map.of(
    "campaignId", campaign.getId(),
    "campaignTitle", campaign.getTitle(),
    "collectedAmount", campaign.getCollectedAmount(),
    "donorCount", campaign.getDonorCount()
);
```

### Score Change
```java
Map<String, Object> data = Map.of(
    "organizationId", org.getId(),
    "previousScore", oldScore,
    "newScore", newScore,
    "changeAmount", newScore.subtract(oldScore)
);
```

---

## User Preferences Integration

Check user preferences before sending:
```java
public void createNotification(UUID userId, NotificationType type, ...) {
    UserPreference prefs = userPreferenceRepository.findByUserId(userId);
    
    // Check in-app preference
    if (prefs != null && !prefs.getEmailNotifications()) {
        // Skip email but still create in-app notification
    }
    
    // Create notification
    Notification notification = new Notification();
    // ... set fields
    notificationRepository.save(notification);
    
    // Send email if enabled
    if (prefs == null || prefs.getEmailNotifications()) {
        // Queue email
    }
}
```

---

## Time Ago Calculation

```java
public String calculateTimeAgo(LocalDateTime createdAt) {
    long minutes = ChronoUnit.MINUTES.between(createdAt, LocalDateTime.now());
    
    if (minutes < 1) {
        return "Az önce";
    }
    if (minutes < 60) {
        return minutes + " dakika önce";
    }
    
    long hours = minutes / 60;
    if (hours < 24) {
        return hours + " saat önce";
    }
    
    long days = hours / 24;
    if (days < 7) {
        return days + " gün önce";
    }
    
    long weeks = days / 7;
    if (weeks < 4) {
        return weeks + " hafta önce";
    }
    
    long months = days / 30;
    if (months < 12) {
        return months + " ay önce";
    }
    
    return (days / 365) + " yıl önce";
}
```

---

## Integration Points

### Phase 7 (Donation)
```java
// In DonationService after successful donation
notificationService.notifyDonationReceived(donation);
notificationService.notifyDonationCompleted(donation);
```

### Phase 5 (Campaign)
```java
// In CampaignService
notificationService.notifyCampaignApproved(campaign);
notificationService.notifyCampaignRejected(campaign, reason);
notificationService.notifyCampaignCompleted(campaign);
```

### Phase 10 (Evidence)
```java
// In EvidenceService
notificationService.notifyEvidenceApproved(evidence);
notificationService.notifyEvidenceRejected(evidence, reason);
```

### Phase 11 (Transparency Score)
```java
// In TransparencyScoreService
notificationService.notifyScoreChange(orgId, oldScore, newScore);
```

---

## Testing Requirements

### Unit Tests
- NotificationServiceTest:
  - Test notification creation
  - Test user preference check
  - Test mark as read
  - Test time ago calculation

- EmailServiceTest:
  - Test email template rendering
  - Test email logging

### Integration Tests
- Donation → notification created
- Campaign approval → organization notified

---

## Success Criteria

- [ ] NotificationType enum with all values
- [ ] Notification and EmailLog entities created
- [ ] Both repositories with custom queries
- [ ] All response DTOs created
- [ ] NotificationService with all methods
- [ ] EmailService with all methods
- [ ] User preference check implemented
- [ ] Time ago calculation working
- [ ] Turkish notification type names
- [ ] All notification triggers integrated
- [ ] Mark as read functionality
- [ ] All endpoints with proper authorization
- [ ] All unit tests pass

---

## Result File Requirement

After completing this phase, create:
**Location:** `docs/Emir/step_results/phase_13.0_result.md`

Include:
1. Summary
2. Files created
3. Notification types table
4. Integration points with other phases
5. Email templates list
6. API endpoints table
7. Testing results
8. Issues and resolutions
9. Next steps (Phase 14.0)
10. Success criteria checklist

---

## Important Notes

1. **Do NOT use ternary operators** - Use if-else
2. **User preferences** - Always check before sending
3. **Email templates** - Use Thymeleaf for templates
4. **JSONB data field** - Use Hibernate Types
5. **Integrate with previous phases** - Add notification calls

---

## Dependencies

From Furkan's work:
- UserPreference, UserPreferenceRepository
- SecurityUtils, ApiResponse
- Email configuration (application.yml)

From previous phases:
- Donation, Campaign, Evidence, Application entities
- All services that trigger notifications

---

## Estimated Duration

2 days

---

## Next Phase

**Phase 14.0: Report Module (Fraud & Complaint Reports)**
