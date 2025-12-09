package com.seffafbagis.api.service.user;

import com.seffafbagis.api.dto.mapper.UserMapper;
import com.seffafbagis.api.dto.request.user.UpdateProfileRequest;
import com.seffafbagis.api.dto.response.user.UserProfileResponse;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.entity.user.UserProfile;
import com.seffafbagis.api.exception.ResourceNotFoundException;
import com.seffafbagis.api.repository.UserProfileRepository;
import com.seffafbagis.api.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for user profile management.
 */
@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserProfileService(UserProfileRepository userProfileRepository,
            UserRepository userRepository,
            UserMapper userMapper) {
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    /**
     * Gets user profile.
     */
    public UserProfileResponse getProfile(UUID userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "userId", userId.toString()));
        return userMapper.toUserProfileResponse(profile);
    }

    /**
     * Updates user profile.
     * Creates new profile if not exists (though it should exist for created users).
     */
    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElse(null);

        // If profile doesn't exist, Create one (should be rare if created at
        // registration, but good for robustness)
        if (profile == null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));
            profile = new UserProfile(user);
        }

        // Update fields
        profile = userMapper.updateProfileFromRequest(profile, request);

        // Save
        profile = userProfileRepository.save(profile);

        return userMapper.toUserProfileResponse(profile);
    }
}
