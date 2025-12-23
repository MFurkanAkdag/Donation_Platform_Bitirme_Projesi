# PHASE 4: USER ENTITY & REPOSITORY LAYER

## CONTEXT AND BACKGROUND

You are working on "Şeffaf Bağış Platformu" (Transparent Donation Platform), a graduation project for Fatih Sultan Mehmet Vakıf University. This is a comprehensive donation platform for Turkey that emphasizes transparency and accountability in charitable giving.

### Project Overview
- **Technology Stack**: Java 17, Spring Boot 3.x, PostgreSQL 15+, Redis, Maven
- **Purpose**: Enable foundations to create campaigns, donors to contribute, and maintain transparency through evidence uploads and scoring systems
- **Compliance Requirements**: KVKK (Turkish Personal Data Protection Law) compliant
- **Developer**: Furkan (responsible for Infrastructure, Auth, User, Admin modules - ~58% of backend)

### Current State
- Phase 0 (Database Migration) has been completed - all database tables exist
- Phase 1 (Project Foundation & Configuration) has been completed
- Phase 2 (Security Infrastructure) has been completed
- Phase 3 (Exception Handling & Common DTOs) has been completed
- BaseEntity.java is available for entity inheritance
- Exception classes are ready for use
- Security infrastructure is waiting for UserRepository

### What This Phase Accomplishes
This phase creates all user-related entities and their corresponding repository interfaces. These form the DATA LAYER FOUNDATION for user management, authentication, and all user-related operations. After this phase, the security infrastructure from Phase 2 will be fully functional.

---

## OBJECTIVE

Create the complete user data layer including:
1. Enum types for user roles and statuses
2. User-related entities (User, UserProfile, UserSensitiveData, UserPreference)
3. Authentication entities (RefreshToken, PasswordResetToken, EmailVerificationToken, LoginHistory)
4. System entities (AuditLog, EmailLog, SystemSetting, FavoriteOrganization)
5. Repository interfaces for all entities

---

## IMPORTANT RULES

### Code Style Requirements
- Use clear, readable code - prefer if-else statements over ternary operators
- Follow SOLID principles
- Add comprehensive comments in English
- Use meaningful variable and method names
- No complex one-liners - prioritize readability over brevity

### JPA/Entity Requirements
- All entities must extend BaseEntity (except junction tables)
- Use appropriate JPA annotations (@Entity, @Table, @Column, etc.)
- Define proper relationships with fetch types (prefer LAZY)
- Add database indexes for frequently queried fields
- Use @Enumerated(EnumType.STRING) for enum fields
- Column names must match database schema (snake_case in DB, camelCase in Java)

### Repository Requirements
- Extend JpaRepository<Entity, UUID>
- Add custom query methods as needed
- Use @Query for complex queries
- Use Optional for single-result queries that may return null
- Add proper method naming following Spring Data conventions

---

## DETAILED REQUIREMENTS

### 1. Enum Types

#### 1.1 UserRole.java
**Location**: `src/main/java/com/seffafbagis/api/enums/UserRole.java`

**Purpose**: Define user roles in the system

**Values**:
- `DONOR` - Regular donors who make donations
- `FOUNDATION` - Organizations/foundations that create campaigns
- `BENEFICIARY` - Aid recipients who can apply for help
- `ADMIN` - System administrators

**Requirements**:
- Simple enum with four values
- Add description field and getter (optional but recommended)
- Values must match database enum: 'donor', 'foundation', 'beneficiary', 'admin'

---

#### 1.2 UserStatus.java
**Location**: `src/main/java/com/seffafbagis/api/enums/UserStatus.java`

**Purpose**: Define user account statuses

**Values**:
- `ACTIVE` - Account is active and can be used
- `INACTIVE` - Account is deactivated by user
- `SUSPENDED` - Account is suspended by admin
- `PENDING_VERIFICATION` - Account awaiting email verification

**Requirements**:
- Simple enum with four values
- Add description field and getter (optional)
- Values must match database enum: 'active', 'inactive', 'suspended', 'pending_verification'

---

### 2. User Entities

#### 2.1 User.java
**Location**: `src/main/java/com/seffafbagis/api/entity/user/User.java`

**Purpose**: Main user entity containing authentication and core user data

**Table**: `users`

**Fields** (mapped to database columns):

| Java Field | DB Column | Type | Constraints |
|------------|-----------|------|-------------|
| id | id | UUID | PK (inherited from BaseEntity) |
| email | email | String(255) | Unique, Not Null |
| passwordHash | password_hash | String(255) | Not Null |
| role | role | UserRole | Not Null, Default DONOR |
| status | user_status | UserStatus | Not Null, Default PENDING_VERIFICATION |
| emailVerified | email_verified | Boolean | Default false |
| emailVerifiedAt | email_verified_at | LocalDateTime | Nullable |
| lastLoginAt | last_login_at | LocalDateTime | Nullable |
| failedLoginAttempts | failed_login_attempts | Integer | Default 0 |
| lockedUntil | locked_until | LocalDateTime | Nullable |
| passwordChangedAt | password_changed_at | LocalDateTime | Nullable |
| createdAt | created_at | LocalDateTime | (inherited) |
| updatedAt | updated_at | LocalDateTime | (inherited) |

**Relationships**:
- OneToOne with UserProfile (mappedBy = "user", cascade ALL, orphanRemoval true)
- OneToOne with UserSensitiveData (mappedBy = "user", cascade ALL, orphanRemoval true)
- OneToOne with UserPreference (mappedBy = "user", cascade ALL, orphanRemoval true)
- OneToMany with RefreshToken (mappedBy = "user", cascade ALL, orphanRemoval true)

**Indexes**:
- idx_users_email on email
- idx_users_role on role
- idx_users_status on status

**Methods**:
- `isAccountLocked()`: Check if lockedUntil is in the future
- `incrementFailedLoginAttempts()`: Increment counter
- `resetFailedLoginAttempts()`: Reset counter to 0
- `lockAccount(Duration duration)`: Set lockedUntil

**Notes**:
- Use @Column(name = "password_hash") for passwordHash field
- Use @Enumerated(EnumType.STRING) for role and status
- The column is named "user_status" in some schemas to avoid reserved word conflicts

---

#### 2.2 UserProfile.java
**Location**: `src/main/java/com/seffafbagis/api/entity/user/UserProfile.java`

**Purpose**: User profile information (name, avatar, bio)

**Table**: `user_profiles`

**Fields**:

| Java Field | DB Column | Type | Constraints |
|------------|-----------|------|-------------|
| id | id | UUID | PK (inherited) |
| user | user_id | User | FK, Unique, Not Null |
| firstName | first_name | String(100) | Nullable |
| lastName | last_name | String(100) | Nullable |
| displayName | display_name | String(100) | Nullable |
| avatarUrl | avatar_url | String(500) | Nullable |
| bio | bio | String(TEXT) | Nullable |
| preferredLanguage | preferred_language | String(5) | Default 'tr' |
| timezone | timezone | String(50) | Default 'Europe/Istanbul' |
| createdAt | created_at | LocalDateTime | (inherited) |
| updatedAt | updated_at | LocalDateTime | (inherited) |

**Relationships**:
- OneToOne with User (JoinColumn = "user_id")

**Methods**:
- `getFullName()`: Return firstName + " " + lastName (handle nulls)

---

#### 2.3 UserSensitiveData.java
**Location**: `src/main/java/com/seffafbagis/api/entity/user/UserSensitiveData.java`

**Purpose**: KVKK-protected sensitive user data (encrypted at rest)

**Table**: `user_sensitive_data`

**Fields**:

| Java Field | DB Column | Type | Constraints |
|------------|-----------|------|-------------|
| id | id | UUID | PK (inherited) |
| user | user_id | User | FK, Unique, Not Null |
| tcKimlikEncrypted | tc_kimlik_encrypted | byte[] | Nullable |
| phoneEncrypted | phone_encrypted | byte[] | Nullable |
| addressEncrypted | address_encrypted | byte[] | Nullable |
| birthDateEncrypted | birth_date_encrypted | byte[] | Nullable |
| dataProcessingConsent | data_processing_consent | Boolean | Default false |
| consentDate | consent_date | LocalDateTime | Nullable |
| marketingConsent | marketing_consent | Boolean | Default false |
| marketingConsentDate | marketing_consent_date | LocalDateTime | Nullable |
| thirdPartySharingConsent | third_party_sharing_consent | Boolean | Default false |
| thirdPartySharingConsentDate | third_party_sharing_consent_date | LocalDateTime | Nullable |
| createdAt | created_at | LocalDateTime | (inherited) |
| updatedAt | updated_at | LocalDateTime | (inherited) |

**Relationships**:
- OneToOne with User (JoinColumn = "user_id")

**Notes**:
- All encrypted fields are stored as byte[] (BYTEA in PostgreSQL)
- Encryption/decryption is handled by EncryptionService (Phase 5)
- DO NOT add any decrypt methods to this entity - that's service layer responsibility

---

#### 2.4 UserPreference.java
**Location**: `src/main/java/com/seffafbagis/api/entity/user/UserPreference.java`

**Purpose**: User preferences for notifications and privacy settings

**Table**: `user_preferences`

**Fields**:

| Java Field | DB Column | Type | Constraints |
|------------|-----------|------|-------------|
| id | id | UUID | PK (inherited) |
| user | user_id | User | FK, Unique, Not Null |
| emailNotifications | email_notifications | Boolean | Default true |
| smsNotifications | sms_notifications | Boolean | Default false |
| donationVisibility | donation_visibility | String(20) | Default 'anonymous' |
| showInDonorList | show_in_donor_list | Boolean | Default false |
| createdAt | created_at | LocalDateTime | (inherited) |
| updatedAt | updated_at | LocalDateTime | (inherited) |

**Relationships**:
- OneToOne with User (JoinColumn = "user_id")

**Notes**:
- donationVisibility values: 'public', 'anonymous', 'private'

---

### 3. Authentication Entities

#### 3.1 RefreshToken.java
**Location**: `src/main/java/com/seffafbagis/api/entity/auth/RefreshToken.java`

**Purpose**: Store JWT refresh tokens for session management

**Table**: `refresh_tokens`

**Fields**:

| Java Field | DB Column | Type | Constraints |
|------------|-----------|------|-------------|
| id | id | UUID | PK (inherited) |
| user | user_id | User | FK, Not Null |
| tokenHash | token_hash | String(255) | Not Null |
| deviceInfo | device_info | String(255) | Nullable |
| ipAddress | ip_address | String(45) | Nullable |
| expiresAt | expires_at | LocalDateTime | Not Null |
| revokedAt | revoked_at | LocalDateTime | Nullable |
| createdAt | created_at | LocalDateTime | (inherited) |

**Relationships**:
- ManyToOne with User (JoinColumn = "user_id")

**Methods**:
- `isExpired()`: Check if expiresAt is in the past
- `isRevoked()`: Check if revokedAt is not null
- `isValid()`: Return !isExpired() && !isRevoked()
- `revoke()`: Set revokedAt to now

**Notes**:
- Store hash of token, not the actual token
- ipAddress uses String to support both IPv4 and IPv6

---

#### 3.2 PasswordResetToken.java
**Location**: `src/main/java/com/seffafbagis/api/entity/auth/PasswordResetToken.java`

**Purpose**: Store password reset tokens

**Table**: `password_reset_tokens`

**Fields**:

| Java Field | DB Column | Type | Constraints |
|------------|-----------|------|-------------|
| id | id | UUID | PK (inherited) |
| user | user_id | User | FK, Not Null |
| tokenHash | token_hash | String(255) | Not Null |
| expiresAt | expires_at | LocalDateTime | Not Null |
| usedAt | used_at | LocalDateTime | Nullable |
| createdAt | created_at | LocalDateTime | (inherited) |

**Relationships**:
- ManyToOne with User (JoinColumn = "user_id")

**Methods**:
- `isExpired()`: Check if expiresAt is in the past
- `isUsed()`: Check if usedAt is not null
- `isValid()`: Return !isExpired() && !isUsed()
- `markAsUsed()`: Set usedAt to now

---

#### 3.3 EmailVerificationToken.java
**Location**: `src/main/java/com/seffafbagis/api/entity/auth/EmailVerificationToken.java`

**Purpose**: Store email verification tokens

**Table**: `email_verification_tokens`

**Fields**:

| Java Field | DB Column | Type | Constraints |
|------------|-----------|------|-------------|
| id | id | UUID | PK (inherited) |
| user | user_id | User | FK, Not Null |
| tokenHash | token_hash | String(255) | Not Null |
| expiresAt | expires_at | LocalDateTime | Not Null |
| verifiedAt | verified_at | LocalDateTime | Nullable |
| createdAt | created_at | LocalDateTime | (inherited) |

**Relationships**:
- ManyToOne with User (JoinColumn = "user_id")

**Methods**:
- `isExpired()`: Check if expiresAt is in the past
- `isVerified()`: Check if verifiedAt is not null
- `isValid()`: Return !isExpired() && !isVerified()
- `markAsVerified()`: Set verifiedAt to now

---

#### 3.4 LoginHistory.java
**Location**: `src/main/java/com/seffafbagis/api/entity/auth/LoginHistory.java`

**Purpose**: Track login attempts for security monitoring

**Table**: `login_history`

**Fields**:

| Java Field | DB Column | Type | Constraints |
|------------|-----------|------|-------------|
| id | id | UUID | PK (inherited) |
| user | user_id | User | FK, Not Null |
| loginStatus | login_status | String(20) | Not Null |
| ipAddress | ip_address | String(45) | Nullable |
| userAgent | user_agent | String(TEXT) | Nullable |
| deviceType | device_type | String(50) | Nullable |
| locationCountry | location_country | String(100) | Nullable |
| locationCity | location_city | String(100) | Nullable |
| failureReason | failure_reason | String(100) | Nullable |
| createdAt | created_at | LocalDateTime | (inherited) |

**Relationships**:
- ManyToOne with User (JoinColumn = "user_id")

**Notes**:
- loginStatus values: 'success', 'failed', 'blocked'
- deviceType values: 'desktop', 'mobile', 'tablet'
- failureReason values: 'invalid_password', 'account_locked', 'account_suspended', 'email_not_verified'

---

### 4. System Entities

#### 4.1 AuditLog.java
**Location**: `src/main/java/com/seffafbagis/api/entity/audit/AuditLog.java`

**Purpose**: System audit logs for KVKK compliance

**Table**: `audit_logs`

**Fields**:

| Java Field | DB Column | Type | Constraints |
|------------|-----------|------|-------------|
| id | id | UUID | PK (inherited) |
| user | user_id | User | FK, Nullable |
| action | action | String(100) | Not Null |
| entityType | entity_type | String(100) | Nullable |
| entityId | entity_id | UUID | Nullable |
| oldValues | old_values | String(JSONB) | Nullable |
| newValues | new_values | String(JSONB) | Nullable |
| ipAddress | ip_address | String(45) | Nullable |
| userAgent | user_agent | String(TEXT) | Nullable |
| requestId | request_id | String(50) | Nullable |
| sessionId | session_id | String(255) | Nullable |
| createdAt | created_at | LocalDateTime | (inherited) |

**Relationships**:
- ManyToOne with User (JoinColumn = "user_id", nullable)

**Notes**:
- oldValues and newValues store JSON - use @Column(columnDefinition = "jsonb")
- User is nullable for system actions or unauthenticated actions

---

#### 4.2 EmailLog.java
**Location**: `src/main/java/com/seffafbagis/api/entity/notification/EmailLog.java`

**Purpose**: Track sent emails

**Table**: `email_logs`

**Fields**:

| Java Field | DB Column | Type | Constraints |
|------------|-----------|------|-------------|
| id | id | UUID | PK (inherited) |
| user | user_id | User | FK, Nullable |
| emailTo | email_to | String(255) | Not Null |
| emailType | email_type | String(100) | Not Null |
| subject | subject | String(255) | Not Null |
| status | status | String(50) | Default 'sent' |
| providerMessageId | provider_message_id | String(255) | Nullable |
| errorMessage | error_message | String(TEXT) | Nullable |
| provider | provider | String(50) | Nullable |
| templateName | template_name | String(100) | Nullable |
| retryCount | retry_count | Integer | Default 0 |
| sentAt | sent_at | LocalDateTime | Default now |

**Relationships**:
- ManyToOne with User (JoinColumn = "user_id", nullable)

**Notes**:
- Does not extend BaseEntity (uses sentAt instead of createdAt)
- Or extend BaseEntity and add sentAt as additional field

---

#### 4.3 SystemSetting.java
**Location**: `src/main/java/com/seffafbagis/api/entity/system/SystemSetting.java`

**Purpose**: Platform-wide configuration settings

**Table**: `system_settings`

**Fields**:

| Java Field | DB Column | Type | Constraints |
|------------|-----------|------|-------------|
| id | id | UUID | PK (inherited) |
| settingKey | setting_key | String(100) | Unique, Not Null |
| settingValue | setting_value | String(TEXT) | Not Null |
| valueType | value_type | String(20) | Default 'string' |
| description | description | String(255) | Nullable |
| isPublic | is_public | Boolean | Default false |
| updatedBy | updated_by | User | FK, Nullable |
| createdAt | created_at | LocalDateTime | (inherited) |
| updatedAt | updated_at | LocalDateTime | (inherited) |

**Relationships**:
- ManyToOne with User (JoinColumn = "updated_by", nullable)

**Notes**:
- valueType: 'string', 'number', 'boolean', 'json'
- isPublic: whether setting should be exposed to frontend

---

#### 4.4 FavoriteOrganization.java
**Location**: `src/main/java/com/seffafbagis/api/entity/favorite/FavoriteOrganization.java`

**Purpose**: User's favorite organizations

**Table**: `favorite_organizations`

**Fields**:

| Java Field | DB Column | Type | Constraints |
|------------|-----------|------|-------------|
| userId | user_id | UUID | PK (composite), FK |
| organizationId | organization_id | UUID | PK (composite), FK |
| createdAt | created_at | LocalDateTime | Default now |

**Notes**:
- This is a junction table with composite primary key
- Does NOT extend BaseEntity
- Use @IdClass or @EmbeddedId for composite key
- References Organization entity (created by Emir) - use UUID for now

**Composite Key Class** - FavoriteOrganizationId.java:
- Implement Serializable
- Fields: userId (UUID), organizationId (UUID)
- Implement equals() and hashCode()

---

### 5. Repository Interfaces

#### 5.1 UserRepository.java
**Location**: `src/main/java/com/seffafbagis/api/repository/UserRepository.java`

**Methods**:
```
- findByEmail(String email): Optional<User>
- existsByEmail(String email): boolean
- findByEmailAndStatus(String email, UserStatus status): Optional<User>
- findAllByRole(UserRole role, Pageable pageable): Page<User>
- findAllByStatus(UserStatus status, Pageable pageable): Page<User>
- findAllByRoleAndStatus(UserRole role, UserStatus status, Pageable pageable): Page<User>
- countByRole(UserRole role): long
- countByStatus(UserStatus status): long
- searchByEmailOrName(String searchTerm, Pageable pageable): Page<User>
  - Custom @Query searching email, firstName, lastName
```

---

#### 5.2 UserProfileRepository.java
**Location**: `src/main/java/com/seffafbagis/api/repository/UserProfileRepository.java`

**Methods**:
```
- findByUserId(UUID userId): Optional<UserProfile>
- findByUser(User user): Optional<UserProfile>
```

---

#### 5.3 UserSensitiveDataRepository.java
**Location**: `src/main/java/com/seffafbagis/api/repository/UserSensitiveDataRepository.java`

**Methods**:
```
- findByUserId(UUID userId): Optional<UserSensitiveData>
- findByUser(User user): Optional<UserSensitiveData>
```

---

#### 5.4 UserPreferenceRepository.java
**Location**: `src/main/java/com/seffafbagis/api/repository/UserPreferenceRepository.java`

**Methods**:
```
- findByUserId(UUID userId): Optional<UserPreference>
- findByUser(User user): Optional<UserPreference>
```

---

#### 5.5 RefreshTokenRepository.java
**Location**: `src/main/java/com/seffafbagis/api/repository/RefreshTokenRepository.java`

**Methods**:
```
- findByTokenHash(String tokenHash): Optional<RefreshToken>
- findAllByUserAndRevokedAtIsNull(User user): List<RefreshToken>
- findAllByUser(User user): List<RefreshToken>
- deleteAllByUser(User user): void
- deleteAllByExpiresAtBefore(LocalDateTime dateTime): int (for cleanup)
- revokeAllByUser(UUID userId): void - Custom @Modifying @Query
```

---

#### 5.6 PasswordResetTokenRepository.java
**Location**: `src/main/java/com/seffafbagis/api/repository/PasswordResetTokenRepository.java`

**Methods**:
```
- findByTokenHash(String tokenHash): Optional<PasswordResetToken>
- findByUserAndUsedAtIsNull(User user): Optional<PasswordResetToken>
- deleteAllByExpiresAtBefore(LocalDateTime dateTime): int
```

---

#### 5.7 EmailVerificationTokenRepository.java
**Location**: `src/main/java/com/seffafbagis/api/repository/EmailVerificationTokenRepository.java`

**Methods**:
```
- findByTokenHash(String tokenHash): Optional<EmailVerificationToken>
- findByUserAndVerifiedAtIsNull(User user): Optional<EmailVerificationToken>
- deleteAllByExpiresAtBefore(LocalDateTime dateTime): int
```

---

#### 5.8 LoginHistoryRepository.java
**Location**: `src/main/java/com/seffafbagis/api/repository/LoginHistoryRepository.java`

**Methods**:
```
- findAllByUser(User user, Pageable pageable): Page<LoginHistory>
- findAllByUserOrderByCreatedAtDesc(User user, Pageable pageable): Page<LoginHistory>
- findAllByUserAndLoginStatus(User user, String loginStatus, Pageable pageable): Page<LoginHistory>
- countByUserAndLoginStatusAndCreatedAtAfter(User user, String loginStatus, LocalDateTime after): long
- deleteAllByCreatedAtBefore(LocalDateTime dateTime): int (for cleanup)
```

---

#### 5.9 AuditLogRepository.java
**Location**: `src/main/java/com/seffafbagis/api/repository/AuditLogRepository.java`

**Methods**:
```
- findAllByUser(User user, Pageable pageable): Page<AuditLog>
- findAllByEntityTypeAndEntityId(String entityType, UUID entityId, Pageable pageable): Page<AuditLog>
- findAllByAction(String action, Pageable pageable): Page<AuditLog>
- findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable): Page<AuditLog>
- deleteAllByCreatedAtBefore(LocalDateTime dateTime): int
```

---

#### 5.10 EmailLogRepository.java
**Location**: `src/main/java/com/seffafbagis/api/repository/EmailLogRepository.java`

**Methods**:
```
- findAllByUser(User user, Pageable pageable): Page<EmailLog>
- findAllByEmailType(String emailType, Pageable pageable): Page<EmailLog>
- findAllByStatus(String status, Pageable pageable): Page<EmailLog>
- countByEmailTypeAndSentAtAfter(String emailType, LocalDateTime after): long
```

---

#### 5.11 SystemSettingRepository.java
**Location**: `src/main/java/com/seffafbagis/api/repository/SystemSettingRepository.java`

**Methods**:
```
- findBySettingKey(String settingKey): Optional<SystemSetting>
- findAllByIsPublicTrue(): List<SystemSetting>
- existsBySettingKey(String settingKey): boolean
```

---

#### 5.12 FavoriteOrganizationRepository.java
**Location**: `src/main/java/com/seffafbagis/api/repository/FavoriteOrganizationRepository.java`

**Methods**:
```
- findAllByUserId(UUID userId): List<FavoriteOrganization>
- findByUserIdAndOrganizationId(UUID userId, UUID organizationId): Optional<FavoriteOrganization>
- existsByUserIdAndOrganizationId(UUID userId, UUID organizationId): boolean
- deleteByUserIdAndOrganizationId(UUID userId, UUID organizationId): void
- countByOrganizationId(UUID organizationId): long
```

---

## FILE STRUCTURE

After completing this phase, the following files should exist:

```
src/main/java/com/seffafbagis/api/
├── enums/
│   ├── UserRole.java
│   └── UserStatus.java
├── entity/
│   ├── user/
│   │   ├── User.java
│   │   ├── UserProfile.java
│   │   ├── UserSensitiveData.java
│   │   └── UserPreference.java
│   ├── auth/
│   │   ├── RefreshToken.java
│   │   ├── PasswordResetToken.java
│   │   ├── EmailVerificationToken.java
│   │   └── LoginHistory.java
│   ├── audit/
│   │   └── AuditLog.java
│   ├── notification/
│   │   └── EmailLog.java
│   ├── system/
│   │   └── SystemSetting.java
│   └── favorite/
│       ├── FavoriteOrganization.java
│       └── FavoriteOrganizationId.java
└── repository/
    ├── UserRepository.java
    ├── UserProfileRepository.java
    ├── UserSensitiveDataRepository.java
    ├── UserPreferenceRepository.java
    ├── RefreshTokenRepository.java
    ├── PasswordResetTokenRepository.java
    ├── EmailVerificationTokenRepository.java
    ├── LoginHistoryRepository.java
    ├── AuditLogRepository.java
    ├── EmailLogRepository.java
    ├── SystemSettingRepository.java
    └── FavoriteOrganizationRepository.java
```

**Total Files**: 26

---

## ENTITY RELATIONSHIP DIAGRAM

```
                                    ┌─────────────────────┐
                                    │       User          │
                                    │─────────────────────│
                                    │ id (PK)             │
                                    │ email               │
                                    │ passwordHash        │
                                    │ role                │
                                    │ status              │
                                    │ emailVerified       │
                                    │ failedLoginAttempts │
                                    │ lockedUntil         │
                                    └─────────┬───────────┘
                                              │
              ┌───────────────┬───────────────┼───────────────┬───────────────┐
              │               │               │               │               │
              ▼               ▼               ▼               ▼               ▼
    ┌─────────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
    │  UserProfile    │ │UserSensitive│ │UserPreference│ │RefreshToken │ │LoginHistory │
    │─────────────────│ │    Data     │ │─────────────│ │─────────────│ │─────────────│
    │ user_id (FK)    │ │─────────────│ │ user_id (FK)│ │ user_id (FK)│ │ user_id (FK)│
    │ firstName       │ │ user_id (FK)│ │ email_notif │ │ tokenHash   │ │ loginStatus │
    │ lastName        │ │ tc_encrypted│ │ sms_notif   │ │ expiresAt   │ │ ipAddress   │
    │ displayName     │ │ phone_enc   │ │ visibility  │ │ revokedAt   │ │ userAgent   │
    │ avatarUrl       │ │ address_enc │ └─────────────┘ └─────────────┘ │ deviceType  │
    │ bio             │ │ consents    │                                 └─────────────┘
    └─────────────────┘ └─────────────┘

    Additional User Relations:
    ┌─────────────────────┐  ┌─────────────────────┐
    │PasswordResetToken   │  │EmailVerificationTok │
    │─────────────────────│  │─────────────────────│
    │ user_id (FK)        │  │ user_id (FK)        │
    │ tokenHash           │  │ tokenHash           │
    │ expiresAt           │  │ expiresAt           │
    │ usedAt              │  │ verifiedAt          │
    └─────────────────────┘  └─────────────────────┘

    System Entities:
    ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
    │   AuditLog      │  │    EmailLog     │  │  SystemSetting  │
    │─────────────────│  │─────────────────│  │─────────────────│
    │ user_id (FK)?   │  │ user_id (FK)?   │  │ settingKey      │
    │ action          │  │ emailTo         │  │ settingValue    │
    │ entityType      │  │ emailType       │  │ valueType       │
    │ entityId        │  │ subject         │  │ isPublic        │
    │ oldValues       │  │ status          │  │ updatedBy (FK)  │
    │ newValues       │  │ provider        │  └─────────────────┘
    └─────────────────┘  └─────────────────┘

    Junction Table:
    ┌─────────────────────────┐
    │  FavoriteOrganization   │
    │─────────────────────────│
    │ user_id (PK, FK)        │
    │ organization_id (PK,FK) │
    │ createdAt               │
    └─────────────────────────┘
```

---

## TESTING REQUIREMENTS

After implementation, verify:

### 1. Entity Compilation Test
- All entities compile without errors
- All relationships are properly defined
- No circular dependency issues

### 2. Application Startup Test
- Run application with `mvn spring-boot:run`
- Hibernate should validate entity mappings against database
- No schema mismatch errors

### 3. Repository Test
- Write integration tests for UserRepository
- Test findByEmail method
- Test pagination methods
- Verify custom queries work

### 4. Relationship Test
- Create User with Profile, Preferences, SensitiveData
- Verify cascade operations work
- Verify orphan removal works

### 5. Security Integration Test
- Verify CustomUserDetailsService now works with UserRepository
- Test loading user by email
- Test loading user by ID

---

## SUCCESS CRITERIA

Phase 4 is considered successful when:

1. ✅ All 26 files are created in correct locations
2. ✅ All entities compile without errors
3. ✅ All repositories compile without errors
4. ✅ Application starts without entity mapping errors
5. ✅ Hibernate validates schema successfully
6. ✅ UserRepository.findByEmail() works
7. ✅ CustomUserDetailsService loads users successfully
8. ✅ Entity relationships work correctly
9. ✅ Cascade operations function as expected
10. ✅ All indexes are recognized by JPA

---

## RESULT FILE REQUIREMENTS

After completing this phase, create a result file at:
`docs/Furkan/step_results/phase_4_result.md`

The result file must include:

1. **Execution Status**: Success or Failure
2. **Files Created**: List all 26 files with their paths
3. **Entity Validation**:
   - Confirm all entities map to database correctly
   - List any schema adjustments needed
4. **Repository Tests**:
   - Test results for key repository methods
   - Confirm custom queries work
5. **Relationship Tests**:
   - Verify OneToOne relationships
   - Verify ManyToOne relationships
   - Verify cascade behavior
6. **Security Integration**:
   - Confirm CustomUserDetailsService works
   - Test authentication flow
7. **Issues Encountered**: Any problems and how they were resolved
8. **Database Adjustments**: Any migration changes needed
9. **Notes for Next Phase**: Observations relevant to Phase 5

---

## NOTES

- This phase creates the data foundation - verify everything carefully
- Entity field names must exactly match database columns
- Test relationships thoroughly before moving to Phase 5
- CustomUserDetailsService should now be fully functional
- FavoriteOrganization references Organization which Emir will create

---

## NEXT PHASE PREVIEW

Phase 5 (Encryption & Security Utilities) will create:
- EncryptionService for AES-256 encryption
- This service will be used to encrypt/decrypt UserSensitiveData fields
- Validators for TC Kimlik, phone, password, IBAN
