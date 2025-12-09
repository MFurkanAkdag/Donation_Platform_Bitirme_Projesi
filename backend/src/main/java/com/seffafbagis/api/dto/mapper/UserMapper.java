package com.seffafbagis.api.dto.mapper;

import com.seffafbagis.api.dto.request.user.UpdatePreferencesRequest;
import com.seffafbagis.api.dto.request.user.UpdateProfileRequest;
import com.seffafbagis.api.dto.response.user.*;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.entity.user.UserPreference;
import com.seffafbagis.api.entity.user.UserProfile;
import com.seffafbagis.api.entity.user.UserSensitiveData;
import com.seffafbagis.api.enums.DonationVisibility;
import com.seffafbagis.api.service.encryption.EncryptionService;
import com.seffafbagis.api.validator.PhoneValidator;
import com.seffafbagis.api.validator.TcKimlikValidator;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Mapper for converting between User entities and DTOs.
 */
@Component
public class UserMapper {

    private final EncryptionService encryptionService;

    public UserMapper(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    /**
     * Converts User entity to UserResponse DTO.
     */
    public UserResponse toUserResponse(User user) {
        return UserResponse.fromEntity(user);
    }

    /**
     * Converts UserProfile entity to UserProfileResponse DTO.
     */
    public UserProfileResponse toUserProfileResponse(UserProfile profile) {
        return UserProfileResponse.fromEntity(profile);
    }

    /**
     * Converts UserPreference entity to UserPreferenceResponse DTO.
     */
    public UserPreferenceResponse toUserPreferenceResponse(UserPreference preference) {
        return UserPreferenceResponse.fromEntity(preference);
    }

    /**
     * Converts UserSensitiveData entity to masked response DTO.
     * Decrypts data temporarily for masking but NEVER returns unmasked data.
     */
    public UserSensitiveDataResponse toUserSensitiveDataResponse(UserSensitiveData sensitiveData) {
        if (sensitiveData == null) {
            return null;
        }

        UserSensitiveDataResponse response = new UserSensitiveDataResponse();
        response.setId(sensitiveData.getId());

        // TC Kimlik Masking
        if (sensitiveData.getTcKimlikEncrypted() != null) {
            String plainTc = encryptionService.decrypt(sensitiveData.getTcKimlikEncrypted());
            if (plainTc != null) {
                TcKimlikValidator validator = new TcKimlikValidator(plainTc);
                response.setHasTcKimlik(true);
                response.setTcKimlikMasked(validator.mask());
            }
        } else {
            response.setHasTcKimlik(false);
        }

        // Phone Masking
        if (sensitiveData.getPhoneEncrypted() != null) {
            String plainPhone = encryptionService.decrypt(sensitiveData.getPhoneEncrypted());
            if (plainPhone != null) {
                PhoneValidator validator = new PhoneValidator(plainPhone);
                response.setHasPhone(true);
                response.setPhoneMasked(validator.mask());
            }
        } else {
            response.setHasPhone(false);
        }

        // Address Masking
        if (sensitiveData.getAddressEncrypted() != null) {
            String plainAddress = encryptionService.decrypt(sensitiveData.getAddressEncrypted());
            if (plainAddress != null && !plainAddress.isEmpty()) {
                response.setHasAddress(true);
                // First 20 chars + "..."
                String preview = plainAddress.length() > 20
                        ? plainAddress.substring(0, 20) + "..."
                        : plainAddress;
                response.setAddressPreview(preview);
            }
        } else {
            response.setHasAddress(false);
        }

        // Birth Date Masking (Only Year)
        if (sensitiveData.getBirthDateEncrypted() != null) {
            String plainBirthDate = encryptionService.decrypt(sensitiveData.getBirthDateEncrypted());
            if (plainBirthDate != null) {
                try {
                    LocalDate birthDate = LocalDate.parse(plainBirthDate);
                    response.setHasBirthDate(true);
                    response.setBirthYear(birthDate.getYear());
                } catch (Exception e) {
                    // If parsing fails, just indicate existence
                    response.setHasBirthDate(true);
                }
            }
        } else {
            response.setHasBirthDate(false);
        }

        // Consents
        response.setDataProcessingConsent(sensitiveData.getDataProcessingConsent());
        if (sensitiveData.getConsentDate() != null) {
            response.setConsentDate(LocalDateTime.ofInstant(sensitiveData.getConsentDate(), ZoneId.systemDefault()));
        }

        // Note: Marketing and ThirdParty consents are not in Entity yet based on
        // provided file,
        // using temporary placeholder logic or ignoring if not in entity.
        // The requirements mentioned them in response DTO but I didn't see them in
        // UserSensitiveData entity view.
        // Checking UserSensitiveData content again...
        // It seems UserSensitiveData only has dataProcessingConsent.
        // I will default marketing/thirdParty to false/null for now as they might be
        // missing in entity definition
        // OR I might have missed them in the view.
        // Re-reading UserSensitiveData entity view...
        // Yes, lines 91-92 only show dataProcessingConsent.
        // I will assume for now that only dataProcessingConsent is available in Entity.
        // To strictly follow requirement to map them, I would need them in Entity.
        // However, I must stick to available Entity fields.

        return response;
    }

    /**
     * Combines all user data into a single response.
     */
    public UserDetailResponse toUserDetailResponse(User user, UserProfile profile, UserPreference preferences,
            UserSensitiveData sensitiveData) {
        return UserDetailResponse.of(
                toUserResponse(user),
                toUserProfileResponse(profile),
                toUserPreferenceResponse(preferences),
                toUserSensitiveDataResponse(sensitiveData));
    }

    /**
     * Updates UserProfile entity from request DTO.
     * Only updates non-null fields from request.
     */
    public UserProfile updateProfileFromRequest(UserProfile profile, UpdateProfileRequest request) {
        if (profile == null || request == null) {
            return profile;
        }

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
        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getPreferredLanguage() != null) {
            profile.setPreferredLanguage(request.getPreferredLanguage());
        }
        if (request.getTimezone() != null) {
            profile.setTimezone(request.getTimezone());
        }

        return profile;
    }

    /**
     * Updates UserPreference entity from request DTO.
     * Only updates non-null fields.
     */
    public UserPreference updatePreferencesFromRequest(UserPreference preferences, UpdatePreferencesRequest request) {
        if (preferences == null || request == null) {
            return preferences;
        }

        if (request.getEmailNotifications() != null) {
            preferences.setEmailNotifications(request.getEmailNotifications());
        }
        if (request.getSmsNotifications() != null) {
            preferences.setSmsNotifications(request.getSmsNotifications());
        }
        if (request.getDonationVisibility() != null) {
            try {
                preferences.setDonationVisibility(DonationVisibility.valueOf(request.getDonationVisibility()));
            } catch (IllegalArgumentException e) {
                // Ignore invalid enum values or handle as validation error
            }
        }
        if (request.getShowInDonorList() != null) {
            preferences.setShowInDonorList(request.getShowInDonorList());
        }

        return preferences;
    }
}
