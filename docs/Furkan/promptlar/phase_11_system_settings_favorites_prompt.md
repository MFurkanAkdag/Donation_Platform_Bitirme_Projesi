# PHASE 11: SYSTEM SETTINGS & FAVORITES

## CONTEXT AND BACKGROUND

You are working on "Şeffaf Bağış Platformu" (Transparent Donation Platform), a graduation project for Fatih Sultan Mehmet Vakıf University. This is a comprehensive donation platform for Turkey that emphasizes transparency and accountability in charitable giving.

### Project Overview
- **Technology Stack**: Java 17, Spring Boot 3.x, PostgreSQL 15+, Redis, Maven
- **Purpose**: Enable foundations to create campaigns, donors to contribute, and maintain transparency through evidence uploads and scoring systems
- **Compliance Requirements**: KVKK (Turkish Personal Data Protection Law) compliant
- **Developer**: Furkan (responsible for Infrastructure, Auth, User, Admin modules - ~58% of backend)

### Current State
- Phase 0-8: Core infrastructure and user management complete
- Phase 9-10: Admin module complete
- SystemSetting and FavoriteOrganization entities exist (from Phase 4)
- SystemSettingRepository and FavoriteOrganizationRepository exist (from Phase 4)
- Redis configuration is in place (from Phase 1)

### What This Phase Accomplishes
This phase implements platform-wide system settings management and user favorite organizations feature. System settings allow administrators to configure platform behavior without code deployments. The favorites feature lets users save organizations they want to follow and donate to regularly.

---

## OBJECTIVE

Create the system settings and favorites module including:
1. System settings DTOs for admin management
2. System settings service with Redis caching
3. System settings controller (admin endpoints + public endpoint)
4. Favorite organizations DTOs and service
5. Favorite organizations controller for users

---

## IMPORTANT RULES

### Code Style Requirements
- Use clear, readable code - prefer if-else statements over ternary operators
- Follow SOLID principles
- Add comprehensive comments in English
- Use meaningful variable and method names
- No complex one-liners - prioritize readability over brevity

### Caching Requirements
- Public settings must be cached in Redis
- Cache must be invalidated when settings change
- Use appropriate TTL (Time To Live)
- Handle cache failures gracefully (fall back to database)
- Log cache hits/misses for debugging

### Settings Security
- Only ADMIN can create/update/delete settings
- Public settings accessible without authentication
- Private settings only accessible by admins
- Setting values can be typed (string, number, boolean, json)

---

## DETAILED REQUIREMENTS

### 1. System Settings DTOs - Request

#### 1.1 CreateSettingRequest.java
**Location**: `src/main/java/com/seffafbagis/api/dto/request/system/CreateSettingRequest.java`

**Purpose**: Request body for creating a new system setting

**Fields**:

| Field | Type | Validation | Description |
|-------|------|------------|-------------|
| settingKey | String | @NotBlank, @Size(max=100), @Pattern(regexp="^[a-z_]+$") | Unique setting key (snake_case) |
| settingValue | String | @NotBlank, @Size(max=5000) | Setting value |
| valueType | String | @Pattern(regexp="^(string\|number\|boolean\|json)$") | Value type, default "string" |
| description | String | @Size(max=255) | Setting description |
| isPublic | Boolean | Default false | Whether publicly accessible |

**Notes**:
- settingKey must be unique
- settingKey should be snake_case (e.g., min_donation_amount)
- valueType helps frontend parse the value correctly

---

#### 1.2 UpdateSettingRequest.java
**Location**: `src/main/java/com/seffafbagis/api/dto/request/system/UpdateSettingRequest.java`

**Purpose**: Request body for updating an existing system setting

**Fields**:

| Field | Type | Validation | Description |
|-------|------|------------|-------------|
| settingValue | String | @NotBlank, @Size(max=5000) | New setting value |
| description | String | @Size(max=255) | Updated description (optional) |
| isPublic | Boolean | Optional | Update public visibility |

**Notes**:
- settingKey and valueType cannot be changed after creation
- Only provided fields are updated

---

### 2. System Settings DTOs - Response

#### 2.1 SystemSettingResponse.java
**Location**: `src/main/java/com/seffafbagis/api/dto/response/system/SystemSettingResponse.java`

**Purpose**: System setting details response

**Fields**:

| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Setting ID |
| settingKey | String | Setting key |
| settingValue | String | Setting value |
| valueType | String | Value type (string, number, boolean, json) |
| description | String | Setting description |
| isPublic | Boolean | Public visibility |
| updatedBy | UserSummary | Admin who last updated |
| createdAt | LocalDateTime | Creation timestamp |
| updatedAt | LocalDateTime | Last update timestamp |

**UserSummary Nested Class**:
- id: UUID
- email: String
- fullName: String

**Static Factory Method**:
- `fromEntity(SystemSetting setting)`

---

#### 2.2 PublicSettingsResponse.java
**Location**: `src/main/java/com/seffafbagis/api/dto/response/system/PublicSettingsResponse.java`

**Purpose**: Public settings for frontend (all public settings in one response)

**Fields**:

| Field | Type | Description |
|-------|------|-------------|
| settings | Map<String, Object> | Key-value pairs of public settings |
| fetchedAt | LocalDateTime | Timestamp when fetched |

**Notes**:
- Values are parsed according to valueType:
  - string: returned as String
  - number: parsed to Number (Integer or Double)
  - boolean: parsed to Boolean
  - json: parsed to Object (Map or List)

**Example Response**:
```json
{
  "settings": {
    "platform_name": "Şeffaf Bağış Platformu",
    "min_donation_amount": 10,
    "max_donation_amount": 1000000,
    "maintenance_mode": false,
    "commission_rate": 0,
    "evidence_deadline_days": 15,
    "transparency_score_threshold": 40
  },
  "fetchedAt": "2024-01-15T10:30:00"
}
```

---

### 3. Favorites DTOs

#### 3.1 FavoriteOrganizationResponse.java
**Location**: `src/main/java/com/seffafbagis/api/dto/response/favorite/FavoriteOrganizationResponse.java`

**Purpose**: Favorite organization details

**Fields**:

| Field | Type | Description |
|-------|------|-------------|
| organizationId | UUID | Organization ID |
| organizationName | String | Organization name |
| organizationLogo | String | Logo URL |
| organizationDescription | String | Short description |
| verificationStatus | String | Verification status |
| activeCampaignsCount | Integer | Number of active campaigns |
| totalRaised | BigDecimal | Total donations received |
| favoritedAt | LocalDateTime | When user favorited |

**Static Factory Method**:
- `fromEntity(FavoriteOrganization favorite, Organization org)`

---

#### 3.2 FavoriteCheckResponse.java
**Location**: `src/main/java/com/seffafbagis/api/dto/response/favorite/FavoriteCheckResponse.java`

**Purpose**: Response for checking if organization is favorited

**Fields**:

| Field | Type | Description |
|-------|------|-------------|
| organizationId | UUID | Organization ID |
| isFavorited | Boolean | Whether user has favorited |
| favoritedAt | LocalDateTime | When favorited (null if not) |

---

### 4. System Settings Service

#### 4.1 SystemSettingService.java
**Location**: `src/main/java/com/seffafbagis/api/service/system/SystemSettingService.java`

**Purpose**: System settings management with Redis caching

**Dependencies**:
- SystemSettingRepository
- UserRepository
- RedisTemplate (for caching)
- ObjectMapper (for JSON parsing)

**Constants**:
```
CACHE_KEY_PREFIX = "settings:"
PUBLIC_SETTINGS_CACHE_KEY = "settings:public"
CACHE_TTL = 1 hour
```

**Methods**:

---

**`getAllSettings()`**

**Purpose**: Get all settings (admin only)

**Flow**:
1. Call repository.findAll()
2. Map to SystemSettingResponse list
3. Return list

**Returns**: List<SystemSettingResponse>

---

**`getSettingByKey(String key)`**

**Purpose**: Get single setting by key

**Flow**:
1. Try to get from cache (CACHE_KEY_PREFIX + key)
2. If cache hit, return cached value
3. If cache miss:
   - Get from database
   - Store in cache with TTL
   - Return value
4. Throw ResourceNotFoundException if not found

**Returns**: SystemSettingResponse

---

**`getPublicSettings()`**

**Purpose**: Get all public settings (cached)

**Flow**:
1. Try to get from cache (PUBLIC_SETTINGS_CACHE_KEY)
2. If cache hit, return cached value
3. If cache miss:
   - Call repository.findAllByIsPublicTrue()
   - Parse values according to valueType
   - Build PublicSettingsResponse
   - Store in cache with TTL
   - Return response
4. Handle cache failures gracefully (fall back to DB)

**Returns**: PublicSettingsResponse

---

**`createSetting(CreateSettingRequest request, UUID adminId)`**

**Purpose**: Create new system setting

**Flow**:
1. Check key uniqueness
   - If exists, throw ConflictException
2. Create SystemSetting entity
3. Set updatedBy to admin user
4. Save to database
5. If isPublic, invalidate public settings cache
6. Return SystemSettingResponse

**Returns**: SystemSettingResponse

---

**`updateSetting(String key, UpdateSettingRequest request, UUID adminId)`**

**Purpose**: Update existing setting

**Flow**:
1. Find setting by key
   - If not found, throw ResourceNotFoundException
2. Update fields from request (only non-null)
3. Set updatedBy to admin user
4. Set updatedAt to now
5. Save to database
6. Invalidate caches:
   - Remove specific setting cache (CACHE_KEY_PREFIX + key)
   - If isPublic changed or setting is public, invalidate public cache
7. Return updated SystemSettingResponse

**Returns**: SystemSettingResponse

---

**`deleteSetting(String key)`**

**Purpose**: Delete system setting

**Flow**:
1. Find setting by key
   - If not found, throw ResourceNotFoundException
2. Check if setting is deletable (some core settings may be protected)
3. Delete from database
4. Invalidate caches
5. Return success message

**Returns**: void

---

**`getSettingValue(String key)`**

**Purpose**: Get typed setting value (for internal use)

**Flow**:
1. Get setting by key
2. Parse value according to valueType
3. Return parsed value

**Returns**: Object (String, Number, Boolean, or parsed JSON)

---

**`getSettingValueOrDefault(String key, Object defaultValue)`**

**Purpose**: Get setting value with default fallback

**Flow**:
1. Try to get setting
2. If not found, return defaultValue
3. Return parsed value

**Returns**: Object

---

**`invalidateCache(String key)`**

**Purpose**: Invalidate specific setting cache

**Flow**:
1. Delete from Redis: CACHE_KEY_PREFIX + key
2. Log cache invalidation

---

**`invalidatePublicCache()`**

**Purpose**: Invalidate public settings cache

**Flow**:
1. Delete from Redis: PUBLIC_SETTINGS_CACHE_KEY
2. Log cache invalidation

---

**`parseValue(String value, String valueType)`**

**Purpose**: Parse string value to appropriate type

**Implementation**:
```
switch (valueType):
    case "string":
        return value
    case "number":
        if contains ".":
            return Double.parseDouble(value)
        else:
            return Long.parseLong(value)
    case "boolean":
        return Boolean.parseBoolean(value)
    case "json":
        return objectMapper.readValue(value, Object.class)
    default:
        return value
```

---

### 5. Favorite Organization Service

#### 5.1 FavoriteOrganizationService.java
**Location**: `src/main/java/com/seffafbagis/api/service/favorite/FavoriteOrganizationService.java`

**Purpose**: User favorite organizations management

**Dependencies**:
- FavoriteOrganizationRepository
- IOrganizationService (interface from Phase 10)

**Methods**:

---

**`getUserFavorites(UUID userId)`**

**Purpose**: Get user's favorite organizations

**Flow**:
1. Call repository.findAllByUserId(userId)
2. For each favorite:
   - Get organization details from IOrganizationService
   - Build FavoriteOrganizationResponse
3. Return list sorted by favoritedAt (newest first)

**Returns**: List<FavoriteOrganizationResponse>

---

**`addFavorite(UUID userId, UUID organizationId)`**

**Purpose**: Add organization to favorites

**Flow**:
1. Check if organization exists
   - Call organizationService.existsById(organizationId)
   - If not, throw ResourceNotFoundException
2. Check if already favorited
   - If yes, return existing favorite (idempotent)
3. Create FavoriteOrganization entity
   - Set userId, organizationId
   - Set createdAt to now
4. Save to database
5. Return FavoriteOrganizationResponse

**Returns**: FavoriteOrganizationResponse

---

**`removeFavorite(UUID userId, UUID organizationId)`**

**Purpose**: Remove organization from favorites

**Flow**:
1. Check if favorite exists
   - If not, return success (idempotent)
2. Delete from database
3. Return success message

**Returns**: void

---

**`isFavorited(UUID userId, UUID organizationId)`**

**Purpose**: Check if user has favorited organization

**Flow**:
1. Call repository.existsByUserIdAndOrganizationId(userId, organizationId)
2. If favorited, get favoritedAt timestamp
3. Build FavoriteCheckResponse
4. Return response

**Returns**: FavoriteCheckResponse

---

**`getFavoriteCount(UUID organizationId)`**

**Purpose**: Get total favorite count for organization

**Flow**:
1. Call repository.countByOrganizationId(organizationId)
2. Return count

**Returns**: Long

---

### 6. Controllers

#### 6.1 SystemSettingController.java
**Location**: `src/main/java/com/seffafbagis/api/controller/system/SystemSettingController.java`

**Purpose**: REST endpoints for system settings

**Class Annotations**:
- @RestController
- @RequestMapping("/api/v1")
- @Tag(name = "System Settings", description = "Platform configuration management")

**Endpoints**:

---

**GET /api/v1/settings/public**

**Purpose**: Get public settings (no auth required)

**Annotations**:
- @GetMapping("/settings/public")
- @Operation(summary = "Get public settings")
- No authentication required

**Logic**:
- Call systemSettingService.getPublicSettings()
- Return ApiResponse<PublicSettingsResponse>

---

**GET /api/v1/admin/settings**

**Purpose**: Get all settings (admin only)

**Annotations**:
- @GetMapping("/admin/settings")
- @Operation(summary = "Get all settings")
- @PreAuthorize("hasRole('ADMIN')")

**Logic**:
- Call systemSettingService.getAllSettings()
- Return ApiResponse<List<SystemSettingResponse>>

---

**GET /api/v1/admin/settings/{key}**

**Purpose**: Get single setting by key (admin only)

**Annotations**:
- @GetMapping("/admin/settings/{key}")
- @Operation(summary = "Get setting by key")
- @PreAuthorize("hasRole('ADMIN')")

**Parameters**:
- @PathVariable String key

**Logic**:
- Call systemSettingService.getSettingByKey(key)
- Return ApiResponse<SystemSettingResponse>

---

**POST /api/v1/admin/settings**

**Purpose**: Create new setting (admin only)

**Annotations**:
- @PostMapping("/admin/settings")
- @Operation(summary = "Create new setting")
- @PreAuthorize("hasRole('ADMIN')")

**Parameters**:
- @Valid @RequestBody CreateSettingRequest request
- @AuthenticationPrincipal CustomUserDetails adminDetails

**Logic**:
- Call systemSettingService.createSetting(request, adminDetails.getId())
- Return ResponseEntity with status 201

---

**PUT /api/v1/admin/settings/{key}**

**Purpose**: Update setting (admin only)

**Annotations**:
- @PutMapping("/admin/settings/{key}")
- @Operation(summary = "Update setting")
- @PreAuthorize("hasRole('ADMIN')")

**Parameters**:
- @PathVariable String key
- @Valid @RequestBody UpdateSettingRequest request
- @AuthenticationPrincipal CustomUserDetails adminDetails

**Logic**:
- Call systemSettingService.updateSetting(key, request, adminDetails.getId())
- Return ApiResponse<SystemSettingResponse>

---

**DELETE /api/v1/admin/settings/{key}**

**Purpose**: Delete setting (admin only)

**Annotations**:
- @DeleteMapping("/admin/settings/{key}")
- @Operation(summary = "Delete setting")
- @PreAuthorize("hasRole('ADMIN')")

**Parameters**:
- @PathVariable String key

**Logic**:
- Call systemSettingService.deleteSetting(key)
- Return ApiResponse with success message

---

#### 6.2 FavoriteOrganizationController.java
**Location**: `src/main/java/com/seffafbagis/api/controller/favorite/FavoriteOrganizationController.java`

**Purpose**: REST endpoints for user favorites

**Class Annotations**:
- @RestController
- @RequestMapping("/api/v1/users/me/favorites")
- @Tag(name = "Favorites", description = "User favorite organizations")
- Requires authentication

**Endpoints**:

---

**GET /api/v1/users/me/favorites**

**Purpose**: Get user's favorite organizations

**Annotations**:
- @GetMapping
- @Operation(summary = "Get favorite organizations")

**Parameters**:
- @AuthenticationPrincipal CustomUserDetails userDetails

**Logic**:
- Call favoriteOrganizationService.getUserFavorites(userDetails.getId())
- Return ApiResponse<List<FavoriteOrganizationResponse>>

---

**POST /api/v1/users/me/favorites/{organizationId}**

**Purpose**: Add organization to favorites

**Annotations**:
- @PostMapping("/{organizationId}")
- @Operation(summary = "Add to favorites")

**Parameters**:
- @PathVariable UUID organizationId
- @AuthenticationPrincipal CustomUserDetails userDetails

**Logic**:
- Call favoriteOrganizationService.addFavorite(userDetails.getId(), organizationId)
- Return ResponseEntity with status 201

---

**DELETE /api/v1/users/me/favorites/{organizationId}**

**Purpose**: Remove organization from favorites

**Annotations**:
- @DeleteMapping("/{organizationId}")
- @Operation(summary = "Remove from favorites")

**Parameters**:
- @PathVariable UUID organizationId
- @AuthenticationPrincipal CustomUserDetails userDetails

**Logic**:
- Call favoriteOrganizationService.removeFavorite(userDetails.getId(), organizationId)
- Return ApiResponse with success message

---

**GET /api/v1/users/me/favorites/check/{organizationId}**

**Purpose**: Check if organization is favorited

**Annotations**:
- @GetMapping("/check/{organizationId}")
- @Operation(summary = "Check if favorited")

**Parameters**:
- @PathVariable UUID organizationId
- @AuthenticationPrincipal CustomUserDetails userDetails

**Logic**:
- Call favoriteOrganizationService.isFavorited(userDetails.getId(), organizationId)
- Return ApiResponse<FavoriteCheckResponse>

---

## FILE STRUCTURE

After completing this phase, the following files should exist:

```
src/main/java/com/seffafbagis/api/
├── dto/
│   ├── request/system/
│   │   ├── CreateSettingRequest.java
│   │   └── UpdateSettingRequest.java
│   └── response/
│       ├── system/
│       │   ├── SystemSettingResponse.java
│       │   └── PublicSettingsResponse.java
│       └── favorite/
│           ├── FavoriteOrganizationResponse.java
│           └── FavoriteCheckResponse.java
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

**Total Files**: 10

---

## DEFAULT SYSTEM SETTINGS

Create initial settings during database seeding or via migration:

| Key | Value | Type | Public | Description |
|-----|-------|------|--------|-------------|
| platform_name | Şeffaf Bağış Platformu | string | true | Platform display name |
| min_donation_amount | 10 | number | true | Minimum donation (TRY) |
| max_donation_amount | 1000000 | number | true | Maximum donation (TRY) |
| evidence_deadline_days | 15 | number | true | Days to upload evidence |
| transparency_score_threshold | 40 | number | true | Min transparency score |
| commission_rate | 0 | number | true | Platform commission % |
| maintenance_mode | false | boolean | true | Maintenance mode flag |
| contact_email | info@seffafbagis.org | string | true | Contact email |
| support_email | destek@seffafbagis.org | string | true | Support email |
| max_file_size_mb | 10 | number | false | Max upload size |
| allowed_file_types | ["jpg","png","pdf"] | json | false | Allowed uploads |

---

## CACHING FLOW DIAGRAM

```
┌─────────────────────────────────────────────────────────────────────┐
│                    SETTINGS CACHING FLOW                             │
└─────────────────────────────────────────────────────────────────────┘

GET Public Settings
        │
        ▼
┌─────────────────────┐
│  Check Redis Cache  │
│  (settings:public)  │
└─────────────────────┘
        │
        ├── Cache HIT ──────────────────────────┐
        │                                        │
        ▼                                        ▼
┌─────────────────────┐               ┌─────────────────────┐
│   Query Database    │               │  Return Cached Data │
│   (findByIsPublic)  │               │                     │
└─────────────────────┘               └─────────────────────┘
        │
        ▼
┌─────────────────────┐
│  Store in Redis     │
│  (TTL: 1 hour)      │
└─────────────────────┘
        │
        ▼
┌─────────────────────┐
│  Return Fresh Data  │
└─────────────────────┘


UPDATE Setting
        │
        ▼
┌─────────────────────┐
│  Update Database    │
└─────────────────────┘
        │
        ▼
┌─────────────────────┐
│  Invalidate Cache   │
│  - Specific key     │
│  - Public cache     │
└─────────────────────┘
        │
        ▼
┌─────────────────────┐
│  Return Updated     │
└─────────────────────┘
```

---

## TESTING REQUIREMENTS

After implementation, verify:

### 1. System Settings Tests

**Test: Get Public Settings - No Auth**
- Access without authentication
- Verify public settings returned
- Verify private settings NOT included

**Test: Caching**
- Get public settings (first call - cache miss)
- Get public settings again (should be cache hit)
- Verify response times

**Test: Create Setting**
- Create new setting as admin
- Verify setting created
- Verify cache invalidated

**Test: Update Setting**
- Update existing setting
- Verify value changed
- Verify cache invalidated

**Test: Delete Setting**
- Delete setting
- Verify setting removed
- Verify cache invalidated

**Test: Duplicate Key**
- Try to create setting with existing key
- Verify 409 Conflict

**Test: Value Type Parsing**
- Create settings with different types
- Verify number parsed correctly
- Verify boolean parsed correctly
- Verify JSON parsed correctly

### 2. Favorites Tests

**Test: Get Favorites - Empty**
- New user with no favorites
- Verify empty list returned

**Test: Add Favorite**
- Add organization to favorites
- Verify favorite created
- Verify response includes org details

**Test: Add Duplicate Favorite**
- Add same org twice
- Verify idempotent (no error, returns existing)

**Test: Remove Favorite**
- Remove favorited org
- Verify favorite removed

**Test: Remove Non-Favorite**
- Remove org not in favorites
- Verify no error (idempotent)

**Test: Check Favorite - True**
- Check favorited org
- Verify isFavorited = true

**Test: Check Favorite - False**
- Check non-favorited org
- Verify isFavorited = false

**Test: Favorite Non-Existent Org**
- Try to favorite non-existent org
- Verify 404 Not Found

### 3. Security Tests

**Test: Admin Endpoints**
- Non-admin tries to create/update/delete setting
- Verify 403 Forbidden

**Test: Public Endpoint**
- Access public settings without auth
- Verify 200 OK

---

## SUCCESS CRITERIA

Phase 11 is considered successful when:

1. ✅ All 10 files are created in correct locations
2. ✅ Public settings accessible without auth
3. ✅ Admin can CRUD settings
4. ✅ Caching works correctly
5. ✅ Cache invalidates on updates
6. ✅ Value type parsing works
7. ✅ Favorites add/remove works
8. ✅ Favorites are idempotent
9. ✅ Favorite check works
10. ✅ Proper error handling

---

## RESULT FILE REQUIREMENTS

After completing this phase, create a result file at:
`docs/Furkan/step_results/phase_11_result.md`

The result file must include:

1. **Execution Status**: Success or Failure
2. **Files Created**: List all 10 files with their paths
3. **Settings Tests**:
   - Public settings access
   - Admin CRUD operations
   - Cache behavior verification
4. **Value Type Tests**:
   - String, number, boolean, JSON parsing
5. **Favorites Tests**:
   - Add/remove flow
   - Idempotency verification
   - Check endpoint results
6. **Performance**:
   - Cache hit/miss timing
7. **Issues Encountered**: Any problems and how they were resolved
8. **Notes for Next Phase**: Observations relevant to Phase 12

---

## API ENDPOINTS SUMMARY

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | /api/v1/settings/public | No | Get public settings |
| GET | /api/v1/admin/settings | Admin | Get all settings |
| GET | /api/v1/admin/settings/{key} | Admin | Get setting by key |
| POST | /api/v1/admin/settings | Admin | Create setting |
| PUT | /api/v1/admin/settings/{key} | Admin | Update setting |
| DELETE | /api/v1/admin/settings/{key} | Admin | Delete setting |
| GET | /api/v1/users/me/favorites | User | Get favorites |
| POST | /api/v1/users/me/favorites/{orgId} | User | Add favorite |
| DELETE | /api/v1/users/me/favorites/{orgId} | User | Remove favorite |
| GET | /api/v1/users/me/favorites/check/{orgId} | User | Check if favorited |

---

## NOTES

- Public settings are frequently accessed - caching is important
- Consider longer TTL for settings that rarely change
- Favorites may be displayed on organization cards - performance matters
- Initial settings should be seeded during deployment

---

## NEXT PHASE PREVIEW

Phase 12 (Audit & Logging) will create:
- Audit log service for KVKK compliance
- Email log service for tracking
- Login history management
- AOP aspect for automatic auditing
