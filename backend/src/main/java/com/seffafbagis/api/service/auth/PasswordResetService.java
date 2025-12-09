package com.seffafbagis.api.service.auth;

import com.seffafbagis.api.dto.request.auth.ResetPasswordRequest;
import com.seffafbagis.api.entity.auth.PasswordResetToken;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.UserStatus;
import com.seffafbagis.api.exception.BadRequestException;
import com.seffafbagis.api.repository.PasswordResetTokenRepository;
import com.seffafbagis.api.repository.RefreshTokenRepository;
import com.seffafbagis.api.repository.UserRepository;
import com.seffafbagis.api.service.notification.EmailService;
import com.seffafbagis.api.validator.PasswordValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;

/**
 * Password Reset Service.
 * 
 * Handles password reset flow including:
 * - Initiating password reset (forgot password)
 * - Resetting password with token
 * - Cleanup of expired tokens
 * 
 * Security Features:
 * - Token expiry: 1 hour
 * - Tokens are single-use (marked as used after use)
 * - Token hashing before storage
 * - Generic error messages (no information leakage)
 * 
 * @author Furkan
 * @version 1.0
 */
@Service
public class PasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);

    // Token expiry: 1 hour (3600 seconds)
    private static final long TOKEN_EXPIRY_SECONDS = 3600;

    // Token length: 32 bytes = 256 bits
    private static final int TOKEN_LENGTH = 32;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /**
     * Constructor injection.
     */
    @Autowired
    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * Initiates password reset by creating a token and sending reset email.
     * 
     * Always returns success regardless of whether email exists.
     * This prevents user enumeration attacks.
     * 
     * Flow:
     * 1. Find user by email (case-insensitive)
     * 2. Check if user is active or pending verification
     * 3. Invalidate existing unused tokens
     * 4. Generate new secure token
     * 5. Hash and store token in database
     * 6. Send reset email with plain token
     * 
     * @param email User's email address
     */
    @Transactional
    public void initiatePasswordReset(String email) {
        // Normalize email
        String normalizedEmail = email.toLowerCase().trim();
        logger.info("Password reset initiated for email: {}", normalizedEmail);

        // Step 1: Find user by email
        Optional<User> userOptional = userRepository.findByEmail(normalizedEmail);

        // Security: Return success regardless of whether user exists
        // This prevents attackers from enumerating registered emails
        if (userOptional.isEmpty()) {
            logger.debug("Password reset requested for non-existent email: {}", normalizedEmail);
            return;
        }

        User user = userOptional.get();

        // Step 2: Check if user can reset password
        // User must be ACTIVE or PENDING_VERIFICATION
        // SUSPENDED users cannot reset password
        if (user.getStatus() == UserStatus.SUSPENDED) {
            logger.warn("Password reset attempted for suspended user: {}", normalizedEmail);
            // Still return success to prevent enumeration
            return;
        }

        // Step 3: Invalidate existing unused reset tokens
        // This ensures only the latest token is valid
        passwordResetTokenRepository.markAllUnusedTokensAsUsed(user.getId(), Instant.now());

        // Step 4: Generate new secure token
        String plainToken = generateSecureToken();

        // Step 5: Hash token before storing
        String tokenHash = hashToken(plainToken);

        // Step 6: Create and store PasswordResetToken entity
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setTokenHash(tokenHash);
        resetToken.setExpiresAt(Instant.now().plus(TOKEN_EXPIRY_SECONDS, ChronoUnit.SECONDS));

        passwordResetTokenRepository.save(resetToken);
        logger.debug("Password reset token created for user: {}", user.getId());

        // Step 7: Send reset email with plain token
        try {
            emailService.sendPasswordResetEmail(user.getEmail(), plainToken, user);
            logger.info("Password reset email sent successfully to: {}", normalizedEmail);
        } catch (Exception ex) {
            logger.error("Failed to send password reset email to: {}", normalizedEmail, ex);
            // Log the error but don't throw - prevent email service failures from blocking
            // auth flow
        }
    }

    /**
     * Resets password using a valid token.
     * 
     * Flow:
     * 1. Validate passwords match
     * 2. Validate password strength
     * 3. Hash and find token in database
     * 4. Validate token (exists, not expired, not used)
     * 5. Get user from token
     * 6. Update password
     * 7. Mark token as used
     * 8. Invalidate all refresh tokens (logout all sessions)
     * 9. Send confirmation email
     * 
     * @param request Password reset request with token and new password
     * @throws BadRequestException if token is invalid, expired, or already used
     * @throws BadRequestException if passwords don't match or are weak
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        logger.info("Password reset attempt");

        // Step 1: Validate passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            logger.warn("Password reset failed - passwords do not match");
            throw new BadRequestException("New passwords do not match");
        }

        // Step 2: Validate password strength
        PasswordValidator validator = new PasswordValidator(request.getNewPassword());
        if (!validator.isValid()) {
            logger.warn("Password reset failed - password does not meet strength requirements");
            throw new BadRequestException("Password does not meet strength requirements");
        }

        // Step 3: Hash and find token
        String tokenHash = hashToken(request.getToken());
        Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByTokenHash(tokenHash);

        // Step 4: Validate token
        if (tokenOptional.isEmpty()) {
            logger.warn("Password reset failed - invalid token");
            throw new BadRequestException("Invalid or expired password reset token");
        }

        PasswordResetToken resetToken = tokenOptional.get();

        // Check if token is expired
        if (resetToken.getExpiresAt() != null && Instant.now().isAfter(resetToken.getExpiresAt())) {
            logger.warn("Password reset failed - token expired");
            throw new BadRequestException("Password reset token has expired");
        }

        // Check if token was already used
        if (resetToken.getUsedAt() != null) {
            logger.warn("Password reset failed - token already used");
            throw new BadRequestException("This password reset token has already been used");
        }

        // Step 5: Get user from token
        User user = resetToken.getUser();

        // Verify user is not suspended
        if (user.getStatus() == UserStatus.SUSPENDED) {
            logger.warn("Password reset failed - user is suspended: {}", user.getEmail());
            throw new BadRequestException("Account is suspended");
        }

        // Step 6: Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(Instant.now());
        userRepository.save(user);
        logger.debug("Password updated for user: {}", user.getId());

        // Step 7: Mark token as used (single-use token)
        resetToken.setUsedAt(Instant.now());
        passwordResetTokenRepository.save(resetToken);

        // Step 8: Invalidate all refresh tokens (logout from all devices)
        // This forces user to login again on all devices for security
        refreshTokenRepository.deleteByUserId(user.getId());
        logger.info("All refresh tokens revoked for user: {} after password reset", user.getId());

        // Step 9: Send confirmation email
        try {
            emailService.sendPasswordChangedEmail(user.getEmail(), user);
            logger.info("Password changed confirmation email sent to: {}", user.getEmail());
        } catch (Exception ex) {
            logger.error("Failed to send password changed confirmation email to: {}", user.getEmail(), ex);
            // Don't throw - password was already reset
        }

        logger.info("Password reset completed successfully for user: {}", user.getEmail());
    }

    /**
     * Scheduled job to clean up expired password reset tokens.
     * 
     * Runs daily to remove expired tokens that are no longer needed.
     * This prevents the database from growing indefinitely.
     * 
     * Scheduled to run once per day at 2:00 AM.
     */
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2:00 AM
    @Transactional
    public void cleanupExpiredTokens() {
        logger.info("Starting cleanup of expired password reset tokens");

        Instant now = Instant.now();
        long deletedCount = passwordResetTokenRepository.deleteExpiredTokens(now);

        logger.info("Deleted {} expired password reset tokens", deletedCount);
    }

    /**
     * Generates a cryptographically secure random token.
     * 
     * Token is 32 bytes (256 bits) encoded in Base64 URL-safe format.
     * This makes it suitable for use in URLs.
     * 
     * @return URL-safe random token string
     */
    private String generateSecureToken() {
        byte[] tokenBytes = new byte[TOKEN_LENGTH];
        new SecureRandom().nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    /**
     * Hashes a token using SHA-256.
     * 
     * Tokens are hashed before storage so even if database is compromised,
     * tokens cannot be directly used. This is similar to password hashing.
     * 
     * @param token Plain token to hash
     * @return Hex-encoded SHA-256 hash of token
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException ex) {
            logger.error("SHA-256 algorithm not found", ex);
            throw new RuntimeException("Token hashing failed", ex);
        }
    }
}
