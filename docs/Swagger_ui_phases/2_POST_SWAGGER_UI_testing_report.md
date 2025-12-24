# Post-Swagger UI Testing Report

## Executive Summary (Daily Standup)
**Problem:** After successfully accessing Swagger UI, attempts to test user registration via the API consistently failed with various 500 and 401 errors.

**Root Causes Identified:**
1. **Context Path Conflict**: Duplicate `/api` prefix causing 401 Unauthorized on public endpoints
2. **JPA Auditing Issue**: Missing `DateTimeProvider` for `OffsetDateTime` fields
3. **JWT Secret Key**: Weak key (408 bits) failed HS512 algorithm requirements (512+ bits needed)
4. **YAML Syntax Error**: Duplicate `app:` configuration sections
5. **PostgreSQL Enum Incompatibility**: Hibernate's `@Enumerated(STRING)` sends VARCHAR, but PostgreSQL custom enums (`user_role`, `user_status`) require explicit casting

**Resolution Status:** 4/5 critical issues resolved. User registration now processes successfully until `user_preferences` schema mismatch (separate issue discovered during testing).

---

## Detailed Issue Analysis and Solutions

### Issue #1: 401 Unauthorized on Public Registration Endpoint

**Symptom:**
```
HTTP 401 Unauthorized
GET /api/api/v1/auth/register
```

**Root Cause:**
- `server.servlet.context-path: /api` in `application.yml` added `/api` prefix to all endpoints
- Controllers already used `/api/v1/...` in `@RequestMapping`
- This created double prefix: `/api/api/v1/auth/register`
- `SecurityConfig` whitelisted `/api/v1/auth/**`, not `/api/api/v1/auth/**`

**Solution:**
Removed `server.servlet.context-path: /api` from `application.yml`

**File Modified:**
- `backend/src/main/resources/application.yml` (line 9)

**Verification:**
```bash
curl http://localhost:8080/api/v1/auth/register  # Now matches SecurityConfig whitelist
```

---

### Issue #2: JPA Auditing `IllegalArgumentException`

**Symptom:**
```
java.lang.IllegalArgumentException: Cannot convert unsupported date type java.time.LocalDateTime to java.time.OffsetDateTime
```

**Root Cause:**
- `BaseEntity` uses `@CreatedDate` and `@LastModifiedDate` with `OffsetDateTime` fields
- Spring Data Auditing defaults to `LocalDateTime`
- No custom `DateTimeProvider` configured

**Solution:**
Added `DateTimeProvider` bean to `AuditConfig` returning `OffsetDateTime.now()`:

```java
@EnableJpaAuditing(auditorAwareRef = "auditorAware", dateTimeProviderRef = "offsetDateTimeProvider")
public class AuditConfig {
    @Bean
    public DateTimeProvider offsetDateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now());
    }
}
```

**Files Modified:**
- `backend/src/main/java/com/seffafbagis/api/config/AuditConfig.java`
- Initially created `JpaAuditingConfig.java` (duplicate, later removed)

---

### Issue #3: Weak JWT Secret Key

**Symptom:**
```
io.jsonwebtoken.security.WeakKeyException: The signing key's size is 408 bits which is not secure enough for the HS512 algorithm
```

**Root Cause:**
- HS512 algorithm requires minimum 512 bits (64 bytes)
- Existing key in `application-dev.yml` was only 408 bits

**Solution:**
Generated secure 512-bit base64-encoded key:

```yaml
app:
  jwt:
    secret: c2VmZmFmLWJhZ2lzLXN1cGVyLXNlY3JldC1rZXktZm9yLWp3dC1zaWduaW5nLWF0LWxlYXN0LTY0LWJ5dGVzLWxvbmctdGhpcy1pcy1hLXNhbXBsZS1mb3ItZGV2LW1vZGUtb25seQ==
```

**File Modified:**
- `backend/src/main/resources/application-dev.yml`

---

### Issue #4: YAML Parse Error

**Symptom:**
```
org.yaml.snakeyaml.constructor.ConstructorException: 
Cannot create property=app for JavaBean=...
```

**Root Cause:**
- `app:` section defined twice in `application-dev.yml` (lines 8 and 35)
- YAML parser cannot merge duplicate top-level keys

**Solution:**
Merged duplicate `app:` sections into single configuration block:

```yaml
app:
  jwt:
    secret: <base64-encoded-key>
  cors:
    allowed-origins: http://localhost:3000,http://127.0.0.1:3000
  frontend-url: http://localhost:3000
  encryption:
    secret-key: 12345678901234567890123456789012
```

**File Modified:**
- `backend/src/main/resources/application-dev.yml`

---

### Issue #5: PostgreSQL Enum Type Incompatibility

**Symptom:**
```sql
ERROR: column "role" is of type user_role but expression is of type character varying
Hint: You will need to rewrite or cast the expression.
```

**Root Cause:**
- Database migration created custom PostgreSQL enum types (`user_role`, `user_status`)
- Hibernate with `@Enumerated(EnumType.STRING)` sends enum values as VARCHAR via PreparedStatement
- PostgreSQL custom enums don't accept VARCHAR without explicit CAST
- Adding `columnDefinition = "user_role"` to `@Column` did NOT work - Hibernate still sends VARCHAR

**Solution:**
Created migration `V23__convert_enums_to_varchar.sql` to convert enum columns to VARCHAR:

```sql
-- Convert users.role from user_role enum to VARCHAR
ALTER TABLE users 
    ALTER COLUMN role TYPE VARCHAR(20) USING role::text;

-- Convert users.status from user_status enum to VARCHAR  
ALTER TABLE users
    ALTER COLUMN status TYPE VARCHAR(30) USING status::text;

-- Drop the now-unused enum types with CASCADE
DROP TYPE IF EXISTS user_role CASCADE;
DROP TYPE IF EXISTS user_status CASCADE;
```

**Why This Approach:**
1. Hibernate `@Enumerated(STRING)` is standard JPA practice
2. VARCHAR provides better ORM compatibility across databases
3. Application-level enum validation through Java enums
4. PostgreSQL custom enums require explicit casting which Hibernate doesn't provide
5. Attempted `columnDefinition` workaround failed - Hibernate PreparedStatement ignores it

**Files Modified:**
- `backend/src/main/resources/db/migration/V23__convert_enums_to_varchar.sql`
- `backend/src/main/java/com/seffafbagis/api/entity/user/User.java` (removed `columnDefinition` attempts)

**Migration Notes:**
- First attempt failed: `DROP TYPE` without CASCADE left dependent default values
- Fixed with `DROP TYPE IF EXISTS user_role CASCADE;`
- Had to delete failed migration from `flyway_schema_history` before retry

---

## Current Status

### ✅ Successfully Resolved:
1. Context path conflict (401 errors)
2. JPA Auditing DateTimeProvider 
3. JWT secret key weakness
4. YAML syntax error
5. PostgreSQL enum type conversion

### ⚠️ Remaining Issue:
**Schema Mismatch in `user_preferences` table:**
```
ERROR: column "notify_on_campaign_complete" of relation "user_preferences" does not exist
```

**Impact:** User registration creates `User` entity successfully but fails during `UserPreferences` creation, causing transaction rollback.

**Next Steps:**
- Compare `UserPreferences` Java entity fields with actual database schema
- Create migration to add missing columns or update entity to match schema

---

## Testing Evidence

### Backend Startup:
```
✅ Flyway migration V23 applied successfully
✅ Tomcat started on port 8080
✅ Application context loaded without errors
```

### Registration Attempt:
```
✅ User object created: emir@gmail.com - Role: DONOR
✅ Email verification token generated
❌ UserPreferences INSERT failed (schema mismatch)
❌ Transaction rolled back
```

---

## Files Modified Summary

| File | Change Description |
|------|-------------------|
| `backend/src/main/resources/application.yml` | Removed `server.servlet.context-path: /api` |
| `backend/src/main/resources/application-dev.yml` | Added secure JWT secret, merged duplicate `app:` sections |
| `backend/src/main/java/com/seffafbagis/api/config/AuditConfig.java` | Added `offsetDateTimeProvider` bean |
| `backend/src/main/java/com/seffafbagis/api/entity/user/User.java` | Attempted `columnDefinition` for enums (later reverted) |
| `backend/src/main/resources/db/migration/V23__convert_enums_to_varchar.sql` | Converted PostgreSQL enums to VARCHAR |

---

## Lessons Learned

1. **Context Path in Spring Boot:** Be cautious with `server.servlet.context-path` - it affects ALL endpoints including security configurations
2. **JPA Auditing with Custom Types:** Always configure `DateTimeProvider` when using non-standard timestamp types
3. **PostgreSQL Enums vs. VARCHAR:** PostgreSQL custom enums are incompatible with Hibernate's standard enum handling - prefer VARCHAR for better ORM compatibility
4. **JWT Key Requirements:** HS512 requires 512+ bit keys - always validate algorithm requirements
5. **Flyway Migration Failures:** Clean up `flyway_schema_history` after failed migrations before retry
6. **CASCADE in DDL:** When dropping PostgreSQL types, use CASCADE to automatically handle dependent objects

---

## Timeline

| Time | Action |
|------|--------|
| 23:13 | Initial 401 error discovered |
| 23:14 | Identified context-path conflict |
| 23:15 | Fixed context-path, discovered JPA Auditing issue |
| 23:18 | Added DateTimeProvider, discovered JWT key weakness |
| 23:20 | Updated JWT secret, discovered YAML error |
| 23:24 | Fixed YAML, discovered PostgreSQL enum issue |
| 23:27 | Created V23 migration (initial version) |
| 23:31 | Fixed migration CASCADE issue |
| 23:40 | Migration successful, discovered user_preferences issue |

**Total Resolution Time:** ~27 minutes for 5 critical issues
