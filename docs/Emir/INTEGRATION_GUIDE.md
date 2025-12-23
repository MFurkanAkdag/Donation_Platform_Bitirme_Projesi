# Integration Guide

How modules integrate in the Şeffaf Bağış Platformu.

---

## Module Architecture

```
┌─────────────────────────────────────────────────────┐
│                    CONTROLLERS                       │
├──────┬──────┬────────┬─────────┬──────┬────────────┤
│ Auth │ User │Campaign│Donation │Report│ Notification│
└──┬───┴──┬───┴────┬───┴────┬────┴──┬───┴─────┬──────┘
   │      │        │        │       │         │
   ▼      ▼        ▼        ▼       ▼         ▼
┌─────────────────────────────────────────────────────┐
│                     SERVICES                         │
├──────┬──────┬────────┬─────────┬──────┬────────────┤
│ Auth │ User │Campaign│Donation │Report│ Notification│
│      │      │        │Payment  │      │             │
│      │      │        │Evidence │      │             │
└──┬───┴──┬───┴────┬───┴────┬────┴──┬───┴─────┬──────┘
   │      │        │        │       │         │
   ▼      ▼        ▼        ▼       ▼         ▼
┌─────────────────────────────────────────────────────┐
│                   REPOSITORIES                       │
└─────────────────────────────────────────────────────┘
```

---

## Event-Driven Integration

Services communicate via Spring Events:

| Event | Publisher | Listeners |
|-------|-----------|-----------|
| `DonationCompletedEvent` | DonationService | NotificationListener, AuditListener |
| `CampaignApprovedEvent` | CampaignService | NotificationListener |
| `EvidenceApprovedEvent` | EvidenceService | TransparencyScoreListener |
| `OrganizationVerifiedEvent` | OrganizationService | TransparencyScoreListener |

---

## Key Integration Points

### 1. Donation → Campaign

When donation is completed:
1. `DonationService` updates donation status
2. Campaign's `collectedAmount` is updated
3. Campaign's `donorCount` is incremented
4. `DonationCompletedEvent` is published

### 2. Evidence → Transparency Score

When evidence is approved:
1. `EvidenceService` updates evidence status
2. `EvidenceApprovedEvent` is published
3. `TransparencyScoreService` calculates new score
4. Score history is recorded

### 3. Report → Organization

When fraud report is upheld:
1. Report status changes to RESOLVED
2. Organization transparency score decreases by 15
3. Organization may be suspended

---

## Database Relationships

```
User ──1:1── Organization ──1:N── Campaign
  │                                   │
  └───────1:N── Donation ─────────────┘
                    │
                    └── Payment/Transaction

Campaign ──1:N── Evidence ── TransparencyScore
Organization ──1:1── TransparencyScore
```

---

## Authentication Flow

1. User registers → Email verification token created
2. User verifies email → Account activated
3. User logs in → JWT access + refresh tokens issued
4. Access token expires → Refresh token used
5. User logs out → Tokens invalidated

---

## Payment Flow

1. Donation created with PENDING status
2. Payment processed via Iyzico
3. Success: Donation → COMPLETED, Campaign updated
4. Failure: Donation → FAILED, User notified

---

## Scheduler Integration

| Scheduler | Frequency | Actions |
|-----------|-----------|---------|
| RecurringDonationScheduler | Daily 7AM | Process due recurring donations |
| BankTransferScheduler | Every 30min | Check pending transfers |
| EvidenceReminderScheduler | Daily 9AM | Remind orgs about deadlines |
| CleanupScheduler | Daily 2AM | Clean expired tokens/data |
