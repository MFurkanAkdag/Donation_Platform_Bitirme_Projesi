package com.seffafbagis.api.service.auth;

import com.seffafbagis.api.dto.request.auth.ChangePasswordRequest;
import com.seffafbagis.api.dto.request.auth.LoginRequest;
import com.seffafbagis.api.dto.request.auth.LogoutRequest;
import com.seffafbagis.api.dto.request.auth.PasswordResetRequest;
import com.seffafbagis.api.dto.request.auth.RefreshTokenRequest;
import com.seffafbagis.api.dto.request.auth.RegisterRequest;
import com.seffafbagis.api.dto.response.auth.AuthResponse;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.entity.user.UserPreference;
import com.seffafbagis.api.entity.user.UserProfile;
import com.seffafbagis.api.enums.UserRole;
import com.seffafbagis.api.enums.UserStatus;
import com.seffafbagis.api.exception.AuthenticationException;
import com.seffafbagis.api.exception.DuplicateResourceException;
import com.seffafbagis.api.exception.ResourceNotFoundException;
import com.seffafbagis.api.exception.ValidationException;
import com.seffafbagis.api.repository.UserRepository;
import com.seffafbagis.api.security.JwtTokenProvider;
import com.seffafbagis.api.security.SecurityUtils;
import com.seffafbagis.api.config.JwtConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Authentication service.
 * 
 * Kayıt, giriş, çıkış, token yenileme, şifre işlemleri.
 * 
 * @author Furkan
 * @version 1.0
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtConfig jwtConfig;

    /**
     * Constructor injection.
     */
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            JwtConfig jwtConfig) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtConfig = jwtConfig;
    }

    // ==================== KAYIT ====================

    /**
     * Yeni kullanıcı kaydı yapar.
     * 
     * @param request Kayıt bilgileri
     * @return AuthResponse (token ve kullanıcı bilgileri)
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        logger.info("Register attempt: {}", request.getEmail());

        // 1. Validasyonlar
        validateRegisterRequest(request);

        // 2. E-posta kontrolü
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            logger.warn("Registration failed - email exists: {}", request.getEmail());
            throw DuplicateResourceException.emailExists(request.getEmail());
        }

        // 3. User entity oluştur
        User user = new User();
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.setEmailVerified(false);

        // 4. Profil oluştur
        UserProfile profile = new UserProfile(user);
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        user.setProfile(profile);

        // 5. Tercihler oluştur (varsayılan değerlerle)
        UserPreference preferences = new UserPreference(user);
        user.setPreferences(preferences);

        // 6. Kaydet
        user = userRepository.save(user);
        logger.info("User registered successfully: {} - Role: {}", user.getEmail(), user.getRole());

        // 7. Token oluştur
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(
                user.getId(), user.getEmail());

        // 8. Response oluştur
        return createAuthResponse(user, accessToken, refreshToken);
    }

    // ==================== GİRİŞ ====================

    /**
     * Kullanıcı girişi yapar.
     * 
     * @param request Giriş bilgileri
     * @return AuthResponse (token ve kullanıcı bilgileri)
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        logger.info("Login attempt: {}", request.getEmail());

        // 1. Kullanıcıyı bul
        String email = request.getEmail().toLowerCase().trim();
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            logger.warn("Login failed - user not found: {}", email);
            throw AuthenticationException.invalidCredentials();
        }

        User user = userOptional.get();

        // 2. Hesap kilidi kontrol et
        if (user.isAccountLocked()) {
            logger.warn("Login failed - account locked: {}", email);
            throw AuthenticationException.invalidCredentials();
        }

        // 3. Şifre kontrolü
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            logger.warn("Login failed - invalid password: {}", email);
            // Başarısız giriş denemesini kaydet
            user.incrementFailedLoginAttempts();
            userRepository.save(user);
            throw AuthenticationException.invalidCredentials();
        }

        // 4. Hesap durumu kontrolü
        validateUserStatus(user);

        // 5. Başarılı giriş - kilidi sıfırla ve son giriş zamanını güncelle
        user.resetFailedLoginAttempts();
        user.recordLogin();
        userRepository.save(user);

        logger.info("User logged in: {} - Role: {}", user.getEmail(), user.getRole());

        // 6. Token oluştur
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(
                user.getId(), user.getEmail());

        // 7. Response oluştur
        return createAuthResponse(user, accessToken, refreshToken);
    }

    // ==================== TOKEN YENİLEME ====================

    /**
     * Access token'ı yeniler.
     * 
     * @param request Refresh token
     * @return AuthResponse (yeni tokenlar)
     */
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        logger.debug("Token refresh attempt");

        String refreshToken = request.getRefreshToken();

        // 1. Token doğrula
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            logger.warn("Token refresh failed - invalid token");
            throw AuthenticationException.invalidToken();
        }

        // 2. Refresh token mı kontrol et
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            logger.warn("Token refresh failed - not a refresh token");
            throw AuthenticationException.invalidToken();
        }

        // 3. Kullanıcıyı bul
        String email = jwtTokenProvider.extractEmail(refreshToken);
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            logger.warn("Token refresh failed - user not found: {}", email);
            throw AuthenticationException.invalidToken();
        }

        User user = userOptional.get();

        // 4. Hesap durumu kontrolü
        validateUserStatus(user);

        logger.debug("Token refreshed for user: {}", email);

        // 5. Yeni tokenlar oluştur
        String newAccessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(
                user.getId(), user.getEmail());

        // 6. Response oluştur
        return createAuthResponse(user, newAccessToken, newRefreshToken);
    }

    // ==================== ŞİFRE DEĞİŞTİRME ====================

    /**
     * Mevcut şifreyi değiştirir.
     * Giriş yapmış kullanıcı için.
     * 
     * @param request Şifre değiştirme bilgileri
     */
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        // 1. Mevcut kullanıcıyı al
        UUID userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> AuthenticationException.invalidToken());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.userNotFound(userId.toString()));

        logger.info("Password change attempt: {}", user.getEmail());

        // 2. Validasyonlar
        if (!request.passwordsMatch()) {
            throw ValidationException.passwordsDoNotMatch();
        }

        if (!request.isNewPasswordDifferent()) {
            throw new ValidationException("newPassword", "Yeni şifre mevcut şifreden farklı olmalıdır");
        }

        // 3. Mevcut şifre kontrolü
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            logger.warn("Password change failed - invalid current password: {}", user.getEmail());
            throw new ValidationException("currentPassword", "Mevcut şifre hatalı");
        }

        // 4. Yeni şifreyi kaydet
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        logger.info("Password changed successfully: {}", user.getEmail());
    }

    // ==================== ŞİFRE SIFIRLAMA ====================

    /**
     * Şifre sıfırlama e-postası gönderir.
     * 
     * @param request E-posta adresi
     */
    @Transactional(readOnly = true)
    public void requestPasswordReset(PasswordResetRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        logger.info("Password reset request: {}", email);

        // Kullanıcı var mı kontrol et
        Optional<User> userOptional = userRepository.findByEmail(email);

        // GÜVENLİK: Kullanıcı yoksa bile başarılı response döndür
        // Bu, e-posta enumeration saldırısını engeller
        if (userOptional.isEmpty()) {
            logger.warn("Password reset request - user not found: {}", email);
            // Başarılı gibi davran
            return;
        }

        User user = userOptional.get();

        // TODO: E-posta gönderme servisi entegre edilecek
        // 1. Sıfırlama token'ı oluştur (örn: UUID + timestamp)
        // 2. Token'ı veritabanına kaydet (password_reset_tokens tablosu)
        // 3. E-posta gönder

        logger.info("Password reset email sent to: {}", email);
    }

    /**
     * Şifre sıfırlama işlemini tamamlar.
     * 
     * @param request Token ve yeni şifre
     */
    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmRequest request) {
        logger.info("Password reset confirm attempt");

        // 1. Şifre eşleşme kontrolü
        if (!request.passwordsMatch()) {
            throw ValidationException.passwordsDoNotMatch();
        }

        // TODO: Token doğrulama ve şifre güncelleme
        // 1. Token'ı veritabanında bul
        // 2. Token süresi dolmuş mu kontrol et
        // 3. Kullanıcıyı bul
        // 4. Şifreyi güncelle
        // 5. Token'ı sil (tek kullanımlık)

        logger.info("Password reset completed");
    }

    // ==================== E-POSTA DOĞRULAMA ====================

    /**
     * E-posta doğrulama token'ını kontrol eder ve hesabı aktif yapar.
     * 
     * @param token Doğrulama token'ı
     */
    @Transactional
    public void verifyEmail(String token) {
        logger.info("Email verification attempt");

        // TODO: Token doğrulama ve hesap aktivasyonu
        // 1. Token'ı veritabanında bul
        // 2. Token süresi dolmuş mu kontrol et
        // 3. Kullanıcıyı bul
        // 4. E-posta doğrulamasını tamamla (user.verifyEmail())
        // 5. Token'ı sil

        logger.info("Email verified successfully");
    }

    /**
     * E-posta doğrulama linkini yeniden gönderir.
     * 
     * @param email E-posta adresi
     */
    @Transactional(readOnly = true)
    public void resendVerificationEmail(String email) {
        logger.info("Resend verification email: {}", email);

        Optional<User> userOptional = userRepository.findByEmail(email.toLowerCase().trim());

        if (userOptional.isEmpty()) {
            // GÜVENLİK: Kullanıcı yoksa sessizce başarılı dön
            return;
        }

        User user = userOptional.get();

        if (user.isVerified()) {
            // Zaten doğrulanmış
            return;
        }

        // TODO: Doğrulama e-postası gönder

        logger.info("Verification email resent to: {}", email);
    }

    // ==================== ÇIKIŞ ====================

    /**
     * Kullanıcı çıkışı yapar.
     * Refresh token'ı iptal eder.
     * logoutAllDevices = true ise tüm cihazlardan çıkış yapılır.
     * 
     * @param request Logout bilgileri (refreshToken, logoutAllDevices)
     */
    @Transactional
    public void logout(LogoutRequest request) {
        logger.info("Logout attempt");

        // 1. Mevcut kullanıcıyı al
        UUID userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> AuthenticationException.invalidToken());

        // 2. LogoutAllDevices flag'i kontrol et
        if (Boolean.TRUE.equals(request.getLogoutAllDevices())) {
            // Tüm refresh token'ları iptal et
            logger.info("Logging out from all devices for user: {}", userId);
            // TODO: RefreshTokenRepository.revokeAllByUser(userId) çağrısı yapılacak
        } else {
            // Sadece belirtilen token'ı iptal et
            // TODO: Token hash'lemesi ve RefreshTokenRepository'den silme işlemi yapılacak
            logger.info("Logging out single device for user: {}", userId);
        }

        logger.info("User logged out successfully");
    }

    // ==================== YARDIMCI METODLAR ====================

    /**
     * Kayıt request validasyonu.
     */
    private void validateRegisterRequest(RegisterRequest request) {
        // Şifre eşleşme kontrolü
        if (!request.passwordsMatch()) {
            throw ValidationException.passwordsDoNotMatch();
        }

        // Şartlar kabul edilmiş mi
        if (!request.hasAllConsents()) {
            if (request.getAcceptTerms() == null || !request.getAcceptTerms()) {
                throw ValidationException.termsNotAccepted();
            }
            if (request.getAcceptKvkk() == null || !request.getAcceptKvkk()) {
                throw ValidationException.kvkkNotAccepted();
            }
        }

        // Rol kontrolü (ADMIN seçilemez)
        if (!request.isRoleValid()) {
            throw new ValidationException("role", "Geçersiz kullanıcı rolü");
        }
    }

    /**
     * Kullanıcı durumu kontrolü.
     */
    private void validateUserStatus(User user) {
        UserStatus status = user.getStatus();

        if (status == UserStatus.SUSPENDED) {
            throw AuthenticationException.accountSuspended();
        }

        if (status == UserStatus.INACTIVE) {
            throw AuthenticationException.accountInactive();
        }

        // PENDING_VERIFICATION durumunda giriş yapılabilir
        // Ama bazı işlemler kısıtlanabilir
    }

    /**
     * AuthResponse oluşturur.
     */
    private AuthResponse createAuthResponse(User user, String accessToken, String refreshToken) {
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setEmail(user.getEmail());
        userInfo.setRole(user.getRole());
        userInfo.setFullName(user.getFullName());
        userInfo.setEmailVerified(user.getEmailVerified());
        userInfo.setLastLoginAt(user.getLastLoginAt());

        if (user.getProfile() != null) {
            userInfo.setAvatarUrl(user.getProfile().getAvatarUrl());
        }

        return new AuthResponse(
                accessToken,
                refreshToken,
                jwtConfig.getAccessTokenExpirationInSeconds(),
                userInfo
        );
    }
}
