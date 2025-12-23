# PHASE 5: ENCRYPTION & SECURITY UTILITIES

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
- Phase 3 (Exception Handling & Common DTOs) has been completed
- Phase 4 (User Entity & Repository Layer) has been completed
- All user entities exist including UserSensitiveData with encrypted fields
- EncryptionException is available for error handling
- Application is running with security enabled

### What This Phase Accomplishes
This phase implements the encryption service for KVKK-compliant data protection and validation utilities. The EncryptionService will be used to encrypt/decrypt sensitive user data (TC Kimlik, phone, address) stored in UserSensitiveData entity. Validators ensure data integrity for critical fields.

---

## OBJECTIVE

Create the complete encryption and validation infrastructure including:
1. AES-256-GCM encryption service for sensitive data
2. Token generation utilities for secure random tokens
3. Validators for Turkish-specific data (TC Kimlik, phone, IBAN)
4. Password strength validator
5. Integration with existing UserSensitiveData entity

---

## IMPORTANT RULES

### Code Style Requirements
- Use clear, readable code - prefer if-else statements over ternary operators
- Follow SOLID principles
- Add comprehensive comments in English
- Use meaningful variable and method names
- No complex one-liners - prioritize readability over brevity

### Security Requirements
- Use AES-256-GCM (authenticated encryption with associated data)
- Generate unique IV (Initialization Vector) for each encryption
- NEVER reuse IVs
- NEVER log sensitive data (plain text, keys)
- Use SecureRandom for all random generation
- Key must be derived from environment variable
- Handle all crypto exceptions gracefully

### KVKK Compliance
- All personal data must be encrypted at rest
- Encryption must be reversible (for data access rights)
- Key management must be secure
- Audit trail for data access (handled by AuditLogService in later phase)

---

## DETAILED REQUIREMENTS

### 1. Encryption Service

#### 1.1 EncryptionService.java
**Location**: `src/main/java/com/seffafbagis/api/service/encryption/EncryptionService.java`

**Purpose**: Provide AES-256-GCM encryption and decryption for sensitive data

**Requirements**:

**Class Structure**:
- Annotate with @Service
- Inject encryption key from configuration (app.encryption.secret-key)
- Use @PostConstruct to initialize cipher components
- Use @Slf4j for logging

**Constants**:
```
ALGORITHM = "AES"
TRANSFORMATION = "AES/GCM/NoPadding"
GCM_IV_LENGTH = 12 bytes (96 bits - recommended for GCM)
GCM_TAG_LENGTH = 128 bits (16 bytes)
```

**Initialization** - `@PostConstruct init()`:
- Validate secret key length (must be 32 characters for AES-256)
- Convert secret key to SecretKeySpec
- Log successful initialization (without exposing key)
- Throw EncryptionException if key is invalid

**Encrypt Method** - `encrypt(String plainText)`:

Step 1: Validate input
- If plainText is null or empty, return null
- This allows optional fields to remain null

Step 2: Generate random IV
- Use SecureRandom to generate 12 bytes
- NEVER reuse IVs

Step 3: Initialize cipher
- Create Cipher instance with TRANSFORMATION
- Create GCMParameterSpec with tag length and IV
- Initialize cipher in ENCRYPT_MODE with key and GCM spec

Step 4: Encrypt
- Convert plainText to bytes (UTF-8)
- Call cipher.doFinal()
- Result includes authentication tag

Step 5: Combine IV and ciphertext
- Prepend IV to ciphertext (IV + encrypted data)
- This allows IV to be extracted during decryption

Step 6: Return result
- Return combined byte array
- This will be stored in BYTEA column

**Error Handling**:
- Catch all crypto exceptions
- Log error without sensitive data
- Throw EncryptionException with generic message
- DO NOT expose internal crypto details

**Decrypt Method** - `decrypt(byte[] encryptedData)`:

Step 1: Validate input
- If encryptedData is null or empty, return null
- Check minimum length (IV_LENGTH + 1)

Step 2: Extract IV
- First 12 bytes are the IV
- Remaining bytes are ciphertext + auth tag

Step 3: Initialize cipher
- Create Cipher instance with TRANSFORMATION
- Create GCMParameterSpec with tag length and IV
- Initialize cipher in DECRYPT_MODE with key and GCM spec

Step 4: Decrypt
- Call cipher.doFinal() on ciphertext
- GCM mode automatically verifies authentication tag
- If tag verification fails, throws AEADBadTagException

Step 5: Return result
- Convert decrypted bytes to String (UTF-8)
- Return plain text

**Error Handling**:
- Catch AEADBadTagException - data may be tampered
- Log error appropriately
- Throw EncryptionException

**Convenience Methods**:

`encryptIfNotNull(String plainText)`:
- Call encrypt only if plainText is not null
- Return null if input is null

`decryptIfNotNull(byte[] encryptedData)`:
- Call decrypt only if encryptedData is not null
- Return null if input is null

`isEncrypted(byte[] data)`:
- Check if data appears to be encrypted
- Verify minimum length for IV + ciphertext
- This is a heuristic, not a guarantee

---

### 2. Token Utilities

#### 2.1 TokenUtils.java
**Location**: `src/main/java/com/seffafbagis/api/util/TokenUtils.java`

**Purpose**: Generate secure random tokens for various purposes

**Requirements**:

**Class Structure**:
- Final class with private constructor (utility class pattern)
- All methods static
- Use SecureRandom for all random generation

**Constants**:
```
DEFAULT_TOKEN_LENGTH = 32 bytes
URL_SAFE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
```

**Methods**:

`generateSecureToken()`:
- Generate 32-byte random token
- Return as URL-safe Base64 encoded string
- Used for: password reset tokens, email verification tokens

`generateSecureToken(int byteLength)`:
- Generate token with specified byte length
- Return as URL-safe Base64 encoded string

`generateTokenHash(String token)`:
- Hash token using SHA-256
- Return hash as hex string
- Used for storing token hashes in database

`generateReferenceCode()`:
- Generate reference code format: SBP-YYYYMMDD-XXXXX
- SBP = Platform prefix
- YYYYMMDD = Current date
- XXXXX = 5 random alphanumeric characters
- Used for bank transfer references

`generateReceiptNumber(long sequenceNumber)`:
- Generate receipt number format: RCPT-YYYY-NNNNNN
- YYYY = Current year
- NNNNNN = Zero-padded sequence number
- Used for donation receipts

`generateRandomString(int length)`:
- Generate random string of specified length
- Use URL_SAFE_CHARACTERS
- Used for various purposes

`generateUUID()`:
- Wrapper for UUID.randomUUID().toString()
- Returns UUID without hyphens (optional)

**Security Notes**:
- Always use SecureRandom, never Random
- Token length should be sufficient for security (at least 32 bytes)
- Hash tokens before storing in database

---

### 3. Validators

#### 3.1 PasswordValidator.java
**Location**: `src/main/java/com/seffafbagis/api/validator/PasswordValidator.java`

**Purpose**: Validate password strength according to security policy

**Requirements**:

**Password Rules**:
- Minimum 8 characters
- Maximum 128 characters
- At least one uppercase letter (A-Z)
- At least one lowercase letter (a-z)
- At least one digit (0-9)
- At least one special character (!@#$%^&*()_+-=[]{}|;:,.<>?)
- No whitespace characters
- Not a commonly used password (optional: check against list)

**Class Structure**:
- Final class with private constructor (utility class)
- All methods static
- Return ValidationResult or throw BadRequestException

**Methods**:

`validate(String password)`:
- Check all rules
- Return ValidationResult with isValid and list of failed rules
- Do not throw exception - let caller decide

`validateOrThrow(String password)`:
- Call validate()
- If invalid, throw BadRequestException with message listing failures
- Message should be user-friendly in Turkish

`isValid(String password)`:
- Quick check returning boolean
- Calls validate() internally

`getStrength(String password)`:
- Return strength score: WEAK, FAIR, STRONG, VERY_STRONG
- Based on length and character variety

**ValidationResult Inner Class**:
- Fields: valid (boolean), errors (List<String>)
- Static factory methods: valid(), invalid(List<String> errors)

---

#### 3.2 TcKimlikValidator.java
**Location**: `src/main/java/com/seffafbagis/api/validator/TcKimlikValidator.java`

**Purpose**: Validate Turkish National ID numbers (TC Kimlik No)

**Requirements**:

**TC Kimlik Rules**:
- Exactly 11 digits
- First digit cannot be 0
- All characters must be digits
- Passes checksum algorithm (Turkish government algorithm)

**Checksum Algorithm**:
```
Let digits be d1, d2, d3, d4, d5, d6, d7, d8, d9, d10, d11

Rule 1: d10 = ((d1 + d3 + d5 + d7 + d9) * 7 - (d2 + d4 + d6 + d8)) mod 10
Rule 2: d11 = (d1 + d2 + d3 + d4 + d5 + d6 + d7 + d8 + d9 + d10) mod 10
```

**Class Structure**:
- Final class with private constructor
- All methods static

**Methods**:

`validate(String tcKimlik)`:
- Check format (11 digits, first not 0)
- Check checksum
- Return ValidationResult

`validateOrThrow(String tcKimlik)`:
- Call validate()
- If invalid, throw BadRequestException

`isValid(String tcKimlik)`:
- Quick check returning boolean

`mask(String tcKimlik)`:
- Return masked version: ***-***-**XX (last 2 digits visible)
- Used for displaying in UI

---

#### 3.3 PhoneValidator.java
**Location**: `src/main/java/com/seffafbagis/api/validator/PhoneValidator.java`

**Purpose**: Validate Turkish phone numbers

**Requirements**:

**Phone Number Rules**:
- Turkish mobile: starts with 5, total 10 digits (without country code)
- With country code: +90 5XX XXX XX XX
- Formats accepted:
  - 5321234567
  - 05321234567
  - +905321234567
  - +90 532 123 45 67
  - 0532 123 45 67

**Class Structure**:
- Final class with private constructor
- All methods static

**Methods**:

`validate(String phone)`:
- Normalize phone number (remove spaces, dashes)
- Check if valid Turkish mobile number
- Return ValidationResult

`validateOrThrow(String phone)`:
- Call validate()
- If invalid, throw BadRequestException

`isValid(String phone)`:
- Quick check returning boolean

`normalize(String phone)`:
- Convert to standard format: +905321234567
- Remove all non-digit characters except leading +
- Add country code if missing

`mask(String phone)`:
- Return masked version: +90 *** *** ** 67
- Show only last 2 digits

`format(String phone)`:
- Return formatted: +90 532 123 45 67
- For display purposes

---

#### 3.4 IbanValidator.java
**Location**: `src/main/java/com/seffafbagis/api/validator/IbanValidator.java`

**Purpose**: Validate IBAN (International Bank Account Number)

**Requirements**:

**IBAN Rules (Turkish)**:
- Starts with "TR"
- Total 26 characters
- Format: TRXX XXXX XXXX XXXX XXXX XXXX XX
- Passes mod-97 checksum (ISO 13616)

**Checksum Algorithm**:
```
1. Move first 4 characters to end
2. Replace letters with numbers (A=10, B=11, ... Z=35)
3. Calculate number mod 97
4. Result must be 1
```

**Class Structure**:
- Final class with private constructor
- All methods static

**Methods**:

`validate(String iban)`:
- Remove spaces and convert to uppercase
- Check country code (TR for Turkish)
- Check length (26 for Turkish)
- Check checksum
- Return ValidationResult

`validateOrThrow(String iban)`:
- Call validate()
- If invalid, throw BadRequestException

`isValid(String iban)`:
- Quick check returning boolean

`normalize(String iban)`:
- Remove spaces, convert to uppercase
- Return: TR123456789012345678901234

`format(String iban)`:
- Return formatted with spaces: TR12 3456 7890 1234 5678 9012 34
- For display purposes

`mask(String iban)`:
- Return masked: TR** **** **** **** **** **12 34
- Show only last 4 digits

`extractBankCode(String iban)`:
- Extract bank code from Turkish IBAN (positions 5-9)
- Return 5-digit bank code

---

### 4. Additional Utility Class

#### 4.1 DateUtils.java
**Location**: `src/main/java/com/seffafbagis/api/util/DateUtils.java`

**Purpose**: Date and time utility methods

**Requirements**:

**Class Structure**:
- Final class with private constructor
- All methods static
- Use Java 8+ time API (java.time)

**Constants**:
```
ZONE_ISTANBUL = ZoneId.of("Europe/Istanbul")
DATE_FORMAT = "dd.MM.yyyy"
DATETIME_FORMAT = "dd.MM.yyyy HH:mm"
ISO_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME
```

**Methods**:

`now()`:
- Return current LocalDateTime in Istanbul timezone

`nowInstant()`:
- Return current Instant

`toInstant(LocalDateTime dateTime)`:
- Convert LocalDateTime to Instant using Istanbul zone

`toLocalDateTime(Instant instant)`:
- Convert Instant to LocalDateTime in Istanbul zone

`formatDate(LocalDateTime dateTime)`:
- Format as "dd.MM.yyyy"

`formatDateTime(LocalDateTime dateTime)`:
- Format as "dd.MM.yyyy HH:mm"

`parseDate(String dateStr)`:
- Parse "dd.MM.yyyy" to LocalDate

`parseDateTime(String dateTimeStr)`:
- Parse "dd.MM.yyyy HH:mm" to LocalDateTime

`isExpired(LocalDateTime expiryTime)`:
- Check if expiryTime is before now

`addMinutes(LocalDateTime dateTime, int minutes)`:
- Add minutes to dateTime

`addHours(LocalDateTime dateTime, int hours)`:
- Add hours to dateTime

`addDays(LocalDateTime dateTime, int days)`:
- Add days to dateTime

`startOfDay(LocalDate date)`:
- Return LocalDateTime at 00:00:00

`endOfDay(LocalDate date)`:
- Return LocalDateTime at 23:59:59.999999999

`isSameDay(LocalDateTime dt1, LocalDateTime dt2)`:
- Check if two datetimes are on the same day

---

## FILE STRUCTURE

After completing this phase, the following files should exist:

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
    ├── TokenUtils.java
    └── DateUtils.java
```

**Total Files**: 7

---

## ENCRYPTION FLOW DIAGRAM

```
┌─────────────────────────────────────────────────────────────────────┐
│                        ENCRYPTION FLOW                               │
└─────────────────────────────────────────────────────────────────────┘

Plain Text ("12345678901")
        │
        ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    EncryptionService.encrypt()                       │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ 1. Generate random 12-byte IV using SecureRandom            │   │
│  │ 2. Initialize AES-256-GCM cipher with key and IV            │   │
│  │ 3. Encrypt plain text → ciphertext + auth tag               │   │
│  │ 4. Prepend IV to result: [IV (12 bytes)][Ciphertext + Tag]  │   │
│  └─────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
        │
        ▼
Encrypted byte[] (stored in database BYTEA column)
        │
        ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    EncryptionService.decrypt()                       │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ 1. Extract IV (first 12 bytes)                               │   │
│  │ 2. Extract ciphertext + tag (remaining bytes)                │   │
│  │ 3. Initialize AES-256-GCM cipher with key and IV            │   │
│  │ 4. Decrypt and verify auth tag                               │   │
│  │ 5. Return plain text                                         │   │
│  └─────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
        │
        ▼
Plain Text ("12345678901")
```

---

## USAGE EXAMPLES

### Encrypting Sensitive Data
```
// In SensitiveDataService (Phase 8)
public void updateTcKimlik(UUID userId, String tcKimlik) {
    // Validate TC Kimlik
    TcKimlikValidator.validateOrThrow(tcKimlik);
    
    // Encrypt
    byte[] encrypted = encryptionService.encrypt(tcKimlik);
    
    // Save
    UserSensitiveData sensitiveData = repository.findByUserId(userId)
        .orElseThrow(() -> new ResourceNotFoundException("UserSensitiveData", "userId", userId));
    sensitiveData.setTcKimlikEncrypted(encrypted);
    repository.save(sensitiveData);
}
```

### Decrypting Sensitive Data
```
// In SensitiveDataService (Phase 8)
public String getMaskedTcKimlik(UUID userId) {
    UserSensitiveData sensitiveData = repository.findByUserId(userId)
        .orElseThrow(() -> new ResourceNotFoundException("UserSensitiveData", "userId", userId));
    
    if (sensitiveData.getTcKimlikEncrypted() == null) {
        return null;
    }
    
    // Decrypt
    String tcKimlik = encryptionService.decrypt(sensitiveData.getTcKimlikEncrypted());
    
    // Return masked for display
    return TcKimlikValidator.mask(tcKimlik);
}
```

### Generating Tokens
```
// In PasswordResetService (Phase 7)
public void createPasswordResetToken(User user) {
    // Generate secure token
    String token = TokenUtils.generateSecureToken();
    
    // Hash for storage
    String tokenHash = TokenUtils.generateTokenHash(token);
    
    // Save hash to database
    PasswordResetToken resetToken = new PasswordResetToken();
    resetToken.setUser(user);
    resetToken.setTokenHash(tokenHash);
    resetToken.setExpiresAt(DateUtils.addHours(DateUtils.now(), 1));
    repository.save(resetToken);
    
    // Send plain token in email (not the hash)
    emailService.sendPasswordResetEmail(user.getEmail(), token);
}
```

---

## TESTING REQUIREMENTS

After implementation, verify:

### 1. Encryption Service Test

**Test: Encrypt and Decrypt**
- Encrypt a string
- Decrypt the result
- Verify original equals decrypted

**Test: Different Encryptions Produce Different Results**
- Encrypt same string twice
- Verify encrypted bytes are different (due to random IV)

**Test: Null Handling**
- encrypt(null) should return null
- decrypt(null) should return null

**Test: Tampered Data Detection**
- Encrypt a string
- Modify a byte in the encrypted data
- Attempt to decrypt
- Verify EncryptionException is thrown (GCM auth tag verification fails)

**Test: Invalid Key**
- Configure with short key (less than 32 chars)
- Verify EncryptionException on startup

### 2. Validator Tests

**Password Validator**:
- Test valid password: "SecureP@ss123"
- Test too short: "Ab1!"
- Test no uppercase: "securep@ss123"
- Test no lowercase: "SECUREP@SS123"
- Test no digit: "SecureP@ssword"
- Test no special char: "SecurePass123"

**TC Kimlik Validator**:
- Test valid TC: Use a valid test TC number
- Test invalid checksum: Modify last digit of valid TC
- Test wrong length: "1234567890" (10 digits)
- Test starts with 0: "01234567890"
- Test non-digit: "1234567890A"

**Phone Validator**:
- Test valid: "5321234567"
- Test with country code: "+905321234567"
- Test with spaces: "+90 532 123 45 67"
- Test landline (invalid mobile): "2121234567"
- Test wrong length: "532123456"

**IBAN Validator**:
- Test valid Turkish IBAN
- Test invalid checksum
- Test wrong country code
- Test wrong length

### 3. Token Utils Test
- Generate multiple tokens, verify uniqueness
- Verify token hash is deterministic (same token → same hash)
- Verify reference code format
- Verify receipt number format

### 4. Date Utils Test
- Test timezone handling
- Test formatting and parsing
- Test expiration check
- Test date arithmetic

---

## SUCCESS CRITERIA

Phase 5 is considered successful when:

1. ✅ All 7 files are created in correct locations
2. ✅ EncryptionService encrypts and decrypts correctly
3. ✅ GCM authentication prevents tampering
4. ✅ Each encryption produces unique ciphertext (random IV)
5. ✅ PasswordValidator enforces all password rules
6. ✅ TcKimlikValidator validates Turkish ID numbers correctly
7. ✅ PhoneValidator handles all Turkish phone formats
8. ✅ IbanValidator validates Turkish IBANs correctly
9. ✅ TokenUtils generates cryptographically secure tokens
10. ✅ DateUtils handles Istanbul timezone correctly
11. ✅ All validators provide proper masking functions
12. ✅ Error handling is robust and secure

---

## RESULT FILE REQUIREMENTS

After completing this phase, create a result file at:
`docs/Furkan/step_results/phase_5_result.md`

The result file must include:

1. **Execution Status**: Success or Failure
2. **Files Created**: List all 7 files with their paths
3. **Encryption Tests**:
   - Encrypt/decrypt round-trip test result
   - Tamper detection test result
   - IV uniqueness verification
4. **Validator Tests**:
   - Results for each validator with sample inputs
   - Edge case handling verification
5. **Token Generation Tests**:
   - Sample generated tokens (can be shown as they're random)
   - Reference code format verification
6. **Integration Verification**:
   - Confirm EncryptionService can be injected
   - Confirm configuration is loaded correctly
7. **Issues Encountered**: Any problems and how they were resolved
8. **Security Review Notes**: Any security considerations
9. **Notes for Next Phase**: Observations relevant to Phase 6

---

## ENVIRONMENT VARIABLES

Ensure these are set for encryption to work:

```
# In .env or application properties
ENCRYPTION_SECRET_KEY=your-32-character-secret-key-here

# Example (DO NOT USE IN PRODUCTION):
ENCRYPTION_SECRET_KEY=ThisIsA32CharacterSecretKey123!
```

**Key Requirements**:
- Exactly 32 characters for AES-256
- Should be random, not predictable
- Store securely (not in code repository)
- Different keys for dev/staging/production

---

## SECURITY CHECKLIST

Before completing this phase, verify:

- [ ] Secret key is loaded from environment variable
- [ ] Secret key is never logged
- [ ] Plain text sensitive data is never logged
- [ ] SecureRandom is used for all random generation
- [ ] IV is unique for each encryption
- [ ] GCM mode is used (not ECB or CBC without authentication)
- [ ] Encryption exceptions don't leak internal details
- [ ] Token hashes are stored, not plain tokens
- [ ] Validators don't leak information through timing attacks (for TC Kimlik)

---

## NOTES

- Encryption is critical for KVKK compliance
- Test thoroughly with various inputs
- Consider performance for bulk operations
- Key rotation strategy should be planned (future enhancement)
- Validators should be used before encrypting data

---

## NEXT PHASE PREVIEW

Phase 6 (Auth Module - Core) will create:
- AuthService using EncryptionService and validators
- Login, Register, Logout functionality
- JWT token management
- This phase will heavily use components from Phase 5
