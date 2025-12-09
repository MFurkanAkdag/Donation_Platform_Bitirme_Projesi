package com.seffafbagis.api.service.user;

import com.seffafbagis.api.dto.mapper.UserMapper;
import com.seffafbagis.api.dto.request.user.UpdatePreferencesRequest;
import com.seffafbagis.api.dto.response.user.UserPreferenceResponse;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.entity.user.UserPreference;
import com.seffafbagis.api.exception.ResourceNotFoundException;
import com.seffafbagis.api.repository.UserPreferenceRepository;
import com.seffafbagis.api.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for user preferences management.
 */
@Service
public class UserPreferenceService {

    private final UserPreferenceRepository userPreferenceRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserPreferenceService(UserPreferenceRepository userPreferenceRepository,
            UserRepository userRepository,
            UserMapper userMapper) {
        this.userPreferenceRepository = userPreferenceRepository;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    /**
     * Gets user preferences.
     */
    public UserPreferenceResponse getPreferences(UUID userId) {
        UserPreference preferences = userPreferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserPreference", "userId", userId.toString()));
        return userMapper.toUserPreferenceResponse(preferences);
    }

    /**
     * Updates user preferences.
     */
    @Transactional
    public UserPreferenceResponse updatePreferences(UUID userId, UpdatePreferencesRequest request) {
        UserPreference preferences = userPreferenceRepository.findByUserId(userId)
                .orElse(null);

        if (preferences == null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));
            preferences = new UserPreference(user);
        }

        preferences = userMapper.updatePreferencesFromRequest(preferences, request);
        preferences = userPreferenceRepository.save(preferences);

        return userMapper.toUserPreferenceResponse(preferences);
    }
}
