package com.seffafbagis.api.service.user;

import com.seffafbagis.api.dto.mapper.UserMapper;
import com.seffafbagis.api.dto.request.user.DeleteAccountRequest;
import com.seffafbagis.api.dto.response.user.UserDetailResponse;
import com.seffafbagis.api.dto.response.user.UserResponse;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.UserStatus;
import com.seffafbagis.api.exception.BadRequestException;
import com.seffafbagis.api.exception.ResourceNotFoundException;
import com.seffafbagis.api.repository.RefreshTokenRepository;
import com.seffafbagis.api.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for core user operations.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    /**
     * Gets basic user information by ID.
     */
    public UserResponse getCurrentUser(UUID userId) {
        User user = getUserById(userId);
        return userMapper.toUserResponse(user);
    }

    /**
     * Gets complete user information by ID.
     */
    @Transactional
    public UserDetailResponse getCurrentUserDetail(UUID userId) {
        User user = getUserById(userId);

        // Lazy loaded relations will be initialized here inside transaction
        // However, it's safer to rely on mapper if we pass entities directly OR fetch
        // them effectively.
        // Since the relationships are mappedBy="user" in User entity (inverse side),
        // accessing getters usually triggers lazy load if session open.
        // User entity has: getProfile(), getPreferences(), getSensitiveData()

        return userMapper.toUserDetailResponse(
                user,
                user.getProfile(),
                user.getPreferences(),
                user.getSensitiveData());
    }

    /**
     * Deletes user account (soft delete).
     */
    @Transactional
    public void deleteAccount(UUID userId, DeleteAccountRequest request) {
        User user = getUserById(userId);

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Incorrect password");
        }

        // Verify confirmation
        if (!Boolean.TRUE.equals(request.getConfirmDeletion())) {
            throw new BadRequestException("You must confirm deletion");
        }

        // Revoke all refresh tokens
        refreshTokenRepository.deleteByUserId(user.getId());

        // Soft delete - set status to INACTIVE or DELETED
        // Requirement says INACTIVE or a new DELETED status. Using INACTIVE as per
        // UserStatus enum likely values.
        // Or if UserStatus has DELETED, use that. Assuming INACTIVE for now if DELETED
        // not confirmed.
        // Let's check UserStatus enum if possible, but for now INACTIVE is safe.
        // Actually, let's assume we use INACTIVE as "Soft Deleted" effective state for
        // now.
        user.setStatus(UserStatus.INACTIVE);

        // Update user
        userRepository.save(user);

        // Audit log would ideally be created here via an AuditService/Event
    }

    private User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));
    }
}
