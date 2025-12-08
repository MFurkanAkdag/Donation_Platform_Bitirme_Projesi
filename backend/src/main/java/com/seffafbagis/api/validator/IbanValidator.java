package com.seffafbagis.api.validator;

import com.seffafbagis.api.exception.BadRequestException;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Validator for Turkish International Bank Account Numbers (IBAN).
 * 
 * Turkish IBAN specifications:
 * - Country code: TR (fixed)
 * - Check digits: 2 digits (mod-97 algorithm)
 * - Bank code: 5 digits
 * - Routing code: 1 digit
 * - Account number: 16 digits
 * - Total: 26 characters
 * - Format: TR**BBBBB*CCCCCCCCCCCCCCCC
 *   where * = routing, B = bank code, C = account number
 * 
 * Accepts formats:
 * - Standard: TR3512345678901234567890123
 * - With spaces: TR35 1234 5678 9012 3456 7890 123
 * - With dashes: TR35-1234-5678-9012-3456-7890-123
 * 
 * Provides methods:
 * - validate() - ValidationResult with details
 * - validateOrThrow() - Throws exception on failure
 * - isValid() - Boolean check
 * - normalize() - Returns standard format without spaces/dashes
 * - format() - Returns formatted version TR** **** **** ****
 * - mask() - Returns masked version TR** **** **** **CCCCCC
 * - extractBankCode() - Returns 5-digit bank code
 * 
 * @author Furkan
 * @version 1.0
 */
public class IbanValidator {

    /**
     * Turkish IBAN country code.
     */
    private static final String COUNTRY_CODE = "TR";

    /**
     * Required length of Turkish IBAN (characters, including country code and check digits).
     */
    private static final int IBAN_LENGTH = 26;

    /**
     * Length of IBAN check digits.
     */
    private static final int CHECK_DIGIT_LENGTH = 2;

    /**
     * Position where check digits end (country code + check digits = 4).
     */
    private static final int CHECK_DIGIT_END = 4;

    /**
     * Position where bank code starts (after country code and check digits).
     */
    private static final int BANK_CODE_START = 4;

    /**
     * Length of bank code in IBAN.
     */
    private static final int BANK_CODE_LENGTH = 5;

    /**
     * Position where bank code ends.
     */
    private static final int BANK_CODE_END = BANK_CODE_START + BANK_CODE_LENGTH;

    /**
     * Position where account number starts.
     */
    private static final int ACCOUNT_NUMBER_START = BANK_CODE_END;

    /**
     * IBAN being validated.
     */
    private final String iban;

    /**
     * Constructor for IbanValidator.
     * 
     * @param iban IBAN to validate
     */
    public IbanValidator(String iban) {
        this.iban = iban;
    }

    /**
     * Validates the IBAN and returns a result object.
     * 
     * @return ValidationResult with validation status and errors
     */
    public ValidationResult validate() {
        List<String> errors = new ArrayList<>();

        if (iban == null) {
            errors.add("IBAN cannot be null");
            return new ValidationResult(false, errors);
        }

        if (iban.isEmpty()) {
            errors.add("IBAN cannot be empty");
            return new ValidationResult(false, errors);
        }

        // Normalize IBAN (remove spaces, dashes, convert to uppercase)
        String normalized = normalize();

        if (normalized == null) {
            errors.add("IBAN format is invalid");
            return new ValidationResult(false, errors);
        }

        // Check country code
        if (!normalized.startsWith(COUNTRY_CODE)) {
            errors.add(String.format("IBAN must start with country code %s", COUNTRY_CODE));
            return new ValidationResult(false, errors);
        }

        // Check length
        if (normalized.length() != IBAN_LENGTH) {
            errors.add(String.format("IBAN must be exactly %d characters long", IBAN_LENGTH));
            return new ValidationResult(false, errors);
        }

        // Check if alphanumeric
        if (!normalized.matches("^[A-Z0-9]+$")) {
            errors.add("IBAN must contain only letters and digits");
            return new ValidationResult(false, errors);
        }

        // Validate checksum (mod-97)
        if (!isValidChecksum(normalized)) {
            errors.add("IBAN checksum is invalid");
            return new ValidationResult(false, errors);
        }

        return new ValidationResult(true, errors);
    }

    /**
     * Validates the IBAN and throws exception on failure.
     * 
     * @throws BadRequestException if IBAN is invalid
     */
    public void validateOrThrow() {
        ValidationResult result = validate();
        if (!result.isValid()) {
            String message = String.join(", ", result.getErrors());
            throw new BadRequestException(message);
        }
    }

    /**
     * Checks if IBAN is valid (simple boolean check).
     * 
     * @return true if IBAN is valid, false otherwise
     */
    public boolean isValid() {
        return validate().isValid();
    }

    /**
     * Returns normalized IBAN (uppercase, no spaces or dashes).
     * 
     * Format: TR3512345678901234567890123
     * 
     * @return Normalized IBAN or null if invalid
     */
    public String normalize() {
        if (iban == null) {
            return null;
        }

        // Remove spaces and dashes, convert to uppercase
        String normalized = iban.replaceAll("[\\s\\-]", "").toUpperCase();

        // Verify basic format
        if (normalized.length() < IBAN_LENGTH || !normalized.startsWith(COUNTRY_CODE)) {
            return null;
        }

        return normalized;
    }

    /**
     * Returns formatted IBAN for display.
     * 
     * Format: TR35 1234 5678 9012 3456 7890 123
     * Groups: 4 + 4 + 4 + 4 + 4 + 4 + 2 characters
     * 
     * @return Formatted IBAN or null if invalid
     */
    public String format() {
        String normalized = normalize();
        if (normalized == null || normalized.length() != IBAN_LENGTH) {
            return null;
        }

        // Format: XXXX XXXX XXXX XXXX XXXX XXXX XX
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < normalized.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                formatted.append(" ");
            }
            formatted.append(normalized.charAt(i));
        }

        return formatted.toString();
    }

    /**
     * Returns masked IBAN for display purposes.
     * 
     * Format: TR** **** **** **CCCCCC
     * Shows country code, check digits, and last 6 account number digits.
     * 
     * @return Masked IBAN or null if invalid
     */
    public String mask() {
        String normalized = normalize();
        if (normalized == null || normalized.length() != IBAN_LENGTH) {
            return null;
        }

        // Extract visible parts
        String countryAndCheck = normalized.substring(0, CHECK_DIGIT_END); // TR35
        String lastAccountDigits = normalized.substring(IBAN_LENGTH - 6); // Last 6

        // Format: TR** **** **** **CCCCCC
        return String.format("%s **** **** **%s", countryAndCheck, lastAccountDigits);
    }

    /**
     * Extracts bank code from IBAN.
     * 
     * Bank code is 5 digits after country code and check digits.
     * Position: characters 5-9 (or indices 4-8).
     * 
     * @return 5-digit bank code or null if IBAN invalid
     */
    public String extractBankCode() {
        String normalized = normalize();
        if (normalized == null || normalized.length() < BANK_CODE_END) {
            return null;
        }

        return normalized.substring(BANK_CODE_START, BANK_CODE_END);
    }

    /**
     * Validates IBAN checksum using mod-97 algorithm.
     * 
     * The algorithm:
     * 1. Move the first 4 characters to the end (country code + check digits)
     * 2. Replace letters with numbers (A=10, B=11, ..., Z=35)
     * 3. Calculate mod 97 of the resulting number
     * 4. Result should be 1
     * 
     * @param normalized Normalized IBAN (26 characters)
     * @return true if checksum is valid
     */
    private boolean isValidChecksum(String normalized) {
        try {
            // Step 1: Move first 4 characters to the end
            String rearranged = normalized.substring(4) + normalized.substring(0, 4);

            // Step 2: Replace letters with numbers
            StringBuilder numeric = new StringBuilder();
            for (char c : rearranged.toCharArray()) {
                if (Character.isDigit(c)) {
                    numeric.append(c);
                } else if (Character.isLetter(c)) {
                    // A=10, B=11, ..., Z=35
                    int value = Character.toUpperCase(c) - 'A' + 10;
                    numeric.append(value);
                } else {
                    return false; // Invalid character
                }
            }

            // Step 3: Calculate mod 97
            // Use modulo operation with BigInteger for large numbers
            String numericString = numeric.toString();
            int remainder = 0;
            for (char digit : numericString.toCharArray()) {
                remainder = (remainder * 10 + Character.getNumericValue(digit)) % 97;
            }

            // Step 4: Check if remainder is 1
            return remainder == 1;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validation result containing status and error messages.
     */
    @Getter
    @ToString
    public static class ValidationResult {
        /**
         * Whether validation passed.
         */
        private final boolean valid;

        /**
         * List of validation error messages.
         */
        private final List<String> errors;

        /**
         * Constructor for ValidationResult.
         * 
         * @param valid whether validation passed
         * @param errors list of error messages
         */
        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }

        /**
         * Convenience method to check validity.
         * 
         * @return true if IBAN is valid
         */
        public boolean isValid() {
            return valid;
        }

        /**
         * Gets error messages list.
         * 
         * @return List of validation errors
         */
        public List<String> getErrors() {
            return errors;
        }

        /**
         * Gets first error message if validation failed.
         * 
         * @return First error message, or empty string if valid
         */
        public String getFirstError() {
            if (errors == null || errors.isEmpty()) {
                return "";
            }
            return errors.get(0);
        }

        /**
         * Gets error message count.
         * 
         * @return Number of validation errors
         */
        public int getErrorCount() {
            return errors == null ? 0 : errors.size();
        }
    }
}
