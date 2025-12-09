package com.seffafbagis.api.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Utility class for generating URL-friendly slugs from text.
 * Handles Turkish character conversion and ensures uniqueness.
 */
public final class SlugGenerator {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern EDGES = Pattern.compile("(^-|-$)");
    private static final Pattern MULTIPLE_HYPHENS = Pattern.compile("-{2,}");
    private static final Locale TURKISH_LOCALE = Locale.forLanguageTag("tr-TR");

    private SlugGenerator() {
        throw new AssertionError("Cannot instantiate SlugGenerator");
    }

    /**
     * Generates a URL-friendly slug from the given text.
     * Default max length is 100 characters.
     *
     * @param text Text to convert to slug
     * @return Generated slug
     */
    public static String generateSlug(String text) {
        return generateSlug(text, 100);
    }

    /**
     * Generates a URL-friendly slug from the given text with a maximum length.
     *
     * @param text      Text to convert to slug
     * @param maxLength Maximum length of the slug
     * @return Generated slug
     */
    public static String generateSlug(String text, int maxLength) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        // 1. Lowercase with Turkish locale
        String input = text.toLowerCase(TURKISH_LOCALE);

        // 2. Replace Turkish characters
        input = toAscii(input);

        // 3. Replace spaces with hyphens
        String noWhitespace = WHITESPACE.matcher(input).replaceAll("-");

        // 4. Remove non-latin characters
        String normalized = Normalizer.normalize(noWhitespace, Normalizer.Form.NFD);
        String slug = NON_LATIN.matcher(normalized).replaceAll("");

        // 5. Replace multiple hyphens with single hyphen
        slug = MULTIPLE_HYPHENS.matcher(slug).replaceAll("-");

        // 6. Remove leading/trailing hyphens
        slug = EDGES.matcher(slug).replaceAll("");

        // 7. Truncate to max length
        if (slug.length() > maxLength) {
            slug = slug.substring(0, maxLength);
            // Try to not cut words
            int lastHyphen = slug.lastIndexOf('-');
            if (lastHyphen > maxLength / 2) { // Only cut at hyphen if it doesn't shorten too much
                slug = slug.substring(0, lastHyphen);
            }
        }

        return slug;
    }

    /**
     * Generates a unique slug by appending a number if the base slug already
     * exists.
     *
     * @param text        Text to convert to slug
     * @param existsCheck Function to check if a slug already exists
     * @return Unique slug
     */
    public static String generateUniqueSlug(String text, Function<String, Boolean> existsCheck) {
        String baseSlug = generateSlug(text);
        if (baseSlug.isEmpty()) {
            return "";
        }

        if (!Boolean.TRUE.equals(existsCheck.apply(baseSlug))) {
            return baseSlug;
        }

        // If exists, append number
        int counter = 2;
        while (true) {
            String candidate = baseSlug + "-" + counter;
            if (!Boolean.TRUE.equals(existsCheck.apply(candidate))) {
                return candidate;
            }
            counter++;
        }
    }

    /**
     * Converts Turkish characters to their ASCII equivalents.
     *
     * @param text Text to convert
     * @return Text with Turkish characters replaced
     */
    public static String toAscii(String text) {
        if (text == null) {
            return null;
        }
        return text.replace("ş", "s")
                .replace("Ş", "s")
                .replace("ğ", "g")
                .replace("Ğ", "g")
                .replace("ı", "i")
                .replace("İ", "i")
                .replace("ö", "o")
                .replace("Ö", "o")
                .replace("ü", "u")
                .replace("Ü", "u")
                .replace("ç", "c")
                .replace("Ç", "c");
    }

    /**
     * Normalizes text by trimming, lowercasing (Turkish aware), and removing extra
     * spaces.
     *
     * @param text Text to normalize
     * @return Normalized text
     */
    public static String normalize(String text) {
        if (text == null) {
            return null;
        }
        return text.trim().toLowerCase(TURKISH_LOCALE).replaceAll("\\s+", " ");
    }
}
