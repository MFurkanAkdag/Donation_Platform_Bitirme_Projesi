package com.seffafbagis.api.service.encryption;

import com.seffafbagis.api.exception.EncryptionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Encryption service for KVKK-compliant data protection.
 * 
 * Provides AES-256-GCM encryption and decryption for sensitive user data
 * such as TC Kimlik, phone numbers, and addresses stored in UserSensitiveData.
 * 
 * Key features:
 * - AES-256-GCM (authenticated encryption with associated data)
 * - Unique IV (Initialization Vector) for each encryption
 * - Automatic authentication tag verification
 * - Secure random number generation
 * - Null-safe operations for optional fields
 * 
 * @author Furkan
 * @version 1.0
 */
@Service
public class EncryptionService {

    /**
     * AES algorithm name.
     */
    private static final String ALGORITHM = "AES";

    /**
     * AES/GCM/NoPadding transformation.
     * GCM mode provides both encryption and authentication.
     */
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    /**
     * GCM Initialization Vector length in bytes.
     * 12 bytes (96 bits) is recommended for GCM mode.
     * This provides optimal balance of security and performance.
     */
    private static final int GCM_IV_LENGTH = 12;

    /**
     * GCM authentication tag length in bits.
     * 128 bits (16 bytes) provides strong authentication.
     */
    private static final int GCM_TAG_LENGTH = 128;

    /**
     * Expected length of AES-256 key in characters.
     * 32 characters = 256 bits for AES-256.
     */
    private static final int AES_256_KEY_LENGTH = 32;

    /**
     * Encryption secret key loaded from environment.
     * Must be exactly 32 characters long for AES-256.
     */
    @Value("${app.encryption.secret-key}")
    private String secretKey;

    /**
     * Secret key specification for cipher operations.
     * Initialized during PostConstruct.
     */
    private SecretKey secretKeySpec;

    /**
     * Secure random number generator.
     * Used for IV generation to ensure cryptographic randomness.
     */
    private SecureRandom secureRandom;

    /**
     * Initialize encryption components after bean construction.
     * 
     * Validates the secret key and prepares cipher components.
     * This method is called automatically after dependency injection.
     * 
     * @throws EncryptionException if secret key is invalid
     */
    @jakarta.annotation.PostConstruct
    public void init() {
        try {
            // Validate secret key length
            if (secretKey == null || secretKey.length() != AES_256_KEY_LENGTH) {
                String message = "Invalid encryption secret key. Must be exactly 32 characters for AES-256.";
                throw new EncryptionException(message);
            }

            // Convert string key to SecretKeySpec
            byte[] decodedKey = secretKey.getBytes(StandardCharsets.UTF_8);
            this.secretKeySpec = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);

            // Initialize secure random
            this.secureRandom = SecureRandom.getInstanceStrong();

        } catch (NoSuchAlgorithmException exception) {
            String message = "Failed to initialize SecureRandom for encryption";
            throw new EncryptionException(message, exception);
        }
    }

    /**
     * Encrypts plaintext using AES-256-GCM.
     * 
     * For each encryption:
     * 1. Generates a unique random IV
     * 2. Encrypts the plaintext
     * 3. Appends IV to ciphertext for storage
     * 
     * @param plainText Text to encrypt (can be null)
     * @return Encrypted bytes with embedded IV, or null if plainText is null
     * @throws EncryptionException if encryption fails
     */
    public byte[] encrypt(String plainText) {
        // Handle null input
        if (plainText == null || plainText.isEmpty()) {
            return null;
        }

        try {
            // Step 1: Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            // Step 2: Initialize cipher
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmSpec);

            // Step 3: Encrypt plaintext
            byte[] plainBytes = plainText.getBytes(StandardCharsets.UTF_8);
            byte[] encryptedBytes = cipher.doFinal(plainBytes);

            // Step 4: Combine IV and ciphertext
            // Format: [IV (12 bytes)] [Ciphertext + Auth Tag]
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encryptedBytes.length);
            byteBuffer.put(iv);
            byteBuffer.put(encryptedBytes);

            return byteBuffer.array();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 InvalidAlgorithmParameterException exception) {
            throw new EncryptionException("Failed to initialize encryption", exception);
        } catch (IllegalBlockSizeException | BadPaddingException exception) {
            throw new EncryptionException("Failed to encrypt data", exception);
        }
    }

    /**
     * Decrypts encrypted data using AES-256-GCM.
     * 
     * Automatically verifies authentication tag for tamper detection.
     * If GCM authentication fails, throws exception indicating tampering.
     * 
     * @param encryptedData Encrypted bytes with embedded IV (can be null)
     * @return Decrypted plaintext, or null if encryptedData is null
     * @throws EncryptionException if decryption fails or data is tampered
     */
    public String decrypt(byte[] encryptedData) {
        // Handle null input
        if (encryptedData == null || encryptedData.length < GCM_IV_LENGTH + 1) {
            return null;
        }

        try {
            // Step 1: Extract IV and ciphertext
            ByteBuffer byteBuffer = ByteBuffer.wrap(encryptedData);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            
            byte[] ciphertext = new byte[byteBuffer.remaining()];
            byteBuffer.get(ciphertext);

            // Step 2: Initialize cipher
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmSpec);

            // Step 3: Decrypt and verify
            byte[] plainBytes = cipher.doFinal(ciphertext);

            // Step 4: Return plaintext
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 InvalidAlgorithmParameterException exception) {
            throw new EncryptionException("Failed to initialize decryption", exception);
        } catch (IllegalBlockSizeException | BadPaddingException exception) {
            // BadPaddingException includes auth tag verification failure
            throw new EncryptionException("Failed to decrypt data - possible tampering detected", exception);
        }
    }

    /**
     * Encrypts a string only if it is not null.
     * 
     * Convenience method for optional fields.
     * 
     * @param plainText Text to encrypt (nullable)
     * @return Encrypted bytes, or null if plainText is null
     */
    public byte[] encryptIfNotNull(String plainText) {
        if (plainText == null) {
            return null;
        }
        return encrypt(plainText);
    }

    /**
     * Decrypts bytes only if they are not null.
     * 
     * Convenience method for optional fields.
     * 
     * @param encryptedData Encrypted bytes (nullable)
     * @return Decrypted plaintext, or null if encryptedData is null
     */
    public String decryptIfNotNull(byte[] encryptedData) {
        if (encryptedData == null) {
            return null;
        }
        return decrypt(encryptedData);
    }

    /**
     * Checks if data appears to be encrypted.
     * 
     * This is a heuristic check, not a guarantee.
     * Verifies minimum length for IV + ciphertext.
     * 
     * @param data Data to check
     * @return true if data appears to be encrypted
     */
    public boolean isEncrypted(byte[] data) {
        // Minimum: IV (12 bytes) + encrypted data (1+ bytes) = 13+ bytes
        return data != null && data.length >= GCM_IV_LENGTH + 1;
    }
}
