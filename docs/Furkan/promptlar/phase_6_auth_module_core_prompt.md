# PHASE 6: AUTH MODULE - CORE

## CONTEXT AND BACKGROUND

You are working on "Şeffaf Bağış Platformu" (Transparent Donation Platform), a graduation project for Fatih Sultan Mehmet Vakıf University. This is a comprehensive donation platform for Turkey that emphasizes transparency and accountability in charitable giving.

### Project Overview
- **Technology Stack**: Java 17, Spring Boot 3.x, PostgreSQL 15+, Redis, Maven
- **Purpose**: Enable foundations to create campaigns, donors to contribute, and maintain transparency through evidence uploads and scoring systems
- **Compliance Requirements**: KVKK (Turkish Personal Data Protection Law) compliant
- **Developer**: Furkan (responsible for Infrastructure, Auth, User, Admin modules - ~58% of backend)

### Current State
- Phase 0 (Database Migration) has been completed
- Phase 1 (Project Foundation & Configuration) has been completed
- Phase 2 (Security Infrastructure) has been completed - JWT components ready
- Phase 3 (Exception Handling & Common DTOs) has been completed
- Phase 4 (User Entity & Repository Layer) has been completed - All entities and repositories ready
- Phase 5 (Encryption & Security Utilities) has been completed - Validators and encryption ready
- All infrastructure is in place for authentication

### What This Phase Accomplishes
This phase implements the CORE authentication functionality: user registration, login, logout, and JWT token refresh. This is the primary entry point for users into the system. After this phase, users will be able to create accounts, authenticate, and maintain sessions.

---

## OBJECTIVE

Create the complete core authentication module including:
1. Auth DTOs for request/response
2. AuthService with login, register, logout, refresh logic
3. AuthController with REST endpoints
4. Login history tracking
5. Account lockout mechanism

---

## IMPORTANT RULES

### Code Style Requirements
- Use clear, readable code - prefer if-else statements over ternary operators
- Follow SOLID principles
- Add comprehensive comments in English
- Use meaningful variable and method names
- No complex one-liners - prioritize readability over brevity

### Security Requirements
- Hash passwords with BCrypt before storing
- Generate secure random tokens for refresh tokens
- Store only token hashes in database
- Implement account lockout after failed attempts
- Track all login attempts in login_history
- Return consistent error messages (prevent user enumeration)
- Never log passwords or tokens

### Authentication Flow Requirements
- Use stateless JWT authentication
- Access tokens expire in 15 minutes
- Refresh tokens expire in 7 days
- Refresh tokens are stored in database (can be revoked)
- Users can have multiple active sessions (multiple refresh tokens)

---

## DETAILED REQUIREMENTS

### 1. Auth DTOs - Request

#### 1.1 LoginRequest.java
**Location**: `src/main/java/com/seffafbagis/api/dto/request/auth/LoginRequest.java`

**Purpose**: Request body for user login

**Fields**:

| Field | Type | Validation | Description |
|-------|------|------------|-------------|
| email | String | @NotBlank, @Email | User's email address |
| password | String | @NotBlank | User's password |
| rememberMe | Boolean | Optional, default false | Extended session duration |
| deviceInfo | String | Optional, @Size(max=255) | Device/browser information |

**Notes**:
- Use Jakarta Validation annotations
- Add @Schema annotations for Swagger documentation
- Include default constructor and all-args constructor
- Add getters and setters

---

#### 1.2 RegisterRequest.java
**Location**: `src/main/java/com/seffafbagis/api/dto/request/auth/RegisterRequest.java`

**Purpose**: Request body for user registration

**Fields**:

| Field | Type | Validation | Description |
|-------|------|------------|-------------|
| email | String | @NotBlank, @Email, @Size(max=255) | User's email address |
| password | String | @NotBlank, @Size(min=8, max=128) | User's password |
| confirmPassword | String | @NotBlank | Password confirmation |
| role | UserRole | @NotNull | DONOR or FOUNDATION |
| firstName | String | Optional, @Size(max=100) | First name |
| lastName | String | Optional, @Size(max=100) | Last name |
| acceptTerms | Boolean | Must be true | Terms acceptance |
| acceptPrivacyPolicy | Boolean | Must be true | Privacy policy acceptance |
| acceptKvkk | Boolean | Must be true | KVKK consent |

**Custom Validation**:
- Password must match confirmPassword
- Role must be DONOR or FOUNDATION (not ADMIN or BENEFICIARY)
- All acceptance fields must be true

**Notes**:
- Consider creating a custom validator for password matching
- ADMIN accounts cannot be created through registration

---

#### 1.3 RefreshTokenRequest.java
**Location**: `src/main/java/com/seffafbagis/api/dto/request/auth/RefreshTokenRequest.java`

**Purpose**: Request body for refreshing access token

**Fields**:

| Field | Type | Validation | Description |
|-------|------|------------|-------------|
| refreshToken | String | @NotBlank | The refresh token |

---

#### 1.4 LogoutRequest.java
**Location**: `src/main/java/com/seffafbagis/api/dto/request/auth/LogoutRequest.java`

**Purpose**: Request body for logout

**Fields**:

| Field | Type | Validation | Description |
|-------|------|------------|-------------|
| refreshToken | String | @NotBlank | The refresh token to revoke |
| logoutAllDevices | Boolean | Optional, default false | Revoke all refresh tokens |

---

### 2. Auth DTOs - Response

#### 2.1 AuthResponse.java
**Location**: `src/main/java/com/seffafbagis/api/dto/response/auth/AuthResponse.java`

**Purpose**: Response body for successful login/register

**Fields**:

| Field | Type | Description |
|-------|------|-------------|
| accessToken | String | JWT access token |
| refreshToken | String | JWT refresh token |
| tokenType | String | Always "Bearer" |
| expiresIn | Long | Access token expiry in seconds |
| user | UserSummary | Basic user information |

**UserSummary Nested Class**:

| Field | Type | Description |
|-------|------|-------------|
| id | UUID | User ID |
| email | String | User email |
| role | UserRole | User role |
| firstName | String | First name (nullable) |
| lastName | String | Last name (nullable) |
| displayName | String | Display name (nullable) |
| avatarUrl | String | Avatar URL (nullable) |
| emailVerified | Boolean | Email verification status |

**Static Factory Method**:
- `of(String accessToken, String refreshToken, long expiresIn, User user, UserProfile profile)`

---

#### 2.2 TokenResponse.java
**Location**: `src/main/java/com/seffafbagis/api/dto/response/auth/TokenResponse.java`

**Purpose**: Response body for token refresh

**Fields**:

| Field | Type | Description |
|-------|------|-------------|
| accessToken | String | New JWT access token |
| refreshToken | String | New JWT refresh token (rotated) |
| tokenType | String | Always "Bearer" |
| expiresIn | Long | Access token expiry in seconds |

**Notes**:
- Refresh token rotation: issue new refresh token on each refresh
- Old refresh token is revoked after use

---

### 3. Auth Service

#### 3.1 AuthService.java
**Location**: `src/main/java/com/seffafbagis/api/service/auth/AuthService.java`

**Purpose**: Core authentication business logic

**Dependencies** (inject via constructor):
- UserRepository
- UserProfileRepository
- UserSensitiveDataRepository
- UserPreferenceRepository
- RefreshTokenRepository
- LoginHistoryRepository
- PasswordEncoder
- JwtTokenProvider
- TokenUtils
- DateUtils

**Methods**:

---

**`register(RegisterRequest request, String ipAddress, String userAgent)`**

**Purpose**: Register a new user

**Flow**:

Step 1: Validate request
- Check password matches confirmPassword
- Validate password strength using PasswordValidator
- Check role is DONOR or FOUNDATION (not ADMIN)
- Verify all acceptance flags are true

Step 2: Check email uniqueness
- Call userRepository.existsByEmail()
- If exists, throw ConflictException("User", "email", request.getEmail())

Step 3: Create User entity
- Set email (lowercase, trimmed)
- Set passwordHash using passwordEncoder.encode()
- Set role from request
- Set status to PENDING_VERIFICATION
- Set emailVerified to false
- Set createdAt and updatedAt

Step 4: Create UserProfile entity
- Set firstName, lastName from request
- Set displayName (combine first + last, or use email prefix)
- Set default language "tr"
- Set default timezone "Europe/Istanbul"

Step 5: Create UserSensitiveData entity
- Set dataProcessingConsent to true
- Set consentDate to now
- All encrypted fields remain null

Step 6: Create UserPreference entity
- Set defaults (email notifications true, etc.)

Step 7: Save all entities
- Save User (cascades to related entities if configured)
- Or save each entity separately

Step 8: Create email verification token
- Generate token using TokenUtils.generateSecureToken()
- Hash token using TokenUtils.generateTokenHash()
- Create EmailVerificationToken entity
- Set expiry to 24 hours from now
- Save token

Step 9: Log registration audit
- Create audit log entry (or let AuditAspect handle it)

Step 10: Send verification email (placeholder)
- TODO: Will be implemented in Phase 7
- For now, log that email should be sent

Step 11: Return success
- Do NOT return tokens yet (email not verified)
- Return a success message indicating verification email sent

**Returns**: ApiResponse with success message

**Exceptions**:
- ConflictException if email exists
- BadRequestException if validation fails

---

**`login(LoginRequest request, String ipAddress, String userAgent)`**

**Purpose**: Authenticate user and return tokens

**Flow**:

Step 1: Find user by email
- Call userRepository.findByEmail(request.getEmail().toLowerCase())
- If not found, record failed login attempt, throw UnauthorizedException
- IMPORTANT: Use same error message whether email exists or not (prevent enumeration)

Step 2: Check account status
- If status is SUSPENDED, throw UnauthorizedException("Account is suspended")
- If status is INACTIVE, throw UnauthorizedException("Account is inactive")

Step 3: Check account lockout
- If user.isAccountLocked(), throw UnauthorizedException with lockout message
- Include remaining lockout time in message

Step 4: Verify password
- Use passwordEncoder.matches(request.getPassword(), user.getPasswordHash())
- If no match:
  - Increment failedLoginAttempts
  - If attempts >= 5, lock account for 15 minutes
  - Record failed login in login_history
  - Save user
  - Throw UnauthorizedException (generic message)

Step 5: Check email verification
- If emailVerified is false, throw UnauthorizedException("Please verify your email")

Step 6: Successful login
- Reset failedLoginAttempts to 0
- Set lastLoginAt to now
- Save user

Step 7: Generate tokens
- Create CustomUserDetails from user
- Generate access token using jwtTokenProvider.generateAccessToken()
- Generate refresh token using jwtTokenProvider.generateRefreshToken()

Step 8: Store refresh token
- Hash the refresh token
- Create RefreshToken entity
- Set expiry (7 days, or longer if rememberMe)
- Set deviceInfo and ipAddress
- Save to database

Step 9: Record successful login
- Create LoginHistory entry with status "success"
- Save to database

Step 10: Load user profile
- Fetch UserProfile for response

Step 11: Return AuthResponse
- Include tokens and user summary

**Returns**: AuthResponse

**Exceptions**:
- UnauthorizedException for all auth failures (generic message)

---

**`refreshToken(RefreshTokenRequest request, String ipAddress)`**

**Purpose**: Issue new tokens using refresh token

**Flow**:

Step 1: Hash the provided refresh token
- Use TokenUtils.generateTokenHash()

Step 2: Find refresh token in database
- Call refreshTokenRepository.findByTokenHash()
- If not found, throw UnauthorizedException("Invalid refresh token")

Step 3: Validate refresh token
- Check if expired: throw UnauthorizedException("Refresh token expired")
- Check if revoked: throw UnauthorizedException("Refresh token revoked")

Step 4: Load user
- Get user from refresh token entity
- Verify user is still active

Step 5: Revoke old refresh token
- Set revokedAt to now
- Save refresh token

Step 6: Generate new tokens
- Generate new access token
- Generate new refresh token

Step 7: Store new refresh token
- Create new RefreshToken entity
- Copy deviceInfo from old token
- Update ipAddress
- Save to database

Step 8: Return TokenResponse
- Include new tokens

**Returns**: TokenResponse

**Exceptions**:
- UnauthorizedException for invalid/expired tokens

---

**`logout(LogoutRequest request, UUID userId)`**

**Purpose**: Revoke refresh token(s) to end session(s)

**Flow**:

Step 1: Hash the provided refresh token
- Use TokenUtils.generateTokenHash()

Step 2: Find and validate refresh token
- Must exist and belong to the requesting user
- If not found or doesn't belong to user, throw BadRequestException

Step 3: Revoke token(s)
- If logoutAllDevices is true:
  - Revoke all refresh tokens for user
  - Call refreshTokenRepository.revokeAllByUser(userId)
- Else:
  - Revoke only the provided token
  - Set revokedAt to now, save

Step 4: Return success
- Return ApiResponse with success message

**Returns**: ApiResponse

---

**`recordLoginAttempt(User user, String status, String ipAddress, String userAgent, String failureReason)`**

**Purpose**: Helper method to record login attempts

**Flow**:
- Create LoginHistory entity
- Set all fields including device type detection
- Save to database

**Device Type Detection**:
- Parse userAgent to determine: desktop, mobile, tablet
- Simple heuristic based on common patterns

---

### 4. Auth Controller

#### 4.1 AuthController.java
**Location**: `src/main/java/com/seffafbagis/api/controller/auth/AuthController.java`

**Purpose**: REST endpoints for authentication

**Class Annotations**:
- @RestController
- @RequestMapping("/api/v1/auth")
- @Tag(name = "Authentication", description = "User authentication endpoints")
- @Slf4j

**Dependencies**:
- AuthService
- HttpServletRequest (for IP and User-Agent extraction)

**Endpoints**:

---

**POST /api/v1/auth/register**

**Purpose**: Register a new user

**Annotations**:
- @PostMapping("/register")
- @Operation(summary = "Register a new user")
- @ApiResponses for 201, 400, 409

**Parameters**:
- @Valid @RequestBody RegisterRequest request
- HttpServletRequest httpRequest

**Logic**:
- Extract IP address from request
- Extract User-Agent header
- Call authService.register()
- Return ResponseEntity with status 201 (CREATED)

**Response**: ApiResponse<String> with success message

---

**POST /api/v1/auth/login**

**Purpose**: Authenticate user and get tokens

**Annotations**:
- @PostMapping("/login")
- @Operation(summary = "User login")
- @ApiResponses for 200, 401

**Parameters**:
- @Valid @RequestBody LoginRequest request
- HttpServletRequest httpRequest

**Logic**:
- Extract IP address from request
- Extract User-Agent header
- Call authService.login()
- Return ResponseEntity with status 200

**Response**: ApiResponse<AuthResponse>

---

**POST /api/v1/auth/refresh**

**Purpose**: Refresh access token

**Annotations**:
- @PostMapping("/refresh")
- @Operation(summary = "Refresh access token")
- @ApiResponses for 200, 401

**Parameters**:
- @Valid @RequestBody RefreshTokenRequest request
- HttpServletRequest httpRequest

**Logic**:
- Extract IP address
- Call authService.refreshToken()
- Return ResponseEntity with status 200

**Response**: ApiResponse<TokenResponse>

---

**POST /api/v1/auth/logout**

**Purpose**: Logout user (revoke refresh token)

**Annotations**:
- @PostMapping("/logout")
- @Operation(summary = "User logout")
- @ApiResponses for 200, 400

**Security**: Requires authentication

**Parameters**:
- @Valid @RequestBody LogoutRequest request
- @AuthenticationPrincipal CustomUserDetails userDetails

**Logic**:
- Get userId from userDetails
- Call authService.logout()
- Return ResponseEntity with status 200

**Response**: ApiResponse<String> with success message

---

**Helper Methods**:

`extractIpAddress(HttpServletRequest request)`:
- Check X-Forwarded-For header first (for proxied requests)
- Fall back to request.getRemoteAddr()
- Handle comma-separated list in X-Forwarded-For

`extractUserAgent(HttpServletRequest request)`:
- Get User-Agent header
- Return empty string if null

---

## FILE STRUCTURE

After completing this phase, the following files should exist:

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
│   └── AuthService.java
└── controller/auth/
    └── AuthController.java
```

**Total Files**: 8

---

## AUTHENTICATION FLOW DIAGRAMS

### Registration Flow
```
┌─────────────────────────────────────────────────────────────────────┐
│                      REGISTRATION FLOW                               │
└─────────────────────────────────────────────────────────────────────┘

Client                          AuthController                    AuthService
   │                                  │                                │
   │  POST /api/v1/auth/register     │                                │
   │  {email, password, role, ...}   │                                │
   │────────────────────────────────>│                                │
   │                                  │   register(request, ip, ua)   │
   │                                  │───────────────────────────────>│
   │                                  │                                │
   │                                  │         ┌─────────────────────┐│
   │                                  │         │ 1. Validate request ││
   │                                  │         │ 2. Check email      ││
   │                                  │         │ 3. Create User      ││
   │                                  │         │ 4. Create Profile   ││
   │                                  │         │ 5. Create Sensitive ││
   │                                  │         │ 6. Create Prefs     ││
   │                                  │         │ 7. Create VerifyTkn ││
   │                                  │         │ 8. (Send Email)     ││
   │                                  │         └─────────────────────┘│
   │                                  │                                │
   │                                  │<───────────────────────────────│
   │  201 Created                    │         Success Message        │
   │  {success: true, message: "..."} │                                │
   │<─────────────────────────────────│                                │
```

### Login Flow
```
┌─────────────────────────────────────────────────────────────────────┐
│                         LOGIN FLOW                                   │
└─────────────────────────────────────────────────────────────────────┘

Client                          AuthController                    AuthService
   │                                  │                                │
   │  POST /api/v1/auth/login        │                                │
   │  {email, password}              │                                │
   │────────────────────────────────>│                                │
   │                                  │   login(request, ip, ua)      │
   │                                  │───────────────────────────────>│
   │                                  │                                │
   │                                  │         ┌─────────────────────┐│
   │                                  │         │ 1. Find user        ││
   │                                  │         │ 2. Check status     ││
   │                                  │         │ 3. Check lockout    ││
   │                                  │         │ 4. Verify password  ││
   │                                  │         │ 5. Check email ver. ││
   │                                  │         │ 6. Reset attempts   ││
   │                                  │         │ 7. Generate tokens  ││
   │                                  │         │ 8. Store refresh    ││
   │                                  │         │ 9. Record login     ││
   │                                  │         └─────────────────────┘│
   │                                  │                                │
   │                                  │<───────────────────────────────│
   │  200 OK                         │         AuthResponse           │
   │  {accessToken, refreshToken,    │                                │
   │   user: {...}}                  │                                │
   │<─────────────────────────────────│                                │
```

### Token Refresh Flow
```
┌─────────────────────────────────────────────────────────────────────┐
│                      TOKEN REFRESH FLOW                              │
└─────────────────────────────────────────────────────────────────────┘

Client                          AuthController                    AuthService
   │                                  │                                │
   │  POST /api/v1/auth/refresh      │                                │
   │  {refreshToken}                 │                                │
   │────────────────────────────────>│                                │
   │                                  │   refreshToken(request, ip)   │
   │                                  │───────────────────────────────>│
   │                                  │                                │
   │                                  │         ┌─────────────────────┐│
   │                                  │         │ 1. Hash token       ││
   │                                  │         │ 2. Find in DB       ││
   │                                  │         │ 3. Validate         ││
   │                                  │         │ 4. Revoke old       ││
   │                                  │         │ 5. Generate new     ││
   │                                  │         │ 6. Store new        ││
   │                                  │         └─────────────────────┘│
   │                                  │                                │
   │                                  │<───────────────────────────────│
   │  200 OK                         │         TokenResponse          │
   │  {accessToken, refreshToken}    │                                │
   │<─────────────────────────────────│                                │
```

---

## ACCOUNT LOCKOUT MECHANISM

### Lockout Rules
- After 5 consecutive failed login attempts, account is locked
- Lockout duration: 15 minutes
- Lockout is per-account, not per-IP
- Successful login resets failed attempt counter
- Admin can manually unlock accounts

### Implementation Details
```
On Failed Login:
1. Increment user.failedLoginAttempts
2. If failedLoginAttempts >= 5:
   - Set user.lockedUntil = now + 15 minutes
   - Log security event
3. Record in login_history with status "failed" or "blocked"
4. Save user

On Successful Login:
1. Check if lockedUntil > now, if so reject
2. Reset failedLoginAttempts to 0
3. Clear lockedUntil
4. Record in login_history with status "success"
5. Save user
```

---

## TESTING REQUIREMENTS

After implementation, verify:

### 1. Registration Tests

**Test: Successful Registration**
- Register with valid data
- Verify user is created with status PENDING_VERIFICATION
- Verify profile, sensitive data, preferences are created
- Verify email verification token is created

**Test: Duplicate Email**
- Register with existing email
- Verify 409 Conflict response

**Test: Invalid Password**
- Register with weak password
- Verify 400 Bad Request with validation message

**Test: Password Mismatch**
- Register with mismatched passwords
- Verify 400 Bad Request

**Test: Invalid Role**
- Try to register as ADMIN
- Verify 400 Bad Request

### 2. Login Tests

**Test: Successful Login**
- Login with valid credentials
- Verify access token and refresh token returned
- Verify user info in response
- Verify login history recorded

**Test: Invalid Email**
- Login with non-existent email
- Verify 401 Unauthorized (generic message)

**Test: Invalid Password**
- Login with wrong password
- Verify 401 Unauthorized
- Verify failedLoginAttempts incremented

**Test: Account Lockout**
- Fail login 5 times
- Verify account is locked
- Verify 6th attempt returns lockout message
- Wait 15 minutes (or mock time)
- Verify login works again

**Test: Unverified Email**
- Login with unverified account
- Verify 401 with "verify email" message

**Test: Suspended Account**
- Login with suspended account
- Verify 401 with "suspended" message

### 3. Token Refresh Tests

**Test: Successful Refresh**
- Refresh with valid token
- Verify new tokens returned
- Verify old token is revoked
- Verify new token is stored

**Test: Invalid Token**
- Refresh with invalid token
- Verify 401 Unauthorized

**Test: Expired Token**
- Refresh with expired token
- Verify 401 Unauthorized

**Test: Revoked Token**
- Refresh with revoked token
- Verify 401 Unauthorized

### 4. Logout Tests

**Test: Successful Logout**
- Logout with valid token
- Verify token is revoked
- Verify token cannot be used for refresh

**Test: Logout All Devices**
- Have multiple refresh tokens
- Logout with logoutAllDevices = true
- Verify all tokens are revoked

---

## SUCCESS CRITERIA

Phase 6 is considered successful when:

1. ✅ All 8 files are created in correct locations
2. ✅ Registration creates user with all related entities
3. ✅ Registration enforces password strength
4. ✅ Registration prevents duplicate emails
5. ✅ Login validates credentials correctly
6. ✅ Login implements account lockout
7. ✅ Login records login history
8. ✅ JWT tokens are generated correctly
9. ✅ Refresh token rotation works
10. ✅ Logout revokes tokens
11. ✅ Error messages don't leak information
12. ✅ All endpoints return proper response format

---

## RESULT FILE REQUIREMENTS

After completing this phase, create a result file at:
`docs/Furkan/step_results/phase_6_result.md`

The result file must include:

1. **Execution Status**: Success or Failure
2. **Files Created**: List all 8 files with their paths
3. **Registration Tests**:
   - Successful registration result
   - Duplicate email handling
   - Validation error handling
4. **Login Tests**:
   - Successful login with token samples
   - Invalid credentials handling
   - Account lockout verification
5. **Token Tests**:
   - Refresh flow verification
   - Token revocation verification
6. **Security Verification**:
   - Confirm passwords are hashed
   - Confirm tokens are hashed in DB
   - Confirm no information leakage
7. **Issues Encountered**: Any problems and how they were resolved
8. **Notes for Next Phase**: Observations relevant to Phase 7

---

## NOTES

- This phase is critical - authentication affects everything
- Test thoroughly with various scenarios
- Ensure error messages are consistent (prevent enumeration)
- Login history is important for security auditing
- Email verification is created but email sending is Phase 7

---

## NEXT PHASE PREVIEW

Phase 7 (Auth Module - Extended) will create:
- Password reset functionality (forgot password flow)
- Email verification flow
- Change password for authenticated users
- Email sending service integration
