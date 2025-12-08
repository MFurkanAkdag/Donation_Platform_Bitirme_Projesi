package com.seffafbagis.api.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

/**
 * Token generation and hashing utility.
 * 
 * Provides static methods for generating:
 * - Secure random tokens (Base64-encoded)
 * - Secure token hashes (SHA-256)
 * - Reference codes with timestamps
 * - Receipt numbers with transaction IDs
 * - Random strings for various purposes
 * 
 * All token generation uses SecureRandom for cryptographic randomness.
 * All hashing uses SHA-256 for consistent security.
 * 
 * @author Furkan
 * @version 1.0
 */
public final class TokenUtils {

    /**
     * Default token length in bytes.
     * 32 bytes = 256 bits, provides 256-bit entropy
     * (44 characters when Base64 encoded).
     */
    private static final int DEFAULT_TOKEN_LENGTH = 32;

    /**
     * URL-safe Base64 encoder/decoder.
     */
    private static final Base64.Encoder URL_SAFE_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_SAFE_DECODER = Base64.getUrlDecoder();

    /**
     * Characters suitable for random string generation.
     * Includes uppercase, lowercase, and digits for good readability.
     */
    private static final String URL_SAFE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";

    /**
     * SHA-256 algorithm name for hashing.
     */
    private static final String SHA_256 = "SHA-256";

    /**
     * Reference code prefix for donation system.
     */
    private static final String REFERENCE_CODE_PREFIX = "SBP";

    /**
     * Receipt code prefix for transaction records.
     */
    private static final String RECEIPT_CODE_PREFIX = "RCPT";

    /**
     * Date format for reference codes: YYYYMMDD.
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Secure random instance.
     */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private TokenUtils() {
        throw new AssertionError("Cannot instantiate TokenUtils");
    }

    /**
     * Generates a cryptographically secure random token.
     * 
     * Uses the default token length (32 bytes = 256 bits).
     * Returns Base64-URL-encoded token (44 characters).
     * 
     * Suitable for:
     * - Email verification tokens
     * - Password reset tokens
     * - API access tokens
     * - Refresh tokens
     * 
     * @return 32-byte random token as Base64-URL string
     */
    public static String generateSecureToken() {
        return generateSecureToken(DEFAULT_TOKEN_LENGTH);
    }

    /**
     * Generates a cryptographically secure random token of specified length.
     * 
     * @param length Token length in bytes
     * @return Random token of specified length as Base64-URL string
     */
    public static String generateSecureToken(int length) {
        byte[] randomBytes = new byte[length];
        SECURE_RANDOM.nextBytes(randomBytes);
        return URL_SAFE_ENCODER.encodeToString(randomBytes);
    }

    /**
     * Generates SHA-256 hash of a token.
     * 
     * Used for secure storage of tokens in database.
     * Tokens should be stored hashed to prevent compromise if database is breached.
     * 
     * @param token Token to hash
     * @return SHA-256 hash as hexadecimal string (64 characters)
     * @throws IllegalArgumentException if hashing fails
     */
    public static String generateTokenHash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            byte[] encodedHash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Generates a reference code for donations.
     * 
     * Format: SBP-YYYYMMDD-XXXXX
     * Example: SBP-20240115-AB3XZ
     * 
     * Includes date and random suffix for uniqueness.
     * Useful for user-visible transaction identification.
     * 
     * @return Reference code as string
     */
    public static String generateReferenceCode() {
        String date = LocalDate.now().format(DATE_FORMATTER);
        String randomSuffix = generateRandomString(5);
        return String.format("%s-%s-%s", REFERENCE_CODE_PREFIX, date, randomSuffix);
    }

    /**
     * Generates a receipt number for transactions.
     * 
     * Format: RCPT-YYYY-NNNNNN
     * Example: RCPT-2024-123456
     * 
     * Includes year and transaction ID for tracking.
     * Useful for receipt generation and audit trails.
     * 
     * @param transactionId Unique transaction identifier
     * @return Receipt number as string
     */
    public static String generateReceiptNumber(long transactionId) {
        int year = LocalDate.now().getYear();
        return String.format("%s-%d-%06d", RECEIPT_CODE_PREFIX, year, transactionId);
    }

    /**
     * Generates a random string of specified length.
     * 
     * Uses URL-safe characters (A-Z, a-z, 0-9, -, _).
     * Suitable for:
     * - Transaction codes
     * - Reference IDs
     * - Verification codes
     * 
     * @param length Length of random string
     * @return Random string of specified length
     */
    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = SECURE_RANDOM.nextInt(URL_SAFE_CHARACTERS.length());
            sb.append(URL_SAFE_CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }

    /**
     * Generates a random UUID.
     * 
     * Useful for:
     * - Entity identifiers
     * - Correlation IDs
     * - Request tracking
     * 
     * @return Random UUID as string
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Converts byte array to hexadecimal string.
     * 
     * Helper method for hash representation.
     * Each byte is converted to 2 hex characters.
     * 
     * @param hash Byte array to convert
     * @return Hexadecimal string representation
     */
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Decodes a Base64-URL-encoded token.
     * 
     * Utility for token verification.
     * 
     * @param encodedToken Base64-URL-encoded token
     * @return Decoded token bytes
     * @throws IllegalArgumentException if token is not properly encoded
     */
    public static byte[] decodeToken(String encodedToken) {
        try {
            return URL_SAFE_DECODER.decode(encodedToken);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid token encoding", e);
        }
    }

    /**
     * Verifies if a token matches its hash.
     * 
     * @param token Original token
     * @param hash Stored hash
     * @return true if token matches hash, false otherwise
     */
    public static boolean verifyTokenHash(String token, String hash) {
        String computedHash = generateTokenHash(token);
        return computedHash.equals(hash);
    }
}
