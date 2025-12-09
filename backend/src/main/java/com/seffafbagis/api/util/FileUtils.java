package com.seffafbagis.api.util;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class for file handling operations.
 */
public final class FileUtils {

    private static final Map<String, String> MIME_TYPES = new HashMap<>();

    static {
        MIME_TYPES.put("pdf", "application/pdf");
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("doc", "application/msword");
        MIME_TYPES.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        MIME_TYPES.put("xls", "application/vnd.ms-excel");
        MIME_TYPES.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        MIME_TYPES.put("txt", "text/plain");
        MIME_TYPES.put("csv", "text/csv");
    }

    private FileUtils() {
        throw new AssertionError("Cannot instantiate FileUtils");
    }

    /**
     * Extracts extension from filename.
     *
     * @param filename Filename
     * @return Extension (lowercase) or empty string
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    /**
     * Gets filename without extension.
     *
     * @param filename Filename
     * @return Filename without extension
     */
    public static String getFileNameWithoutExtension(String filename) {
        if (filename == null)
            return null;
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1) {
            return filename;
        }
        return filename.substring(0, dotIndex);
    }

    /**
     * Generates a unique filename while preserving the extension.
     *
     * @param originalFilename Original filename
     * @return Unique filename with UUID
     */
    public static String generateUniqueFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return extension.isEmpty() ? uuid : uuid + "." + extension;
    }

    /**
     * Sanitizes filename by removing unsafe characters and replacing spaces.
     *
     * @param filename Input filename
     * @return Sanitized filename
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null)
            return "";

        // 1. Convert Turkish characters using SlugGenerator logic (reusing
        // implementation details or similar logic)
        String sanitized = SlugGenerator.toAscii(filename);

        // 2. Replace spaces with underscores
        sanitized = sanitized.replaceAll("\\s+", "_");

        // 3. Keep only allowed characters
        sanitized = sanitized.replaceAll("[^a-zA-Z0-9._-]", "");

        return sanitized;
    }

    /**
     * Checks if file extension is allowed.
     *
     * @param filename          Filename to check
     * @param allowedExtensions List of allowed extensions (without dot)
     * @return true if allowed
     */
    public static boolean isAllowedExtension(String filename, List<String> allowedExtensions) {
        if (allowedExtensions == null || allowedExtensions.isEmpty()) {
            return true;
        }
        String ext = getFileExtension(filename);
        return allowedExtensions.contains(ext);
    }

    /**
     * Checks if MIME type is allowed.
     *
     * @param mimeType         MIME type to check
     * @param allowedMimeTypes List of allowed MIME types
     * @return true if allowed
     */
    public static boolean isAllowedMimeType(String mimeType, List<String> allowedMimeTypes) {
        if (allowedMimeTypes == null || allowedMimeTypes.isEmpty()) {
            return true;
        }
        return allowedMimeTypes.contains(mimeType);
    }

    /**
     * Formats file size in readable format (B, KB, MB, GB).
     *
     * @param sizeInBytes Size in bytes
     * @return Formatted string
     */
    public static String formatFileSize(long sizeInBytes) {
        if (sizeInBytes <= 0)
            return "0 B";

        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(sizeInBytes) / Math.log10(1024));

        if (digitGroups >= units.length) {
            digitGroups = units.length - 1;
        }

        // Use standard Java formatting if specific Turkish number formatting isn't
        // strictly required here,
        // but let's stick to using DecimalFormat with English locale default to avoid
        // comma/dot mixup in technical displays,
        // OR better yet, use consistent formatting. Requirement says "1.00 KB" (dot),
        // so English/Default locale is fine.
        return new DecimalFormat("#,##0.00").format(sizeInBytes / Math.pow(1024, digitGroups)) + " "
                + units[digitGroups];
    }

    /**
     * Guesses MIME type from filename extension.
     *
     * @param filename Filename
     * @return MIME type or application/octet-stream
     */
    public static String getMimeType(String filename) {
        String ext = getFileExtension(filename);
        return MIME_TYPES.getOrDefault(ext, "application/octet-stream");
    }
}
