# PHASE 13: UTILITY CLASSES

## CONTEXT AND BACKGROUND

You are working on "Şeffaf Bağış Platformu" (Transparent Donation Platform), a graduation project for Fatih Sultan Mehmet Vakıf University. This is a comprehensive donation platform for Turkey that emphasizes transparency and accountability in charitable giving.

### Project Overview
- **Technology Stack**: Java 17, Spring Boot 3.x, PostgreSQL 15+, Redis, Maven
- **Purpose**: Enable foundations to create campaigns, donors to contribute, and maintain transparency through evidence uploads and scoring systems
- **Compliance Requirements**: KVKK (Turkish Personal Data Protection Law) compliant
- **Developer**: Furkan (responsible for Infrastructure, Auth, User, Admin modules - ~58% of backend)

### Current State
- Phase 0-12: All core modules complete
- TokenUtils and DateUtils partially exist (from Phase 5)
- Various services need utility methods for:
  - URL-friendly slugs for campaigns
  - Bank transfer reference codes
  - Donation receipt numbers
  - File handling utilities

### What This Phase Accomplishes
This phase implements various utility classes used across the application. These utilities provide common functionality for slug generation, reference codes, file handling, and other cross-cutting concerns. Well-designed utilities reduce code duplication and ensure consistency.

---

## OBJECTIVE

Create comprehensive utility classes including:
1. SlugGenerator for URL-friendly slugs from Turkish text
2. ReferenceCodeGenerator for bank transfer references
3. ReceiptNumberGenerator for donation receipts
4. FileUtils for file handling operations
5. StringUtils for string manipulation
6. NumberUtils for number formatting

---

## IMPORTANT RULES

### Code Style Requirements
- Use clear, readable code - prefer if-else statements over ternary operators
- Follow SOLID principles
- Add comprehensive comments in English
- Use meaningful variable and method names
- No complex one-liners - prioritize readability over brevity

### Utility Class Requirements
- All utility classes should be final with private constructors
- All methods should be static
- Methods should be null-safe (handle null inputs gracefully)
- Methods should be thread-safe
- Include comprehensive unit tests

### Turkish Language Requirements
- Handle Turkish-specific characters (ş, ğ, ı, ö, ü, ç)
- Support Turkish locale for case conversion
- Handle Turkish number formatting (comma for decimals in display)

---

## DETAILED REQUIREMENTS

### 1. Slug Generator

#### 1.1 SlugGenerator.java
**Location**: `src/main/java/com/seffafbagis/api/util/SlugGenerator.java`

**Purpose**: Generate URL-friendly slugs from text, especially Turkish text

**Requirements**:

**Turkish Character Mapping**:
```
ş → s
ğ → g
ı → i
ö → o
ü → u
ç → c
Ş → s
Ğ → g
İ → i
Ö → o
Ü → u
Ç → c
```

**Methods**:

---

**`generateSlug(String text)`**

**Purpose**: Convert text to URL-friendly slug

**Flow**:
1. If text is null or empty, return empty string
2. Convert to lowercase (Turkish locale aware)
3. Replace Turkish characters with ASCII equivalents
4. Replace spaces with hyphens
5. Remove all characters except a-z, 0-9, hyphen
6. Replace multiple consecutive hyphens with single hyphen
7. Remove leading and trailing hyphens
8. Truncate to max length (100 chars default)
9. Return slug

**Examples**:
```
"Deprem Yardım Kampanyası" → "deprem-yardim-kampanyasi"
"İstanbul'dan Sevgilerle!" → "istanbuldan-sevgilerle"
"2024 Eğitim Desteği" → "2024-egitim-destegi"
"  Multiple   Spaces  " → "multiple-spaces"
```

---

**`generateSlug(String text, int maxLength)`**

**Purpose**: Generate slug with custom max length

**Flow**:
- Same as above but truncate to specified maxLength
- Truncate at word boundary if possible (don't cut words)

---

**`generateUniqueSlug(String text, Function<String, Boolean> existsCheck)`**

**Purpose**: Generate unique slug by appending number if exists

**Flow**:
1. Generate base slug
2. Check if exists using provided function
3. If exists, append "-2", "-3", etc. until unique
4. Return unique slug

**Example**:
```
If "deprem-yardim" exists:
  Try "deprem-yardim-2"
  If exists, try "deprem-yardim-3"
  etc.
```

---

**`toAscii(String text)`**

**Purpose**: Convert Turkish characters to ASCII

**Flow**:
1. Replace each Turkish character with ASCII equivalent
2. Return result

---

**`normalize(String text)`**

**Purpose**: Normalize text (trim, lowercase, remove extra spaces)

**Flow**:
1. Trim whitespace
2. Convert to lowercase
3. Replace multiple spaces with single space
4. Return normalized text

---

### 2. Reference Code Generator

#### 2.1 ReferenceCodeGenerator.java
**Location**: `src/main/java/com/seffafbagis/api/util/ReferenceCodeGenerator.java`

**Purpose**: Generate unique reference codes for bank transfers

**Reference Code Format**: `SBP-YYYYMMDD-XXXXX`
- SBP: Platform prefix (Şeffaf Bağış Platformu)
- YYYYMMDD: Current date
- XXXXX: 5 random alphanumeric characters (uppercase)

**Methods**:

---

**`generate()`**

**Purpose**: Generate a new reference code

**Flow**:
1. Get current date formatted as YYYYMMDD
2. Generate 5 random alphanumeric characters (A-Z, 0-9)
3. Combine: "SBP-" + date + "-" + random
4. Return reference code

**Example Output**: `SBP-20240115-A3K9M`

---

**`generate(String prefix)`**

**Purpose**: Generate reference code with custom prefix

**Flow**:
- Same as above but use provided prefix instead of "SBP"

---

**`generateWithChecksum()`**

**Purpose**: Generate reference code with checksum digit

**Flow**:
1. Generate base reference code
2. Calculate checksum (simple sum of char values mod 10)
3. Append checksum digit
4. Return code

**Example Output**: `SBP-20240115-A3K9M-7`

---

**`validateFormat(String referenceCode)`**

**Purpose**: Validate reference code format

**Flow**:
1. Check matches pattern: `^[A-Z]{2,5}-\d{8}-[A-Z0-9]{5}(-\d)?$`
2. Return boolean

---

**`extractDate(String referenceCode)`**

**Purpose**: Extract date from reference code

**Flow**:
1. Parse the YYYYMMDD portion
2. Return LocalDate

---

**`isExpired(String referenceCode, int validDays)`**

**Purpose**: Check if reference code is expired

**Flow**:
1. Extract date from code
2. Compare with current date
3. Return true if older than validDays

---

### 3. Receipt Number Generator

#### 3.1 ReceiptNumberGenerator.java
**Location**: `src/main/java/com/seffafbagis/api/util/ReceiptNumberGenerator.java`

**Purpose**: Generate sequential receipt numbers for donations

**Receipt Number Format**: `RCPT-YYYY-NNNNNN`
- RCPT: Receipt prefix
- YYYY: Year
- NNNNNN: 6-digit sequential number (zero-padded)

**Methods**:

---

**`generate(int year, long sequenceNumber)`**

**Purpose**: Generate receipt number from year and sequence

**Flow**:
1. Format sequence number with leading zeros (6 digits)
2. Combine: "RCPT-" + year + "-" + paddedSequence
3. Return receipt number

**Example**: `generate(2024, 42)` → `RCPT-2024-000042`

---

**`generate(long sequenceNumber)`**

**Purpose**: Generate receipt number for current year

**Flow**:
- Call generate(currentYear, sequenceNumber)

---

**`parseSequenceNumber(String receiptNumber)`**

**Purpose**: Extract sequence number from receipt number

**Flow**:
1. Parse the NNNNNN portion
2. Return as long

---

**`parseYear(String receiptNumber)`**

**Purpose**: Extract year from receipt number

**Flow**:
1. Parse the YYYY portion
2. Return as int

---

**`validateFormat(String receiptNumber)`**

**Purpose**: Validate receipt number format

**Flow**:
1. Check matches pattern: `^RCPT-\d{4}-\d{6}$`
2. Return boolean

---

### 4. File Utilities

#### 4.1 FileUtils.java
**Location**: `src/main/java/com/seffafbagis/api/util/FileUtils.java`

**Purpose**: File handling utility methods

**Methods**:

---

**`getFileExtension(String filename)`**

**Purpose**: Extract file extension

**Flow**:
1. If filename is null or has no dot, return empty string
2. Return substring after last dot (lowercase)

**Examples**:
```
"document.pdf" → "pdf"
"image.PNG" → "png"
"noextension" → ""
"file.name.txt" → "txt"
```

---

**`getFileNameWithoutExtension(String filename)`**

**Purpose**: Get filename without extension

**Flow**:
1. If no extension, return original
2. Return substring before last dot

---

**`generateUniqueFilename(String originalFilename)`**

**Purpose**: Generate unique filename preserving extension

**Flow**:
1. Extract extension
2. Generate UUID
3. Combine: UUID + "." + extension
4. Return unique filename

**Example**: `report.pdf` → `a1b2c3d4-e5f6-7890-abcd-ef1234567890.pdf`

---

**`sanitizeFilename(String filename)`**

**Purpose**: Remove unsafe characters from filename

**Flow**:
1. Replace Turkish characters with ASCII
2. Remove characters except a-z, A-Z, 0-9, dot, hyphen, underscore
3. Replace spaces with underscores
4. Return sanitized filename

---

**`isAllowedExtension(String filename, List<String> allowedExtensions)`**

**Purpose**: Check if file extension is allowed

**Flow**:
1. Extract extension
2. Check if in allowed list (case-insensitive)
3. Return boolean

---

**`isAllowedMimeType(String mimeType, List<String> allowedMimeTypes)`**

**Purpose**: Check if MIME type is allowed

**Flow**:
1. Check if mimeType is in allowed list
2. Return boolean

---

**`formatFileSize(long sizeInBytes)`**

**Purpose**: Format file size for display

**Flow**:
1. Convert to appropriate unit (B, KB, MB, GB)
2. Format with 2 decimal places
3. Return formatted string

**Examples**:
```
1024 → "1.00 KB"
1048576 → "1.00 MB"
1073741824 → "1.00 GB"
500 → "500 B"
```

---

**`getMimeType(String filename)`**

**Purpose**: Get MIME type from filename

**Flow**:
1. Map common extensions to MIME types
2. Return MIME type or "application/octet-stream" for unknown

**Common Mappings**:
```
pdf → application/pdf
jpg, jpeg → image/jpeg
png → image/png
doc → application/msword
docx → application/vnd.openxmlformats-officedocument.wordprocessingml.document
xls → application/vnd.ms-excel
xlsx → application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
```

---

### 5. String Utilities

#### 5.1 StringUtils.java
**Location**: `src/main/java/com/seffafbagis/api/util/StringUtils.java`

**Purpose**: String manipulation utilities (beyond Apache Commons)

**Methods**:

---

**`truncate(String text, int maxLength)`**

**Purpose**: Truncate text to max length

**Flow**:
1. If text is null or shorter than maxLength, return as-is
2. Truncate to maxLength - 3
3. Append "..."
4. Return truncated text

---

**`truncateAtWord(String text, int maxLength)`**

**Purpose**: Truncate at word boundary

**Flow**:
1. If shorter than maxLength, return as-is
2. Find last space before maxLength
3. Truncate at that space
4. Append "..."
5. Return truncated text

---

**`maskEmail(String email)`**

**Purpose**: Mask email for display

**Flow**:
1. Split at @
2. Show first 2 chars of local part
3. Mask middle with ***
4. Show last char before @
5. Show full domain

**Example**: `furkan@example.com` → `fu***n@example.com`

---

**`maskPhone(String phone)`**

**Purpose**: Mask phone number

**Flow**:
1. Show country code and first 3 digits
2. Mask middle
3. Show last 2 digits

**Example**: `+905321234567` → `+90 532 *** ** 67`

---

**`toTitleCase(String text)`**

**Purpose**: Convert to title case (Turkish aware)

**Flow**:
1. Split by spaces
2. Capitalize first letter of each word
3. Handle Turkish İ/ı correctly
4. Join with spaces
5. Return result

**Example**: `merhaba dünya` → `Merhaba Dünya`

---

**`removeHtmlTags(String html)`**

**Purpose**: Remove HTML tags from string

**Flow**:
1. Use regex to remove all HTML tags
2. Decode HTML entities
3. Return plain text

---

**`isValidEmail(String email)`**

**Purpose**: Validate email format

**Flow**:
1. Check against email regex pattern
2. Return boolean

---

**`isValidUrl(String url)`**

**Purpose**: Validate URL format

**Flow**:
1. Check against URL regex pattern
2. Return boolean

---

**`generateRandomString(int length, boolean includeNumbers, boolean includeSpecial)`**

**Purpose**: Generate random string with options

**Flow**:
1. Build character set based on options
2. Generate random string from character set
3. Return result

---

### 6. Number Utilities

#### 6.1 NumberUtils.java
**Location**: `src/main/java/com/seffafbagis/api/util/NumberUtils.java`

**Purpose**: Number formatting and parsing utilities

**Methods**:

---

**`formatCurrency(BigDecimal amount)`**

**Purpose**: Format amount as Turkish Lira

**Flow**:
1. Format with 2 decimal places
2. Use Turkish locale (comma for decimal, dot for thousands)
3. Append " TL"
4. Return formatted string

**Example**: `1234567.89` → `1.234.567,89 TL`

---

**`formatCurrency(BigDecimal amount, String currencyCode)`**

**Purpose**: Format amount with specified currency

**Flow**:
1. Format number
2. Append currency symbol or code
3. Return formatted string

---

**`formatPercentage(double value)`**

**Purpose**: Format as percentage

**Flow**:
1. Multiply by 100 if needed
2. Format with 1 decimal place
3. Append "%"
4. Return formatted string

**Example**: `0.456` → `45.6%`

---

**`formatNumber(long number)`**

**Purpose**: Format number with thousand separators

**Flow**:
1. Use Turkish locale for formatting
2. Return formatted string

**Example**: `1234567` → `1.234.567`

---

**`formatCompact(long number)`**

**Purpose**: Format large numbers compactly

**Flow**:
1. If >= 1 billion: format as X.X Milyar
2. If >= 1 million: format as X.X Milyon
3. If >= 1 thousand: format as X.X Bin
4. Else: format normally

**Examples**:
```
1500000000 → "1.5 Milyar"
2500000 → "2.5 Milyon"
15000 → "15 Bin"
500 → "500"
```

---

**`parseCurrency(String text)`**

**Purpose**: Parse Turkish currency string to BigDecimal

**Flow**:
1. Remove currency symbols and spaces
2. Replace Turkish decimal separator (,) with dot
3. Remove thousand separators (.)
4. Parse to BigDecimal
5. Return value

---

**`roundToNearest(BigDecimal value, BigDecimal nearest)`**

**Purpose**: Round to nearest value

**Flow**:
1. Divide by nearest
2. Round to integer
3. Multiply by nearest
4. Return result

**Example**: `roundToNearest(127.50, 5)` → `130.00`

---

**`calculatePercentage(BigDecimal part, BigDecimal whole)`**

**Purpose**: Calculate percentage

**Flow**:
1. If whole is zero, return zero
2. Calculate (part / whole) * 100
3. Round to 2 decimal places
4. Return result

---

## FILE STRUCTURE

After completing this phase, the following files should exist:

```
src/main/java/com/seffafbagis/api/util/
├── SlugGenerator.java
├── ReferenceCodeGenerator.java
├── ReceiptNumberGenerator.java
├── FileUtils.java
├── StringUtils.java
└── NumberUtils.java
```

**Total Files**: 6

---

## TESTING REQUIREMENTS

After implementation, verify:

### 1. SlugGenerator Tests

**Test: Basic Slug**
```
Input: "Merhaba Dünya"
Expected: "merhaba-dunya"
```

**Test: Turkish Characters**
```
Input: "Şeffaf Bağış"
Expected: "seffaf-bagis"
```

**Test: Special Characters**
```
Input: "Hello! World? #2024"
Expected: "hello-world-2024"
```

**Test: Multiple Spaces**
```
Input: "Multiple   Spaces"
Expected: "multiple-spaces"
```

**Test: Unique Slug**
- Generate slug
- Check exists (return true)
- Generate with suffix
- Verify unique

### 2. ReferenceCodeGenerator Tests

**Test: Generate**
- Generate reference code
- Verify format matches pattern
- Verify date is today

**Test: Validate**
- Validate valid code → true
- Validate invalid code → false

**Test: Extract Date**
- Generate code
- Extract date
- Verify matches today

### 3. ReceiptNumberGenerator Tests

**Test: Generate**
```
Input: (2024, 42)
Expected: "RCPT-2024-000042"
```

**Test: Parse**
```
Input: "RCPT-2024-000042"
Extract Year: 2024
Extract Sequence: 42
```

### 4. FileUtils Tests

**Test: Extension**
```
"file.pdf" → "pdf"
"FILE.PDF" → "pdf"
"noext" → ""
```

**Test: Sanitize**
```
"Dosya Adı (1).pdf" → "Dosya_Adi_1.pdf"
```

**Test: Format Size**
```
1024 → "1.00 KB"
1048576 → "1.00 MB"
```

### 5. StringUtils Tests

**Test: Mask Email**
```
"test@example.com" → "te**t@example.com"
```

**Test: Mask Phone**
```
"+905321234567" → "+90 532 *** ** 67"
```

**Test: Title Case**
```
"merhaba dünya" → "Merhaba Dünya"
```

### 6. NumberUtils Tests

**Test: Format Currency**
```
BigDecimal(1234.56) → "1.234,56 TL"
```

**Test: Format Compact**
```
1500000 → "1.5 Milyon"
```

**Test: Parse Currency**
```
"1.234,56 TL" → BigDecimal(1234.56)
```

---

## SUCCESS CRITERIA

Phase 13 is considered successful when:

1. ✅ All 6 files are created in correct locations
2. ✅ SlugGenerator handles Turkish characters correctly
3. ✅ SlugGenerator creates unique slugs when needed
4. ✅ ReferenceCodeGenerator creates valid codes
5. ✅ ReceiptNumberGenerator creates sequential numbers
6. ✅ FileUtils handles all file operations
7. ✅ StringUtils masks sensitive data correctly
8. ✅ NumberUtils formats Turkish currency correctly
9. ✅ All methods are null-safe
10. ✅ All methods are thread-safe
11. ✅ Comprehensive unit tests pass

---

## RESULT FILE REQUIREMENTS

After completing this phase, create a result file at:
`docs/Furkan/step_results/phase_13_result.md`

The result file must include:

1. **Execution Status**: Success or Failure
2. **Files Created**: List all 6 files with their paths
3. **SlugGenerator Tests**:
   - Turkish character handling
   - Unique slug generation
4. **ReferenceCodeGenerator Tests**:
   - Format validation
   - Date extraction
5. **ReceiptNumberGenerator Tests**:
   - Format verification
   - Parsing verification
6. **FileUtils Tests**:
   - Extension handling
   - Size formatting
7. **StringUtils Tests**:
   - Masking functions
   - Title case conversion
8. **NumberUtils Tests**:
   - Currency formatting
   - Compact number formatting
9. **Issues Encountered**: Any problems and how they were resolved
10. **Notes for Next Phase**: Observations relevant to Phase 14

---

## USAGE EXAMPLES

### SlugGenerator in Campaign Service
```java
public Campaign createCampaign(CreateCampaignRequest request) {
    String baseSlug = SlugGenerator.generateSlug(request.getTitle());
    String uniqueSlug = SlugGenerator.generateUniqueSlug(
        baseSlug, 
        slug -> campaignRepository.existsBySlug(slug)
    );
    
    Campaign campaign = new Campaign();
    campaign.setTitle(request.getTitle());
    campaign.setSlug(uniqueSlug);
    // ...
}
```

### ReferenceCodeGenerator in Bank Transfer
```java
public BankTransferReference createReference(UUID donationId) {
    BankTransferReference ref = new BankTransferReference();
    ref.setReferenceCode(ReferenceCodeGenerator.generate());
    ref.setDonationId(donationId);
    ref.setExpiresAt(LocalDateTime.now().plusDays(3));
    return repository.save(ref);
}
```

### NumberUtils in Report Generation
```java
public String generateDonationSummary(Campaign campaign) {
    return String.format(
        "Toplam Bağış: %s\nHedef: %s\nİlerleme: %s",
        NumberUtils.formatCurrency(campaign.getCurrentAmount()),
        NumberUtils.formatCurrency(campaign.getTargetAmount()),
        NumberUtils.formatPercentage(
            campaign.getCurrentAmount()
                .divide(campaign.getTargetAmount(), 4, RoundingMode.HALF_UP)
                .doubleValue()
        )
    );
}
```

---

## NOTES

- Utility classes should be stateless
- Use SecureRandom for any random generation
- Consider caching compiled regex patterns for performance
- Turkish locale handling is critical for this platform
- These utilities will be used across multiple services

---

## NEXT PHASE PREVIEW

Phase 14 (Integration & Final Testing) will:
- Create integration tests for complete flows
- Test end-to-end scenarios
- Generate final documentation
- Verify all components work together
- Performance testing
- Security review
