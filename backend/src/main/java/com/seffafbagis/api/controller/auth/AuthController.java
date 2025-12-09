package com.seffafbagis.api.controller.auth;

import com.seffafbagis.api.dto.request.auth.ChangePasswordRequest;
import com.seffafbagis.api.dto.request.auth.ForgotPasswordRequest;
import com.seffafbagis.api.dto.request.auth.LoginRequest;
import com.seffafbagis.api.dto.request.auth.LogoutRequest;
import com.seffafbagis.api.dto.request.auth.PasswordResetRequest;
import com.seffafbagis.api.dto.request.auth.RefreshTokenRequest;
import com.seffafbagis.api.dto.request.auth.RegisterRequest;
import com.seffafbagis.api.dto.request.auth.ResetPasswordRequest;
import com.seffafbagis.api.dto.request.auth.ResendVerificationRequest;
import com.seffafbagis.api.dto.request.auth.VerifyEmailRequest;
import com.seffafbagis.api.dto.response.common.ApiResponse;
import com.seffafbagis.api.dto.response.auth.AuthResponse;
import com.seffafbagis.api.service.auth.AuthService;
import com.seffafbagis.api.service.auth.PasswordResetService;
import com.seffafbagis.api.service.auth.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication controller.
 * 
 * Kayıt, giriş, çıkış, token yenileme, şifre işlemleri.
 * 
 * Base URL: /api/v1/auth
 * 
 * @author Furkan
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Kimlik doğrulama işlemleri")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final EmailVerificationService emailVerificationService;

    /**
     * Constructor injection.
     */
    @Autowired
    public AuthController(
            AuthService authService,
            PasswordResetService passwordResetService,
            EmailVerificationService emailVerificationService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
        this.emailVerificationService = emailVerificationService;
    }

    // ==================== KAYIT ====================

    /**
     * Yeni kullanıcı kaydı yapar.
     * 
     * POST /api/v1/auth/register
     * 
     * @param request Kayıt bilgileri
     * @return AuthResponse
     */
    @PostMapping("/register")
    @Operation(summary = "Kullanıcı kaydı", description = "Yeni kullanıcı hesabı oluşturur. E-posta doğrulama gerektirir.")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        logger.info("Register endpoint called: {}", request.getEmail());

        AuthResponse authResponse = authService.register(request);

        ApiResponse<AuthResponse> response = ApiResponse.success(
                "Kayıt başarılı. Lütfen e-posta adresinizi doğrulayın.",
                authResponse);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ==================== GİRİŞ ====================

    /**
     * Kullanıcı girişi yapar.
     * 
     * POST /api/v1/auth/login
     * 
     * @param request Giriş bilgileri
     * @return AuthResponse
     */
    @PostMapping("/login")
    @Operation(summary = "Kullanıcı girişi", description = "E-posta ve şifre ile giriş yapar. JWT token döner.")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        logger.info("Login endpoint called: {}", request.getEmail());

        AuthResponse authResponse = authService.login(request);

        ApiResponse<AuthResponse> response = ApiResponse.success(
                "Giriş başarılı",
                authResponse);

        return ResponseEntity.ok(response);
    }

    // ==================== TOKEN YENİLEME ====================

    /**
     * Access token'ı yeniler.
     * 
     * POST /api/v1/auth/refresh
     * 
     * @param request Refresh token
     * @return AuthResponse (yeni tokenlar)
     */
    @PostMapping("/refresh")
    @Operation(summary = "Token yenileme", description = "Refresh token ile yeni access token alır.")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        logger.debug("Refresh token endpoint called");

        AuthResponse authResponse = authService.refreshToken(request);

        ApiResponse<AuthResponse> response = ApiResponse.success(
                "Token yenilendi",
                authResponse);

        return ResponseEntity.ok(response);
    }

    // ==================== ÇIKIŞ ====================

    /**
     * Kullanıcı çıkışı yapar.
     * 
     * POST /api/v1/auth/logout
     * 
     * Refresh token'ı iptal eder ve oturumu sonlandırır.
     * logoutAllDevices = true ise tüm cihazlardan çıkış yapılır.
     * 
     * @param request Logout request (refreshToken ve logoutAllDevices)
     * @return Success response
     */
    @PostMapping("/logout")
    @Operation(summary = "Kullanıcı çıkışı", description = "Mevcut oturumu sonlandırır. Refresh token'ı iptal eder.")
    public ResponseEntity<ApiResponse<String>> logout(
            @Valid @RequestBody LogoutRequest request) {

        logger.info("Logout endpoint called");

        authService.logout(request);

        ApiResponse<String> response = ApiResponse.success(
                "Çıkış başarılı");

        return ResponseEntity.ok(response);
    }

    // ==================== ŞİFRE DEĞİŞTİRME ====================

    /**
     * Mevcut şifreyi değiştirir.
     * 
     * POST /api/v1/auth/change-password
     * 
     * @param request Şifre değiştirme bilgileri
     * @return Success response
     */
    @PostMapping("/change-password")
    @Operation(summary = "Şifre değiştirme", description = "Giriş yapmış kullanıcının şifresini değiştirir.")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        logger.info("Change password endpoint called");

        authService.changePassword(request);

        ApiResponse<Void> response = ApiResponse.success("Şifre başarıyla değiştirildi");
        return ResponseEntity.ok(response);
    }

    // ==================== ŞİFRE SIFIRLAMA ====================

    /**
     * Şifre sıfırlama e-postası gönderir.
     * 
     * POST /api/v1/auth/forgot-password
     * 
     * @param request E-posta adresi
     * @return Success response
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Şifremi unuttum", description = "Şifre sıfırlama linki e-posta ile gönderilir.")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        logger.info("Forgot password endpoint called: {}", request.getEmail());

        passwordResetService.initiatePasswordReset(request.getEmail());

        // GÜVENLİK: Her durumda aynı mesajı döndür
        ApiResponse<Void> response = ApiResponse.success(
                "Eğer e-posta adresi kayıtlı ise şifre sıfırlama linki gönderildi");
        return ResponseEntity.ok(response);
    }

    /**
     * Şifre sıfırlama işlemini tamamlar.
     * 
     * POST /api/v1/auth/reset-password
     * 
     * @param request Token ve yeni şifre
     * @return Success response
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Şifre sıfırlama", description = "E-posta ile gelen token ile yeni şifre belirlenir.")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        logger.info("Reset password endpoint called");

        passwordResetService.resetPassword(request);

        ApiResponse<Void> response = ApiResponse.success("Şifre başarıyla sıfırlandı");
        return ResponseEntity.ok(response);
    }

    // ==================== E-POSTA DOĞRULAMA ====================

    /**
     * E-posta doğrulamasını tamamlar.
     * 
     * POST /api/v1/auth/verify-email
     * 
     * @param request Doğrulama token'ı
     * @return Success response
     */
    @PostMapping("/verify-email")
    @Operation(summary = "E-posta doğrulama", description = "E-posta ile gelen doğrulama linkindeki token ile hesap aktif edilir.")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request) {

        logger.info("Verify email endpoint called");

        emailVerificationService.verifyEmail(request.getToken());

        ApiResponse<Void> response = ApiResponse.success("E-posta başarıyla doğrulandı");
        return ResponseEntity.ok(response);
    }

    /**
     * E-posta doğrulama linkini yeniden gönderir.
     * 
     * POST /api/v1/auth/resend-verification
     * 
     * @param request E-posta adresi
     * @return Success response
     */
    @PostMapping("/resend-verification")
    @Operation(summary = "Doğrulama e-postası yeniden gönder", description = "E-posta doğrulama linkini tekrar gönderir.")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request) {

        logger.info("Resend verification endpoint called: {}", request.getEmail());

        emailVerificationService.resendVerificationEmail(request.getEmail());

        // GÜVENLİK: Her durumda aynı mesajı döndür
        ApiResponse<Void> response = ApiResponse.success(
                "Eğer e-posta adresi kayıtlı ve henüz doğrulanmamış ise doğrulama linki gönderildi");
        return ResponseEntity.ok(response);
    }
}
