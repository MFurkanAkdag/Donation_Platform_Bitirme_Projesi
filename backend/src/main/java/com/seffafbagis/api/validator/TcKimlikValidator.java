package com.seffafbagis.api.validator;

import com.seffafbagis.api.exception.BadRequestException;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Validator for Turkish National ID (TC Kimlik).
 * 
 * Turkish National ID specifications:
 * - Exactly 11 digits
 * - First digit cannot be 0
 * - Passes mod-10 checksum algorithm
 * - Mandatory for Turkish citizens
 * 
 * Provides validation modes:
 * 1. validate() - Returns ValidationResult with details
 * 2. validateOrThrow() - Throws BadRequestException on failure
 * 3. isValid() - Returns boolean
 * 4. mask() - Returns masked version (***-***-**XX)
 * 
 * Example valid TC Kimlik: 12345678901
 * 
 * @author Furkan
 * @version 1.0
 */
public class TcKimlikValidator {

    /**
     * Required length of TC Kimlik (11 digits).
     */
    private static final int TC_KIMLIK_LENGTH = 11;

    /**
     * TC Kimlik being validated.
     */
    private final String tcKimlik;

    /**
     * Constructor for TcKimlikValidator.
     * 
     * @param tcKimlik TC Kimlik number as string
     */
    public TcKimlikValidator(String tcKimlik) {
        this.tcKimlik = tcKimlik;
    }

    /**
     * Validates the TC Kimlik and returns a result object.
     * 
     * @return ValidationResult with validation status and errors
     */
    public ValidationResult validate() {
        List<String> errors = new ArrayList<>();

        if (tcKimlik == null) {
            errors.add("TC Kimlik cannot be null");
            return new ValidationResult(false, errors);
        }

        if (tcKimlik.isEmpty()) {
            errors.add("TC Kimlik cannot be empty");
            return new ValidationResult(false, errors);
        }

        // Remove whitespace and dashes
        String cleaned = tcKimlik.replaceAll("[\\s\\-]", "");

        // Check if only digits
        if (!cleaned.matches("\\d+")) {
            errors.add("TC Kimlik must contain only digits");
            return new ValidationResult(false, errors);
        }

        // Check length
        if (cleaned.length() != TC_KIMLIK_LENGTH) {
            errors.add(String.format("TC Kimlik must be exactly %d digits long", TC_KIMLIK_LENGTH));
            return new ValidationResult(false, errors);
        }

        // Check first digit is not 0
        if (cleaned.charAt(0) == '0') {
            errors.add("TC Kimlik cannot start with 0");
            return new ValidationResult(false, errors);
        }

        // Validate checksum
        if (!isValidChecksum(cleaned)) {
            errors.add("TC Kimlik checksum is invalid");
            return new ValidationResult(false, errors);
        }

        return new ValidationResult(true, errors);
    }

    /**
     * Validates the TC Kimlik and throws exception on failure.
     * 
     * @throws BadRequestException if TC Kimlik is invalid
     */
    public void validateOrThrow() {
        ValidationResult result = validate();
        if (!result.isValid()) {
            String message = String.join(", ", result.getErrors());
            throw new BadRequestException(message);
        }
    }

    /**
     * Checks if TC Kimlik is valid (simple boolean check).
     * 
     * @return true if TC Kimlik is valid, false otherwise
     */
    public boolean isValid() {
        return validate().isValid();
    }

    /**
     * Returns masked TC Kimlik for display purposes.
     * 
     * Masks all but last two digits.
     * Format: ***-***-**XX
     * Example: 12345678901 becomes ***-***-**01
     * 
     * @return Masked TC Kimlik string
     */
    public String mask() {
        if (tcKimlik == null) {
            return null;
        }

        String cleaned = tcKimlik.replaceAll("[\\s\\-]", "");

        if (cleaned.length() != TC_KIMLIK_LENGTH) {
            return cleaned; // Return unmasked if invalid length
        }

        String lastTwo = cleaned.substring(TC_KIMLIK_LENGTH - 2);
        return String.format("***-***-**%s", lastTwo);
    }

    /**
     * Normalizes TC Kimlik to standard format (11 digits without formatting).
     * 
     * Removes whitespace and dashes.
     * Suitable for storage and comparison.
     * 
     * @return Normalized TC Kimlik or null if input is null
     */
    public String normalize() {
        if (tcKimlik == null) {
            return null;
        }
        return tcKimlik.replaceAll("[\\s\\-]", "");
    }

    /**
     * Validates TC Kimlik checksum using mod-10 algorithm.
     * 
     * The algorithm:
     * 1. Sum of odd positions (1st, 3rd, 5th, ..., 9th) * 3
     * 2. Sum of even positions (2nd, 4th, 6th, ..., 8th) * 7
     * 3. Sum of all above
     * 4. 10th digit = (sum % 10 == 0) ? 0 : (10 - (sum % 10))
     * 5. 11th digit = (sum1 + sum2) * 3) % 10
     * 
     * Where:
     * - sum1 = sum of odd positions * 3 + sum of even positions * 7
     * - sum2 = sum of first 10 digits
     * 
     * @param cleaned Cleaned TC Kimlik (11 digits)
     * @return true if checksum is valid
     */
    private boolean isValidChecksum(String cleaned) {
        // Extract digits
        int[] digits = new int[TC_KIMLIK_LENGTH];
        for (int i = 0; i < TC_KIMLIK_LENGTH; i++) {
            digits[i] = Character.getNumericValue(cleaned.charAt(i));
        }

        // Calculate 10th digit (index 9)
        int oddSum = digits[0] + digits[2] + digits[4] + digits[6] + digits[8];
        int evenSum = digits[1] + digits[3] + digits[5] + digits[7];

        int firstCheck = (oddSum * 3 + evenSum * 7) % 10;
        if (firstCheck != digits[9]) {
            return false;
        }

        // Calculate 11th digit (index 10)
        int sum = digits[0] + digits[1] + digits[2] + digits[3] + digits[4] +
                  digits[5] + digits[6] + digits[7] + digits[8] + digits[9];
        int secondCheck = sum % 10;
        if (secondCheck != digits[10]) {
            return false;
        }

        return true;
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
         * @return true if TC Kimlik is valid
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
