# PHASE 1.0: CATEGORY & DONATION TYPE MODULE

## Context and Background

You are working on the "Şeffaf Bağış Platformu" (Transparent Donation Platform), a comprehensive donation platform for Turkey that emphasizes transparency and accountability in charitable giving. This is a graduation project for Fatih Sultan Mehmet Vakıf University.

**Project Stack:**
- Java 17
- Spring Boot 3.x
- PostgreSQL 15+
- Redis (for caching)
- Maven

**Team Structure:**
- Furkan (~58%): Infrastructure, Auth, User, Admin modules - COMPLETED
- Furkan (~8%): Organization, Campaign -IN PROGRESS 
- Emir (~34) : Donation, Payment, Evidence, Transparency, Application, Category, Notification, Report modules - IN PROGRESS

**Current Phase:** 1.0 - Category & Donation Type Module

This is the FIRST phase of Emir's work. Furkan has already completed the foundational infrastructure including:
- BaseEntity (common entity fields)
- SecurityConfig, JwtTokenProvider, JwtAuthenticationFilter
- GlobalExceptionHandler, ApiResponse, ErrorResponse, PageResponse
- User entities and repositories
- EncryptionService, SlugGenerator, ReferenceCodeGenerator
- All configuration files (application.yml, CorsConfig, RedisConfig, etc.)

---

## Objective

Create the Category and DonationType module which serves as the foundation for Campaigns, Donations, and Applications. Categories organize campaigns (Education, Health, Food, etc.), while DonationTypes define religious/social donation rules (Zakat, Fitr, Sadaka, etc.).

---

## What This Phase Will Solve

1. **Campaign Categorization**: Campaigns need to be organized into categories for filtering and discovery
2. **Hierarchical Categories**: Categories should support parent-child relationships (e.g., Education > Higher Education > Scholarship)
3. **Donation Type Classification**: Donations need type classification for religious compliance (Zakat nisab rules, Fitr calculations)
4. **Predefined Data**: System needs initial seed data for categories and donation types

---

## Database Schema Reference

From the existing `database_schema.sql`, the relevant tables are:

### categories table
```sql
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    name_en VARCHAR(100),
    slug VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    icon_name VARCHAR(50),
    color_code VARCHAR(7),                    -- HEX color
    parent_id UUID REFERENCES categories(id), -- Alt kategoriler için
    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
```

### donation_types table
```sql
CREATE TABLE donation_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type_code donation_type_enum UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    name_en VARCHAR(100),
    description TEXT,
    rules TEXT,                               -- Fıkhi kurallar (zekat nisabı vb.)
    minimum_amount DECIMAL(12,2),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
```

### donation_type_enum
```sql
CREATE TYPE donation_type_enum AS ENUM ('zekat', 'fitre', 'sadaka', 'kurban', 'genel', 'afet');
```

---

## Files to Create

### 1. Enum
**Location:** `src/main/java/com/seffafbagis/api/enums/DonationTypeCode.java`

Create enum matching the database `donation_type_enum`:
- ZEKAT
- FITRE
- SADAKA
- KURBAN
- GENEL
- AFET

Use `@Enumerated(EnumType.STRING)` for JPA mapping.

---

### 2. Entities

**Location:** `src/main/java/com/seffafbagis/api/entity/category/`

#### Category.java
- Extend BaseEntity (already created by Furkan)
- Fields: name, nameEn, slug, description, iconName, colorCode, parentId, displayOrder, isActive, createdAt
- Self-referential relationship for parent-child hierarchy
- Use `@ManyToOne` for parent and `@OneToMany` for children
- Add proper indexes with `@Table(indexes = {...})`

#### DonationType.java
- Extend BaseEntity
- Fields: typeCode (enum), name, nameEn, description, rules, minimumAmount, isActive, createdAt
- Use `@Enumerated(EnumType.STRING)` for typeCode
- `@Column(unique = true)` for typeCode

---

### 3. Repositories

**Location:** `src/main/java/com/seffafbagis/api/repository/`

#### CategoryRepository.java
```java
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findBySlug(String slug);
    List<Category> findByIsActiveTrueOrderByDisplayOrderAsc();
    List<Category> findByParentIdIsNullAndIsActiveTrueOrderByDisplayOrderAsc();
    List<Category> findByParentIdAndIsActiveTrueOrderByDisplayOrderAsc(UUID parentId);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, UUID id);
}
```

#### DonationTypeRepository.java
```java
public interface DonationTypeRepository extends JpaRepository<DonationType, UUID> {
    Optional<DonationType> findByTypeCode(DonationTypeCode typeCode);
    List<DonationType> findByIsActiveTrueOrderByNameAsc();
    boolean existsByTypeCode(DonationTypeCode typeCode);
}
```

---

### 4. DTOs

**Location:** `src/main/java/com/seffafbagis/api/dto/`

#### Request DTOs (`request/category/`)

**CreateCategoryRequest.java**
- name (required, max 100)
- nameEn (optional, max 100)
- description (optional)
- iconName (optional, max 50)
- colorCode (optional, max 7, hex format validation)
- parentId (optional, UUID)
- displayOrder (optional, default 0)

**UpdateCategoryRequest.java**
- Same fields as create, all optional for partial update

#### Response DTOs (`response/category/`)

**CategoryResponse.java**
- id, name, nameEn, slug, description, iconName, colorCode, displayOrder, isActive, createdAt
- parentId (if has parent)

**CategoryTreeResponse.java**
- Same as CategoryResponse
- Plus: `List<CategoryTreeResponse> children` for nested structure

**DonationTypeResponse.java**
- id, typeCode, name, nameEn, description, rules, minimumAmount, isActive, createdAt

---

### 5. Mapper

**Location:** `src/main/java/com/seffafbagis/api/dto/mapper/CategoryMapper.java`

Use MapStruct or manual mapping:
- `Category toEntity(CreateCategoryRequest request)`
- `CategoryResponse toResponse(Category entity)`
- `CategoryTreeResponse toTreeResponse(Category entity)`
- `DonationTypeResponse toResponse(DonationType entity)`
- `List<CategoryResponse> toResponseList(List<Category> entities)`

---

### 6. Services

**Location:** `src/main/java/com/seffafbagis/api/service/category/`

#### CategoryService.java
Methods:
- `List<CategoryResponse> getAllActiveCategories()`
- `List<CategoryTreeResponse> getCategoryTree()`
- `CategoryResponse getCategoryById(UUID id)`
- `CategoryResponse getCategoryBySlug(String slug)`
- `CategoryResponse createCategory(CreateCategoryRequest request)` - ADMIN only
- `CategoryResponse updateCategory(UUID id, UpdateCategoryRequest request)` - ADMIN only
- `void deactivateCategory(UUID id)` - ADMIN only (soft delete)

Business Logic:
- Auto-generate slug from name using SlugGenerator (from Furkan's utilities)
- Ensure slug uniqueness (append number if exists)
- Validate parent exists if parentId provided
- Cannot deactivate category with active children

#### DonationTypeService.java
Methods:
- `List<DonationTypeResponse> getAllDonationTypes()`
- `List<DonationTypeResponse> getActiveDonationTypes()`
- `DonationTypeResponse getByTypeCode(DonationTypeCode code)`

Note: DonationTypes are predefined and should not have create/update endpoints. They are seeded via migration.

---

### 7. Controllers

**Location:** `src/main/java/com/seffafbagis/api/controller/category/`

#### CategoryController.java
```
GET    /api/v1/categories                  - List all active categories (PUBLIC)
GET    /api/v1/categories/tree             - Get category tree structure (PUBLIC)
GET    /api/v1/categories/{id}             - Get category by ID (PUBLIC)
GET    /api/v1/categories/slug/{slug}      - Get category by slug (PUBLIC)
POST   /api/v1/categories                  - Create category (ADMIN only)
PUT    /api/v1/categories/{id}             - Update category (ADMIN only)
DELETE /api/v1/categories/{id}             - Deactivate category (ADMIN only)
```

#### DonationTypeController.java
```
GET    /api/v1/donation-types              - List all donation types (PUBLIC)
GET    /api/v1/donation-types/active       - List active types only (PUBLIC)
GET    /api/v1/donation-types/{code}       - Get by type code (PUBLIC)
```

---

### 8. Database Migration for Seed Data

**Location:** `src/main/resources/db/migration/V16__seed_categories_and_donation_types.sql`

Note: Check existing migration files and use the next available version number.

Seed data to include:

**Categories:**
```sql
-- Main categories
INSERT INTO categories (id, name, name_en, slug, description, icon_name, color_code, display_order, is_active) VALUES
(gen_random_uuid(), 'Eğitim', 'Education', 'egitim', 'Eğitim alanındaki yardım kampanyaları', 'school', '#4CAF50', 1, true),
(gen_random_uuid(), 'Sağlık', 'Health', 'saglik', 'Sağlık alanındaki yardım kampanyaları', 'medical', '#F44336', 2, true),
(gen_random_uuid(), 'Gıda', 'Food', 'gida', 'Gıda yardımı kampanyaları', 'food', '#FF9800', 3, true),
(gen_random_uuid(), 'Barınma', 'Shelter', 'barinma', 'Barınma ve konut yardımları', 'home', '#2196F3', 4, true),
(gen_random_uuid(), 'Afet Yardımı', 'Disaster Relief', 'afet-yardimi', 'Doğal afet yardım kampanyaları', 'emergency', '#9C27B0', 5, true),
(gen_random_uuid(), 'Çocuk', 'Children', 'cocuk', 'Çocuklara yönelik yardımlar', 'child', '#E91E63', 6, true),
(gen_random_uuid(), 'Yaşlı', 'Elderly', 'yasli', 'Yaşlılara yönelik yardımlar', 'elderly', '#607D8B', 7, true),
(gen_random_uuid(), 'Engelli', 'Disabled', 'engelli', 'Engelli bireylere yönelik yardımlar', 'accessible', '#795548', 8, true);
```

**Donation Types:**
```sql
INSERT INTO donation_types (id, type_code, name, name_en, description, rules, minimum_amount, is_active) VALUES
(gen_random_uuid(), 'zekat', 'Zekât', 'Zakat', 'İslami farz olan mal zekâtı', 'Nisab miktarı: 85 gram altın veya eşdeğeri. Üzerinden 1 yıl geçmiş mal varlığının %2.5''i verilir.', 0, true),
(gen_random_uuid(), 'fitre', 'Fitre', 'Fitr', 'Ramazan ayında verilen sadaka-i fıtır', 'Ramazan bayramından önce verilmesi gerekir. Bir kişinin bir günlük yiyeceği miktarıdır.', 0, true),
(gen_random_uuid(), 'sadaka', 'Sadaka', 'Sadaqah', 'İsteğe bağlı hayır bağışı', 'Herhangi bir miktar sınırı yoktur. Gönüllü olarak yapılan her türlü yardımdır.', 0, true),
(gen_random_uuid(), 'kurban', 'Kurban', 'Qurbani', 'Kurban bayramı bağışı', 'Kurban bayramında kesilen kurban bağışıdır. Nisab miktarına sahip Müslümanlara vaciptir.', 0, true),
(gen_random_uuid(), 'genel', 'Genel Bağış', 'General Donation', 'Genel amaçlı bağış', 'Herhangi bir dini veya sosyal kısıtlaması olmayan genel bağış türüdür.', 0, true),
(gen_random_uuid(), 'afet', 'Afet Bağışı', 'Disaster Donation', 'Afet durumlarına özel bağış', 'Deprem, sel, yangın gibi afet durumlarında yapılan acil yardım bağışlarıdır.', 0, true);
```

---

## Validation Rules

### Category Validation
- name: Required, 1-100 characters
- slug: Auto-generated, must be unique
- colorCode: If provided, must match hex format (#RRGGBB)
- parentId: If provided, must reference existing active category

### DonationType
- Read-only via API (no validation needed for create/update)

---

## Security Configuration

Use existing security from Furkan's implementation:
- Public endpoints: No authentication required
- Admin endpoints: Require ADMIN role

```java
@PreAuthorize("hasRole('ADMIN')")
public CategoryResponse createCategory(...) { }
```

---

## Testing Requirements

After implementation, create tests:

### Unit Tests
**Location:** `src/test/java/com/seffafbagis/api/service/category/`

1. `CategoryServiceTest.java`
   - Test getAllActiveCategories returns only active
   - Test getCategoryTree builds proper hierarchy
   - Test createCategory generates unique slug
   - Test createCategory with parent links correctly
   - Test deactivateCategory with children throws exception

2. `DonationTypeServiceTest.java`
   - Test getActiveDonationTypes returns only active
   - Test getByTypeCode returns correct type

### Integration Tests
**Location:** `src/test/java/com/seffafbagis/api/integration/`

1. `CategoryIntegrationTest.java`
   - Test full CRUD flow
   - Test tree structure endpoint
   - Test admin authorization

---

## Success Criteria

Before completing this phase, verify:

- [ ] DonationTypeCode enum created with all 6 values
- [ ] Category entity with self-referential parent-child relationship
- [ ] DonationType entity with enum type code
- [ ] Both repositories with custom query methods
- [ ] All DTOs created with proper validation annotations
- [ ] CategoryMapper handles all conversions including tree structure
- [ ] CategoryService generates unique slugs automatically
- [ ] CategoryService prevents deactivating categories with active children
- [ ] DonationTypeService provides read-only access
- [ ] CategoryController exposes all endpoints with proper authorization
- [ ] DonationTypeController exposes read-only endpoints
- [ ] Migration file seeds initial data
- [ ] All unit tests pass
- [ ] Application starts without errors
- [ ] Swagger UI shows all endpoints correctly

---

## Result File Requirement

After completing this phase, you MUST create a result file at:

**Location:** `docs/Emir/step_results/phase_1.0_result.md`

The result file must include:

1. **Summary**: Brief description of what was accomplished
2. **Files Created**: List all files created with their paths
3. **Database Changes**: Any migrations added
4. **API Endpoints**: List of all endpoints with their HTTP methods
5. **Testing Results**: Which tests were run and their status
6. **Issues Encountered**: Any problems faced and how they were resolved
7. **Next Steps**: What needs to be done in the next phase
8. **Checklist**: Mark all success criteria as completed or note what's pending

Example format:
```markdown
# Phase 1.0 Result: Category & Donation Type Module

## Summary
[Brief description]

## Files Created
- `src/main/java/com/seffafbagis/api/enums/DonationTypeCode.java`
- [... list all files]

## Database Changes
- Added migration: `V16__seed_categories_and_donation_types.sql`

## API Endpoints
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | /api/v1/categories | List categories | Public |
| [... list all] |

## Testing Results
- CategoryServiceTest: ✅ All tests passed
- [... list all test results]

## Issues Encountered
[Any issues and resolutions]

## Next Steps
- Phase 2.0: Organization Module - Entities & Repository

## Success Criteria Checklist
- [x] DonationTypeCode enum created
- [x] Category entity created
- [... complete checklist]
```

---

## Important Notes

1. **Do NOT use ternary operators** - Use if-else statements for better readability
2. **Follow existing code style** - Match patterns from Furkan's completed work
3. **Use existing utilities** - SlugGenerator is already available from Furkan's util package
4. **Check migration version** - Verify the next available migration version number before creating
5. **Test database connectivity** - Ensure PostgreSQL is running and accessible
6. **Verify Redis connectivity** - Caching should work for category tree

---

## Dependencies from Furkan's Work

You will need to use these existing components:
- `BaseEntity` from `entity/base/BaseEntity.java`
- `SlugGenerator` from `util/SlugGenerator.java`
- `ApiResponse` from `dto/response/common/ApiResponse.java`
- `PageResponse` from `dto/response/common/PageResponse.java`
- `ResourceNotFoundException` from `exception/ResourceNotFoundException.java`
- `BadRequestException` from `exception/BadRequestException.java`

---

## Estimated Duration

2 days

---

## Next Phase

After completing this phase, proceed to:
**Phase 2.0: Organization Module - Entities & Repository**
