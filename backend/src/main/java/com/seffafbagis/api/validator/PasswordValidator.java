package com.seffafbagis.api.validator;

import com.seffafbagis.api.exception.BadRequestException;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Validator for user passwords.
 * 
 * Enforces OWASP password strength requirements:
 * - Minimum 8 characters, maximum 128 characters
 * - At least one uppercase letter (A-Z)
 * - At least one lowercase letter (a-z)
 * - At least one digit (0-9)
 * - At least one special character (!@#$%^&*)
 * - No whitespace characters
 * 
 * Provides three validation modes:
 * 1. validate() - Returns ValidationResult with details
 * 2. validateOrThrow() - Throws BadRequestException on failure
 * 3. isValid() - Returns boolean for simple checks
 * 
 * Also provides password strength assessment (WEAK, FAIR, STRONG, VERY_STRONG).
 * 
 * @author Furkan
 * @version 1.0
 */
public class PasswordValidator {

    /**
     * Minimum password length in characters.
     */
    private static final int MIN_LENGTH = 8;

    /**
     * Maximum password length in characters.
     */
    private static final int MAX_LENGTH = 128;

    /**
     * Special characters allowed in passwords.
     */
    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()-_=+[]{}|;:'\",.<>?/";

    /**
     * Password being validated.
     */
    private final String password;

    /**
     * Constructor for PasswordValidator.
     * 
     * @param password Password to validate
     */
    public PasswordValidator(String password) {
        this.password = password;
    }

    /**
     * Validates the password and returns a result object.
     * 
     * Contains validation status and list of error messages.
     * Suitable for form validation with detailed error reporting.
     * 
     * @return ValidationResult with validation status and errors
     */
    public ValidationResult validate() {
        List<String> errors = new ArrayList<>();

        if (password == null) {
            errors.add("Password cannot be null");
            return new ValidationResult(false, errors);
        }

        if (password.isEmpty()) {
            errors.add("Password cannot be empty");
            return new ValidationResult(false, errors);
        }

        // Check length
        if (password.length() < MIN_LENGTH) {
            errors.add(String.format("Password must be at least %d characters long", MIN_LENGTH));
        }
        if (password.length() > MAX_LENGTH) {
            errors.add(String.format("Password must not exceed %d characters", MAX_LENGTH));
        }

        // Check for uppercase
        if (!password.matches(".*[A-Z].*")) {
            errors.add("Password must contain at least one uppercase letter (A-Z)");
        }

        // Check for lowercase
        if (!password.matches(".*[a-z].*")) {
            errors.add("Password must contain at least one lowercase letter (a-z)");
        }

        // Check for digit
        if (!password.matches(".*\\d.*")) {
            errors.add("Password must contain at least one digit (0-9)");
        }

        // Check for special character
        boolean hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (SPECIAL_CHARACTERS.indexOf(c) >= 0) {
                hasSpecial = true;
                break;
            }
        }
        if (!hasSpecial) {
            errors.add(String.format("Password must contain at least one special character (%s)", SPECIAL_CHARACTERS));
        }

        // Check for whitespace
        if (password.contains(" ") || password.contains("\t") || password.contains("\n")) {
            errors.add("Password must not contain whitespace characters");
        }

        boolean valid = errors.isEmpty();
        return new ValidationResult(valid, errors);
    }

    /**
     * Validates the password and throws exception on failure.
     * 
     * Suitable for API endpoints and business logic
     * where validation failures should stop processing.
     * 
     * @throws BadRequestException if password is invalid
     */
    public void validateOrThrow() {
        ValidationResult result = validate();
        if (!result.isValid()) {
            String message = String.join(", ", result.getErrors());
            throw new BadRequestException(message);
        }
    }

    /**
     * Checks if password is valid (simple boolean check).
     * 
     * @return true if password meets all requirements, false otherwise
     */
    public boolean isValid() {
        return validate().isValid();
    }

    /**
     * Assesses password strength.
     * 
     * Strength levels:
     * - WEAK: Basic requirements met, minimal entropy
     * - FAIR: Good length and character variety
     * - STRONG: Excellent length and character variety
     * - VERY_STRONG: Maximum security (16+ chars with all requirements)
     * 
     * @return Password strength level
     */
    public PasswordStrength getStrength() {
        // First validate basic requirements
        if (!isValid()) {
            return PasswordStrength.WEAK;
        }

        int length = password.length();

        // Check for additional strength indicators
        boolean hasConsecutiveChars = hasConsecutiveCharacters();
        boolean hasRepeatedChars = hasRepeatedCharacters();
        boolean hasNumberPattern = hasNumberPattern();

        // Calculate strength score
        int strengthScore = 0;

        // Length score
        if (length >= 16) strengthScore += 3;
        else if (length >= 12) strengthScore += 2;
        else strengthScore += 1;

        // Character variety score
        int varietyCount = 0;
        if (password.matches(".*[A-Z].*")) varietyCount++;
        if (password.matches(".*[a-z].*")) varietyCount++;
        if (password.matches(".*\\d.*")) varietyCount++;
        if (password.matches(".*[" + SPECIAL_CHARACTERS.replace("]", "\\]").replace("\\", "\\\\") + "].*")) varietyCount++;

        strengthScore += varietyCount;

        // Penalty for patterns
        if (hasConsecutiveChars) strengthScore -= 1;
        if (hasRepeatedChars) strengthScore -= 1;
        if (hasNumberPattern) strengthScore -= 1;

        // Map score to strength level
        if (strengthScore >= 8) {
            return PasswordStrength.VERY_STRONG;
        } else if (strengthScore >= 6) {
            return PasswordStrength.STRONG;
        } else if (strengthScore >= 4) {
            return PasswordStrength.FAIR;
        } else {
            return PasswordStrength.WEAK;
        }
    }

    /**
     * Checks for consecutive character patterns (abc, 123, etc.).
     * 
     * @return true if password contains consecutive sequences
     */
    private boolean hasConsecutiveCharacters() {
        for (int i = 0; i < password.length() - 2; i++) {
            char c1 = password.charAt(i);
            char c2 = password.charAt(i + 1);
            char c3 = password.charAt(i + 2);

            if ((c2 == c1 + 1 && c3 == c2 + 1) || (c2 == c1 - 1 && c3 == c2 - 1)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks for repeated character patterns (aaa, 111, etc.).
     * 
     * @return true if password contains 3+ repeated characters
     */
    private boolean hasRepeatedCharacters() {
        for (int i = 0; i < password.length() - 2; i++) {
            if (password.charAt(i) == password.charAt(i + 1) &&
                password.charAt(i) == password.charAt(i + 2)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks for common number patterns (2024, 1234, 0000, etc.).
     * 
     * @return true if password contains common number patterns
     */
    private boolean hasNumberPattern() {
        // Check for year-like patterns (1900-2099)
        if (password.matches(".*[12]\\d{3}.*")) {
            return true;
        }

        // Check for ascending/descending sequences
        if (password.matches(".*[0-9]{2,}.*")) {
            for (int i = 0; i < password.length() - 1; i++) {
                char c = password.charAt(i);
                if (Character.isDigit(c)) {
                    char next = password.charAt(i + 1);
                    if (Character.isDigit(next) && Math.abs(c - next) == 1) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Password strength levels.
     */
    public enum PasswordStrength {
        WEAK("Weak - Does not meet security requirements"),
        FAIR("Fair - Meets basic requirements"),
        STRONG("Strong - Good security level"),
        VERY_STRONG("Very Strong - Excellent security level");

        private final String description;

        PasswordStrength(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
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
         * Empty if valid is true.
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
         * @return true if password is valid
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
