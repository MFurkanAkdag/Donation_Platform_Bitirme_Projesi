# PHASE 3: EXCEPTION HANDLING & COMMON DTOs

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
- Phase 2 (Security Infrastructure) has been completed
- JWT authentication is configured
- Security filter chain is in place
- Application starts successfully with security enabled

### What This Phase Accomplishes
This phase creates a centralized exception handling system and standardized response structures. These components ensure consistent API responses across ALL endpoints and provide proper error handling for the entire application.

---

## OBJECTIVE

Create the complete exception handling and response infrastructure including:
1. Custom exception classes for different error scenarios
2. Global exception handler using @ControllerAdvice
3. Standardized API response wrappers
4. Error response structures with field-level validation support
5. Pagination response wrapper

---

## IMPORTANT RULES

### Code Style Requirements
- Use clear, readable code - prefer if-else statements over ternary operators
- Follow SOLID principles
- Add comprehensive comments in English
- Use meaningful variable and method names
- No complex one-liners - prioritize readability over brevity

### Exception Handling Requirements
- NEVER expose stack traces to clients in production
- NEVER expose internal system details in error messages
- Log full exception details server-side
- Return user-friendly messages to clients
- Use appropriate HTTP status codes
- Include request tracking information for debugging

### Response Standards
- All responses must follow the same structure
- Success responses include data and optional message
- Error responses include error code and user-friendly message
- All responses include timestamp
- Pagination responses include metadata

---

## DETAILED REQUIREMENTS

### 1. Custom Exception Classes

#### 1.1 ResourceNotFoundException.java
**Location**: `src/main/java/com/seffafbagis/api/exception/ResourceNotFoundException.java`

**Purpose**: Thrown when a requested resource does not exist (HTTP 404)

**Requirements**:
- Extend RuntimeException
- Include fields for:
  - resourceName: String (e.g., "User", "Campaign")
  - fieldName: String (e.g., "id", "email")
  - fieldValue: Object (the value that was searched for)
- Constructor accepting resourceName, fieldName, fieldValue
- Generate message: "{resourceName} not found with {fieldName}: {fieldValue}"
- Getter methods for all fields

**Usage Example**:
```
throw new ResourceNotFoundException("User", "id", userId);
// Message: "User not found with id: 123e4567-e89b-..."
```

---

#### 1.2 BadRequestException.java
**Location**: `src/main/java/com/seffafbagis/api/exception/BadRequestException.java`

**Purpose**: Thrown for invalid request data or business rule violations (HTTP 400)

**Requirements**:
- Extend RuntimeException
- Include fields for:
  - message: String (user-friendly error message)
  - errorCode: String (machine-readable code, optional)
- Multiple constructors:
  - BadRequestException(String message)
  - BadRequestException(String message, String errorCode)
- Getter methods for all fields

**Usage Example**:
```
throw new BadRequestException("Password must be at least 8 characters", "WEAK_PASSWORD");
```

---

#### 1.3 UnauthorizedException.java
**Location**: `src/main/java/com/seffafbagis/api/exception/UnauthorizedException.java`

**Purpose**: Thrown when authentication is required but not provided or invalid (HTTP 401)

**Requirements**:
- Extend RuntimeException
- Include field for:
  - message: String (default: "Authentication required")
- Multiple constructors:
  - UnauthorizedException() - uses default message
  - UnauthorizedException(String message)
- Getter for message

**Usage Example**:
```
throw new UnauthorizedException("Invalid or expired token");
```

---

#### 1.4 ForbiddenException.java
**Location**: `src/main/java/com/seffafbagis/api/exception/ForbiddenException.java`

**Purpose**: Thrown when user is authenticated but not authorized for the action (HTTP 403)

**Requirements**:
- Extend RuntimeException
- Include field for:
  - message: String (default: "Access denied")
- Multiple constructors:
  - ForbiddenException() - uses default message
  - ForbiddenException(String message)
- Getter for message

**Usage Example**:
```
throw new ForbiddenException("Only admins can perform this action");
```

---

#### 1.5 ConflictException.java
**Location**: `src/main/java/com/seffafbagis/api/exception/ConflictException.java`

**Purpose**: Thrown when there's a conflict with existing data (HTTP 409)

**Requirements**:
- Extend RuntimeException
- Include fields for:
  - resourceName: String (e.g., "User")
  - fieldName: String (e.g., "email")
  - fieldValue: Object (the conflicting value)
- Constructor accepting resourceName, fieldName, fieldValue
- Generate message: "{resourceName} already exists with {fieldName}: {fieldValue}"
- Getter methods for all fields

**Usage Example**:
```
throw new ConflictException("User", "email", "test@example.com");
// Message: "User already exists with email: test@example.com"
```

---

#### 1.6 FileStorageException.java
**Location**: `src/main/java/com/seffafbagis/api/exception/FileStorageException.java`

**Purpose**: Thrown when file storage operations fail (HTTP 500)

**Requirements**:
- Extend RuntimeException
- Include fields for:
  - message: String
  - cause: Throwable (optional)
- Multiple constructors:
  - FileStorageException(String message)
  - FileStorageException(String message, Throwable cause)
- Getter for message

**Usage Example**:
```
throw new FileStorageException("Failed to store file: " + filename, ioException);
```

---

#### 1.7 EncryptionException.java
**Location**: `src/main/java/com/seffafbagis/api/exception/EncryptionException.java`

**Purpose**: Thrown when encryption or decryption operations fail (HTTP 500)

**Requirements**:
- Extend RuntimeException
- Include fields for:
  - message: String
  - cause: Throwable (optional)
- Multiple constructors:
  - EncryptionException(String message)
  - EncryptionException(String message, Throwable cause)
- Getter for message

**Usage Example**:
```
throw new EncryptionException("Failed to decrypt sensitive data", cryptoException);
```

---

#### 1.8 PaymentException.java
**Location**: `src/main/java/com/seffafbagis/api/exception/PaymentException.java`

**Purpose**: Thrown when payment processing fails (HTTP 400 or 500 depending on cause)

**Requirements**:
- Extend RuntimeException
- Include fields for:
  - message: String
  - errorCode: String (payment provider error code)
  - retryable: boolean (whether the operation can be retried)
- Constructor accepting all fields
- Getter methods for all fields

**Usage Example**:
```
throw new PaymentException("Card declined", "CARD_DECLINED", false);
```

---

### 2. Global Exception Handler

#### 2.1 GlobalExceptionHandler.java
**Location**: `src/main/java/com/seffafbagis/api/exception/GlobalExceptionHandler.java`

**Purpose**: Centralized exception handling for the entire application using @ControllerAdvice

**Requirements**:

**Class Structure**:
- Annotate with @RestControllerAdvice
- Inject MessageSource for i18n messages
- Inject HttpServletRequest for request details
- Use @Slf4j for logging

**Handler Methods**:

**1. Handle ResourceNotFoundException**:
- Annotate with @ExceptionHandler(ResourceNotFoundException.class)
- Return HTTP 404
- Create ErrorResponse with:
  - code: "NOT_FOUND"
  - message: exception message
  - timestamp: current time
  - path: request URI
- Log at WARN level

**2. Handle BadRequestException**:
- Annotate with @ExceptionHandler(BadRequestException.class)
- Return HTTP 400
- Create ErrorResponse with:
  - code: exception.getErrorCode() or "BAD_REQUEST"
  - message: exception message
- Log at WARN level

**3. Handle UnauthorizedException**:
- Annotate with @ExceptionHandler(UnauthorizedException.class)
- Return HTTP 401
- Create ErrorResponse with:
  - code: "UNAUTHORIZED"
  - message: exception message
- Log at WARN level

**4. Handle ForbiddenException**:
- Annotate with @ExceptionHandler(ForbiddenException.class)
- Return HTTP 403
- Create ErrorResponse with:
  - code: "FORBIDDEN"
  - message: exception message
- Log at WARN level

**5. Handle ConflictException**:
- Annotate with @ExceptionHandler(ConflictException.class)
- Return HTTP 409
- Create ErrorResponse with:
  - code: "CONFLICT"
  - message: exception message
- Log at WARN level

**6. Handle FileStorageException**:
- Annotate with @ExceptionHandler(FileStorageException.class)
- Return HTTP 500
- Create ErrorResponse with:
  - code: "FILE_STORAGE_ERROR"
  - message: "File operation failed. Please try again."
- Log at ERROR level with full exception

**7. Handle EncryptionException**:
- Annotate with @ExceptionHandler(EncryptionException.class)
- Return HTTP 500
- Create ErrorResponse with:
  - code: "INTERNAL_ERROR"
  - message: "An internal error occurred. Please try again."
- Log at ERROR level with full exception
- DO NOT expose encryption details to client

**8. Handle PaymentException**:
- Annotate with @ExceptionHandler(PaymentException.class)
- Return HTTP 400 or 500 based on exception type
- Create ErrorResponse with:
  - code: exception.getErrorCode()
  - message: exception message
- Log at ERROR level

**9. Handle MethodArgumentNotValidException** (Bean Validation):
- Annotate with @ExceptionHandler(MethodArgumentNotValidException.class)
- Return HTTP 400
- Extract all field errors
- Create ErrorResponse with:
  - code: "VALIDATION_ERROR"
  - message: "Validation failed"
  - fieldErrors: list of FieldError objects
- Log at WARN level

**10. Handle ConstraintViolationException** (Path/Query param validation):
- Annotate with @ExceptionHandler(ConstraintViolationException.class)
- Return HTTP 400
- Create ErrorResponse with validation details
- Log at WARN level

**11. Handle HttpMessageNotReadableException** (Invalid JSON):
- Annotate with @ExceptionHandler(HttpMessageNotReadableException.class)
- Return HTTP 400
- Create ErrorResponse with:
  - code: "INVALID_REQUEST_BODY"
  - message: "Request body is invalid or malformed"
- Log at WARN level

**12. Handle AccessDeniedException** (Spring Security):
- Annotate with @ExceptionHandler(AccessDeniedException.class)
- Return HTTP 403
- Create ErrorResponse with:
  - code: "FORBIDDEN"
  - message: "You don't have permission to perform this action"
- Log at WARN level

**13. Handle AuthenticationException** (Spring Security):
- Annotate with @ExceptionHandler(AuthenticationException.class)
- Return HTTP 401
- Create ErrorResponse with:
  - code: "UNAUTHORIZED"
  - message: "Authentication failed"
- Log at WARN level

**14. Handle Generic Exception** (Catch-all):
- Annotate with @ExceptionHandler(Exception.class)
- Return HTTP 500
- Create ErrorResponse with:
  - code: "INTERNAL_ERROR"
  - message: "An unexpected error occurred. Please try again later."
- Log at ERROR level with full stack trace
- DO NOT expose exception details to client

**Helper Methods**:

`buildErrorResponse(String code, String message, HttpServletRequest request)`:
- Create ErrorResponse with timestamp and path
- Return ResponseEntity with appropriate status

`extractFieldErrors(BindingResult bindingResult)`:
- Extract field errors from validation result
- Map to FieldError DTOs
- Return list

---

### 3. Common Response DTOs

#### 3.1 ApiResponse.java
**Location**: `src/main/java/com/seffafbagis/api/dto/response/common/ApiResponse.java`

**Purpose**: Generic wrapper for all successful API responses

**Requirements**:
- Generic class: ApiResponse<T>
- Fields:
  - success: boolean (always true for this class)
  - message: String (optional success message)
  - data: T (response payload)
  - timestamp: LocalDateTime

**Static Factory Methods**:

`success(T data)`:
- Create response with data and no message
- success = true
- timestamp = now

`success(T data, String message)`:
- Create response with data and message
- success = true
- timestamp = now

`successMessage(String message)`:
- Create response with message only, no data
- For operations that don't return data

**Usage Example**:
```java
return ResponseEntity.ok(ApiResponse.success(userDto, "User created successfully"));
```

**JSON Output**:
```json
{
  "success": true,
  "message": "User created successfully",
  "data": {
    "id": "123e4567-e89b-...",
    "email": "user@example.com"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

#### 3.2 ErrorResponse.java
**Location**: `src/main/java/com/seffafbagis/api/dto/response/common/ErrorResponse.java`

**Purpose**: Standardized error response structure

**Requirements**:
- Fields:
  - success: boolean (always false)
  - error: ErrorDetail (nested object)
  - timestamp: LocalDateTime
  - path: String (request URI)

**ErrorDetail Nested Class**:
- Fields:
  - code: String (machine-readable error code)
  - message: String (user-friendly message)
  - fieldErrors: List<FieldError> (for validation errors, optional)

**FieldError Nested Class**:
- Fields:
  - field: String (field name)
  - message: String (validation message)
  - rejectedValue: Object (the invalid value, optional)

**Static Factory Methods**:

`of(String code, String message, String path)`:
- Create error response without field errors

`of(String code, String message, List<FieldError> fieldErrors, String path)`:
- Create error response with field errors

**Builder Pattern** (optional):
- Implement builder for flexible construction

**JSON Output (Simple Error)**:
```json
{
  "success": false,
  "error": {
    "code": "NOT_FOUND",
    "message": "User not found with id: 123"
  },
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/v1/users/123"
}
```

**JSON Output (Validation Error)**:
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "fieldErrors": [
      {
        "field": "email",
        "message": "Invalid email format",
        "rejectedValue": "invalid-email"
      },
      {
        "field": "password",
        "message": "Password must be at least 8 characters",
        "rejectedValue": null
      }
    ]
  },
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/v1/auth/register"
}
```

---

#### 3.3 PageResponse.java
**Location**: `src/main/java/com/seffafbagis/api/dto/response/common/PageResponse.java`

**Purpose**: Wrapper for paginated responses

**Requirements**:
- Generic class: PageResponse<T>
- Fields:
  - success: boolean (true)
  - data: List<T> (page content)
  - pagination: PaginationMeta (pagination metadata)
  - timestamp: LocalDateTime

**PaginationMeta Nested Class**:
- Fields:
  - currentPage: int (0-indexed page number)
  - pageSize: int (items per page)
  - totalElements: long (total items across all pages)
  - totalPages: int (total number of pages)
  - first: boolean (is first page)
  - last: boolean (is last page)
  - hasNext: boolean
  - hasPrevious: boolean

**Static Factory Method**:

`of(Page<T> page)`:
- Create PageResponse from Spring Data Page object
- Map all pagination metadata

`of(List<T> content, Page<?> page)`:
- Create PageResponse with mapped content (useful when mapping entities to DTOs)

**JSON Output**:
```json
{
  "success": true,
  "data": [
    { "id": "1", "name": "Item 1" },
    { "id": "2", "name": "Item 2" }
  ],
  "pagination": {
    "currentPage": 0,
    "pageSize": 10,
    "totalElements": 25,
    "totalPages": 3,
    "first": true,
    "last": false,
    "hasNext": true,
    "hasPrevious": false
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## FILE STRUCTURE

After completing this phase, the following files should exist:

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
│   ├── EncryptionException.java
│   └── PaymentException.java
└── dto/response/common/
    ├── ApiResponse.java
    ├── ErrorResponse.java
    └── PageResponse.java
```

**Total Files**: 12

---

## TESTING REQUIREMENTS

After implementation, verify:

### 1. Exception Handling Test
Create a temporary test controller with endpoints that throw each exception type:

```
GET /api/v1/test/not-found        -> throws ResourceNotFoundException
GET /api/v1/test/bad-request      -> throws BadRequestException
GET /api/v1/test/unauthorized     -> throws UnauthorizedException
GET /api/v1/test/forbidden        -> throws ForbiddenException
GET /api/v1/test/conflict         -> throws ConflictException
GET /api/v1/test/error            -> throws RuntimeException
POST /api/v1/test/validate        -> triggers validation error
```

### 2. Response Format Test
- Verify all responses have consistent structure
- Verify timestamp is present
- Verify path is correct
- Verify error codes are machine-readable

### 3. Validation Error Test
- Create a test DTO with validation annotations
- Send invalid data
- Verify field errors are properly formatted

### 4. Security Exception Test
- Access protected endpoint without token
- Verify 401 response with proper format
- Access admin endpoint as regular user
- Verify 403 response with proper format

### 5. Production Safety Test
- Trigger a generic exception
- Verify stack trace is NOT exposed in response
- Verify generic message is returned
- Verify full exception is logged server-side

---

## SUCCESS CRITERIA

Phase 3 is considered successful when:

1. ✅ All 12 files are created in correct locations
2. ✅ All exception classes compile without errors
3. ✅ GlobalExceptionHandler handles all exception types
4. ✅ ResourceNotFoundException returns 404 with proper format
5. ✅ BadRequestException returns 400 with proper format
6. ✅ UnauthorizedException returns 401 with proper format
7. ✅ ForbiddenException returns 403 with proper format
8. ✅ ConflictException returns 409 with proper format
9. ✅ Validation errors include field-level details
10. ✅ Generic exceptions return 500 without exposing details
11. ✅ All responses include timestamp and path
12. ✅ ApiResponse properly wraps success responses
13. ✅ PageResponse properly handles pagination

---

## RESULT FILE REQUIREMENTS

After completing this phase, create a result file at:
`docs/Furkan/step_results/phase_3_result.md`

The result file must include:

1. **Execution Status**: Success or Failure
2. **Files Created**: List all 12 files with their paths
3. **Exception Handling Tests**:
   - Test results for each exception type
   - Sample response bodies
4. **Validation Test**:
   - Sample validation error response
   - Confirm field errors are included
5. **Security Test**:
   - 401 response sample
   - 403 response sample
6. **Production Safety**:
   - Confirm stack traces not exposed
   - Confirm logging works
7. **Issues Encountered**: Any problems and how they were resolved
8. **Notes for Next Phase**: Observations relevant to Phase 4

---

## INTEGRATION WITH PREVIOUS PHASES

### Phase 2 Integration
The GlobalExceptionHandler should properly handle Spring Security exceptions:
- AuthenticationException → 401
- AccessDeniedException → 403

These exceptions may be thrown by the security infrastructure created in Phase 2.

### Response Consistency
All controllers created in subsequent phases must use:
- ApiResponse for success responses
- ErrorResponse format for errors (handled automatically by GlobalExceptionHandler)
- PageResponse for paginated results

---

## COMMON ISSUES AND SOLUTIONS

### Issue 1: Exception Handler Not Invoked
**Symptom**: Custom exceptions not being handled by GlobalExceptionHandler
**Solution**: Ensure @RestControllerAdvice is present and component scanning includes the exception package

### Issue 2: Validation Messages Not Localized
**Symptom**: Validation messages not in Turkish/English
**Solution**: Ensure messages.properties is properly configured and MessageSource is injected

### Issue 3: Circular Dependency with Security
**Symptom**: Circular dependency when handling security exceptions
**Solution**: Use @Lazy annotation or restructure dependencies

### Issue 4: JSON Serialization Issues
**Symptom**: LocalDateTime not serializing properly
**Solution**: Ensure Jackson JSR-310 module is configured (should be auto-configured by Spring Boot)

---

## NOTES

- Exception handling is critical for user experience
- Always log full exception details server-side
- Never expose sensitive information in error messages
- Use consistent error codes for client-side handling
- Consider adding request ID for tracking in distributed systems

---

## NEXT PHASE PREVIEW

Phase 4 (User Entity & Repository Layer) will create:
- User-related entities that will throw these exceptions
- Repositories that may throw DataIntegrityViolationException (mapped to ConflictException)
- Foundation for the Auth module

The exception handling created in this phase will be used extensively in Phase 4 and beyond.
