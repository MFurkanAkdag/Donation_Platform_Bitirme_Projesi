package com.seffafbagis.api.validator;

import com.seffafbagis.api.exception.BadRequestException;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Validator for Turkish mobile phone numbers.
 * 
 * Turkish mobile phone specifications:
 * - Turkish country code: +90
 * - Mobile network prefixes: 5XX (where XX is 0-9)
 * - Total: 12 digits with country code (+90), or 10 without
 * - Starts with 5 when stripped to national format
 * 
 * Accepts multiple input formats:
 * - International: +905321234567
 * - With spaces: +90 532 123 45 67
 * - With dashes: +90-532-123-4567
 * - National: 05321234567
 * - National with spaces: 0532 123 45 67
 * 
 * Provides methods:
 * - validate() - ValidationResult with details
 * - validateOrThrow() - Throws exception on failure
 * - isValid() - Boolean check
 * - normalize() - Returns +905321234567 format
 * - mask() - Returns masked version +90 *** *** ** 67
 * - format() - Returns formatted +90 532 123 45 67
 * 
 * @author Furkan
 * @version 1.0
 */
public class PhoneValidator {

    /**
     * Turkish country code.
     */
    private static final String COUNTRY_CODE = "90";

    /**
     * National prefix for Turkish landline/mobile.
     */
    private static final String NATIONAL_PREFIX = "0";

    /**
     * International prefix.
     */
    private static final String INTERNATIONAL_PREFIX = "+";

    /**
     * Valid mobile network prefixes (first digit of 10-digit national number is always 5).
     */
    private static final String MOBILE_PREFIX = "5";

    /**
     * Length of Turkish mobile number in national format (0-prefixed).
     */
    private static final int NATIONAL_LENGTH = 10;

    /**
     * Length of Turkish mobile number in international format (+90-prefixed).
     */
    private static final int INTERNATIONAL_LENGTH = 12;

    /**
     * Phone number being validated.
     */
    private final String phoneNumber;

    /**
     * Constructor for PhoneValidator.
     * 
     * @param phoneNumber Phone number to validate
     */
    public PhoneValidator(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Validates the phone number and returns a result object.
     * 
     * @return ValidationResult with validation status and errors
     */
    public ValidationResult validate() {
        List<String> errors = new ArrayList<>();

        if (phoneNumber == null) {
            errors.add("Phone number cannot be null");
            return new ValidationResult(false, errors);
        }

        if (phoneNumber.isEmpty()) {
            errors.add("Phone number cannot be empty");
            return new ValidationResult(false, errors);
        }

        // Normalize phone number
        String normalized = extractDigits(phoneNumber);

        if (normalized.isEmpty()) {
            errors.add("Phone number must contain digits");
            return new ValidationResult(false, errors);
        }

        // Check format
        if (!isValidFormat(normalized)) {
            errors.add("Invalid phone number format. Turkish mobile must start with 90 (international) or 0 (national) followed by 5");
            return new ValidationResult(false, errors);
        }

        // Check length
        if (!isValidLength(normalized)) {
            errors.add("Phone number length is invalid. Must be 10 digits (national) or 12 digits (international with 90)");
            return new ValidationResult(false, errors);
        }

        return new ValidationResult(true, errors);
    }

    /**
     * Validates the phone number and throws exception on failure.
     * 
     * @throws BadRequestException if phone number is invalid
     */
    public void validateOrThrow() {
        ValidationResult result = validate();
        if (!result.isValid()) {
            String message = String.join(", ", result.getErrors());
            throw new BadRequestException(message);
        }
    }

    /**
     * Checks if phone number is valid (simple boolean check).
     * 
     * @return true if phone number is valid, false otherwise
     */
    public boolean isValid() {
        return validate().isValid();
    }

    /**
     * Returns normalized Turkish phone number.
     * 
     * Format: +905321234567 (international with +90 prefix)
     * All characters except digits removed, then formatted to international.
     * 
     * @return Normalized phone number or null if input is null
     */
    public String normalize() {
        if (phoneNumber == null) {
            return null;
        }

        String digits = extractDigits(phoneNumber);

        // If already international format with 90
        if (digits.startsWith(COUNTRY_CODE) && digits.length() == INTERNATIONAL_LENGTH) {
            return INTERNATIONAL_PREFIX + digits;
        }

        // If national format with 0
        if (digits.startsWith(NATIONAL_PREFIX) && digits.length() == NATIONAL_LENGTH) {
            return INTERNATIONAL_PREFIX + COUNTRY_CODE + digits.substring(1);
        }

        // If 10 digits without prefix (just the number)
        if (digits.length() == NATIONAL_LENGTH && digits.startsWith(MOBILE_PREFIX)) {
            return INTERNATIONAL_PREFIX + COUNTRY_CODE + digits;
        }

        return null; // Invalid format
    }

    /**
     * Returns masked phone number for display purposes.
     * 
     * Format: +90 *** *** ** XX
     * Only shows last 2 digits.
     * Example: 05321234567 becomes +90 *** *** ** 67
     * 
     * @return Masked phone number or null if invalid
     */
    public String mask() {
        String normalized = normalize();
        if (normalized == null) {
            return null;
        }

        // Remove +90 prefix for manipulation
        String digits = normalized.substring(3); // Remove +90

        if (digits.length() < 2) {
            return normalized;
        }

        String lastTwo = digits.substring(digits.length() - 2);
        return String.format("+90 *** *** ** %s", lastTwo);
    }

    /**
     * Returns formatted phone number for display.
     * 
     * Format: +90 532 123 45 67
     * Suitable for user interface display.
     * 
     * @return Formatted phone number or null if invalid
     */
    public String format() {
        String normalized = normalize();
        if (normalized == null) {
            return null;
        }

        // Remove +90 prefix
        String digits = normalized.substring(3); // +90 = 3 chars

        // Format: XXX XXX XX XX (without the +90)
        if (digits.length() == 10) {
            return String.format("+90 %s %s %s %s",
                    digits.substring(0, 3),
                    digits.substring(3, 6),
                    digits.substring(6, 8),
                    digits.substring(8, 10));
        }

        return normalized;
    }

    /**
     * Extracts only digits from phone number.
     * 
     * Removes all non-digit characters (spaces, dashes, parentheses, +).
     * 
     * @param input Phone number string
     * @return String containing only digits
     */
    private String extractDigits(String input) {
        if (input == null) {
            return "";
        }
        return input.replaceAll("\\D", "");
    }

    /**
     * Validates phone number format.
     * 
     * Checks:
     * - International format: starts with 90 followed by 5
     * - National format: starts with 0 followed by 5
     * - No prefix format: starts with 5
     * 
     * @param digits Extracted digits from phone number
     * @return true if format is valid
     */
    private boolean isValidFormat(String digits) {
        // International format: 905XXXXXXXXX
        if (digits.startsWith(COUNTRY_CODE + MOBILE_PREFIX)) {
            return true;
        }

        // National format: 05XXXXXXXXX
        if (digits.startsWith(NATIONAL_PREFIX + MOBILE_PREFIX)) {
            return true;
        }

        // No prefix: 5XXXXXXXXX (10 digits total)
        if (digits.startsWith(MOBILE_PREFIX) && digits.length() == NATIONAL_LENGTH) {
            return true;
        }

        return false;
    }

    /**
     * Validates phone number length.
     * 
     * @param digits Extracted digits from phone number
     * @return true if length is valid
     */
    private boolean isValidLength(String digits) {
        // International: 12 digits (90 + 10 digits)
        if (digits.startsWith(COUNTRY_CODE)) {
            return digits.length() == INTERNATIONAL_LENGTH;
        }

        // National: 10 digits (0 + 9 digits, or just 10 for 5XXXXXXXXX)
        return digits.length() == NATIONAL_LENGTH;
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
         * @return true if phone number is valid
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
