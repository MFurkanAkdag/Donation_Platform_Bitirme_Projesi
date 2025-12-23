# FURKAN BACKEND DEVELOPMENT - COMPLETE PHASE BREAKDOWN

## PROJECT OVERVIEW

**Project**: Şeffaf Bağış Platformu (Transparent Donation Platform)  
**Developer**: Furkan  
**Responsibility**: ~58% of Backend (Infrastructure, Auth, User, Admin, System Settings, Favorites, Audit, Encryption, Utilities)  
**Technology Stack**: Java 17, Spring Boot 3.x, PostgreSQL, Redis, JWT

---

## PHASE SUMMARY TABLE

| Phase | Name | Description | Estimated Duration | Dependencies |
|-------|------|-------------|-------------------|--------------|
| 0 | Database Migration | Add missing columns and tables for real-world compatibility | 1 day | None |
| 1 | Project Foundation & Configuration | Base configs, properties, common components | 2 days | Phase 0 |
| 2 | Security Infrastructure | Spring Security, JWT provider, filters | 3 days | Phase 1 |
| 3 | Exception Handling & Common DTOs | Global exception handler, API response structures | 1 day | Phase 1 |
| 4 | User Entity & Repository Layer | User-related entities, repositories, base entity | 2 days | Phase 3 |
| 5 | Encryption & Security Utilities | AES-256 encryption service, security utils | 2 days | Phase 4 |
| 6 | Auth Module - Core | Login, Register, JWT token management | 3 days | Phase 5 |
| 7 | Auth Module - Extended | Password reset, email verification, refresh tokens | 2 days | Phase 6 |
| 8 | User Module - Profile & Preferences | User profile, preferences, sensitive data management | 3 days | Phase 7 |
| 9 | Admin Module - User Management | Admin user operations, user listing, status management | 2 days | Phase 8 |
| 10 | Admin Module - Organization & Campaign | Admin organization/campaign approval (interface-based) | 2 days | Phase 9 |
| 11 | System Settings & Favorites | System configuration, favorite organizations | 2 days | Phase 8 |
| 12 | Audit & Logging | Audit log service, email logging | 2 days | Phase 8 |
| 13 | Utility Classes | Slug generator, date utils, reference code generator | 1 day | Phase 1 |
| 14 | Integration & Final Testing | End-to-end testing, bug fixes, documentation | 3 days | All Phases |

**Total Estimated Duration**: 31 days (~6-7 weeks)

---

## DETAILED PHASE BREAKDOWN

---

# PHASE 1: PROJECT FOUNDATION & CONFIGURATION

## Overview
This phase establishes the core configuration infrastructure that all subsequent phases depend on. It includes Spring Boot configuration files, environment-specific properties, and common configuration classes.

## Problems Being Solved
- Application needs proper configuration for different environments (dev, prod, test)
- Database connection, Redis cache, and mail server need configuration
- CORS settings required for frontend communication
- API documentation (Swagger/OpenAPI) setup needed
- Audit logging infrastructure must be configured

## Components to Create

### 1.1 Application Properties Files
**Location**: `src/main/resources/`

| File | Purpose |
|------|---------|
| `application.yml` | Main configuration with common settings |
| `application-dev.yml` | Development environment overrides |
| `application-prod.yml` | Production environment settings |
| `application-test.yml` | Test environment configuration |

**Key Configurations**:
- Server port and context path
- PostgreSQL datasource configuration
- Redis connection settings
- JWT secret and expiration times
- Mail server (SMTP) settings
- Flyway migration settings
- Logging levels

### 1.2 Configuration Classes
**Location**: `src/main/java/com/seffafbagis/api/config/`

| Class | Purpose |
|-------|---------|
| `CorsConfig.java` | CORS settings for frontend access |
| `RedisConfig.java` | Redis cache configuration and serialization |
| `OpenApiConfig.java` | Swagger/OpenAPI documentation setup |
| `AuditConfig.java` | JPA Auditing configuration (createdAt, updatedAt) |
| `JwtConfig.java` | JWT configuration properties holder |
| `MailConfig.java` | JavaMailSender configuration |

### 1.3 Base Entity
**Location**: `src/main/java/com/seffafbagis/api/entity/base/`

| Class | Purpose |
|-------|---------|
| `BaseEntity.java` | Abstract base class with id, createdAt, updatedAt |

## Expected Outputs
- All configuration files created and properly structured
- Application starts without errors
- Redis connection established
- Swagger UI accessible at `/swagger-ui.html`
- Database connection working

## Success Criteria
- [ ] Application boots successfully with `mvn spring-boot:run`
- [ ] No configuration errors in logs
- [ ] Swagger UI loads correctly
- [ ] Database tables created by Flyway
- [ ] Redis ping returns PONG

## Files to Create (Count: 11)
```
src/main/resources/
├── application.yml
├── application-dev.yml
├── application-prod.yml
├── application-test.yml
├── messages.properties
└── messages_en.properties

src/main/java/com/seffafbagis/api/
├── config/
│   ├── CorsConfig.java
│   ├── RedisConfig.java
│   ├── OpenApiConfig.java
│   ├── AuditConfig.java
│   ├── JwtConfig.java
│   └── MailConfig.java
└── entity/base/
    └── BaseEntity.java
```

---

# PHASE 2: SECURITY INFRASTRUCTURE

## Overview
This phase implements the complete Spring Security infrastructure including JWT token generation, validation, and authentication filters. This is the most critical security foundation.

## Problems Being Solved
- Application needs stateless JWT-based authentication
- Each request must be validated for valid token
- Role-based access control (DONOR, FOUNDATION, ADMIN) required
- Unauthorized requests need proper handling
- Security utilities needed for getting current user context

## Components to Create

### 2.1 Security Configuration
**Location**: `src/main/java/com/seffafbagis/api/config/`

| Class | Purpose |
|-------|---------|
| `SecurityConfig.java` | Main Spring Security configuration, filter chain, endpoint permissions |

### 2.2 JWT Components
**Location**: `src/main/java/com/seffafbagis/api/security/`

| Class | Purpose |
|-------|---------|
| `JwtTokenProvider.java` | Generate, validate, parse JWT tokens |
| `JwtAuthenticationFilter.java` | Filter to extract and validate JWT from requests |
| `JwtAuthenticationEntryPoint.java` | Handle unauthorized access attempts |

### 2.3 User Details Components
**Location**: `src/main/java/com/seffafbagis/api/security/`

| Class | Purpose |
|-------|---------|
| `CustomUserDetails.java` | Spring Security UserDetails implementation |
| `CustomUserDetailsService.java` | Load user from database for authentication |

### 2.4 Security Utilities
**Location**: `src/main/java/com/seffafbagis/api/security/`

| Class | Purpose |
|-------|---------|
| `SecurityUtils.java` | Static utilities: getCurrentUser(), getCurrentUserId(), hasRole() |

## Security Flow
```
Request → JwtAuthenticationFilter → Extract Token → Validate Token
    ↓
Token Valid? → Yes → Set SecurityContext → Continue to Controller
    ↓
Token Invalid/Missing? → JwtAuthenticationEntryPoint → 401 Response
```

## Expected Outputs
- Complete JWT authentication flow working
- Protected endpoints return 401 without token
- Valid token grants access
- Current user retrievable via SecurityUtils

## Success Criteria
- [ ] SecurityConfig compiles and loads without errors
- [ ] JWT tokens can be generated with user info
- [ ] JWT tokens can be validated and parsed
- [ ] Protected endpoints reject requests without valid token
- [ ] SecurityUtils.getCurrentUser() returns authenticated user

## Files to Create (Count: 7)
```
src/main/java/com/seffafbagis/api/
├── config/
│   └── SecurityConfig.java
└── security/
    ├── JwtTokenProvider.java
    ├── JwtAuthenticationFilter.java
    ├── JwtAuthenticationEntryPoint.java
    ├── CustomUserDetails.java
    ├── CustomUserDetailsService.java
    └── SecurityUtils.java
```

---

# PHASE 3: EXCEPTION HANDLING & COMMON DTOs

## Overview
This phase creates a centralized exception handling system and common response structures that will be used across all modules.

## Problems Being Solved
- Exceptions need consistent handling and response format
- API responses need standardized structure
- Validation errors need proper formatting
- Common response patterns (success, error, paginated) needed

## Components to Create

### 3.1 Custom Exceptions
**Location**: `src/main/java/com/seffafbagis/api/exception/`

| Class | Purpose |
|-------|---------|
| `ResourceNotFoundException.java` | 404 - Resource not found |
| `BadRequestException.java` | 400 - Invalid request data |
| `UnauthorizedException.java` | 401 - Authentication required |
| `ForbiddenException.java` | 403 - Access denied |
| `ConflictException.java` | 409 - Resource conflict (duplicate email, etc.) |
| `FileStorageException.java` | 500 - File operation errors |
| `EncryptionException.java` | 500 - Encryption/decryption errors |
| `GlobalExceptionHandler.java` | @ControllerAdvice for all exceptions |

### 3.2 Common Response DTOs
**Location**: `src/main/java/com/seffafbagis/api/dto/response/common/`

| Class | Purpose |
|-------|---------|
| `ApiResponse.java` | Generic wrapper: success, message, data, timestamp |
| `ErrorResponse.java` | Error details: code, message, field errors |
| `PageResponse.java` | Paginated response wrapper |

## Response Structure Example
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## Error Response Example
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "fieldErrors": [
      {"field": "email", "message": "Invalid email format"}
    ]
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## Expected Outputs
- All exceptions handled consistently
- API responses follow standard format
- Validation errors properly formatted
- No stack traces leaked to clients

## Success Criteria
- [ ] GlobalExceptionHandler catches all exception types
- [ ] ResourceNotFoundException returns 404
- [ ] Validation errors return 400 with field details
- [ ] ApiResponse used across all controllers
- [ ] No sensitive information in error responses

## Files to Create (Count: 11)
```
src/main/java/com/seffafbagis/api/
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── BadRequestException.java
│   ├── UnauthorizedException.java
│   ├── ForbiddenException.java
│   ├── ConflictException.java
│   ├── FileStorageException.java
│   └── EncryptionException.java
└── dto/response/common/
    ├── ApiResponse.java
    ├── ErrorResponse.java
    └── PageResponse.java
```

---

# PHASE 4: USER ENTITY & REPOSITORY LAYER

## Overview
This phase creates all user-related entities and their corresponding repository interfaces. These form the data layer foundation for user management.

## Problems Being Solved
- User data needs proper JPA entity mapping
- Sensitive data (TC, phone) needs separate encrypted storage
- User preferences need dedicated entity
- Auth tokens need entity representation
- Repository methods needed for data access

## Components to Create

### 4.1 Enum Types
**Location**: `src/main/java/com/seffafbagis/api/enums/`

| Enum | Purpose |
|------|---------|
| `UserRole.java` | DONOR, FOUNDATION, BENEFICIARY, ADMIN |
| `UserStatus.java` | ACTIVE, INACTIVE, SUSPENDED, PENDING_VERIFICATION |

### 4.2 User Entities
**Location**: `src/main/java/com/seffafbagis/api/entity/user/`

| Entity | Purpose |
|--------|---------|
| `User.java` | Main user entity with auth fields |
| `UserProfile.java` | Profile info (name, avatar, bio) |
| `UserSensitiveData.java` | Encrypted KVKK data (TC, phone, address) |
| `UserPreference.java` | Notification and privacy preferences |

### 4.3 Auth Entities
**Location**: `src/main/java/com/seffafbagis/api/entity/auth/`

| Entity | Purpose |
|--------|---------|
| `RefreshToken.java` | JWT refresh tokens |
| `PasswordResetToken.java` | Password reset tokens |
| `EmailVerificationToken.java` | Email verification tokens |

### 4.4 Additional Entities (Furkan's Responsibility)
**Location**: `src/main/java/com/seffafbagis/api/entity/`

| Entity | Purpose |
|--------|---------|
| `audit/AuditLog.java` | System audit logs |
| `notification/EmailLog.java` | Email sending logs |
| `system/SystemSetting.java` | Platform settings |
| `favorite/FavoriteOrganization.java` | User favorite organizations |
| `auth/LoginHistory.java` | Login attempt history |

### 4.5 Repository Interfaces
**Location**: `src/main/java/com/seffafbagis/api/repository/`

| Repository | Purpose |
|------------|---------|
| `UserRepository.java` | User CRUD + custom queries |
| `UserProfileRepository.java` | Profile operations |
| `UserSensitiveDataRepository.java` | Sensitive data access |
| `UserPreferenceRepository.java` | Preference operations |
| `RefreshTokenRepository.java` | Token management |
| `PasswordResetTokenRepository.java` | Reset token operations |
| `EmailVerificationTokenRepository.java` | Verification tokens |
| `AuditLogRepository.java` | Audit log queries |
| `EmailLogRepository.java` | Email log operations |
| `SystemSettingRepository.java` | Settings access |
| `FavoriteOrganizationRepository.java` | Favorites operations |
| `LoginHistoryRepository.java` | Login history queries |

## Entity Relationships
```
User (1) ──── (1) UserProfile
     │
     ├─── (1) UserSensitiveData
     │
     ├─── (1) UserPreference
     │
     ├─── (N) RefreshToken
     │
     ├─── (N) PasswordResetToken
     │
     ├─── (N) EmailVerificationToken
     │
     ├─── (N) AuditLog
     │
     ├─── (N) LoginHistory
     │
     └─── (N) FavoriteOrganization
```

## Expected Outputs
- All entities properly mapped to database tables
- Repositories provide required query methods
- JPA relationships correctly defined
- Indexes properly annotated

## Success Criteria
- [ ] All entities compile without errors
- [ ] Application starts with entities loaded
- [ ] Repository methods work correctly
- [ ] Database tables match entity definitions
- [ ] Foreign key constraints properly created

## Files to Create (Count: 21)
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
│       └── FavoriteOrganization.java
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

---

# PHASE 5: ENCRYPTION & SECURITY UTILITIES

## Overview
This phase implements the encryption service for KVKK-compliant data protection and additional security utility methods.

## Problems Being Solved
- Sensitive data (TC Kimlik, phone, address) must be encrypted at rest
- AES-256 encryption required for KVKK compliance
- Encryption keys must be securely managed
- Token generation utilities needed
- Password validation rules needed

## Components to Create

### 5.1 Encryption Service
**Location**: `src/main/java/com/seffafbagis/api/service/encryption/`

| Class | Purpose |
|-------|---------|
| `EncryptionService.java` | AES-256 encrypt/decrypt operations |

**Key Features**:
- AES-256-GCM encryption (authenticated encryption)
- Secure key derivation from configuration
- IV (Initialization Vector) management
- Base64 encoding for storage
- Exception handling for crypto operations

### 5.2 Validators
**Location**: `src/main/java/com/seffafbagis/api/validator/`

| Class | Purpose |
|-------|---------|
| `PasswordValidator.java` | Password strength validation |
| `TcKimlikValidator.java` | Turkish ID number validation |
| `PhoneValidator.java` | Turkish phone number validation |
| `IbanValidator.java` | IBAN format validation |

### 5.3 Utility Classes
**Location**: `src/main/java/com/seffafbagis/api/util/`

| Class | Purpose |
|-------|---------|
| `TokenUtils.java` | Secure random token generation |

## Encryption Flow
```
Plain Text → AES-256-GCM Encrypt → IV + Ciphertext → Base64 Encode → Store in DB
                                                                         ↓
DB → Base64 Decode → Extract IV + Ciphertext → AES-256-GCM Decrypt → Plain Text
```

## Password Rules
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character

## TC Kimlik Validation Rules
- Exactly 11 digits
- First digit cannot be 0
- Checksum validation (Turkish algorithm)

## Expected Outputs
- Encryption service working correctly
- All validators functioning
- Token generation secure and random
- No plain text sensitive data in database

## Success Criteria
- [ ] Encrypt then decrypt returns original text
- [ ] Different encryptions produce different ciphertexts (IV uniqueness)
- [ ] Invalid TC Kimlik rejected
- [ ] Weak passwords rejected
- [ ] Invalid phone numbers rejected

## Files to Create (Count: 6)
```
src/main/java/com/seffafbagis/api/
├── service/encryption/
│   └── EncryptionService.java
├── validator/
│   ├── PasswordValidator.java
│   ├── TcKimlikValidator.java
│   ├── PhoneValidator.java
│   └── IbanValidator.java
└── util/
    └── TokenUtils.java
```

---

# PHASE 6: AUTH MODULE - CORE

## Overview
This phase implements the core authentication functionality including login, registration, and JWT token management.

## Problems Being Solved
- Users need to register with email/password
- Users need to login and receive JWT tokens
- Access tokens need refresh mechanism
- Logout must invalidate tokens
- Login attempts need tracking (security)

## Components to Create

### 6.1 Auth DTOs - Request
**Location**: `src/main/java/com/seffafbagis/api/dto/request/auth/`

| DTO | Purpose |
|-----|---------|
| `LoginRequest.java` | Email, password |
| `RegisterRequest.java` | Email, password, role, profile info |
| `RefreshTokenRequest.java` | Refresh token for new access token |
| `LogoutRequest.java` | Refresh token to invalidate |

### 6.2 Auth DTOs - Response
**Location**: `src/main/java/com/seffafbagis/api/dto/response/auth/`

| DTO | Purpose |
|-----|---------|
| `AuthResponse.java` | Access token, refresh token, user info |
| `TokenResponse.java` | New tokens after refresh |

### 6.3 Auth Service
**Location**: `src/main/java/com/seffafbagis/api/service/auth/`

| Class | Purpose |
|-------|---------|
| `AuthService.java` | Login, register, token refresh, logout logic |
| `JwtService.java` | Token generation, validation wrapper |

### 6.4 Auth Controller
**Location**: `src/main/java/com/seffafbagis/api/controller/auth/`

| Class | Purpose |
|-------|---------|
| `AuthController.java` | REST endpoints for auth operations |

## Auth Endpoints
```
POST /api/v1/auth/register    - User registration
POST /api/v1/auth/login       - User login
POST /api/v1/auth/refresh     - Refresh access token
POST /api/v1/auth/logout      - Logout (invalidate refresh token)
```

## Login Flow
```
1. Receive email/password
2. Check if account is locked (failed_login_attempts)
3. Validate credentials
4. If invalid: increment failed attempts, record in login_history
5. If valid: reset failed attempts, generate tokens
6. Create refresh token record in database
7. Record successful login in login_history
8. Return tokens and user info
```

## Registration Flow
```
1. Validate request data
2. Check email uniqueness
3. Hash password with BCrypt
4. Create User entity
5. Create UserProfile entity
6. Create UserSensitiveData entity (empty)
7. Create UserPreference entity (defaults)
8. Generate email verification token
9. Send verification email
10. Return success (user must verify email)
```

## Expected Outputs
- Complete registration flow working
- Login returns valid JWT tokens
- Refresh token extends session
- Logout invalidates tokens
- Account lockout after failed attempts

## Success Criteria
- [ ] Registration creates all related entities
- [ ] Login returns valid access and refresh tokens
- [ ] Invalid credentials return 401
- [ ] Account locks after 5 failed attempts
- [ ] Refresh token generates new access token
- [ ] Logout invalidates refresh token

## Files to Create (Count: 10)
```
src/main/java/com/seffafbagis/api/
├── dto/
│   ├── request/auth/
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── RefreshTokenRequest.java
│   │   └── LogoutRequest.java
│   └── response/auth/
│       ├── AuthResponse.java
│       └── TokenResponse.java
├── service/auth/
│   ├── AuthService.java
│   └── JwtService.java
└── controller/auth/
    └── AuthController.java
```

---

# PHASE 7: AUTH MODULE - EXTENDED

## Overview
This phase extends authentication with password reset, email verification, and change password functionality.

## Problems Being Solved
- Users forget passwords and need reset mechanism
- Email addresses need verification
- Users need to change passwords securely
- Tokens must expire appropriately

## Components to Create

### 7.1 Additional Auth DTOs
**Location**: `src/main/java/com/seffafbagis/api/dto/request/auth/`

| DTO | Purpose |
|-----|---------|
| `ForgotPasswordRequest.java` | Email for password reset |
| `ResetPasswordRequest.java` | Token and new password |
| `ChangePasswordRequest.java` | Old password, new password |
| `VerifyEmailRequest.java` | Verification token |
| `ResendVerificationRequest.java` | Email for resending verification |

### 7.2 Password Reset Service
**Location**: `src/main/java/com/seffafbagis/api/service/auth/`

| Class | Purpose |
|-------|---------|
| `PasswordResetService.java` | Password reset flow management |
| `EmailVerificationService.java` | Email verification flow |

### 7.3 Email Service
**Location**: `src/main/java/com/seffafbagis/api/service/notification/`

| Class | Purpose |
|-------|---------|
| `EmailService.java` | Send transactional emails |

### 7.4 Email Templates
**Location**: `src/main/resources/templates/email/`

| Template | Purpose |
|----------|---------|
| `welcome.html` | Welcome email after registration |
| `password-reset.html` | Password reset link email |
| `email-verification.html` | Email verification link |

## Additional Auth Endpoints
```
POST /api/v1/auth/forgot-password     - Request password reset
POST /api/v1/auth/reset-password      - Reset password with token
POST /api/v1/auth/change-password     - Change password (authenticated)
POST /api/v1/auth/verify-email        - Verify email with token
POST /api/v1/auth/resend-verification - Resend verification email
```

## Password Reset Flow
```
1. User submits email via /forgot-password
2. System generates secure token
3. Token stored with expiry (1 hour)
4. Email sent with reset link
5. User clicks link, submits new password
6. Token validated and marked used
7. Password updated, all refresh tokens invalidated
8. User redirected to login
```

## Email Verification Flow
```
1. During registration, verification token created
2. Welcome email sent with verification link
3. User clicks link within 24 hours
4. Token validated
5. User email_verified = true
6. User status = ACTIVE
```

## Expected Outputs
- Password reset flow complete
- Email verification working
- Change password for logged-in users
- All emails sent and logged

## Success Criteria
- [ ] Forgot password sends email
- [ ] Reset password with valid token works
- [ ] Expired token rejected
- [ ] Email verification activates account
- [ ] Change password requires old password
- [ ] All refresh tokens invalidated on password change

## Files to Create (Count: 11)
```
src/main/java/com/seffafbagis/api/
├── dto/request/auth/
│   ├── ForgotPasswordRequest.java
│   ├── ResetPasswordRequest.java
│   ├── ChangePasswordRequest.java
│   ├── VerifyEmailRequest.java
│   └── ResendVerificationRequest.java
├── service/
│   ├── auth/
│   │   ├── PasswordResetService.java
│   │   └── EmailVerificationService.java
│   └── notification/
│       └── EmailService.java
└── resources/templates/email/
    ├── welcome.html
    ├── password-reset.html
    └── email-verification.html
```

---

# PHASE 8: USER MODULE - PROFILE & PREFERENCES

## Overview
This phase implements user profile management, preferences, and sensitive data handling with full KVKK compliance.

## Problems Being Solved
- Users need to view and update their profiles
- User preferences for notifications and privacy needed
- Sensitive data must be encrypted and KVKK compliant
- Users should be able to manage their own data

## Components to Create

### 8.1 User DTOs - Request
**Location**: `src/main/java/com/seffafbagis/api/dto/request/user/`

| DTO | Purpose |
|-----|---------|
| `UpdateProfileRequest.java` | Name, avatar, bio updates |
| `UpdatePreferencesRequest.java` | Notification, privacy settings |
| `UpdateSensitiveDataRequest.java` | TC, phone, address (KVKK) |
| `UpdateConsentRequest.java` | Marketing, third-party consents |
| `DeleteAccountRequest.java` | Account deletion confirmation |

### 8.2 User DTOs - Response
**Location**: `src/main/java/com/seffafbagis/api/dto/response/user/`

| DTO | Purpose |
|-----|---------|
| `UserResponse.java` | Basic user info |
| `UserProfileResponse.java` | Full profile details |
| `UserPreferenceResponse.java` | Current preferences |
| `UserSensitiveDataResponse.java` | Masked sensitive data |
| `UserDetailResponse.java` | Complete user information |

### 8.3 Mapper
**Location**: `src/main/java/com/seffafbagis/api/dto/mapper/`

| Class | Purpose |
|-------|---------|
| `UserMapper.java` | Entity to DTO conversions |

### 8.4 User Services
**Location**: `src/main/java/com/seffafbagis/api/service/user/`

| Class | Purpose |
|-------|---------|
| `UserService.java` | User CRUD operations |
| `UserProfileService.java` | Profile management |
| `UserPreferenceService.java` | Preference management |
| `SensitiveDataService.java` | Encrypted data management |

### 8.5 User Controllers
**Location**: `src/main/java/com/seffafbagis/api/controller/user/`

| Class | Purpose |
|-------|---------|
| `UserController.java` | General user operations |
| `UserProfileController.java` | Profile endpoints |
| `UserPreferenceController.java` | Preference endpoints |
| `SensitiveDataController.java` | KVKK data endpoints |

## User Endpoints
```
GET    /api/v1/users/me                    - Get current user
PUT    /api/v1/users/me                    - Update current user
DELETE /api/v1/users/me                    - Delete account

GET    /api/v1/users/me/profile            - Get profile
PUT    /api/v1/users/me/profile            - Update profile

GET    /api/v1/users/me/preferences        - Get preferences
PUT    /api/v1/users/me/preferences        - Update preferences

GET    /api/v1/users/me/sensitive-data     - Get masked sensitive data
PUT    /api/v1/users/me/sensitive-data     - Update sensitive data
PUT    /api/v1/users/me/consents           - Update KVKK consents
DELETE /api/v1/users/me/sensitive-data     - Delete sensitive data (KVKK right)
```

## Sensitive Data Handling
```
Input (Plain) → Validate → Encrypt (AES-256) → Store in DB
                                                    ↓
Response (Masked): TC: ***-***-**39, Phone: +90 *** *** ** 45
                                                    ↓
Full Decrypt: Only for authenticated user viewing own data
```

## KVKK Compliance Features
- Explicit consent tracking
- Right to access (GET sensitive data)
- Right to rectification (PUT sensitive data)
- Right to erasure (DELETE sensitive data)
- Consent withdrawal tracking

## Expected Outputs
- Profile management fully working
- Preferences update correctly
- Sensitive data encrypted in database
- KVKK rights implemented

## Success Criteria
- [ ] Profile updates persist correctly
- [ ] Preferences affect notification behavior
- [ ] Sensitive data encrypted at rest
- [ ] Masked data returned in responses
- [ ] Account deletion removes all data
- [ ] Consent dates tracked properly

## Files to Create (Count: 16)
```
src/main/java/com/seffafbagis/api/
├── dto/
│   ├── request/user/
│   │   ├── UpdateProfileRequest.java
│   │   ├── UpdatePreferencesRequest.java
│   │   ├── UpdateSensitiveDataRequest.java
│   │   ├── UpdateConsentRequest.java
│   │   └── DeleteAccountRequest.java
│   ├── response/user/
│   │   ├── UserResponse.java
│   │   ├── UserProfileResponse.java
│   │   ├── UserPreferenceResponse.java
│   │   ├── UserSensitiveDataResponse.java
│   │   └── UserDetailResponse.java
│   └── mapper/
│       └── UserMapper.java
├── service/user/
│   ├── UserService.java
│   ├── UserProfileService.java
│   ├── UserPreferenceService.java
│   └── SensitiveDataService.java
└── controller/user/
    ├── UserController.java
    ├── UserProfileController.java
    ├── UserPreferenceController.java
    └── SensitiveDataController.java
```

---

# PHASE 9: ADMIN MODULE - USER MANAGEMENT

## Overview
This phase implements admin functionality for managing users including listing, searching, status changes, and role management.

## Problems Being Solved
- Admins need to view all users
- Admins need to search and filter users
- Admins need to suspend/activate accounts
- Admins need to change user roles
- User management audit trail needed

## Components to Create

### 9.1 Admin DTOs
**Location**: `src/main/java/com/seffafbagis/api/dto/request/admin/`

| DTO | Purpose |
|-----|---------|
| `UpdateUserStatusRequest.java` | Status change with reason |
| `UpdateUserRoleRequest.java` | Role change |
| `UserSearchRequest.java` | Search/filter criteria |

**Location**: `src/main/java/com/seffafbagis/api/dto/response/admin/`

| DTO | Purpose |
|-----|---------|
| `AdminUserResponse.java` | User with admin-level details |
| `AdminUserListResponse.java` | Paginated user list |
| `AdminDashboardResponse.java` | Dashboard statistics |

### 9.2 Admin User Service
**Location**: `src/main/java/com/seffafbagis/api/service/admin/`

| Class | Purpose |
|-------|---------|
| `AdminUserService.java` | Admin user management operations |

### 9.3 Admin User Controller
**Location**: `src/main/java/com/seffafbagis/api/controller/admin/`

| Class | Purpose |
|-------|---------|
| `AdminUserController.java` | Admin user management endpoints |

## Admin User Endpoints
```
GET    /api/v1/admin/users                 - List all users (paginated)
GET    /api/v1/admin/users/search          - Search users
GET    /api/v1/admin/users/{id}            - Get user details
PUT    /api/v1/admin/users/{id}/status     - Update user status
PUT    /api/v1/admin/users/{id}/role       - Update user role
DELETE /api/v1/admin/users/{id}            - Delete user
GET    /api/v1/admin/dashboard             - Dashboard statistics
```

## User Status Transitions
```
PENDING_VERIFICATION → ACTIVE (email verified)
ACTIVE → SUSPENDED (admin action)
SUSPENDED → ACTIVE (admin action)
ACTIVE → INACTIVE (user request)
Any → DELETED (soft delete)
```

## Dashboard Statistics
- Total users by role
- New registrations (daily/weekly/monthly)
- Active users
- Suspended users
- Pending verifications

## Expected Outputs
- User listing with pagination
- Search and filtering working
- Status changes logged
- Role changes restricted appropriately

## Success Criteria
- [ ] Only ADMIN role can access endpoints
- [ ] User listing paginated correctly
- [ ] Search filters work
- [ ] Status changes create audit log
- [ ] Cannot delete last admin
- [ ] Dashboard stats accurate

## Files to Create (Count: 8)
```
src/main/java/com/seffafbagis/api/
├── dto/
│   ├── request/admin/
│   │   ├── UpdateUserStatusRequest.java
│   │   ├── UpdateUserRoleRequest.java
│   │   └── UserSearchRequest.java
│   └── response/admin/
│       ├── AdminUserResponse.java
│       ├── AdminUserListResponse.java
│       └── AdminDashboardResponse.java
├── service/admin/
│   └── AdminUserService.java
└── controller/admin/
    └── AdminUserController.java
```

---

# PHASE 10: ADMIN MODULE - ORGANIZATION & CAMPAIGN

## Overview
This phase implements admin functionality for organization verification and campaign approval. Uses interfaces to avoid dependency on Emir's code.

## Problems Being Solved
- Organizations need admin verification
- Campaigns need admin approval
- Rejection reasons must be tracked
- Reports/complaints need management
- Interface-based design for parallel development

## Components to Create

### 10.1 Service Interfaces (For Emir to Implement)
**Location**: `src/main/java/com/seffafbagis/api/service/interfaces/`

| Interface | Purpose |
|-----------|---------|
| `IOrganizationService.java` | Organization operations contract |
| `ICampaignService.java` | Campaign operations contract |

### 10.2 Admin DTOs
**Location**: `src/main/java/com/seffafbagis/api/dto/request/admin/`

| DTO | Purpose |
|-----|---------|
| `VerifyOrganizationRequest.java` | Approval/rejection with reason |
| `ApproveCampaignRequest.java` | Campaign approval/rejection |
| `ResolveReportRequest.java` | Report resolution |

### 10.3 Admin Services
**Location**: `src/main/java/com/seffafbagis/api/service/admin/`

| Class | Purpose |
|-------|---------|
| `AdminOrganizationService.java` | Admin org verification |
| `AdminCampaignService.java` | Admin campaign approval |
| `AdminReportService.java` | Report management |

### 10.4 Admin Controllers
**Location**: `src/main/java/com/seffafbagis/api/controller/admin/`

| Class | Purpose |
|-------|---------|
| `AdminOrganizationController.java` | Org verification endpoints |
| `AdminCampaignController.java` | Campaign approval endpoints |
| `AdminReportController.java` | Report management endpoints |

## Admin Organization Endpoints
```
GET    /api/v1/admin/organizations                    - List organizations
GET    /api/v1/admin/organizations/pending            - Pending verifications
GET    /api/v1/admin/organizations/{id}               - Organization details
PUT    /api/v1/admin/organizations/{id}/verify        - Verify organization
PUT    /api/v1/admin/organizations/{id}/reject        - Reject organization
```

## Admin Campaign Endpoints
```
GET    /api/v1/admin/campaigns                        - List campaigns
GET    /api/v1/admin/campaigns/pending                - Pending approvals
GET    /api/v1/admin/campaigns/{id}                   - Campaign details
PUT    /api/v1/admin/campaigns/{id}/approve           - Approve campaign
PUT    /api/v1/admin/campaigns/{id}/reject            - Reject campaign
```

## Admin Report Endpoints
```
GET    /api/v1/admin/reports                          - List reports
GET    /api/v1/admin/reports/{id}                     - Report details
PUT    /api/v1/admin/reports/{id}/assign              - Assign to admin
PUT    /api/v1/admin/reports/{id}/resolve             - Resolve report
```

## Interface Pattern for Parallel Development
```java
// Furkan defines interface
public interface IOrganizationService {
    OrganizationResponse getById(UUID id);
    Page<OrganizationResponse> getPendingVerifications(Pageable pageable);
    void verify(UUID id, VerifyOrganizationRequest request);
}

// Emir implements interface
@Service
public class OrganizationService implements IOrganizationService {
    // Implementation
}
```

## Expected Outputs
- Organization verification flow working
- Campaign approval flow working
- Report management functional
- Clear interfaces for Emir

## Success Criteria
- [ ] Interfaces clearly defined
- [ ] Admin can list pending organizations
- [ ] Verification updates status and timestamp
- [ ] Rejection stores reason
- [ ] Report assignment works
- [ ] Audit logs created for all admin actions

## Files to Create (Count: 12)
```
src/main/java/com/seffafbagis/api/
├── service/interfaces/
│   ├── IOrganizationService.java
│   └── ICampaignService.java
├── dto/request/admin/
│   ├── VerifyOrganizationRequest.java
│   ├── ApproveCampaignRequest.java
│   └── ResolveReportRequest.java
├── service/admin/
│   ├── AdminOrganizationService.java
│   ├── AdminCampaignService.java
│   └── AdminReportService.java
└── controller/admin/
    ├── AdminOrganizationController.java
    ├── AdminCampaignController.java
    └── AdminReportController.java
```

---

# PHASE 11: SYSTEM SETTINGS & FAVORITES

## Overview
This phase implements platform-wide system settings management and user favorite organizations feature.

## Problems Being Solved
- Platform needs configurable settings
- Settings should be changeable without deployment
- Users want to save favorite organizations
- Settings need caching for performance

## Components to Create

### 11.1 System Settings DTOs
**Location**: `src/main/java/com/seffafbagis/api/dto/request/system/`

| DTO | Purpose |
|-----|---------|
| `UpdateSettingRequest.java` | Setting update |
| `CreateSettingRequest.java` | New setting creation |

**Location**: `src/main/java/com/seffafbagis/api/dto/response/system/`

| DTO | Purpose |
|-----|---------|
| `SystemSettingResponse.java` | Single setting |
| `PublicSettingsResponse.java` | Public settings for frontend |

### 11.2 Favorites DTOs
**Location**: `src/main/java/com/seffafbagis/api/dto/response/favorite/`

| DTO | Purpose |
|-----|---------|
| `FavoriteOrganizationResponse.java` | Favorite org details |

### 11.3 Services
**Location**: `src/main/java/com/seffafbagis/api/service/`

| Class | Purpose |
|-------|---------|
| `system/SystemSettingService.java` | Settings CRUD with caching |
| `favorite/FavoriteOrganizationService.java` | Favorites management |

### 11.4 Controllers
**Location**: `src/main/java/com/seffafbagis/api/controller/`

| Class | Purpose |
|-------|---------|
| `system/SystemSettingController.java` | Settings endpoints |
| `favorite/FavoriteOrganizationController.java` | Favorites endpoints |

## System Settings Endpoints
```
GET    /api/v1/settings/public             - Public settings (no auth)
GET    /api/v1/admin/settings              - All settings (admin)
GET    /api/v1/admin/settings/{key}        - Single setting
PUT    /api/v1/admin/settings/{key}        - Update setting
POST   /api/v1/admin/settings              - Create setting
DELETE /api/v1/admin/settings/{key}        - Delete setting
```

## Favorites Endpoints
```
GET    /api/v1/users/me/favorites          - Get user's favorites
POST   /api/v1/users/me/favorites/{orgId}  - Add favorite
DELETE /api/v1/users/me/favorites/{orgId}  - Remove favorite
GET    /api/v1/users/me/favorites/check/{orgId} - Check if favorited
```

## Default System Settings
```
platform_name: "Şeffaf Bağış Platformu"
min_donation_amount: 10
max_donation_amount: 1000000
evidence_deadline_days: 15
transparency_score_threshold: 40
commission_rate: 0
maintenance_mode: false
```

## Caching Strategy
- Settings cached in Redis with 1-hour TTL
- Cache invalidated on setting update
- Public settings cached separately

## Expected Outputs
- System settings configurable
- Caching working for performance
- Favorites feature functional

## Success Criteria
- [ ] Public settings accessible without auth
- [ ] Admin can manage all settings
- [ ] Cache updates on setting change
- [ ] Favorites add/remove works
- [ ] Duplicate favorites prevented

## Files to Create (Count: 10)
```
src/main/java/com/seffafbagis/api/
├── dto/
│   ├── request/system/
│   │   ├── UpdateSettingRequest.java
│   │   └── CreateSettingRequest.java
│   └── response/
│       ├── system/
│       │   ├── SystemSettingResponse.java
│       │   └── PublicSettingsResponse.java
│       └── favorite/
│           └── FavoriteOrganizationResponse.java
├── service/
│   ├── system/
│   │   └── SystemSettingService.java
│   └── favorite/
│       └── FavoriteOrganizationService.java
└── controller/
    ├── system/
    │   └── SystemSettingController.java
    └── favorite/
        └── FavoriteOrganizationController.java
```

---

# PHASE 12: AUDIT & LOGGING

## Overview
This phase implements comprehensive audit logging for KVKK compliance and email delivery logging.

## Problems Being Solved
- All data access must be logged (KVKK)
- Admin actions need audit trail
- Email delivery needs tracking
- Login history needs management
- Logs need retention policy

## Components to Create

### 12.1 Audit DTOs
**Location**: `src/main/java/com/seffafbagis/api/dto/response/audit/`

| DTO | Purpose |
|-----|---------|
| `AuditLogResponse.java` | Audit log entry |
| `AuditLogListResponse.java` | Paginated audit logs |
| `LoginHistoryResponse.java` | Login history entry |

### 12.2 Audit Service
**Location**: `src/main/java/com/seffafbagis/api/service/audit/`

| Class | Purpose |
|-------|---------|
| `AuditLogService.java` | Audit log creation and queries |
| `LoginHistoryService.java` | Login history management |

### 12.3 Audit Controller
**Location**: `src/main/java/com/seffafbagis/api/controller/audit/`

| Class | Purpose |
|-------|---------|
| `AuditLogController.java` | Audit log viewing (admin) |

### 12.4 Email Log Service
**Location**: `src/main/java/com/seffafbagis/api/service/notification/`

| Class | Purpose |
|-------|---------|
| `EmailLogService.java` | Email log management |

### 12.5 Aspect for Automatic Auditing
**Location**: `src/main/java/com/seffafbagis/api/aspect/`

| Class | Purpose |
|-------|---------|
| `AuditAspect.java` | AOP aspect for automatic auditing |

## Audit Log Endpoints
```
GET    /api/v1/admin/audit-logs            - List audit logs (admin)
GET    /api/v1/admin/audit-logs/user/{id}  - Logs for specific user
GET    /api/v1/admin/audit-logs/entity/{type}/{id} - Logs for entity
GET    /api/v1/users/me/login-history      - User's own login history
GET    /api/v1/admin/login-history/{userId} - User login history (admin)
```

## Audit Actions to Track
```
USER_LOGIN, USER_LOGOUT, USER_REGISTER
USER_UPDATE, USER_DELETE, USER_STATUS_CHANGE
SENSITIVE_DATA_ACCESS, SENSITIVE_DATA_UPDATE
ORGANIZATION_VERIFY, ORGANIZATION_REJECT
CAMPAIGN_APPROVE, CAMPAIGN_REJECT
DONATION_CREATE, REFUND_PROCESS
ADMIN_ACTION, SETTING_UPDATE
```

## Audit Log Structure
```json
{
  "id": "uuid",
  "userId": "uuid",
  "action": "USER_LOGIN",
  "entityType": "user",
  "entityId": "uuid",
  "oldValues": {},
  "newValues": {},
  "ipAddress": "192.168.1.1",
  "userAgent": "Mozilla/5.0...",
  "requestId": "req-123",
  "sessionId": "sess-456",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

## Expected Outputs
- All critical actions logged
- Audit logs queryable
- Login history tracked
- Email delivery logged

## Success Criteria
- [ ] Login creates audit log
- [ ] Sensitive data access logged
- [ ] Admin actions logged
- [ ] Logs queryable by various criteria
- [ ] Login history shows all attempts

## Files to Create (Count: 9)
```
src/main/java/com/seffafbagis/api/
├── dto/response/audit/
│   ├── AuditLogResponse.java
│   ├── AuditLogListResponse.java
│   └── LoginHistoryResponse.java
├── service/
│   ├── audit/
│   │   ├── AuditLogService.java
│   │   └── LoginHistoryService.java
│   └── notification/
│       └── EmailLogService.java
├── controller/audit/
│   └── AuditLogController.java
└── aspect/
    └── AuditAspect.java
```

---

# PHASE 13: UTILITY CLASSES

## Overview
This phase implements various utility classes used across the application.

## Problems Being Solved
- URL-friendly slugs needed for campaigns
- Unique reference codes for bank transfers
- Date formatting and manipulation
- Receipt number generation

## Components to Create

### 13.1 Utility Classes
**Location**: `src/main/java/com/seffafbagis/api/util/`

| Class | Purpose |
|-------|---------|
| `SlugGenerator.java` | URL-friendly slug from Turkish text |
| `ReferenceCodeGenerator.java` | Bank transfer reference codes |
| `DateUtils.java` | Date formatting and helpers |
| `ReceiptNumberGenerator.java` | Donation receipt numbers |

## Slug Generator Features
- Converts Turkish characters (ş→s, ğ→g, etc.)
- Removes special characters
- Converts spaces to hyphens
- Ensures uniqueness with suffix if needed

## Reference Code Format
```
SBP-YYYYMMDD-XXXXX
SBP = Şeffaf Bağış Platformu
YYYYMMDD = Date
XXXXX = Random alphanumeric
```

## Receipt Number Format
```
RCPT-2024-000001
RCPT-2024-000002
Sequential per year
```

## Expected Outputs
- All utilities working correctly
- Slugs properly formatted
- Reference codes unique
- Receipt numbers sequential

## Success Criteria
- [ ] Turkish characters converted correctly
- [ ] Slugs are URL-safe
- [ ] Reference codes unique
- [ ] Receipt numbers sequential
- [ ] Date utils handle timezone correctly

## Files to Create (Count: 4)
```
src/main/java/com/seffafbagis/api/util/
├── SlugGenerator.java
├── ReferenceCodeGenerator.java
├── DateUtils.java
└── ReceiptNumberGenerator.java
```

---

# PHASE 14: INTEGRATION & FINAL TESTING

## Overview
This phase focuses on integration testing, bug fixes, and final documentation.

## Problems Being Solved
- Components need integration testing
- Edge cases need handling
- Documentation needs completion
- Performance optimization needed

## Tasks to Complete

### 14.1 Integration Tests
**Location**: `src/test/java/com/seffafbagis/api/integration/`

| Test Class | Purpose |
|------------|---------|
| `AuthIntegrationTest.java` | Full auth flow testing |
| `UserIntegrationTest.java` | User management testing |
| `AdminIntegrationTest.java` | Admin operations testing |

### 14.2 Documentation
**Location**: `docs/`

| Document | Purpose |
|----------|---------|
| `API.md` | API documentation |
| `SECURITY.md` | Security implementation details |
| `KVKK.md` | KVKK compliance documentation |

### 14.3 Performance Optimization
- Review N+1 queries
- Add missing indexes
- Optimize hot paths
- Review caching strategy

### 14.4 Bug Fixes
- Address issues found during testing
- Handle edge cases
- Improve error messages

## Test Scenarios
```
Auth Flow:
- Register → Verify Email → Login → Refresh → Logout
- Register → Failed Login (5x) → Account Locked → Unlock

User Flow:
- Update Profile → Verify Changes
- Add Sensitive Data → Verify Encryption → Retrieve Masked

Admin Flow:
- List Users → Search → Update Status → Verify Audit Log
```

## Expected Outputs
- All tests passing
- Documentation complete
- No critical bugs
- Performance acceptable

## Success Criteria
- [ ] All integration tests pass
- [ ] API documentation complete
- [ ] No security vulnerabilities
- [ ] Response times under 500ms
- [ ] All audit logs properly created

## Files to Create (Count: 6+)
```
src/test/java/com/seffafbagis/api/integration/
├── AuthIntegrationTest.java
├── UserIntegrationTest.java
└── AdminIntegrationTest.java

docs/
├── API.md
├── SECURITY.md
└── KVKK.md
```

---

## COMPLETE FILE COUNT SUMMARY

| Phase | Files to Create |
|-------|-----------------|
| Phase 0 | 1 (migration SQL) |
| Phase 1 | 11 |
| Phase 2 | 7 |
| Phase 3 | 11 |
| Phase 4 | 21 |
| Phase 5 | 6 |
| Phase 6 | 10 |
| Phase 7 | 11 |
| Phase 8 | 16 |
| Phase 9 | 8 |
| Phase 10 | 12 |
| Phase 11 | 10 |
| Phase 12 | 9 |
| Phase 13 | 4 |
| Phase 14 | 6+ |
| **TOTAL** | **~143 files** |

---

## DEPENDENCY GRAPH

```
Phase 0 (Database)
    ↓
Phase 1 (Config)
    ↓
    ├── Phase 2 (Security) ──┐
    │       ↓               │
    │   Phase 3 (Exception) │
    │       ↓               │
    │   Phase 4 (Entity)    │
    │       ↓               │
    │   Phase 5 (Encryption)│
    │       ↓               │
    └───────┴───────────────┘
            ↓
    Phase 6 (Auth Core)
            ↓
    Phase 7 (Auth Extended)
            ↓
    Phase 8 (User Module)
            ↓
    ├── Phase 9 (Admin User)
    │       ↓
    │   Phase 10 (Admin Org/Campaign)
    │
    ├── Phase 11 (Settings/Favorites)
    │
    └── Phase 12 (Audit)
            ↓
    Phase 13 (Utilities) ← Can be done in parallel
            ↓
    Phase 14 (Integration)
```

---

## NOTES FOR PROMPT CREATION

1. Each phase prompt will be created as a separate file
2. Prompts will be in English
3. No code in prompts (except Phase 0 for database)
4. Each prompt will include:
   - Context and background
   - Objective
   - Detailed requirements
   - File structure
   - Testing requirements
   - Success criteria
   - Result file requirements
5. Result files will be created at `docs/Furkan/step_results/phase_X_result.md`

---

## READY FOR PROMPT CREATION

With this phase breakdown approved, we can proceed to create individual prompts for each phase, starting with Phase 1.
