package com.seffafbagis.api.service.auth;

import com.seffafbagis.api.entity.auth.EmailVerificationToken;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.UserStatus;
import com.seffafbagis.api.exception.BadRequestException;
import com.seffafbagis.api.repository.EmailVerificationTokenRepository;
import com.seffafbagis.api.repository.UserRepository;
import com.seffafbagis.api.service.notification.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;

/**
 * Email Verification Service.
 * 
 * Handles email verification flow including:
 * - Creating verification tokens during registration
 * - Verifying email addresses
 * - Resending verification emails with rate limiting
 * 
 * Security Features:
 * - Token expiry: 24 hours
 * - Tokens are single-use (marked as verified after use)
 * - Token hashing before storage
 * - Rate limiting: max 3 resend requests per hour per email
 * - Generic error messages (no information leakage)
 * 
 * @author Furkan
 * @version 1.0
 */
@Service
public class EmailVerificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailVerificationService.class);

    // Token expiry: 24 hours (86400 seconds)
    private static final long TOKEN_EXPIRY_SECONDS = 86400;

    // Token length: 32 bytes = 256 bits
    private static final int TOKEN_LENGTH = 32;

    // Rate limiting: max 3 verification emails per hour
    private static final int MAX_RESEND_ATTEMPTS = 3;
    private static final long RESEND_RATE_LIMIT_SECONDS = 3600; // 1 hour

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final EmailService emailService;

    /**
     * Constructor injection.
     */
    @Autowired
    public EmailVerificationService(
            UserRepository userRepository,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.emailService = emailService;
    }

    /**
     * Verifies email address using token.
     * 
     * Called when user clicks verification link in email.
     * 
     * Flow:
     * 1. Hash and find token
     * 2. Validate token (exists, not expired, not already verified)
     * 3. Get user from token
     * 4. Update user: emailVerified=true, status=ACTIVE
     * 5. Mark token as verified
     * 6. Send welcome email
     * 
     * @param token Email verification token from email link
     * @throws BadRequestException if token is invalid, expired, or already used
     */
    @Transactional
    public void verifyEmail(String token) {
        logger.info("Email verification attempt");

        // Step 1: Hash and find token
        String tokenHash = hashToken(token);
        Optional<EmailVerificationToken> tokenOptional = emailVerificationTokenRepository.findByTokenHash(tokenHash);

        // Step 2: Validate token
        if (tokenOptional.isEmpty()) {
            logger.warn("Email verification failed - invalid token");
            throw new BadRequestException("Invalid verification token");
        }

        EmailVerificationToken verificationToken = tokenOptional.get();

        // Check if token is expired
        if (LocalDateTime.now().isAfter(verificationToken.getExpiresAt())) {
            logger.warn("Email verification failed - token expired");
            throw new BadRequestException("Verification link has expired");
        }

        // Check if token was already verified (idempotent - return success)
        if (verificationToken.getVerifiedAt() != null) {
            logger.debug("Email already verified - idempotent operation");
            // Return success - this is idempotent operation
            return;
        }

        // Step 3: Get user from token
        User user = verificationToken.getUser();

        // Step 4: Update user
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(Instant.now());

        // If user is still pending verification, mark as active
        if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
            user.setStatus(UserStatus.ACTIVE);
            logger.info("User status updated to ACTIVE: {}", user.getId());
        }

        userRepository.save(user);
        logger.debug("Email verified for user: {}", user.getId());

        // Step 5: Mark token as verified
        verificationToken.setVerifiedAt(LocalDateTime.now());
        emailVerificationTokenRepository.save(verificationToken);

        // Step 6: Send welcome email
        try {
            emailService.sendWelcomeEmail(user.getEmail(), user, user.getProfile());
            logger.info("Welcome email sent to: {}", user.getEmail());
        } catch (Exception ex) {
            logger.error("Failed to send welcome email to: {}", user.getEmail(), ex);
            // Don't throw - email was already verified
        }

        logger.info("Email verification completed successfully for user: {}", user.getEmail());
    }

    /**
     * Resends verification email.
     * 
     * Rate limited to prevent abuse.
     * Returns success regardless of whether user exists (prevents enumeration).
     * 
     * Flow:
     * 1. Find user by email
     * 2. Check if already verified
     * 3. Check rate limit
     * 4. Invalidate existing tokens
     * 5. Create new token
     * 6. Send verification email
     * 
     * @param email User's email address
     * @throws BadRequestException if email is already verified
     */
    @Transactional
    public void resendVerificationEmail(String email) {
        // Normalize email
        String normalizedEmail = email.toLowerCase().trim();
        logger.info("Resend verification email requested for: {}", normalizedEmail);

        // Step 1: Find user by email
        Optional<User> userOptional = userRepository.findByEmail(normalizedEmail);

        // Security: Return success regardless of whether user exists
        if (userOptional.isEmpty()) {
            logger.debug("Resend verification requested for non-existent email: {}", normalizedEmail);
            return;
        }

        User user = userOptional.get();

        // Step 2: Check if already verified
        if (user.isVerified()) {
            logger.warn("Resend verification requested for already verified email: {}", normalizedEmail);
            throw new BadRequestException("Email is already verified");
        }

        // Step 3: Check rate limit
        long recentTokenCount = emailVerificationTokenRepository.countRecentTokens(
                user.getId(),
                LocalDateTime.now().minus(RESEND_RATE_LIMIT_SECONDS, ChronoUnit.SECONDS));

        if (recentTokenCount >= MAX_RESEND_ATTEMPTS) {
            logger.warn("Resend verification rate limit exceeded for user: {}", user.getId());
            throw new BadRequestException("Too many resend attempts. Please try again later");
        }

        // Step 4: Invalidate existing unused tokens
        emailVerificationTokenRepository.markAllUnverifiedTokensAsVerified(user.getId(), LocalDateTime.now());

        // Step 5: Create new token
        String plainToken = generateSecureToken();
        String tokenHash = hashToken(plainToken);

        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setUser(user);
        verificationToken.setTokenHash(tokenHash);
        verificationToken.setExpiresAt(LocalDateTime.now().plus(TOKEN_EXPIRY_SECONDS, ChronoUnit.SECONDS));

        emailVerificationTokenRepository.save(verificationToken);
        logger.debug("New verification token created for user: {}", user.getId());

        // Step 6: Send verification email
        try {
            emailService.sendVerificationEmail(user.getEmail(), plainToken, user);
            logger.info("Verification email resent to: {}", normalizedEmail);
        } catch (Exception ex) {
            logger.error("Failed to resend verification email to: {}", normalizedEmail, ex);
            // Log error but don't throw
        }
    }

    /**
     * Creates verification token for a new user (called during registration).
     * 
     * This is a helper method used by AuthService during user registration.
     * 
     * Flow:
     * 1. Generate secure token
     * 2. Hash token
     * 3. Create and store EmailVerificationToken entity
     * 4. Return plain token (for email)
     * 
     * @param user User entity
     * @return Plain token (not hashed) for use in email link
     */
    @Transactional
    public String createVerificationToken(User user) {
        logger.debug("Creating verification token for user: {}", user.getId());

        // Step 1: Generate secure token
        String plainToken = generateSecureToken();

        // Step 2: Hash token before storing
        String tokenHash = hashToken(plainToken);

        // Step 3: Create and store EmailVerificationToken
        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setUser(user);
        verificationToken.setTokenHash(tokenHash);
        verificationToken.setExpiresAt(LocalDateTime.now().plus(TOKEN_EXPIRY_SECONDS, ChronoUnit.SECONDS));

        emailVerificationTokenRepository.save(verificationToken);
        logger.debug("Verification token stored for user: {}", user.getId());

        // Step 4: Return plain token (for email)
        return plainToken;
    }

    /**
     * Generates a cryptographically secure random token.
     * 
     * Token is 32 bytes (256 bits) encoded in Base64 URL-safe format.
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
     * Tokens are hashed before storage for security.
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
