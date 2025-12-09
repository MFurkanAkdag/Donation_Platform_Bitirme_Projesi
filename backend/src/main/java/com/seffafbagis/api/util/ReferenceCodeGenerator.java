package com.seffafbagis.api.util;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * Utility class for generating unique reference codes.
 * Format: SBP-YYYYMMDD-XXXXX
 */
public final class ReferenceCodeGenerator {

    private static final String DEFAULT_PREFIX = "SBP";
    private static final String ALPHANUMERIC = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Removed ambiguous chars like I, 1,
                                                                                   // O, 0
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Pattern REFERENCE_PATTERN = Pattern.compile("^[A-Z]{2,5}-\\d{8}-[A-Z0-9]{5}(-\\d)?$");

    private ReferenceCodeGenerator() {
        throw new AssertionError("Cannot instantiate ReferenceCodeGenerator");
    }

    /**
     * Generates a new reference code with default prefix "SBP".
     *
     * @return Reference code (e.g., SBP-20240115-A3K9M)
     */
    public static String generate() {
        return generate(DEFAULT_PREFIX);
    }

    /**
     * Generates a new reference code with custom prefix.
     *
     * @param prefix Prefix to use
     * @return Reference code
     */
    public static String generate(String prefix) {
        String datePart = LocalDate.now().format(DATE_FORMATTER);
        String randomPart = generateRandomString(5);
        return String.format("%s-%s-%s", prefix, datePart, randomPart);
    }

    /**
     * Generates a reference code with a checksum digit.
     *
     * @return Reference code with checksum
     */
    public static String generateWithChecksum() {
        String base = generate();
        int checksum = calculateChecksum(base);
        return base + "-" + checksum;
    }

    /**
     * Validates the format of a reference code.
     *
     * @param referenceCode Code to validate
     * @return true if format is valid
     */
    public static boolean validateFormat(String referenceCode) {
        if (referenceCode == null) {
            return false;
        }
        return REFERENCE_PATTERN.matcher(referenceCode).matches();
    }

    /**
     * Extracts the date part from a reference code.
     *
     * @param referenceCode Reference code
     * @return LocalDate extracted from code, or null if invalid
     */
    public static LocalDate extractDate(String referenceCode) {
        if (!validateFormat(referenceCode)) {
            return null;
        }
        try {
            String[] parts = referenceCode.split("-");
            // SBP-20240115-XXXXX
            // parts[0] = prefix, parts[1] = date
            return LocalDate.parse(parts[1], DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Checks if a reference code is expired based on its date.
     *
     * @param referenceCode Reference code
     * @param validDays     Number of days the code is valid for
     * @return true if expired
     */
    public static boolean isExpired(String referenceCode, int validDays) {
        LocalDate date = extractDate(referenceCode);
        if (date == null) {
            return true; // Treat invalid as expired/invalid
        }
        LocalDate expiratonDate = date.plusDays(validDays);
        return LocalDate.now().isAfter(expiratonDate);
    }

    private static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }

    private static int calculateChecksum(String code) {
        int sum = 0;
        for (char c : code.toCharArray()) {
            if (c != '-') {
                sum += c;
            }
        }
        return sum % 10;
    }
}
