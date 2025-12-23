# PHASE 1: PROJECT FOUNDATION & CONFIGURATION

## CONTEXT AND BACKGROUND

You are working on "Şeffaf Bağış Platformu" (Transparent Donation Platform), a graduation project for Fatih Sultan Mehmet Vakıf University. This is a comprehensive donation platform for Turkey that emphasizes transparency and accountability in charitable giving.

### Project Overview
- **Technology Stack**: Java 17, Spring Boot 3.x, PostgreSQL 15+, Redis, Maven
- **Purpose**: Enable foundations to create campaigns, donors to contribute, and maintain transparency through evidence uploads and scoring systems
- **Compliance Requirements**: KVKK (Turkish Personal Data Protection Law) compliant
- **Developer**: Furkan (responsible for Infrastructure, Auth, User, Admin modules - ~58% of backend)

### Current State
- Phase 0 (Database Migration) has been completed
- Database schema is ready with all required tables and columns
- Project directory structure exists but contains no code
- Maven pom.xml is configured with required dependencies

### What This Phase Accomplishes
This phase establishes the core configuration infrastructure that ALL subsequent phases depend on. Without proper configuration, no other component can function correctly.

---

## OBJECTIVE

Create the complete project foundation including:
1. Application property files for all environments
2. Core configuration classes
3. Base entity for JPA inheritance
4. Internationalization (i18n) message files

---

## IMPORTANT RULES

### Code Style Requirements
- Use clear, readable code - prefer if-else statements over ternary operators
- Follow SOLID principles
- Add comprehensive comments in English
- Use meaningful variable and method names
- No complex one-liners - prioritize readability over brevity

### Project Standards
- All classes must have proper package declarations
- All configuration classes must be annotated with @Configuration
- Use constructor injection over field injection
- All properties must have sensible defaults
- Sensitive data (passwords, secrets) must come from environment variables

### Documentation Requirements
- Every class must have a JavaDoc comment explaining its purpose
- Every public method must have JavaDoc
- Configuration properties must have inline comments

---

## DETAILED REQUIREMENTS

### 1. Application Properties Files

#### 1.1 Main Configuration: `application.yml`
**Location**: `src/main/resources/application.yml`

This is the main configuration file with common settings shared across all environments.

**Required Configuration Sections**:

**Server Configuration**:
- Port: 8080
- Context path: /api
- Servlet encoding: UTF-8

**Spring Application**:
- Application name: seffaf-bagis-api
- Active profile: from environment variable with default 'dev'

**Database (PostgreSQL)**:
- URL: from environment variable SPRING_DATASOURCE_URL
- Username: from environment variable SPRING_DATASOURCE_USERNAME  
- Password: from environment variable SPRING_DATASOURCE_PASSWORD
- Driver: org.postgresql.Driver
- Hikari pool settings: minimum-idle=5, maximum-pool-size=20, idle-timeout=300000

**JPA/Hibernate**:
- DDL-auto: validate (Flyway handles migrations)
- Show SQL: false (override in dev)
- Dialect: PostgreSQL
- Open-in-view: false
- Physical naming strategy: CamelCaseToUnderscoresNamingStrategy

**Flyway**:
- Enabled: true
- Locations: classpath:db/migration
- Baseline-on-migrate: true

**Redis**:
- Host: from environment variable REDIS_HOST with default localhost
- Port: from environment variable REDIS_PORT with default 6379
- Password: from environment variable REDIS_PASSWORD (optional)
- Timeout: 60000ms

**JWT Configuration** (custom properties under 'app.jwt'):
- Secret: from environment variable JWT_SECRET
- Access token expiration: 900000 (15 minutes in milliseconds)
- Refresh token expiration: 604800000 (7 days in milliseconds)
- Token prefix: "Bearer "
- Header name: "Authorization"

**Mail Configuration**:
- Host: from environment variable MAIL_HOST
- Port: from environment variable MAIL_PORT with default 587
- Username: from environment variable MAIL_USERNAME
- Password: from environment variable MAIL_PASSWORD
- Properties: SMTP auth=true, starttls.enable=true
- Default from address: from environment variable MAIL_FROM with default "noreply@seffafbagis.org"

**File Upload**:
- Max file size: 10MB
- Max request size: 50MB
- Upload directory: from environment variable UPLOAD_DIR with default "./uploads"

**Logging**:
- Root level: INFO
- Application package level: INFO
- SQL logging: WARN

**Encryption** (custom properties under 'app.encryption'):
- Secret key: from environment variable ENCRYPTION_SECRET_KEY
- Algorithm: AES/GCM/NoPadding

**CORS** (custom properties under 'app.cors'):
- Allowed origins: from environment variable CORS_ALLOWED_ORIGINS with default "http://localhost:3000"
- Allowed methods: GET, POST, PUT, DELETE, PATCH, OPTIONS
- Allowed headers: "*"
- Allow credentials: true
- Max age: 3600

---

#### 1.2 Development Configuration: `application-dev.yml`
**Location**: `src/main/resources/application-dev.yml`

Development-specific overrides:

- Show SQL: true
- Format SQL: true
- Log level for application: DEBUG
- Log level for SQL: DEBUG
- Swagger/OpenAPI enabled: true
- CORS allowed origins: http://localhost:3000, http://127.0.0.1:3000

---

#### 1.3 Production Configuration: `application-prod.yml`
**Location**: `src/main/resources/application-prod.yml`

Production-specific settings:

- Show SQL: false
- Log level: WARN
- Swagger/OpenAPI enabled: false
- Hikari pool: minimum-idle=10, maximum-pool-size=50
- Additional security headers enabled

---

#### 1.4 Test Configuration: `application-test.yml`
**Location**: `src/main/resources/application-test.yml`

Test environment settings:

- Use H2 in-memory database for tests
- DDL-auto: create-drop
- Show SQL: true
- Flyway disabled (use JPA auto-create for tests)
- Embedded Redis or mock Redis

---

### 2. Configuration Classes

#### 2.1 CORS Configuration: `CorsConfig.java`
**Location**: `src/main/java/com/seffafbagis/api/config/CorsConfig.java`

**Purpose**: Configure Cross-Origin Resource Sharing for frontend access

**Requirements**:
- Read allowed origins from application properties
- Configure allowed methods: GET, POST, PUT, DELETE, PATCH, OPTIONS
- Allow all headers
- Allow credentials
- Set max age for preflight cache
- Apply to all endpoints (/**)
- Implement WebMvcConfigurer interface

---

#### 2.2 Redis Configuration: `RedisConfig.java`
**Location**: `src/main/java/com/seffafbagis/api/config/RedisConfig.java`

**Purpose**: Configure Redis connection and serialization

**Requirements**:
- Create RedisConnectionFactory bean
- Create RedisTemplate bean with proper serializers
- Use StringRedisSerializer for keys
- Use GenericJackson2JsonRedisSerializer for values
- Create CacheManager bean for @Cacheable support
- Configure cache TTL defaults (1 hour)
- Handle Redis connection failures gracefully

---

#### 2.3 OpenAPI/Swagger Configuration: `OpenApiConfig.java`
**Location**: `src/main/java/com/seffafbagis/api/config/OpenApiConfig.java`

**Purpose**: Configure API documentation

**Requirements**:
- Set API title: "Şeffaf Bağış Platformu API"
- Set API description: "Transparent Donation Platform REST API Documentation"
- Set version: "1.0.0"
- Set contact info: Furkan, project email
- Configure JWT security scheme (Bearer token)
- Add security requirement to all endpoints
- Group endpoints by tags (Auth, User, Admin, etc.)
- Set license: MIT

---

#### 2.4 Audit Configuration: `AuditConfig.java`
**Location**: `src/main/java/com/seffafbagis/api/config/AuditConfig.java`

**Purpose**: Enable JPA Auditing for automatic timestamp management

**Requirements**:
- Enable JPA Auditing with @EnableJpaAuditing
- Create AuditorAware bean for tracking who made changes
- Get current user from SecurityContext
- Return "system" if no authenticated user (for scheduled tasks)

---

#### 2.5 JWT Configuration: `JwtConfig.java`
**Location**: `src/main/java/com/seffafbagis/api/config/JwtConfig.java`

**Purpose**: Hold JWT configuration properties

**Requirements**:
- Use @ConfigurationProperties with prefix "app.jwt"
- Properties: secret, accessTokenExpiration, refreshTokenExpiration, tokenPrefix, headerName
- Validate that secret is at least 256 bits (32 characters)
- Add validation annotations
- Make immutable (final fields, constructor injection)

---

#### 2.6 Mail Configuration: `MailConfig.java`
**Location**: `src/main/java/com/seffafbagis/api/config/MailConfig.java`

**Purpose**: Configure JavaMailSender for sending emails

**Requirements**:
- Create JavaMailSender bean
- Configure SMTP properties
- Enable TLS
- Configure authentication
- Handle mail server connection failures gracefully
- Log mail configuration on startup (without passwords)

---

### 3. Base Entity

#### 3.1 BaseEntity: `BaseEntity.java`
**Location**: `src/main/java/com/seffafbagis/api/entity/base/BaseEntity.java`

**Purpose**: Abstract base class for all entities with common fields

**Requirements**:
- Abstract class annotated with @MappedSuperclass
- UUID id field with @Id and auto-generation
- createdAt field with @CreatedDate annotation
- updatedAt field with @LastModifiedDate annotation
- Use TIMESTAMPTZ for timestamp columns
- Implement equals() and hashCode() based on id
- Make id protected so subclasses can access
- Do NOT include audit user fields (handled separately)

---

### 4. Internationalization (i18n) Files

#### 4.1 Turkish Messages: `messages.properties`
**Location**: `src/main/resources/messages.properties`

**Purpose**: Default messages in Turkish

**Required Message Categories**:

**Validation Messages**:
- validation.required={0} alanı zorunludur
- validation.email.invalid=Geçersiz e-posta adresi
- validation.password.weak=Şifre en az 8 karakter, bir büyük harf, bir küçük harf, bir rakam ve bir özel karakter içermelidir
- validation.phone.invalid=Geçersiz telefon numarası
- validation.tc.invalid=Geçersiz TC Kimlik numarası
- validation.iban.invalid=Geçersiz IBAN

**Auth Messages**:
- auth.login.success=Giriş başarılı
- auth.login.failed=E-posta veya şifre hatalı
- auth.logout.success=Çıkış başarılı
- auth.register.success=Kayıt başarılı. Lütfen e-posta adresinizi doğrulayın
- auth.email.not.verified=E-posta adresi doğrulanmamış
- auth.account.locked=Hesabınız geçici olarak kilitlendi. Lütfen {0} dakika sonra tekrar deneyin
- auth.account.suspended=Hesabınız askıya alınmış
- auth.token.invalid=Geçersiz veya süresi dolmuş token
- auth.token.expired=Token süresi dolmuş
- auth.password.reset.sent=Şifre sıfırlama bağlantısı e-posta adresinize gönderildi
- auth.password.reset.success=Şifreniz başarıyla değiştirildi
- auth.password.mismatch=Mevcut şifre hatalı

**User Messages**:
- user.not.found=Kullanıcı bulunamadı
- user.email.exists=Bu e-posta adresi zaten kayıtlı
- user.profile.updated=Profil güncellendi
- user.preferences.updated=Tercihler güncellendi
- user.deleted=Hesabınız silindi

**Error Messages**:
- error.generic=Bir hata oluştu. Lütfen daha sonra tekrar deneyin
- error.not.found={0} bulunamadı
- error.forbidden=Bu işlem için yetkiniz yok
- error.unauthorized=Lütfen giriş yapın
- error.validation=Doğrulama hatası
- error.conflict={0} zaten mevcut

**Success Messages**:
- success.created={0} başarıyla oluşturuldu
- success.updated={0} başarıyla güncellendi
- success.deleted={0} başarıyla silindi

---

#### 4.2 English Messages: `messages_en.properties`
**Location**: `src/main/resources/messages_en.properties`

**Purpose**: English translations

Same keys as Turkish with English values.

---

### 5. Main Application Class Update

#### 5.1 SeffafBagisApplication.java
**Location**: `src/main/java/com/seffafbagis/api/SeffafBagisApplication.java`

If not already existing, ensure the main application class:
- Has @SpringBootApplication annotation
- Has @EnableConfigurationProperties annotation for JwtConfig
- Has proper package declaration
- Logs startup information

---

## FILE STRUCTURE

After completing this phase, the following files should exist:

```
src/main/
├── java/com/seffafbagis/api/
│   ├── SeffafBagisApplication.java
│   ├── config/
│   │   ├── CorsConfig.java
│   │   ├── RedisConfig.java
│   │   ├── OpenApiConfig.java
│   │   ├── AuditConfig.java
│   │   ├── JwtConfig.java
│   │   └── MailConfig.java
│   └── entity/base/
│       └── BaseEntity.java
└── resources/
    ├── application.yml
    ├── application-dev.yml
    ├── application-prod.yml
    ├── application-test.yml
    ├── messages.properties
    └── messages_en.properties
```

**Total Files**: 13

---

## TESTING REQUIREMENTS

After implementation, verify:

### 1. Application Startup Test
- Run `mvn spring-boot:run -Dspring.profiles.active=dev`
- Application should start without errors
- Check logs for:
  - "Started SeffafBagisApplication"
  - No configuration errors
  - Database connection established
  - Redis connection established (or graceful failure if Redis not running)

### 2. Swagger UI Test
- Navigate to http://localhost:8080/api/swagger-ui.html
- Swagger UI should load
- API documentation should be visible
- JWT security scheme should be configured

### 3. Configuration Validation
- All environment variables should have defaults for development
- No sensitive data in property files (use ${ENV_VAR} syntax)
- CORS should allow requests from localhost:3000

### 4. Profile Test
- Test with different profiles: dev, prod, test
- Verify profile-specific settings are applied

---

## ENVIRONMENT VARIABLES

Create a `.env.example` file documenting required environment variables:

```
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/seffaf_bagis_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# JWT
JWT_SECRET=your-256-bit-secret-key-minimum-32-characters-long

# Mail
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_FROM=noreply@seffafbagis.org

# Encryption
ENCRYPTION_SECRET_KEY=your-32-character-encryption-key

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000

# File Upload
UPLOAD_DIR=./uploads
```

---

## SUCCESS CRITERIA

Phase 1 is considered successful when:

1. ✅ All 13 files are created in correct locations
2. ✅ Application starts successfully with `mvn spring-boot:run`
3. ✅ No configuration errors in startup logs
4. ✅ Swagger UI accessible and displays API documentation
5. ✅ Database connection works (check with health endpoint or logs)
6. ✅ Redis connection attempted (graceful failure OK if Redis not running)
7. ✅ CORS configuration allows frontend origin
8. ✅ All environment variables have sensible defaults for development
9. ✅ No sensitive data hardcoded in property files
10. ✅ BaseEntity compiles without errors

---

## RESULT FILE REQUIREMENTS

After completing this phase, create a result file at:
`docs/Furkan/step_results/phase_1_result.md`

The result file must include:

1. **Execution Status**: Success or Failure
2. **Files Created**: List all 13 files with their paths
3. **Startup Verification**: 
   - Application startup logs (relevant portions)
   - Any warnings or errors encountered
4. **Swagger Verification**: Confirm Swagger UI is accessible
5. **Configuration Verification**:
   - List of environment variables used
   - Confirmation of default values working
6. **Issues Encountered**: Any problems and how they were resolved
7. **Notes for Next Phase**: Any observations relevant to Phase 2

---

## DEPENDENCIES FOR NEXT PHASE

Phase 2 (Security Infrastructure) depends on:
- JwtConfig.java (for JWT settings)
- BaseEntity.java (for entity inheritance)
- Application startup working correctly

Ensure these are fully functional before proceeding.

---

## NOTES

- This phase creates the foundation - take extra care with property names and paths
- All subsequent phases will import these configurations
- Test thoroughly before moving to Phase 2
- If Redis is not available locally, the application should still start (with caching disabled)
