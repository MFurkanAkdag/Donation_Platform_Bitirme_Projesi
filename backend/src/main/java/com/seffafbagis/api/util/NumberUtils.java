package com.seffafbagis.api.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Utility class for number formatting and currency operations.
 */
public final class NumberUtils {

    private static final Locale TURKISH_LOCALE = Locale.forLanguageTag("tr-TR");
    private static final DecimalFormat CURRENCY_FORMATTER;
    private static final DecimalFormat NUMBER_FORMATTER;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(TURKISH_LOCALE);
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');

        CURRENCY_FORMATTER = new DecimalFormat("#,##0.00", symbols);
        NUMBER_FORMATTER = new DecimalFormat("#,##0", symbols);
    }

    private NumberUtils() {
        throw new AssertionError("Cannot instantiate NumberUtils");
    }

    /**
     * Formats amount as Turkish Lira (e.g., 1.234,56 TL).
     *
     * @param amount Amount to format
     * @return Formatted string
     */
    public static String formatCurrency(BigDecimal amount) {
        if (amount == null)
            return "0,00 TL";
        return CURRENCY_FORMATTER.format(amount) + " TL";
    }

    /**
     * Formats amount with specified currency usage.
     *
     * @param amount       Amount to format
     * @param currencyCode Currency code/symbol
     * @return Formatted string
     */
    public static String formatCurrency(BigDecimal amount, String currencyCode) {
        if (amount == null)
            return "0,00 " + currencyCode;
        return CURRENCY_FORMATTER.format(amount) + " " + currencyCode;
    }

    /**
     * Formats value as percentage.
     *
     * @param value Value (e.g., 0.456)
     * @return Formatted percentage (e.g., 45.6%)
     */
    public static String formatPercentage(double value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US); // Verify if % needs TR locale symbols,
                                                                            // usually . is preferred for technical % or
                                                                            // , for TR. Let's use TR compatible.
        // Actually requirement example: 45.6% -> seems to use dot. Let's use dot for
        // percentage to match example.
        return new DecimalFormat("0.0", symbols).format(value * 100) + "%";
    }

    /**
     * Formats number with thousand separators.
     *
     * @param number Number to format
     * @return Formatted string
     */
    public static String formatNumber(long number) {
        return NUMBER_FORMATTER.format(number);
    }

    /**
     * Formats large numbers seamlessly (e.g., 1.5 Milyon).
     *
     * @param number Number to format
     * @return Formatted string
     */
    public static String formatCompact(long number) {
        if (number >= 1_000_000_000) {
            return String.format(TURKISH_LOCALE, "%.1f Milyar", number / 1_000_000_000.0);
        }
        if (number >= 1_000_000) {
            return String.format(TURKISH_LOCALE, "%.1f Milyon", number / 1_000_000.0);
        }
        if (number >= 1_000) {
            // Requirement says 15 Bin, but standard TR usage often keeps full numbers for
            // thousands unless very rounded.
            // "15 Bin" is requested.
            return String.format(TURKISH_LOCALE, "%.1f Bin", number / 1_000.0).replace(",0", ""); // Clean up .0 if
                                                                                                  // exact
        }
        return String.valueOf(number);
    }

    /**
     * Parses Turkish currency string to BigDecimal.
     *
     * @param text Currency string (e.g., "1.234,56 TL")
     * @return BigDecimal value
     */
    public static BigDecimal parseCurrency(String text) {
        if (text == null || text.isEmpty())
            return BigDecimal.ZERO;

        // Remove currency symbols and non-numeric except , and .
        String clean = text.replace("TL", "")
                .replace("₺", "")
                .trim();

        // Remove . (grouping)
        clean = clean.replace(".", "");

        // Replace , (decimal) with .
        clean = clean.replace(",", ".");

        try {
            return new BigDecimal(clean);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Rounds value to nearest increment.
     *
     * @param value   Value to round
     * @param nearest Increment to round to
     * @return Rounded value
     */
    public static BigDecimal roundToNearest(BigDecimal value, BigDecimal nearest) {
        if (nearest == null || nearest.compareTo(BigDecimal.ZERO) == 0)
            return value;

        BigDecimal divided = value.divide(nearest, 0, RoundingMode.HALF_UP);
        return divided.multiply(nearest);
    }

    /**
     * Calculates percentage of part in whole.
     *
     * @param part  Part value
     * @param whole Whole value
     * @return Percentage value (0-100)
     */
    public static BigDecimal calculatePercentage(BigDecimal part, BigDecimal whole) {
        if (whole == null || whole.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;

        return part.divide(whole, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
