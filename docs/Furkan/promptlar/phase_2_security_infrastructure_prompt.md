# PHASE 2: SECURITY INFRASTRUCTURE

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
- All configuration files are in place
- JwtConfig.java is available for JWT settings
- BaseEntity.java is available for entity inheritance
- Application starts successfully

### What This Phase Accomplishes
This phase implements the complete Spring Security infrastructure including JWT token generation, validation, and authentication filters. This is the MOST CRITICAL security foundation - every authenticated request will pass through these components.

---

## OBJECTIVE

Create the complete security infrastructure including:
1. Spring Security configuration with JWT-based stateless authentication
2. JWT token provider for generating and validating tokens
3. Authentication filter for processing JWT tokens on every request
4. Custom UserDetails implementation for Spring Security
5. Security utilities for accessing current user context

---

## IMPORTANT RULES

### Code Style Requirements
- Use clear, readable code - prefer if-else statements over ternary operators
- Follow SOLID principles
- Add comprehensive comments in English
- Use meaningful variable and method names
- No complex one-liners - prioritize readability over brevity

### Security Requirements
- NEVER log sensitive information (tokens, passwords)
- All passwords must be hashed with BCrypt
- JWT tokens must be signed with HMAC-SHA512
- Token validation must check expiration, signature, and claims
- Failed authentication attempts must return consistent error messages (prevent user enumeration)

### Project Standards
- All classes must have proper package declarations
- Use constructor injection over field injection
- Handle all exceptions gracefully
- Return proper HTTP status codes

---

## DETAILED REQUIREMENTS

### 1. Security Configuration

#### 1.1 SecurityConfig.java
**Location**: `src/main/java/com/seffafbagis/api/config/SecurityConfig.java`

**Purpose**: Main Spring Security configuration that defines the security filter chain and endpoint permissions.

**Requirements**:

**Class Structure**:
- Annotate with @Configuration and @EnableWebSecurity
- Annotate with @EnableMethodSecurity for method-level security
- Inject JwtAuthenticationFilter, JwtAuthenticationEntryPoint, CustomUserDetailsService

**Password Encoder Bean**:
- Create BCryptPasswordEncoder bean with strength 12

**Security Filter Chain Configuration**:
- Disable CSRF (stateless API)
- Set session management to STATELESS
- Configure exception handling with JwtAuthenticationEntryPoint
- Add JwtAuthenticationFilter before UsernamePasswordAuthenticationFilter

**Endpoint Permissions**:

Public endpoints (permitAll):
- POST /api/v1/auth/register
- POST /api/v1/auth/login
- POST /api/v1/auth/refresh
- POST /api/v1/auth/forgot-password
- POST /api/v1/auth/reset-password
- POST /api/v1/auth/verify-email
- GET /api/v1/settings/public
- GET /api/v1/campaigns/** (public campaign viewing)
- GET /api/v1/organizations/** (public org viewing)
- GET /api/v1/categories/** (public categories)
- Swagger UI paths: /swagger-ui/**, /v3/api-docs/**, /swagger-resources/**
- Health check: /actuator/health

Admin only endpoints (hasRole ADMIN):
- /api/v1/admin/**

Foundation only endpoints (hasRole FOUNDATION):
- /api/v1/foundation/**

Authenticated endpoints (authenticated):
- All other endpoints require authentication

**Authentication Manager Bean**:
- Create AuthenticationManager bean using AuthenticationConfiguration

**CORS Configuration**:
- Reference the CorsConfig from Phase 1

---

### 2. JWT Components

#### 2.1 JwtTokenProvider.java
**Location**: `src/main/java/com/seffafbagis/api/security/JwtTokenProvider.java`

**Purpose**: Generate, validate, and parse JWT tokens.

**Requirements**:

**Class Structure**:
- Annotate with @Component
- Inject JwtConfig for configuration values
- Use io.jsonwebtoken (JJWT) library

**Token Generation Method** - `generateAccessToken(CustomUserDetails userDetails)`:
- Create JWT with claims:
  - subject: user ID (UUID as string)
  - email: user email
  - role: user role
  - issuedAt: current timestamp
  - expiration: current time + accessTokenExpiration
- Sign with HMAC-SHA512 using secret from JwtConfig
- Return token string

**Refresh Token Generation** - `generateRefreshToken(CustomUserDetails userDetails)`:
- Similar to access token but with longer expiration
- Include only essential claims (subject, issuedAt, expiration)
- Add claim "type": "refresh" to distinguish from access tokens

**Token Validation Method** - `validateToken(String token)`:
- Parse token and verify signature
- Check expiration
- Return true if valid, false if invalid
- Catch and handle specific exceptions:
  - ExpiredJwtException: token expired
  - MalformedJwtException: invalid token format
  - SignatureException: invalid signature
  - IllegalArgumentException: empty token
- Log validation failures at DEBUG level (no sensitive data)

**Get Claims Method** - `getClaims(String token)`:
- Parse token and return Claims object
- Used for extracting user information

**Get User ID Method** - `getUserIdFromToken(String token)`:
- Extract subject (user ID) from token
- Return as UUID

**Get Email Method** - `getEmailFromToken(String token)`:
- Extract email claim from token

**Get Role Method** - `getRoleFromToken(String token)`:
- Extract role claim from token

**Get Expiration Method** - `getExpirationFromToken(String token)`:
- Extract expiration date from token

**Token Type Check** - `isRefreshToken(String token)`:
- Check if token has "type": "refresh" claim

---

#### 2.2 JwtAuthenticationFilter.java
**Location**: `src/main/java/com/seffafbagis/api/security/JwtAuthenticationFilter.java`

**Purpose**: Filter that extracts and validates JWT token from every request.

**Requirements**:

**Class Structure**:
- Extend OncePerRequestFilter
- Annotate with @Component
- Inject JwtTokenProvider and CustomUserDetailsService

**Filter Logic** - `doFilterInternal(request, response, filterChain)`:

Step 1: Extract token from request
- Get Authorization header
- Check if header exists and starts with "Bearer "
- Extract token (remove "Bearer " prefix)
- If no token, continue filter chain without authentication

Step 2: Validate token
- Call jwtTokenProvider.validateToken(token)
- If invalid, continue filter chain without authentication

Step 3: Load user details
- Extract user ID from token
- Load user using CustomUserDetailsService
- If user not found, continue filter chain without authentication

Step 4: Set authentication
- Create UsernamePasswordAuthenticationToken with userDetails and authorities
- Set details with WebAuthenticationDetailsSource
- Set authentication in SecurityContextHolder

Step 5: Continue filter chain
- Call filterChain.doFilter(request, response)

**Skip Filter for Certain Paths** - `shouldNotFilter(request)`:
- Return true for public paths (login, register, etc.)
- This is an optimization to avoid unnecessary processing

**Extract Token Helper** - `extractTokenFromRequest(request)`:
- Get Authorization header
- Validate format
- Return token or null

---

#### 2.3 JwtAuthenticationEntryPoint.java
**Location**: `src/main/java/com/seffafbagis/api/security/JwtAuthenticationEntryPoint.java`

**Purpose**: Handle unauthorized access attempts (when authentication fails or is missing).

**Requirements**:

**Class Structure**:
- Implement AuthenticationEntryPoint
- Annotate with @Component
- Inject ObjectMapper for JSON serialization

**Commence Method** - `commence(request, response, authException)`:
- Set response status to 401 (UNAUTHORIZED)
- Set content type to application/json
- Create error response object with:
  - success: false
  - error code: "UNAUTHORIZED"
  - message: "Authentication required. Please login."
  - timestamp: current time
  - path: request URI
- Write JSON response using ObjectMapper
- Log authentication failure at DEBUG level

---

### 3. User Details Components

#### 3.1 CustomUserDetails.java
**Location**: `src/main/java/com/seffafbagis/api/security/CustomUserDetails.java`

**Purpose**: Spring Security UserDetails implementation that wraps our User entity.

**Requirements**:

**Class Structure**:
- Implement UserDetails interface
- Store user information needed for authentication

**Fields**:
- id: UUID
- email: String
- password: String (hashed)
- role: UserRole enum
- status: UserStatus enum
- emailVerified: boolean

**Constructor**:
- Accept all fields
- This will be created from User entity in CustomUserDetailsService

**Static Factory Method** - `fromUser(User user)`:
- Create CustomUserDetails from User entity
- Map all required fields

**UserDetails Interface Methods**:

`getAuthorities()`:
- Return collection with single authority: "ROLE_" + role.name()
- Example: ROLE_ADMIN, ROLE_DONOR, ROLE_FOUNDATION

`getPassword()`:
- Return password hash

`getUsername()`:
- Return email (we use email as username)

`isAccountNonExpired()`:
- Return true (we don't use account expiration)

`isAccountNonLocked()`:
- Return status != SUSPENDED
- Locked accounts return false

`isCredentialsNonExpired()`:
- Return true (we don't use credential expiration)

`isEnabled()`:
- Return status == ACTIVE && emailVerified
- Only active and verified accounts are enabled

**Additional Getters**:
- getId(): return id
- getRole(): return role
- getStatus(): return status
- isEmailVerified(): return emailVerified

---

#### 3.2 CustomUserDetailsService.java
**Location**: `src/main/java/com/seffafbagis/api/security/CustomUserDetailsService.java`

**Purpose**: Load user from database for Spring Security authentication.

**Requirements**:

**Class Structure**:
- Implement UserDetailsService
- Annotate with @Service
- Inject UserRepository

**Load by Username (Email)** - `loadUserByUsername(String email)`:
- Find user by email using UserRepository
- If not found, throw UsernameNotFoundException
- Convert User entity to CustomUserDetails
- Return CustomUserDetails

**Load by ID** - `loadUserById(UUID id)`:
- Find user by ID using UserRepository
- If not found, throw UsernameNotFoundException
- Convert User entity to CustomUserDetails
- Return CustomUserDetails
- This method is used by JwtAuthenticationFilter

**Note**: This class depends on UserRepository which will be created in Phase 4. For now, create the class with a TODO comment indicating the dependency. The class should compile but will need the repository to be functional.

---

### 4. Security Utilities

#### 4.1 SecurityUtils.java
**Location**: `src/main/java/com/seffafbagis/api/security/SecurityUtils.java`

**Purpose**: Static utility methods for accessing security context.

**Requirements**:

**Class Structure**:
- Final class with private constructor (utility class pattern)
- All methods static

**Get Current User** - `getCurrentUser()`:
- Get Authentication from SecurityContextHolder
- Check if authentication exists and is authenticated
- Check if principal is CustomUserDetails
- Return Optional<CustomUserDetails>
- Return Optional.empty() if not authenticated

**Get Current User ID** - `getCurrentUserId()`:
- Call getCurrentUser()
- Map to user ID
- Return Optional<UUID>

**Get Current User Email** - `getCurrentUserEmail()`:
- Call getCurrentUser()
- Map to email
- Return Optional<String>

**Get Current User Role** - `getCurrentUserRole()`:
- Call getCurrentUser()
- Map to role
- Return Optional<UserRole>

**Get Current User Required** - `getCurrentUserOrThrow()`:
- Call getCurrentUser()
- If empty, throw UnauthorizedException
- Return CustomUserDetails

**Check if Authenticated** - `isAuthenticated()`:
- Return true if getCurrentUser() is present

**Check Role** - `hasRole(UserRole role)`:
- Get current user role
- Compare with given role
- Return boolean

**Check if Admin** - `isAdmin()`:
- Return hasRole(UserRole.ADMIN)

**Check if Foundation** - `isFoundation()`:
- Return hasRole(UserRole.FOUNDATION)

**Check if Donor** - `isDonor()`:
- Return hasRole(UserRole.DONOR)

**Check if Current User** - `isCurrentUser(UUID userId)`:
- Get current user ID
- Compare with given ID
- Return boolean

---

## FILE STRUCTURE

After completing this phase, the following files should exist:

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

**Total Files**: 7

---

## SECURITY FLOW DIAGRAM

```
┌─────────────────────────────────────────────────────────────────────┐
│                         HTTP REQUEST                                 │
└─────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    JwtAuthenticationFilter                           │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ 1. Extract "Authorization: Bearer <token>" header            │   │
│  │ 2. Validate token with JwtTokenProvider                      │   │
│  │ 3. Load user with CustomUserDetailsService                   │   │
│  │ 4. Set SecurityContext if valid                              │   │
│  └─────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
                                  │
                    ┌─────────────┴─────────────┐
                    │                           │
              Token Valid               Token Invalid/Missing
                    │                           │
                    ▼                           ▼
┌─────────────────────────────┐   ┌─────────────────────────────┐
│   SecurityContext SET       │   │   SecurityContext EMPTY     │
│   User is authenticated     │   │   User is anonymous         │
└─────────────────────────────┘   └─────────────────────────────┘
                    │                           │
                    ▼                           ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       SecurityConfig                                 │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ Check endpoint permissions:                                  │   │
│  │ - Public endpoints: permitAll()                              │   │
│  │ - Admin endpoints: hasRole('ADMIN')                          │   │
│  │ - Other endpoints: authenticated()                           │   │
│  └─────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
                                  │
                    ┌─────────────┴─────────────┐
                    │                           │
              Access Granted            Access Denied
                    │                           │
                    ▼                           ▼
┌─────────────────────────────┐   ┌─────────────────────────────┐
│      Controller Method      │   │ JwtAuthenticationEntryPoint │
│      Executes               │   │ Returns 401 Unauthorized    │
└─────────────────────────────┘   └─────────────────────────────┘
```

---

## TESTING REQUIREMENTS

After implementation, verify:

### 1. Application Startup Test
- Run application with `mvn spring-boot:run`
- Application should start without errors
- Check logs for security filter chain initialization
- No circular dependency errors

### 2. Public Endpoint Test
- Create a simple test controller with a public endpoint (temporary)
- Access without token - should succeed
- Example: GET /api/v1/settings/public

### 3. Protected Endpoint Test
- Create a simple test controller with a protected endpoint (temporary)
- Access without token - should return 401
- Response should be JSON with error details

### 4. JWT Token Test
- Write a unit test for JwtTokenProvider
- Test token generation
- Test token validation
- Test token parsing
- Test expired token handling

### 5. Security Utils Test
- Test getCurrentUser when authenticated
- Test getCurrentUser when not authenticated
- Test role checking methods

---

## DEPENDENCIES

### Required Dependencies (should be in pom.xml from Phase 1)
```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
```

### Placeholder Dependencies
CustomUserDetailsService depends on UserRepository which doesn't exist yet. Create the class with:
- A TODO comment noting the dependency
- A temporary implementation that throws UnsupportedOperationException
- This will be properly implemented in Phase 4

---

## SUCCESS CRITERIA

Phase 2 is considered successful when:

1. ✅ All 7 files are created in correct locations
2. ✅ Application starts without errors
3. ✅ No circular dependency issues
4. ✅ JwtTokenProvider can generate valid tokens
5. ✅ JwtTokenProvider can validate tokens correctly
6. ✅ JwtAuthenticationFilter processes requests
7. ✅ Protected endpoints return 401 without token
8. ✅ JwtAuthenticationEntryPoint returns proper JSON error
9. ✅ SecurityUtils methods work correctly
10. ✅ BCrypt password encoder is available as a bean

---

## RESULT FILE REQUIREMENTS

After completing this phase, create a result file at:
`docs/Furkan/step_results/phase_2_result.md`

The result file must include:

1. **Execution Status**: Success or Failure
2. **Files Created**: List all 7 files with their paths
3. **Compilation Verification**: Confirm all classes compile without errors
4. **Startup Verification**: Application starts with security configured
5. **Token Generation Test**: 
   - Sample generated token (redact sensitive parts)
   - Token validation result
6. **Endpoint Security Test**:
   - Result of accessing protected endpoint without token
   - Confirm 401 response with JSON body
7. **Issues Encountered**: Any problems and how they were resolved
8. **Placeholder Notes**: Document any placeholder code that needs Phase 4
9. **Notes for Next Phase**: Observations relevant to Phase 3

---

## COMMON ISSUES AND SOLUTIONS

### Issue 1: Circular Dependency
**Symptom**: Application fails to start with circular dependency error
**Solution**: Use @Lazy annotation on one of the dependencies, or restructure to avoid circular reference

### Issue 2: JWT Secret Too Short
**Symptom**: Error about key length
**Solution**: Ensure JWT_SECRET environment variable is at least 32 characters

### Issue 3: BCrypt Multiple Beans
**Symptom**: Multiple beans of type PasswordEncoder
**Solution**: Ensure only one @Bean method creates PasswordEncoder

### Issue 4: Filter Order
**Symptom**: Authentication not working correctly
**Solution**: Verify JwtAuthenticationFilter is added before UsernamePasswordAuthenticationFilter

---

## NOTES

- Security is critical - review code carefully
- Never log tokens or passwords
- Use consistent error messages to prevent user enumeration
- Test token expiration handling
- Ensure proper exception handling in filter

---

## NEXT PHASE PREVIEW

Phase 3 (Exception Handling & Common DTOs) will create:
- Global exception handler that works with security exceptions
- Common response DTOs that security components will use
- Error response structures for authentication failures

The security infrastructure from this phase will integrate with Phase 3's exception handling.
