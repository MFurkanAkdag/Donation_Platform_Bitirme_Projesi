# PHASE 8: USER MODULE - PROFILE & PREFERENCES

## CONTEXT AND BACKGROUND

You are working on "Şeffaf Bağış Platformu" (Transparent Donation Platform), a graduation project for Fatih Sultan Mehmet Vakıf University. This is a comprehensive donation platform for Turkey that emphasizes transparency and accountability in charitable giving.

### Project Overview
- **Technology Stack**: Java 17, Spring Boot 3.x, PostgreSQL 15+, Redis, Maven
- **Purpose**: Enable foundations to create campaigns, donors to contribute, and maintain transparency through evidence uploads and scoring systems
- **Compliance Requirements**: KVKK (Turkish Personal Data Protection Law) compliant
- **Developer**: Furkan (responsible for Infrastructure, Auth, User, Admin modules - ~58% of backend)

### Current State
- Phase 0-5: Infrastructure complete (including EncryptionService, Validators)
- Phase 6-7: Auth Module complete (Login, Register, Password Reset, Email Verification)
- Users can authenticate and maintain sessions
- All user entities exist (User, UserProfile, UserSensitiveData, UserPreference)
- EncryptionService is available for sensitive data

### What This Phase Accomplishes
This phase implements user profile management, preferences management, and sensitive data handling with full KVKK compliance. Users will be able to view and update their profiles, manage notification preferences, and handle their sensitive personal data (TC Kimlik, phone, address) with proper encryption and consent management.

---

## OBJECTIVE

Create the complete user management module including:
1. User DTOs for requests and responses
2. User mapper for entity-DTO conversion
3. User services (profile, preferences, sensitive data)
4. User controllers with REST endpoints
5. KVKK-compliant sensitive data handling

---

## IMPORTANT RULES

### Code Style Requirements
- Use clear, readable code - prefer if-else statements over ternary operators
- Follow SOLID principles
- Add comprehensive comments in English
- Use meaningful variable and method names
- No complex one-liners - prioritize readability over brevity

### KVKK Compliance Requirements
- All sensitive data must be encrypted at rest
- Users must be able to access their data (right of access)
- Users must be able to correct their data (right of rectification)
- Users must be able to delete their data (right of erasure)
- All data processing requires explicit consent
- Consent timestamps must be recorded
- Data access must be audited

### Security Requirements
- Sensitive data displayed only in masked form by default
- Full decryption only for the data owner
- All sensitive data operations must be logged
- Validate all input data before encryption
- Return proper error messages without exposing internal details

---

## DETAILED REQUIREMENTS

### 1. User DTOs - Request

#### 1.1 UpdateProfileRequest.java
**Location**: `src/main/java/com/seffafbagis/api/dto/request/user/UpdateProfileRequest.java`

**Purpose**: Request body for updating user profile

**Fields**:

| Field | Type | Validation | Description |
|-------|------|------------|-------------|
| firstName | String | @Size(max=100) | First name |
| lastName | String | @Size(max=100) | Last name |
| displayName | String | @Size(max=100) | Display name |
| bio | String | @Size(max=1000) | User biography |
| avatarUrl | String | @Size(max=500), @URL | Avatar image URL |
| preferredLanguage | String | @Pattern(regexp="^(tr\|en)$") | Language preference |
| timezone | String | @Size(max=50) | Timezone |

**Notes**:
- All fields are optional (partial updates allowed)
- Use @Valid annotation on controller
- Null values mean "don't update this field"

---

#### 1.2 UpdatePreferencesRequest.java
**Location**: `src/main/java/com/seffafbagis/api/dto/request/user/UpdatePreferencesRequest.java`

**Purpose**: Request body for updating user preferences

**Fields**:

| Field | Type | Validation | Description |
|-------|------|------------|-------------|
| emailNotifications | Boolean | - | Email notification preference |
| smsNotifications | Boolean | - | SMS notification preference |
| donationVisibility | String | @Pattern(regexp="^(public\|anonymous\|private)$") | Donation visibility |
| showInDonorList | Boolean | - | Show in public donor lists |

**Notes**:
- All fields are optional
- Null values mean "don't update this field"

---

#### 1.3 UpdateSensitiveDataRequest.java
**Location**: `src/main/java/com/seffafbagis/api/dto/request/user/UpdateSensitiveDataRequest.java`

**Purpose**: Request body for updating KVKK-protected sensitive data

**Fields**:

| Field | Type | Validation | Description |
|-------|------|------------|-------------|
| tcKimlik | String | @Size(min=11, max=11) | Turkish National ID |
| phone | String | @Size(max=20) | Phone number |
| address | String | @Size(max=500) | Physical address |
| birthDate | LocalDate | @Past | Birth date |

**Notes**:
- All fields are optional
- Validation happens before encryption
- Custom validators will be applied (TcKimlikValidator, PhoneValidator)

---

#### 1.4 UpdateConsentRequest.java
**Location**: `src/main/java/com/seffafbagis/api/dto/request/user/UpdateConsentRequest.java`

**Purpose**: Request body for updating KVKK consent settings

**Fields**:

| Field | Type | Validation | Description |
|-------|------|------------|-------------|
| marketingConsent | Boolean | @NotNull | Marketing communications consent |
| thirdPartySharingConsent | Boolean | @NotNull | Third-party data sharing consent |

**Notes**:
- Cannot withdraw dataProcessingConsent (would require account deletion)
- Consent changes are timestamped automatically

---

#### 1.5 DeleteAccountRequest.java
**Location**: `src/main/java/com/seffafbagis/api/dto/request/user/DeleteAccountRequest.java`

**Purpose**: Request body for account deletion confirmation

**Fields**:

| Field | Type | Validation | Description |
|-------|------|------------|-------------|
| password | String | @NotBlank | Current password for confirmation |
| confirmDeletion | Boolean | Must be true | Explicit deletion confirmation |
| reason | String | @Size(max=500) | Optional reason for leaving |

**Notes**:
- Requires password verification
- Must explicitly confirm deletion
- Reason is optional but helpful for feedback

---

### 2. User DTOs - Response

#### 2.1 UserResponse.java
**Location**: `src/main/java/com/seffafbagis/api/dto/response/user/UserResponse.java`

**Purpose**: Basic user information response

**Fields**:

| Field | Type | Description |
|-------|------|-------------|
| id | UUID | User ID |
| email | String | User email |
| role | UserRole | User role |
| status | UserStatus | Account status |
| emailVerified | Boolean | Email verification status |
| createdAt | LocalDateTime | Account creation date |

**Static Factory Method**:
- `fromEntity(User user)`

---

#### 2.2 UserProfileResponse.java
**Location**: `src/main/java/com/seffafbagis/api/dto/response/user/UserProfileResponse.java`

**Purpose**: User profile information response

**Fields**:

| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Profile ID |
| userId | UUID | Associated user ID |
| firstName | String | First name |
| lastName | String | Last name |
| displayName | String | Display name |
| fullName | String | Computed full name |
| avatarUrl | String | Avatar URL |
| bio | String | Biography |
| preferredLanguage | String | Language preference |
| timezone | String | Timezone |
| createdAt | LocalDateTime | Creation timestamp |
| updatedAt | LocalDateTime | Last update timestamp |

**Static Factory Method**:
- `fromEntity(UserProfile profile)`

---

#### 2.3 UserPreferenceResponse.java
**Location**: `src/main/java/com/seffafbagis/api/dto/response/user/UserPreferenceResponse.java`

**Purpose**: User preferences response

**Fields**:

| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Preference ID |
| emailNotifications | Boolean | Email notification setting |
| smsNotifications | Boolean | SMS notification setting |
| donationVisibility | String | Donation visibility setting |
| showInDonorList | Boolean | Donor list visibility |
| updatedAt | LocalDateTime | Last update timestamp |

**Static Factory Method**:
- `fromEntity(UserPreference preference)`

---

#### 2.4 UserSensitiveDataResponse.java
**Location**: `src/main/java/com/seffafbagis/api/dto/response/user/UserSensitiveDataResponse.java`

**Purpose**: Masked sensitive data response (KVKK compliant)

**Fields**:

| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Record ID |
| hasTcKimlik | Boolean | Whether TC Kimlik is stored |
| tcKimlikMasked | String | Masked TC: "***-***-**45" |
| hasPhone | Boolean | Whether phone is stored |
| phoneMasked | String | Masked phone: "+90 *** *** ** 67" |
| hasAddress | Boolean | Whether address is stored |
| addressPreview | String | First 20 chars + "..." |
| hasBirthDate | Boolean | Whether birth date is stored |
| birthYear | Integer | Only year shown |
| dataProcessingConsent | Boolean | Data processing consent |
| consentDate | LocalDateTime | Consent timestamp |
| marketingConsent | Boolean | Marketing consent |
| marketingConsentDate | LocalDateTime | Marketing consent timestamp |
| thirdPartySharingConsent | Boolean | Third-party sharing consent |
| thirdPartySharingConsentDate | LocalDateTime | Sharing consent timestamp |

**Notes**:
- Never return full sensitive data in this DTO
- Use mask methods from validators
- Only show whether data exists + masked preview

---

#### 2.5 UserDetailResponse.java
**Location**: `src/main/java/com/seffafbagis/api/dto/response/user/UserDetailResponse.java`

**Purpose**: Complete user information (combines all user data)

**Fields**:

| Field | Type | Description |
|-------|------|-------------|
| user | UserResponse | Basic user info |
| profile | UserProfileResponse | Profile info |
| preferences | UserPreferenceResponse | Preferences |
| sensitiveData | UserSensitiveDataResponse | Masked sensitive data |
| statistics | UserStatistics | Optional user statistics |

**UserStatistics Nested Class**:
- totalDonations: Integer
- totalDonationAmount: BigDecimal
- favoriteOrganizationsCount: Integer
- memberSince: LocalDate

**Static Factory Method**:
- `of(User user, UserProfile profile, UserPreference preferences, UserSensitiveData sensitiveData)`

---

### 3. User Mapper

#### 3.1 UserMapper.java
**Location**: `src/main/java/com/seffafbagis/api/dto/mapper/UserMapper.java`

**Purpose**: Convert between entities and DTOs

**Class Annotations**:
- @Component
- @RequiredArgsConstructor

**Dependencies**:
- EncryptionService (for sensitive data)
- TcKimlikValidator, PhoneValidator (for masking)

**Methods**:

---

**`toUserResponse(User user)`**
- Convert User entity to UserResponse DTO
- Simple field mapping

---

**`toUserProfileResponse(UserProfile profile)`**
- Convert UserProfile entity to UserProfileResponse DTO
- Include computed fullName

---

**`toUserPreferenceResponse(UserPreference preference)`**
- Convert UserPreference entity to UserPreferenceResponse DTO
- Simple field mapping

---

**`toUserSensitiveDataResponse(UserSensitiveData sensitiveData)`**
- Convert UserSensitiveData entity to masked response
- Decrypt data temporarily for masking
- Use validators' mask methods
- Set boolean flags for data presence
- NEVER return unmasked data

**Implementation Logic**:
```
For TC Kimlik:
- If tcKimlikEncrypted is not null:
  - Decrypt to get plain TC
  - Set hasTcKimlik = true
  - Set tcKimlikMasked = TcKimlikValidator.mask(plainTc)
- Else:
  - Set hasTcKimlik = false
  - Set tcKimlikMasked = null

Similar logic for phone, address, birthDate
```

---

**`toUserDetailResponse(User user, UserProfile profile, UserPreference preferences, UserSensitiveData sensitiveData)`**
- Combine all mappers into single response
- Include optional statistics if available

---

**`updateProfileFromRequest(UserProfile profile, UpdateProfileRequest request)`**
- Update entity fields from request
- Only update non-null request fields (partial update)
- Return updated entity

---

**`updatePreferencesFromRequest(UserPreference preferences, UpdatePreferencesRequest request)`**
- Update entity fields from request
- Only update non-null request fields
- Return updated entity

---

### 4. User Services

#### 4.1 UserService.java
**Location**: `src/main/java/com/seffafbagis/api/service/user/UserService.java`

**Purpose**: Core user operations

**Dependencies**:
- UserRepository
- UserProfileRepository
- UserPreferenceRepository
- UserSensitiveDataRepository
- RefreshTokenRepository
- PasswordEncoder
- UserMapper

**Methods**:

**`getCurrentUser(UUID userId)`**
- Load user by ID
- Throw ResourceNotFoundException if not found
- Map to UserResponse
- Return response

**`getCurrentUserDetail(UUID userId)`**
- Load user with all related entities (profile, preferences, sensitive data)
- Map to UserDetailResponse
- Return complete user information

**`deleteAccount(UUID userId, DeleteAccountRequest request)`**

Flow:
1. Load user by ID
2. Verify password matches using passwordEncoder.matches()
3. Check confirmDeletion is true
4. Revoke all refresh tokens for user
5. Soft delete: Set user status to INACTIVE (or a new DELETED status)
6. Optionally anonymize personal data
7. Create audit log entry
8. Return success message

---

#### 4.2 UserProfileService.java
**Location**: `src/main/java/com/seffafbagis/api/service/user/UserProfileService.java`

**Purpose**: User profile management

**Dependencies**:
- UserProfileRepository
- UserMapper

**Methods**:

**`getProfile(UUID userId)`**
- Find profile by user ID
- Throw ResourceNotFoundException if not found
- Map to UserProfileResponse
- Return response

**`updateProfile(UUID userId, UpdateProfileRequest request)`**

Flow:
1. Find profile by user ID
2. Use mapper to update only non-null fields from request
3. Set updatedAt to now
4. Save profile to database
5. Map to UserProfileResponse
6. Return response

---

#### 4.3 UserPreferenceService.java
**Location**: `src/main/java/com/seffafbagis/api/service/user/UserPreferenceService.java`

**Purpose**: User preferences management

**Dependencies**:
- UserPreferenceRepository
- UserMapper

**Methods**:

**`getPreferences(UUID userId)`**
- Find preferences by user ID
- Throw ResourceNotFoundException if not found
- Map to UserPreferenceResponse
- Return response

**`updatePreferences(UUID userId, UpdatePreferencesRequest request)`**

Flow:
1. Find preferences by user ID
2. Use mapper to update only non-null fields
3. Set updatedAt to now
4. Save preferences to database
5. Map to UserPreferenceResponse
6. Return response

---

#### 4.4 SensitiveDataService.java
**Location**: `src/main/java/com/seffafbagis/api/service/user/SensitiveDataService.java`

**Purpose**: KVKK-compliant sensitive data management

**Dependencies**:
- UserSensitiveDataRepository
- EncryptionService
- TcKimlikValidator
- PhoneValidator
- UserMapper

**Methods**:

**`getSensitiveData(UUID userId)`**
- Find sensitive data by user ID
- Map to masked response using mapper
- Return UserSensitiveDataResponse (masked)

**`updateSensitiveData(UUID userId, UpdateSensitiveDataRequest request)`**

Flow:
1. Find sensitive data record by user ID
2. For each non-null field in request:
   - Validate using appropriate validator
   - Encrypt using encryptionService.encrypt()
   - Set encrypted bytes on entity
3. Save to database
4. Map to masked response
5. Return response

Validation and encryption for each field:
- tcKimlik: TcKimlikValidator.validateOrThrow(), then encrypt
- phone: PhoneValidator.validateOrThrow(), normalize, then encrypt
- address: Direct encrypt (no special validation)
- birthDate: Convert to string, then encrypt

**`updateConsent(UUID userId, UpdateConsentRequest request)`**

Flow:
1. Find sensitive data record
2. For each consent field:
   - If value changed from current:
     - Update the consent boolean
     - Set consent date to now
3. Save to database
4. Map to response
5. Return response

**`deleteSensitiveData(UUID userId)`**
- Find sensitive data record
- Set all encrypted fields to null
- Keep consent records (for audit purposes)
- Save to database
- Return success message

**`exportSensitiveData(UUID userId)`**
- Find sensitive data record
- Decrypt all encrypted fields
- Build export object with plain text values
- Log export request for audit
- Return decrypted data object

---

### 5. User Controllers

#### 5.1 UserController.java
**Location**: `src/main/java/com/seffafbagis/api/controller/user/UserController.java`

**Purpose**: General user endpoints

**Annotations**:
- @RestController
- @RequestMapping("/api/v1/users")
- @Tag(name = "User", description = "User management endpoints")
- @RequiredArgsConstructor

**Endpoints**:

**GET /api/v1/users/me**
- Get current user's basic information
- Parameters: @AuthenticationPrincipal CustomUserDetails userDetails
- Returns: ApiResponse<UserResponse>

**GET /api/v1/users/me/detail**
- Get current user's complete information
- Parameters: @AuthenticationPrincipal CustomUserDetails userDetails
- Returns: ApiResponse<UserDetailResponse>

**DELETE /api/v1/users/me**
- Delete current user's account
- Parameters: @Valid @RequestBody DeleteAccountRequest, @AuthenticationPrincipal
- Returns: ApiResponse<String>

---

#### 5.2 UserProfileController.java
**Location**: `src/main/java/com/seffafbagis/api/controller/user/UserProfileController.java`

**Purpose**: User profile endpoints

**Annotations**:
- @RestController
- @RequestMapping("/api/v1/users/me/profile")
- @Tag(name = "User Profile", description = "User profile management")

**Endpoints**:

**GET /api/v1/users/me/profile**
- Get current user's profile
- Returns: ApiResponse<UserProfileResponse>

**PUT /api/v1/users/me/profile**
- Update current user's profile
- Parameters: @Valid @RequestBody UpdateProfileRequest
- Returns: ApiResponse<UserProfileResponse>

---

#### 5.3 UserPreferenceController.java
**Location**: `src/main/java/com/seffafbagis/api/controller/user/UserPreferenceController.java`

**Purpose**: User preference endpoints

**Annotations**:
- @RestController
- @RequestMapping("/api/v1/users/me/preferences")
- @Tag(name = "User Preferences", description = "User preference management")

**Endpoints**:

**GET /api/v1/users/me/preferences**
- Get current user's preferences
- Returns: ApiResponse<UserPreferenceResponse>

**PUT /api/v1/users/me/preferences**
- Update current user's preferences
- Parameters: @Valid @RequestBody UpdatePreferencesRequest
- Returns: ApiResponse<UserPreferenceResponse>

---

#### 5.4 SensitiveDataController.java
**Location**: `src/main/java/com/seffafbagis/api/controller/user/SensitiveDataController.java`

**Purpose**: KVKK-compliant sensitive data endpoints

**Annotations**:
- @RestController
- @RequestMapping("/api/v1/users/me/sensitive-data")
- @Tag(name = "Sensitive Data", description = "KVKK-compliant sensitive data management")

**Endpoints**:

**GET /api/v1/users/me/sensitive-data**
- Get masked sensitive data
- Returns: ApiResponse<UserSensitiveDataResponse>

**PUT /api/v1/users/me/sensitive-data**
- Update sensitive data
- Parameters: @Valid @RequestBody UpdateSensitiveDataRequest
- Returns: ApiResponse<UserSensitiveDataResponse>

**PUT /api/v1/users/me/consents**
- Update KVKK consents
- Parameters: @Valid @RequestBody UpdateConsentRequest
- Returns: ApiResponse<UserSensitiveDataResponse>

**DELETE /api/v1/users/me/sensitive-data**
- Delete sensitive data (KVKK right to erasure)
- Returns: ApiResponse<String>

**GET /api/v1/users/me/sensitive-data/export**
- Export sensitive data (KVKK data portability)
- Returns: Decrypted sensitive data for download

---

## FILE STRUCTURE

After completing this phase, the following files should exist:

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

**Total Files**: 19

---

## TESTING REQUIREMENTS

After implementation, verify:

### 1. Profile Tests

**Test: Get Profile**
- Get profile for authenticated user
- Verify all fields returned correctly

**Test: Update Profile - Partial**
- Update only firstName
- Verify only firstName changed, others unchanged

**Test: Update Profile - Full**
- Update all fields
- Verify all fields updated correctly

**Test: Update Profile - Invalid Data**
- Send invalid avatar URL
- Verify 400 Bad Request

### 2. Preferences Tests

**Test: Get Preferences**
- Get preferences for authenticated user
- Verify default values

**Test: Update Preferences**
- Update notification settings
- Verify changes persisted

**Test: Update Donation Visibility**
- Update to each valid value (public, anonymous, private)
- Verify each works
- Try invalid value, verify rejection

### 3. Sensitive Data Tests

**Test: Get Sensitive Data - Empty**
- New user with no sensitive data
- Verify all "has" flags are false
- Verify masked fields are null

**Test: Update TC Kimlik - Valid**
- Update with valid TC Kimlik
- Verify data is encrypted in database
- Verify response shows masked value

**Test: Update TC Kimlik - Invalid**
- Update with invalid TC (wrong checksum)
- Verify 400 Bad Request with validation message

**Test: Update Phone - Valid**
- Update with valid Turkish phone
- Verify normalization works
- Verify masked response

**Test: Update Phone - Invalid**
- Update with invalid phone
- Verify 400 Bad Request

**Test: Update Multiple Fields**
- Update TC, phone, address in one request
- Verify all encrypted correctly

**Test: Get Sensitive Data - With Data**
- After updates, get sensitive data
- Verify all "has" flags are true
- Verify masked values show correctly
- Verify ACTUAL values are NOT returned

**Test: Delete Sensitive Data**
- Delete sensitive data
- Verify all encrypted fields are null
- Verify consent records remain

**Test: Export Sensitive Data**
- Export data
- Verify decrypted values in export
- Verify audit log created

### 4. Consent Tests

**Test: Update Marketing Consent - Grant**
- Set marketingConsent to true
- Verify timestamp recorded

**Test: Update Marketing Consent - Revoke**
- Set marketingConsent to false
- Verify new timestamp recorded

**Test: Consent Timestamps**
- Update consent
- Verify timestamp only changes when value changes

### 5. Account Deletion Tests

**Test: Delete Account - Valid**
- Delete with correct password
- Verify account status changed
- Verify tokens revoked
- Verify can no longer login

**Test: Delete Account - Wrong Password**
- Delete with wrong password
- Verify 400 Bad Request
- Verify account NOT deleted

**Test: Delete Account - Not Confirmed**
- Delete with confirmDeletion = false
- Verify 400 Bad Request

### 6. Security Tests

**Test: Access Other User's Data**
- Try to access another user's profile via ID manipulation
- Verify only own data accessible

**Test: Audit Logging**
- Perform sensitive data operations
- Verify audit logs created

---

## SUCCESS CRITERIA

Phase 8 is considered successful when:

1. ✅ All 19 files are created in correct locations
2. ✅ Profile CRUD operations work correctly
3. ✅ Preferences CRUD operations work correctly
4. ✅ Sensitive data is encrypted at rest
5. ✅ Sensitive data is returned masked
6. ✅ TC Kimlik validation works (Turkish algorithm)
7. ✅ Phone validation and normalization work
8. ✅ Consent changes are timestamped
9. ✅ Account deletion works with password verification
10. ✅ KVKK right to erasure implemented
11. ✅ KVKK data export implemented
12. ✅ All operations are audit logged
13. ✅ Users can only access their own data

---

## RESULT FILE REQUIREMENTS

After completing this phase, create a result file at:
`docs/Furkan/step_results/phase_8_result.md`

The result file must include:

1. **Execution Status**: Success or Failure
2. **Files Created**: List all 19 files with their paths
3. **Profile Tests**:
   - Get profile result
   - Update profile (partial and full)
4. **Preferences Tests**:
   - Get and update results
5. **Sensitive Data Tests**:
   - Encryption verification (show encrypted bytes in DB)
   - Masking verification (show masked response)
   - Validation tests for TC Kimlik, Phone
6. **Consent Tests**:
   - Timestamp recording verification
7. **Account Deletion Tests**:
   - Successful deletion flow
   - Password verification
8. **KVKK Compliance**:
   - Right to access (GET sensitive data)
   - Right to rectification (PUT sensitive data)
   - Right to erasure (DELETE sensitive data)
   - Data portability (export)
9. **Audit Logging**:
   - Sample audit log entries
10. **Issues Encountered**: Any problems and how they were resolved
11. **Notes for Next Phase**: Observations relevant to Phase 9

---

## KVKK COMPLIANCE CHECKLIST

Before completing this phase, verify:

- [ ] All sensitive data is encrypted with AES-256-GCM
- [ ] Sensitive data is never returned unencrypted in responses
- [ ] Users can view their data (masked)
- [ ] Users can update their data
- [ ] Users can delete their sensitive data
- [ ] Users can export their data
- [ ] Consent timestamps are recorded
- [ ] Consent can be granted and revoked
- [ ] All data access is audit logged
- [ ] Data processing consent is required at registration

---

## API ENDPOINTS SUMMARY

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1/users/me | Get current user basic info |
| GET | /api/v1/users/me/detail | Get current user full details |
| DELETE | /api/v1/users/me | Delete account |
| GET | /api/v1/users/me/profile | Get profile |
| PUT | /api/v1/users/me/profile | Update profile |
| GET | /api/v1/users/me/preferences | Get preferences |
| PUT | /api/v1/users/me/preferences | Update preferences |
| GET | /api/v1/users/me/sensitive-data | Get masked sensitive data |
| PUT | /api/v1/users/me/sensitive-data | Update sensitive data |
| PUT | /api/v1/users/me/consents | Update consents |
| DELETE | /api/v1/users/me/sensitive-data | Delete sensitive data |
| GET | /api/v1/users/me/sensitive-data/export | Export sensitive data |

---

## NOTES

- All endpoints require authentication
- Users can only access/modify their own data
- Sensitive data operations should be extra careful
- Audit logging is mandatory for KVKK
- Consider rate limiting on sensitive endpoints
- Export feature may need to generate a downloadable file

---

## NEXT PHASE PREVIEW

Phase 9 (Admin Module - User Management) will create:
- Admin endpoints for managing all users
- User search and filtering
- User status management (suspend, activate)
- User role management
- Admin dashboard statistics
