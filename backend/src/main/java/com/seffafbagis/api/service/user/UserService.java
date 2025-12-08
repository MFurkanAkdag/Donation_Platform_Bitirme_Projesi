package com.seffafbagis.api.service.user;

import com.seffafbagis.api.dto.request.user.UpdatePreferencesRequest;
import com.seffafbagis.api.dto.request.user.UpdateProfileRequest;
import com.seffafbagis.api.dto.response.user.UserResponse;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.entity.user.UserPreference;
import com.seffafbagis.api.entity.user.UserProfile;
import com.seffafbagis.api.enums.UserStatus;
import com.seffafbagis.api.exception.AccessDeniedException;
import com.seffafbagis.api.exception.ResourceNotFoundException;
import com.seffafbagis.api.repository.UserRepository;
import com.seffafbagis.api.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Kullanıcı service.
 * 
 * Kullanıcı CRUD işlemleri, profil ve tercih yönetimi.
 * 
 * @author Furkan
 * @version 1.0
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    /**
     * Constructor injection.
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ==================== OKUMA İŞLEMLERİ ====================

    /**
     * Mevcut kullanıcının bilgilerini döndürür.
     * 
     * @return UserResponse
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        UUID userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException("Kullanıcı girişi gerekli"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.userNotFound(userId.toString()));

        return UserResponse.fromEntity(user);
    }

    /**
     * ID ile kullanıcı bilgilerini döndürür.
     * 
     * @param userId Kullanıcı ID
     * @return UserResponse
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.userNotFound(userId.toString()));

        return UserResponse.fromEntity(user);
    }

    /**
     * E-posta ile kullanıcı bilgilerini döndürür.
     * 
     * @param email E-posta adresi
     * @return UserResponse
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> ResourceNotFoundException.userNotFound(email));

        return UserResponse.fromEntity(user);
    }

    /**
     * Tüm kullanıcıları sayfalama ile döndürür.
     * Admin için.
     * 
     * @param pageable Sayfalama bilgisi
     * @return Sayfalanmış kullanıcı listesi
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        // Admin kontrolü
        if (!SecurityUtils.isAdmin()) {
            throw AccessDeniedException.adminRequired();
        }

        Page<User> users = userRepository.findAll(pageable);
        return users.map(UserResponse::fromEntity);
    }

    /**
     * Kullanıcı arama.
     * Admin için.
     * 
     * @param searchTerm Arama terimi
     * @param pageable Sayfalama bilgisi
     * @return Sayfalanmış kullanıcı listesi
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String searchTerm, Pageable pageable) {
        // Admin kontrolü
        if (!SecurityUtils.isAdmin()) {
            throw AccessDeniedException.adminRequired();
        }

        Page<User> users = userRepository.searchByEmail(searchTerm, pageable);
        return users.map(UserResponse::fromEntity);
    }

    // ==================== PROFİL GÜNCELLEME ====================

    /**
     * Mevcut kullanıcının profilini günceller.
     * 
     * @param request Profil güncelleme bilgileri
     * @return Güncellenmiş UserResponse
     */
    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException("Kullanıcı girişi gerekli"));

        return updateUserProfile(userId, request);
    }

    /**
     * Belirli bir kullanıcının profilini günceller.
     * Admin veya profil sahibi için.
     * 
     * @param userId Kullanıcı ID
     * @param request Profil güncelleme bilgileri
     * @return Güncellenmiş UserResponse
     */
    @Transactional
    public UserResponse updateUserProfile(UUID userId, UpdateProfileRequest request) {
        // Yetki kontrolü
        if (!SecurityUtils.isCurrentUserOrAdmin(userId)) {
            throw AccessDeniedException.notOwner();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.userNotFound(userId.toString()));

        logger.info("Updating profile for user: {}", user.getEmail());

        // Profil yoksa oluştur
        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = new UserProfile(user);
            user.setProfile(profile);
        }

        // Gönderilen alanları güncelle (partial update)
        if (request.getFirstName() != null) {
            profile.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            profile.setLastName(request.getLastName());
        }
        if (request.getDisplayName() != null) {
            profile.setDisplayName(request.getDisplayName());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getPreferredLanguage() != null) {
            profile.setPreferredLanguage(request.getPreferredLanguage());
        }
        if (request.getTimezone() != null) {
            profile.setTimezone(request.getTimezone());
        }

        user = userRepository.save(user);
        logger.info("Profile updated for user: {}", user.getEmail());

        return UserResponse.fromEntity(user);
    }

    // ==================== TERCİH GÜNCELLEME ====================

    /**
     * Mevcut kullanıcının tercihlerini günceller.
     * 
     * @param request Tercih güncelleme bilgileri
     * @return Güncellenmiş UserResponse
     */
    @Transactional
    public UserResponse updatePreferences(UpdatePreferencesRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException("Kullanıcı girişi gerekli"));

        return updateUserPreferences(userId, request);
    }

    /**
     * Belirli bir kullanıcının tercihlerini günceller.
     * 
     * @param userId Kullanıcı ID
     * @param request Tercih güncelleme bilgileri
     * @return Güncellenmiş UserResponse
     */
    @Transactional
    public UserResponse updateUserPreferences(UUID userId, UpdatePreferencesRequest request) {
        // Yetki kontrolü
        if (!SecurityUtils.isCurrentUserOrAdmin(userId)) {
            throw AccessDeniedException.notOwner();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.userNotFound(userId.toString()));

        logger.info("Updating preferences for user: {}", user.getEmail());

        // Tercihler yoksa oluştur
        UserPreference preferences = user.getPreferences();
        if (preferences == null) {
            preferences = new UserPreference(user);
            user.setPreferences(preferences);
        }

        // Gönderilen alanları güncelle (partial update)
        if (request.getEmailNotifications() != null) {
            preferences.setEmailNotifications(request.getEmailNotifications());
        }
        if (request.getSmsNotifications() != null) {
            preferences.setSmsNotifications(request.getSmsNotifications());
        }
        if (request.getPushNotifications() != null) {
            preferences.setPushNotifications(request.getPushNotifications());
        }
        if (request.getDonationVisibility() != null) {
            preferences.setDonationVisibility(request.getDonationVisibility());
        }
        if (request.getShowInDonorList() != null) {
            preferences.setShowInDonorList(request.getShowInDonorList());
        }
        if (request.getWeeklySummaryEmail() != null) {
            preferences.setWeeklySummaryEmail(request.getWeeklySummaryEmail());
        }
        if (request.getNotifyOnCampaignComplete() != null) {
            preferences.setNotifyOnCampaignComplete(request.getNotifyOnCampaignComplete());
        }
        if (request.getNotifyOnCampaignUpdate() != null) {
            preferences.setNotifyOnCampaignUpdate(request.getNotifyOnCampaignUpdate());
        }

        user = userRepository.save(user);
        logger.info("Preferences updated for user: {}", user.getEmail());

        return UserResponse.fromEntity(user);
    }

    // ==================== HESAP YÖNETİMİ ====================

    /**
     * Kullanıcı hesabını deaktif eder.
     * Kullanıcı kendi hesabını kapatabilir.
     */
    @Transactional
    public void deactivateAccount() {
        UUID userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException("Kullanıcı girişi gerekli"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.userNotFound(userId.toString()));

        logger.info("Deactivating account: {}", user.getEmail());

        user.deactivate();
        userRepository.save(user);

        logger.info("Account deactivated: {}", user.getEmail());
    }

    /**
     * Kullanıcı hesabını askıya alır.
     * Admin için.
     * 
     * @param userId Askıya alınacak kullanıcı ID
     */
    @Transactional
    public void suspendUser(UUID userId) {
        // Admin kontrolü
        if (!SecurityUtils.isAdmin()) {
            throw AccessDeniedException.adminRequired();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.userNotFound(userId.toString()));

        logger.info("Suspending user: {}", user.getEmail());

        user.suspend();
        userRepository.save(user);

        logger.info("User suspended: {}", user.getEmail());
    }

    /**
     * Kullanıcı hesabını aktif eder.
     * Admin için.
     * 
     * @param userId Aktif edilecek kullanıcı ID
     */
    @Transactional
    public void activateUser(UUID userId) {
        // Admin kontrolü
        if (!SecurityUtils.isAdmin()) {
            throw AccessDeniedException.adminRequired();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.userNotFound(userId.toString()));

        logger.info("Activating user: {}", user.getEmail());

        user.activate();
        userRepository.save(user);

        logger.info("User activated: {}", user.getEmail());
    }

    // ==================== AVATAR ====================

    /**
     * Avatar URL'ini günceller.
     * 
     * @param avatarUrl Yeni avatar URL
     * @return Güncellenmiş UserResponse
     */
    @Transactional
    public UserResponse updateAvatar(String avatarUrl) {
        UUID userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException("Kullanıcı girişi gerekli"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.userNotFound(userId.toString()));

        // Profil yoksa oluştur
        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = new UserProfile(user);
            user.setProfile(profile);
        }

        profile.setAvatarUrl(avatarUrl);
        user = userRepository.save(user);

        logger.info("Avatar updated for user: {}", user.getEmail());

        return UserResponse.fromEntity(user);
    }

    /**
     * Avatar'ı siler.
     * 
     * @return Güncellenmiş UserResponse
     */
    @Transactional
    public UserResponse removeAvatar() {
        return updateAvatar(null);
    }
}
