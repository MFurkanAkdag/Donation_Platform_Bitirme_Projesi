package com.seffafbagis.api.util;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utility class for string manipulation.
 */
public final class StringUtils {

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final String NUMERIC = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()_+-=[]{}|;:,.<>?";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern URL_PATTERN = Pattern.compile("^(https?|ftp)://[^\\s/$.?#].[^\\s]*$");
    private static final Pattern HTML_TAGS = Pattern.compile("<[^>]*>");
    private static final Locale TURKISH_LOCALE = Locale.forLanguageTag("tr-TR");

    private StringUtils() {
        throw new AssertionError("Cannot instantiate StringUtils");
    }

    /**
     * Truncates text to max length.
     *
     * @param text      Text to truncate
     * @param maxLength Max length
     * @return Truncated text with "..." appended
     */
    public static String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Truncates text at word boundary.
     *
     * @param text      Text to truncate
     * @param maxLength Max length
     * @return Truncated text
     */
    public static String truncateAtWord(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }

        int end = text.lastIndexOf(' ', maxLength - 3);
        if (end == -1) {
            return truncate(text, maxLength);
        }

        return text.substring(0, end) + "...";
    }

    /**
     * Masks email address.
     * Example: fu***n@example.com
     *
     * @param email Email to mask
     * @return Masked email
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }

        String[] parts = email.split("@");
        String local = parts[0];
        String domain = parts[1];

        if (local.length() <= 3) {
            return local.charAt(0) + "***@" + domain;
        }

        return local.substring(0, 2) + "***" + local.charAt(local.length() - 1) + "@" + domain;
    }

    /**
     * Masks phone number.
     * Example: +90 532 *** ** 67
     *
     * @param phone Phone number
     * @return Masked phone number
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 10) {
            return phone;
        }

        // This is a simple implementation, adjusting to +90 532 ... pattern requires
        // parsing
        // Assuming input like +905321234567

        int len = phone.length();
        String start = phone.substring(0, len - 7); // +90532
        String end = phone.substring(len - 2); // 67

        // Simple generic mask: +90532*****67
        // Requirements ask for spaces: +90 532 *** ** 67
        // Let's try to format if it looks like a standard Turkish mobile

        if (phone.length() == 13 && phone.startsWith("+90")) {
            return phone.substring(0, 3) + " " + phone.substring(3, 6) + " *** ** " + phone.substring(11);
        }

        return start + "*****" + end;
    }

    /**
     * Converts text to title case (Turkish aware).
     *
     * @param text Text to convert
     * @return Title cased text
     */
    public static String toTitleCase(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String[] words = text.split("\\s+");
        StringBuilder sb = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(word.substring(0, 1).toUpperCase(TURKISH_LOCALE));
                sb.append(word.substring(1).toLowerCase(TURKISH_LOCALE));
                sb.append(" ");
            }
        }

        return sb.toString().trim();
    }

    /**
     * Removes HTML tags from string.
     *
     * @param html HTML string
     * @return Plain text
     */
    public static String removeHtmlTags(String html) {
        if (html == null) {
            return null;
        }
        return HTML_TAGS.matcher(html).replaceAll("");
    }

    /**
     * Validates email format.
     *
     * @param email Email to validate
     * @return true if valid
     */
    public static boolean isValidEmail(String email) {
        if (email == null)
            return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validates URL format.
     *
     * @param url URL to validate
     * @return true if valid
     */
    public static boolean isValidUrl(String url) {
        if (url == null)
            return false;
        return URL_PATTERN.matcher(url).matches();
    }

    /**
     * Generates a random string.
     *
     * @param length         Length of string
     * @param includeNumbers Include numbers
     * @param includeSpecial Include special characters
     * @return Random string
     */
    public static String generateRandomString(int length, boolean includeNumbers, boolean includeSpecial) {
        StringBuilder chars = new StringBuilder(ALPHANUMERIC);
        if (includeNumbers) {
            chars.append(NUMERIC);
        }
        if (includeSpecial) {
            chars.append(SPECIAL);
        }

        StringBuilder sb = new StringBuilder(length);
        String charSet = chars.toString();
        for (int i = 0; i < length; i++) {
            sb.append(charSet.charAt(RANDOM.nextInt(charSet.length())));
        }
        return sb.toString();
    }
}
