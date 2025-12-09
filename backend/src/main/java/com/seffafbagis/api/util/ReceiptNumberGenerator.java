package com.seffafbagis.api.util;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for generating sequential receipt numbers.
 * Format: RCPT-YYYY-NNNNNN
 */
public final class ReceiptNumberGenerator {

    private static final String PREFIX = "RCPT";
    private static final Pattern RECEIPT_PATTERN = Pattern.compile("^RCPT-(\\d{4})-(\\d{6})$");

    private ReceiptNumberGenerator() {
        throw new AssertionError("Cannot instantiate ReceiptNumberGenerator");
    }

    /**
     * Generates a receipt number for the given year and sequence number.
     *
     * @param year           Year (e.g., 2024)
     * @param sequenceNumber Sequence number (will be padded to 6 digits)
     * @return Receipt number (e.g., RCPT-2024-000042)
     */
    public static String generate(int year, long sequenceNumber) {
        return String.format("%s-%d-%06d", PREFIX, year, sequenceNumber);
    }

    /**
     * Generates a receipt number for the current year.
     *
     * @param sequenceNumber Sequence number
     * @return Receipt number
     */
    public static String generate(long sequenceNumber) {
        return generate(LocalDate.now().getYear(), sequenceNumber);
    }

    /**
     * Parses the sequence number from a receipt number.
     *
     * @param receiptNumber Receipt number
     * @return Sequence number, or -1 if format is invalid
     */
    public static long parseSequenceNumber(String receiptNumber) {
        if (receiptNumber == null)
            return -1;

        Matcher matcher = RECEIPT_PATTERN.matcher(receiptNumber);
        if (matcher.matches()) {
            return Long.parseLong(matcher.group(2));
        }
        return -1;
    }

    /**
     * Parses the year from a receipt number.
     *
     * @param receiptNumber Receipt number
     * @return Year, or -1 if format is invalid
     */
    public static int parseYear(String receiptNumber) {
        if (receiptNumber == null)
            return -1;

        Matcher matcher = RECEIPT_PATTERN.matcher(receiptNumber);
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(1));
        }
        return -1;
    }

    /**
     * Validates the format of a receipt number.
     *
     * @param receiptNumber Receipt number to validate
     * @return true if valid
     */
    public static boolean validateFormat(String receiptNumber) {
        if (receiptNumber == null)
            return false;
        return RECEIPT_PATTERN.matcher(receiptNumber).matches();
    }
}
