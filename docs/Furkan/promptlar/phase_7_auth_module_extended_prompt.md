# PHASE 7: AUTH MODULE - EXTENDED

## CONTEXT AND BACKGROUND

You are working on "Şeffaf Bağış Platformu" (Transparent Donation Platform), a graduation project for Fatih Sultan Mehmet Vakıf University. This is a comprehensive donation platform for Turkey that emphasizes transparency and accountability in charitable giving.

### Project Overview
- **Technology Stack**: Java 17, Spring Boot 3.x, PostgreSQL 15+, Redis, Maven
- **Purpose**: Enable foundations to create campaigns, donors to contribute, and maintain transparency through evidence uploads and scoring systems
- **Compliance Requirements**: KVKK (Turkish Personal Data Protection Law) compliant
- **Developer**: Furkan (responsible for Infrastructure, Auth, User, Admin modules - ~58% of backend)

### Current State
- Phase 0-5: Infrastructure complete
- Phase 6 (Auth Module - Core) has been completed
- Users can register, login, refresh tokens, and logout
- Email verification tokens are created during registration
- Password reset tokens entity exists
- Email sending is not yet implemented

### What This Phase Accomplishes
This phase extends the authentication module with password reset, email verification, and password change functionality. It also implements the email sending service for transactional emails. After this phase, the complete authentication flow will be functional.

---

## OBJECTIVE

Create the extended authentication functionality including:
1. Password reset flow (forgot password → reset password)
2. Email verification flow
3. Change password for authenticated users
4. Email service for sending transactional emails
5. Email templates (HTML)

---

## IMPORTANT RULES

### Code Style Requirements
- Use clear, readable code - prefer if-else statements over ternary operators
- Follow SOLID principles
- Add comprehensive comments in English
- Use meaningful variable and method names
- No complex one-liners - prioritize readability over brevity

### Security Requirements
- Password reset tokens expire in 1 hour
- Email verification tokens expire in 24 hours
- Tokens are single-use (mark as used after use)
- Hash tokens before storing in database
- Invalidate all refresh tokens when password changes
- Rate limit password reset requests (prevent abuse)
- Don't reveal if email exists in forgot password response

### Email Requirements
- Use Thymeleaf for HTML email templates
- Support both HTML and plain text versions
- Log all email sends (success and failure)
- Handle email send failures gracefully
- Include unsubscribe links where appropriate

---

## DETAILED REQUIREMENTS

### 1. Auth DTOs - Additional Requests

#### 1.1 ForgotPasswordRequest.java
**Location**: `src/main/java/com/seffafbagis/api/dto/request/auth/ForgotPasswordRequest.java`

**Purpose**: Request body for initiating password reset

**Fields**:

| Field | Type | Validation | Description |
|-------|------|------------|-------------|
| email | String | @NotBlank, @Email | User's email address |

**Notes**:
- Simple DTO with just email field
- Response should not indicate if email exists (security)

---

#### 1.2 ResetPasswordRequest.java
**Location**: `src/main/java/com/seffafbagis/api/dto/request/auth/ResetPasswordRequest.java`

**Purpose**: Request body for resetting password with token

**Fields**:

| Field | Type | Validation | Description |
|-------|------|------------|-------------|
| token | String | @NotBlank | Password reset token from email |
| newPassword | String | @NotBlank, @Size(min=8, max=128) | New password |
| confirmPassword | String | @NotBlank | Password confirmation |

**Validation**:
- newPassword must match confirmPassword
- newPassword must pass strength validation

---

#### 1.3 ChangePasswordRequest.java
**Location**: `src/main/java/com/seffafbagis/api/dto/request/auth/ChangePasswordRequest.java`

**Purpose**: Request body for changing password (authenticated users)

**Fields**:

| Field | Type | Validation | Description |
|-------|------|------------|-------------|
| currentPassword | String | @NotBlank | Current password for verification |
| newPassword | String | @NotBlank, @Size(min=8, max=128) | New password |
| confirmPassword | String | @NotBlank | Password confirmation |

**Validation**:
- newPassword must match confirmPassword
- newPassword must be different from currentPassword
- newPassword must pass strength validation

---

#### 1.4 VerifyEmailRequest.java
**Location**: `src/main/java/com/seffafbagis/api/dto/request/auth/VerifyEmailRequest.java`

**Purpose**: Request body for email verification

**Fields**:

| Field | Type | Validation | Description |
|-------|------|------------|-------------|
| token | String | @NotBlank | Email verification token |

---

#### 1.5 ResendVerificationRequest.java
**Location**: `src/main/java/com/seffafbagis/api/dto/request/auth/ResendVerificationRequest.java`

**Purpose**: Request body for resending verification email

**Fields**:

| Field | Type | Validation | Description |
|-------|------|------------|-------------|
| email | String | @NotBlank, @Email | User's email address |

**Notes**:
- Implement rate limiting (max 3 requests per hour per email)

---

### 2. Password Reset Service

#### 2.1 PasswordResetService.java
**Location**: `src/main/java/com/seffafbagis/api/service/auth/PasswordResetService.java`

**Purpose**: Handle password reset flow

**Dependencies**:
- UserRepository
- PasswordResetTokenRepository
- RefreshTokenRepository
- PasswordEncoder
- EmailService
- TokenUtils
- DateUtils
- PasswordValidator

**Methods**:

---

**`initiatePasswordReset(String email)`**

**Purpose**: Create password reset token and send email

**Flow**:

Step 1: Find user by email (case-insensitive)
- If not found, log but DON'T reveal to user
- Return success message regardless (prevent enumeration)

Step 2: Check if user can reset password
- User status must be ACTIVE or PENDING_VERIFICATION
- If SUSPENDED, log and return success (don't reveal)

Step 3: Invalidate existing reset tokens
- Find any unused tokens for this user
- Mark them as used (or delete)

Step 4: Generate new token
- Use TokenUtils.generateSecureToken()
- Token should be URL-safe

Step 5: Create PasswordResetToken entity
- Hash token before storing
- Set expiry to 1 hour from now
- Save to database

Step 6: Send reset email
- Call emailService.sendPasswordResetEmail()
- Pass plain token (not hash) for the email link

Step 7: Log the action
- Log password reset initiated (without token)

Step 8: Return success message
- Same message whether user exists or not

**Returns**: ApiResponse with generic success message

---

**`resetPassword(ResetPasswordRequest request)`**

**Purpose**: Reset password using token

**Flow**:

Step 1: Validate passwords match
- Check newPassword equals confirmPassword
- If not, throw BadRequestException

Step 2: Validate password strength
- Use PasswordValidator.validateOrThrow()

Step 3: Hash and find token
- Hash the provided token
- Find in database by tokenHash

Step 4: Validate token
- If not found, throw BadRequestException("Invalid or expired token")
- If expired, throw BadRequestException("Token has expired")
- If already used, throw BadRequestException("Token has already been used")

Step 5: Get user from token
- Load user entity
- Verify user is not SUSPENDED

Step 6: Update password
- Hash new password
- Update user.passwordHash
- Update user.passwordChangedAt to now

Step 7: Mark token as used
- Set usedAt to now
- Save token

Step 8: Invalidate all refresh tokens
- Revoke all refresh tokens for this user
- This logs out all sessions

Step 9: Log the action
- Create audit log for password reset

Step 10: Send confirmation email
- Notify user that password was changed

Step 11: Return success
- Return success message

**Returns**: ApiResponse with success message

**Exceptions**:
- BadRequestException for invalid token or validation failures

---

**`cleanupExpiredTokens()`**

**Purpose**: Scheduled job to clean up expired tokens

**Flow**:
- Delete all tokens where expiresAt < now
- Run daily via @Scheduled

---

### 3. Email Verification Service

#### 3.1 EmailVerificationService.java
**Location**: `src/main/java/com/seffafbagis/api/service/auth/EmailVerificationService.java`

**Purpose**: Handle email verification flow

**Dependencies**:
- UserRepository
- EmailVerificationTokenRepository
- EmailService
- TokenUtils
- DateUtils

**Methods**:

---

**`verifyEmail(String token)`**

**Purpose**: Verify user's email address

**Flow**:

Step 1: Hash and find token
- Hash the provided token
- Find in database by tokenHash

Step 2: Validate token
- If not found, throw BadRequestException("Invalid verification token")
- If expired, throw BadRequestException("Verification link has expired")
- If already verified, return success (idempotent)

Step 3: Get user from token
- Load user entity

Step 4: Update user
- Set emailVerified to true
- Set emailVerifiedAt to now
- If status is PENDING_VERIFICATION, set to ACTIVE

Step 5: Mark token as verified
- Set verifiedAt to now
- Save token

Step 6: Log the action
- Create audit log for email verification

Step 7: Send welcome email
- Send welcome email now that account is verified

Step 8: Return success
- Return success message with redirect info

**Returns**: ApiResponse with success message

---

**`resendVerificationEmail(String email)`**

**Purpose**: Resend verification email

**Flow**:

Step 1: Find user by email
- If not found, return success (don't reveal)

Step 2: Check if already verified
- If emailVerified is true, throw BadRequestException("Email already verified")

Step 3: Check rate limit
- Count recent verification tokens for this user
- If more than 3 in last hour, throw BadRequestException("Too many requests")

Step 4: Invalidate existing tokens
- Mark existing unused tokens as verified/used

Step 5: Create new token
- Generate and save new verification token
- Set expiry to 24 hours

Step 6: Send verification email
- Call emailService.sendVerificationEmail()

Step 7: Return success
- Return success message

**Returns**: ApiResponse with success message

---

**`createVerificationToken(User user)`**

**Purpose**: Helper to create verification token (used by AuthService during registration)

**Flow**:
- Generate secure token
- Hash token
- Create EmailVerificationToken entity
- Save to database
- Return plain token (for email)

**Returns**: String (plain token)

---

### 4. Email Service

#### 4.1 EmailService.java
**Location**: `src/main/java/com/seffafbagis/api/service/notification/EmailService.java`

**Purpose**: Send transactional emails

**Dependencies**:
- JavaMailSender
- SpringTemplateEngine (Thymeleaf)
- EmailLogRepository
- Configuration properties (from address, base URL)

**Configuration Properties**:
- app.mail.from-address
- app.mail.from-name
- app.base-url (for generating links)

**Methods**:

---

**`sendEmail(EmailRequest emailRequest)`**

**Purpose**: Generic email sending method

**EmailRequest Inner Class Fields**:
- to: String (recipient email)
- subject: String
- templateName: String (Thymeleaf template)
- templateVariables: Map<String, Object>
- userId: UUID (optional, for logging)
- emailType: String (for logging)

**Flow**:

Step 1: Create MimeMessage
- Use JavaMailSender.createMimeMessage()

Step 2: Set up MimeMessageHelper
- Set from address with name
- Set to address
- Set subject
- Set HTML content (true)

Step 3: Process template
- Use templateEngine.process(templateName, context)
- Context includes templateVariables

Step 4: Send email
- Call mailSender.send(message)

Step 5: Log success
- Create EmailLog entry
- Set status to "sent"
- Save to database

Step 6: Handle errors
- Catch exceptions
- Log failure
- Create EmailLog with error message
- Rethrow or handle gracefully

**Returns**: void (or boolean success)

---

**`sendVerificationEmail(String toEmail, String token, User user)`**

**Purpose**: Send email verification email

**Flow**:
- Build verification URL: {baseUrl}/verify-email?token={token}
- Set template variables: userName, verificationUrl, expiryHours
- Call sendEmail with template "email-verification"

---

**`sendPasswordResetEmail(String toEmail, String token, User user)`**

**Purpose**: Send password reset email

**Flow**:
- Build reset URL: {baseUrl}/reset-password?token={token}
- Set template variables: userName, resetUrl, expiryMinutes
- Call sendEmail with template "password-reset"

---

**`sendWelcomeEmail(String toEmail, User user, UserProfile profile)`**

**Purpose**: Send welcome email after verification

**Flow**:
- Set template variables: userName, loginUrl
- Call sendEmail with template "welcome"

---

**`sendPasswordChangedEmail(String toEmail, User user)`**

**Purpose**: Notify user that password was changed

**Flow**:
- Set template variables: userName, changeTime, supportUrl
- Call sendEmail with template "password-changed"

---

### 5. Email Templates

#### 5.1 email-verification.html
**Location**: `src/main/resources/templates/email/email-verification.html`

**Purpose**: Email verification email template

**Content Structure**:
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>E-posta Doğrulama</title>
    <!-- Inline CSS for email compatibility -->
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Şeffaf Bağış Platformu</h1>
        </div>
        <div class="content">
            <h2>E-posta Adresinizi Doğrulayın</h2>
            <p>Merhaba <span th:text="${userName}">Kullanıcı</span>,</p>
            <p>Şeffaf Bağış Platformu'na kaydolduğunuz için teşekkür ederiz.</p>
            <p>E-posta adresinizi doğrulamak için aşağıdaki butona tıklayın:</p>
            <a th:href="${verificationUrl}" class="button">E-postamı Doğrula</a>
            <p class="note">Bu bağlantı <span th:text="${expiryHours}">24</span> saat içinde geçerliliğini yitirecektir.</p>
            <p class="note">Bu işlemi siz yapmadıysanız, bu e-postayı dikkate almayın.</p>
        </div>
        <div class="footer">
            <p>© 2024 Şeffaf Bağış Platformu. Tüm hakları saklıdır.</p>
        </div>
    </div>
</body>
</html>
```

**Styling Notes**:
- Use inline CSS for email client compatibility
- Use table-based layout for older clients
- Test with major email clients
- Include both button and plain text link

---

#### 5.2 password-reset.html
**Location**: `src/main/resources/templates/email/password-reset.html`

**Purpose**: Password reset email template

**Content Structure**:
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Şifre Sıfırlama</title>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Şeffaf Bağış Platformu</h1>
        </div>
        <div class="content">
            <h2>Şifre Sıfırlama Talebi</h2>
            <p>Merhaba <span th:text="${userName}">Kullanıcı</span>,</p>
            <p>Hesabınız için bir şifre sıfırlama talebi aldık.</p>
            <p>Şifrenizi sıfırlamak için aşağıdaki butona tıklayın:</p>
            <a th:href="${resetUrl}" class="button">Şifremi Sıfırla</a>
            <p class="warning">Bu bağlantı <span th:text="${expiryMinutes}">60</span> dakika içinde geçerliliğini yitirecektir.</p>
            <p class="note">Bu işlemi siz yapmadıysanız, şifreniz güvende - bu e-postayı dikkate almayın.</p>
        </div>
        <div class="footer">
            <p>© 2024 Şeffaf Bağış Platformu. Tüm hakları saklıdır.</p>
        </div>
    </div>
</body>
</html>
```

---

#### 5.3 welcome.html
**Location**: `src/main/resources/templates/email/welcome.html`

**Purpose**: Welcome email after email verification

**Content Structure**:
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Hoş Geldiniz</title>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Şeffaf Bağış Platformu</h1>
        </div>
        <div class="content">
            <h2>Şeffaf Bağış Platformu'na Hoş Geldiniz!</h2>
            <p>Merhaba <span th:text="${userName}">Kullanıcı</span>,</p>
            <p>E-posta adresiniz başarıyla doğrulandı. Artık platformumuzu kullanmaya başlayabilirsiniz.</p>
            <p>Şeffaf Bağış Platformu ile:</p>
            <ul>
                <li>Güvenilir vakıflara bağış yapabilir</li>
                <li>Bağışlarınızın nasıl kullanıldığını takip edebilir</li>
                <li>Şeffaflık raporlarını inceleyebilirsiniz</li>
            </ul>
            <a th:href="${loginUrl}" class="button">Giriş Yap</a>
        </div>
        <div class="footer">
            <p>© 2024 Şeffaf Bağış Platformu. Tüm hakları saklıdır.</p>
        </div>
    </div>
</body>
</html>
```

---

#### 5.4 password-changed.html
**Location**: `src/main/resources/templates/email/password-changed.html`

**Purpose**: Password change notification email

**Content Structure**:
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Şifre Değiştirildi</title>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Şeffaf Bağış Platformu</h1>
        </div>
        <div class="content">
            <h2>Şifreniz Değiştirildi</h2>
            <p>Merhaba <span th:text="${userName}">Kullanıcı</span>,</p>
            <p>Hesabınızın şifresi <span th:text="${changeTime}">tarih</span> tarihinde değiştirildi.</p>
            <p class="warning">Bu değişikliği siz yapmadıysanız, lütfen hemen bizimle iletişime geçin.</p>
            <a th:href="${supportUrl}" class="button">Destek</a>
        </div>
        <div class="footer">
            <p>© 2024 Şeffaf Bağış Platformu. Tüm hakları saklıdır.</p>
        </div>
    </div>
</body>
</html>
```

---

### 6. Update AuthService

Update AuthService from Phase 6 to integrate with new services:

**Changes**:

1. Inject EmailVerificationService

2. Update register method:
   - Call emailVerificationService.createVerificationToken()
   - Call emailService.sendVerificationEmail()

3. Add changePassword method to AuthService (or put in separate service)

---

### 7. Update AuthController

Add new endpoints to AuthController:

---

**POST /api/v1/auth/forgot-password**

**Purpose**: Initiate password reset

**Annotations**:
- @PostMapping("/forgot-password")
- @Operation(summary = "Request password reset")

**Parameters**:
- @Valid @RequestBody ForgotPasswordRequest request

**Logic**:
- Call passwordResetService.initiatePasswordReset()
- Always return 200 with success message (don't reveal if email exists)

**Response**: ApiResponse with generic success message

---

**POST /api/v1/auth/reset-password**

**Purpose**: Reset password with token

**Annotations**:
- @PostMapping("/reset-password")
- @Operation(summary = "Reset password with token")

**Parameters**:
- @Valid @RequestBody ResetPasswordRequest request

**Logic**:
- Call passwordResetService.resetPassword()
- Return success message

**Response**: ApiResponse with success message

---

**POST /api/v1/auth/verify-email**

**Purpose**: Verify email address

**Annotations**:
- @PostMapping("/verify-email")
- @Operation(summary = "Verify email address")

**Parameters**:
- @Valid @RequestBody VerifyEmailRequest request

**Logic**:
- Call emailVerificationService.verifyEmail()
- Return success message

**Response**: ApiResponse with success message

---

**POST /api/v1/auth/resend-verification**

**Purpose**: Resend verification email

**Annotations**:
- @PostMapping("/resend-verification")
- @Operation(summary = "Resend verification email")

**Parameters**:
- @Valid @RequestBody ResendVerificationRequest request

**Logic**:
- Call emailVerificationService.resendVerificationEmail()
- Return success message (always 200)

**Response**: ApiResponse with generic success message

---

**POST /api/v1/auth/change-password**

**Purpose**: Change password (authenticated)

**Annotations**:
- @PostMapping("/change-password")
- @Operation(summary = "Change password")
- Requires authentication

**Parameters**:
- @Valid @RequestBody ChangePasswordRequest request
- @AuthenticationPrincipal CustomUserDetails userDetails

**Logic**:
- Verify current password
- Validate and update new password
- Invalidate all refresh tokens
- Send notification email

**Response**: ApiResponse with success message

---

## FILE STRUCTURE

After completing this phase, the following files should exist:

```
src/main/java/com/seffafbagis/api/
├── dto/request/auth/
│   ├── ForgotPasswordRequest.java
│   ├── ResetPasswordRequest.java
│   ├── ChangePasswordRequest.java
│   ├── VerifyEmailRequest.java
│   └── ResendVerificationRequest.java
├── service/auth/
│   ├── PasswordResetService.java
│   └── EmailVerificationService.java
└── service/notification/
    └── EmailService.java

src/main/resources/templates/email/
├── email-verification.html
├── password-reset.html
├── welcome.html
└── password-changed.html
```

**Total Files**: 12 (5 DTOs + 3 Services + 4 Templates)

**Updates**: AuthController.java (add 5 new endpoints)

---

## FLOW DIAGRAMS

### Password Reset Flow
```
┌─────────────────────────────────────────────────────────────────────┐
│                    PASSWORD RESET FLOW                               │
└─────────────────────────────────────────────────────────────────────┘

User                    Frontend                    Backend                    Email
 │                         │                          │                          │
 │ 1. Click "Forgot"      │                          │                          │
 │────────────────────────>│                          │                          │
 │                         │ POST /forgot-password    │                          │
 │                         │ {email}                  │                          │
 │                         │─────────────────────────>│                          │
 │                         │                          │                          │
 │                         │                          │──┐ Create token          │
 │                         │                          │  │ Hash & store          │
 │                         │                          │<─┘                       │
 │                         │                          │                          │
 │                         │                          │ Send reset email         │
 │                         │                          │─────────────────────────>│
 │                         │                          │                          │
 │                         │ 200 OK                   │                          │
 │                         │ "Check your email"       │                          │
 │                         │<─────────────────────────│                          │
 │                         │                          │                          │
 │                         │<─────────────────────────────────────────────────────│
 │                         │         Email with reset link                       │
 │                         │                          │                          │
 │ 2. Click link in email │                          │                          │
 │────────────────────────>│                          │                          │
 │                         │                          │                          │
 │ 3. Enter new password  │                          │                          │
 │────────────────────────>│                          │                          │
 │                         │ POST /reset-password     │                          │
 │                         │ {token, newPassword}     │                          │
 │                         │─────────────────────────>│                          │
 │                         │                          │                          │
 │                         │                          │──┐ Validate token        │
 │                         │                          │  │ Update password       │
 │                         │                          │  │ Revoke all sessions   │
 │                         │                          │<─┘                       │
 │                         │                          │                          │
 │                         │ 200 OK                   │ Send confirmation email  │
 │                         │ "Password changed"       │─────────────────────────>│
 │                         │<─────────────────────────│                          │
```

### Email Verification Flow
```
┌─────────────────────────────────────────────────────────────────────┐
│                   EMAIL VERIFICATION FLOW                            │
└─────────────────────────────────────────────────────────────────────┘

User                    Frontend                    Backend                    Email
 │                         │                          │                          │
 │ 1. Register            │                          │                          │
 │────────────────────────>│                          │                          │
 │                         │ POST /register           │                          │
 │                         │─────────────────────────>│                          │
 │                         │                          │──┐ Create user           │
 │                         │                          │  │ Create verify token   │
 │                         │                          │<─┘                       │
 │                         │                          │ Send verification email  │
 │                         │                          │─────────────────────────>│
 │                         │ 201 Created              │                          │
 │                         │<─────────────────────────│                          │
 │                         │                          │                          │
 │<────────────────────────────────────────────────────────────────────────────────│
 │                      Email with verification link                              │
 │                         │                          │                          │
 │ 2. Click verify link   │                          │                          │
 │────────────────────────>│                          │                          │
 │                         │ POST /verify-email       │                          │
 │                         │ {token}                  │                          │
 │                         │─────────────────────────>│                          │
 │                         │                          │──┐ Validate token        │
 │                         │                          │  │ Activate account      │
 │                         │                          │<─┘                       │
 │                         │                          │ Send welcome email       │
 │                         │                          │─────────────────────────>│
 │                         │ 200 OK                   │                          │
 │                         │ "Email verified"         │                          │
 │                         │<─────────────────────────│                          │
 │                         │                          │                          │
 │ 3. Now can login       │                          │                          │
```

---

## TESTING REQUIREMENTS

After implementation, verify:

### 1. Password Reset Tests

**Test: Initiate Reset - Existing Email**
- Request reset for existing user
- Verify token is created
- Verify email is sent (check logs or mock)
- Verify response is generic success

**Test: Initiate Reset - Non-existent Email**
- Request reset for non-existent email
- Verify response is same generic success (no enumeration)
- Verify no email is sent

**Test: Reset Password - Valid Token**
- Use valid token to reset password
- Verify password is changed
- Verify token is marked as used
- Verify all refresh tokens are revoked
- Verify confirmation email is sent

**Test: Reset Password - Expired Token**
- Use expired token
- Verify 400 Bad Request

**Test: Reset Password - Used Token**
- Use token that was already used
- Verify 400 Bad Request

**Test: Reset Password - Invalid Token**
- Use invalid/random token
- Verify 400 Bad Request

### 2. Email Verification Tests

**Test: Verify Email - Valid Token**
- Verify with valid token
- Verify user.emailVerified is true
- Verify user.status is ACTIVE
- Verify welcome email is sent

**Test: Verify Email - Expired Token**
- Use expired token
- Verify 400 Bad Request

**Test: Resend Verification - Unverified User**
- Resend for unverified user
- Verify new token is created
- Verify old token is invalidated
- Verify email is sent

**Test: Resend Verification - Already Verified**
- Resend for verified user
- Verify 400 Bad Request with message

**Test: Resend Verification - Rate Limit**
- Request 4 times in one hour
- Verify 4th request is rejected

### 3. Change Password Tests

**Test: Change Password - Valid**
- Change with correct current password
- Verify password is updated
- Verify all refresh tokens are revoked
- Verify notification email is sent

**Test: Change Password - Wrong Current**
- Provide wrong current password
- Verify 400 Bad Request

**Test: Change Password - Weak New Password**
- Provide weak new password
- Verify validation error

### 4. Email Service Tests

**Test: Email Sending**
- Mock JavaMailSender
- Verify template is processed
- Verify email log is created

**Test: Email Failure**
- Mock send failure
- Verify error is logged
- Verify error is handled gracefully

---

## SUCCESS CRITERIA

Phase 7 is considered successful when:

1. ✅ All 12 files are created in correct locations
2. ✅ AuthController is updated with 5 new endpoints
3. ✅ Forgot password sends email without revealing if email exists
4. ✅ Reset password validates token and updates password
5. ✅ Reset password revokes all refresh tokens
6. ✅ Email verification activates account
7. ✅ Resend verification respects rate limits
8. ✅ Change password requires current password
9. ✅ All emails are logged
10. ✅ Email templates render correctly
11. ✅ Error handling is robust
12. ✅ Security requirements are met

---

## RESULT FILE REQUIREMENTS

After completing this phase, create a result file at:
`docs/Furkan/step_results/phase_7_result.md`

The result file must include:

1. **Execution Status**: Success or Failure
2. **Files Created**: List all 12 files with their paths
3. **Endpoint Updates**: List new endpoints added to AuthController
4. **Password Reset Tests**:
   - Initiate reset test results
   - Reset with valid/invalid tokens
5. **Email Verification Tests**:
   - Verification flow results
   - Resend and rate limiting
6. **Email Service Tests**:
   - Confirm emails are sent
   - Confirm logging works
7. **Template Verification**:
   - Screenshots or HTML output of templates
8. **Security Verification**:
   - No information leakage
   - Token expiry working
   - Rate limiting working
9. **Issues Encountered**: Any problems and how they were resolved
10. **Notes for Next Phase**: Observations relevant to Phase 8

---

## NOTES

- Email configuration must be set in application properties
- For development, consider using Mailtrap or similar
- Templates should be tested in multiple email clients
- Rate limiting is important to prevent abuse
- Always use same response for forgot-password to prevent enumeration

---

## NEXT PHASE PREVIEW

Phase 8 (User Module - Profile & Preferences) will create:
- User profile management
- User preferences management
- Sensitive data management (with encryption)
- KVKK consent management
