package com.seffafbagis.api.service.admin;

import com.seffafbagis.api.dto.request.admin.AdminCreateUserRequest;
import com.seffafbagis.api.dto.request.admin.UpdateUserRoleRequest;
import com.seffafbagis.api.dto.request.admin.UpdateUserStatusRequest;
import com.seffafbagis.api.dto.request.admin.UserSearchRequest;
import com.seffafbagis.api.dto.response.admin.AdminDashboardResponse;
import com.seffafbagis.api.dto.response.admin.AdminUserListResponse;
import com.seffafbagis.api.dto.response.admin.AdminUserResponse;
import com.seffafbagis.api.dto.response.admin.UserStatusChangeResponse;
import com.seffafbagis.api.dto.response.admin.AdminUserResponse.AdminUserStatistics;
import com.seffafbagis.api.dto.response.common.ApiResponse;
import com.seffafbagis.api.dto.response.common.PageResponse;
import com.seffafbagis.api.entity.auth.EmailVerificationToken;
import com.seffafbagis.api.entity.auth.LoginHistory;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.entity.user.UserPreference;
import com.seffafbagis.api.entity.user.UserProfile;
import com.seffafbagis.api.entity.user.UserSensitiveData;
import com.seffafbagis.api.enums.UserRole;
import com.seffafbagis.api.enums.UserStatus;
import com.seffafbagis.api.exception.BadRequestException;
import com.seffafbagis.api.exception.ConflictException;
import com.seffafbagis.api.exception.ForbiddenException;
import com.seffafbagis.api.exception.ResourceNotFoundException;
import com.seffafbagis.api.repository.EmailVerificationTokenRepository;
import com.seffafbagis.api.repository.LoginHistoryRepository;
import com.seffafbagis.api.repository.RefreshTokenRepository;
import com.seffafbagis.api.repository.UserPreferenceRepository;
import com.seffafbagis.api.repository.UserProfileRepository;
import com.seffafbagis.api.repository.UserRepository;
import com.seffafbagis.api.repository.UserSensitiveDataRepository;
import com.seffafbagis.api.service.audit.AuditLogService;
import com.seffafbagis.api.service.notification.EmailService;
import com.seffafbagis.api.specification.UserSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminUserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final UserSensitiveDataRepository userSensitiveDataRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    public AdminUserService(UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            RefreshTokenRepository refreshTokenRepository,
            LoginHistoryRepository loginHistoryRepository,
            UserPreferenceRepository userPreferenceRepository,
            UserSensitiveDataRepository userSensitiveDataRepository,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.loginHistoryRepository = loginHistoryRepository;
        this.userPreferenceRepository = userPreferenceRepository;
        this.userSensitiveDataRepository = userSensitiveDataRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminUserListResponse> getAllUsers(Pageable pageable) {
        Page<User> usersPage = userRepository.findAll(pageable);
        return mapToPageResponse(usersPage);
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminUserListResponse> searchUsers(UserSearchRequest request, Pageable pageable) {
        Specification<User> spec = UserSpecification.buildSpecification(request);

        // Handle sorting if provided in request (overrides Pageable sort if needed, or
        // constructed outside)
        // Here we assume Pageable carries the sort info, but we can enforce default if
        // needed

        Page<User> usersPage = userRepository.findAll(spec, pageable);
        return mapToPageResponse(usersPage);
    }

    @Transactional(readOnly = true)
    public AdminUserResponse getUserById(UUID userId) {
        User user = getUser(userId);
        UserProfile profile = userProfileRepository.findById(userId).orElse(null);

        AdminUserResponse response = mapToAdminUserResponse(user, profile);

        // Populate statistics
        AdminUserStatistics stats = new AdminUserStatistics();
        stats.setLoginCount((int) loginHistoryRepository.countByUserId(userId)); // Cast long to int
        loginHistoryRepository.findTopByUserOrderByCreatedAtDesc(user)
                .ifPresent(login -> {
                    // Convert OffsetDateTime to LocalDateTime
                    if (login.getCreatedAt() != null) {
                        stats.setLastActiveAt(login.getCreatedAt().toLocalDateTime());
                    }
                });

        // TODO: Populate donation stats when Donation module is ready
        stats.setTotalDonations(0);
        stats.setTotalDonationAmount(BigDecimal.ZERO);
        stats.setFavoriteOrganizationsCount(0);

        response.setStatistics(stats);

        return response;
    }

    public UserStatusChangeResponse updateUserStatus(UUID userId, UpdateUserStatusRequest request, UUID adminId) {
        User user = getUser(userId);

        if (user.getId().equals(adminId)) {
            throw new ForbiddenException("Admins cannot change their own status");
        }

        if (request.getStatus() == UserStatus.PENDING_VERIFICATION) {
            throw new BadRequestException("Cannot manually set status to PENDING_VERIFICATION");
        }

        UserStatus previousStatus = user.getStatus();
        user.setStatus(request.getStatus());

        if (request.getStatus() == UserStatus.SUSPENDED) {
            if (request.getDuration() != null) {
                user.setLockedUntil(Instant.now().plus(java.time.Duration.ofDays(request.getDuration())));
            }
            // Revoke refresh tokens
            refreshTokenRepository.deleteByUserId(userId);
        } else if (request.getStatus() == UserStatus.ACTIVE) {
            user.setLockedUntil(null);
            user.setFailedLoginAttempts(0);
        }

        User savedUser = userRepository.save(user);

        // Audit log
        auditLogService.logAction(adminId, "UPDATE_USER_STATUS",
                "Changed user " + userId + " status from " + previousStatus + " to " + request.getStatus()
                        + ". Reason: " + request.getReason(),
                userId.toString());

        // Notification
        if (Boolean.TRUE.equals(request.getNotifyUser())) {
            // Need to implement specific email method for status change
            // emailService.sendAccountStatusChangeEmail(user.getEmail(),
            // request.getStatus(), request.getReason());
            // using generic sendEmail for now or TODO
        }

        return new UserStatusChangeResponse(
                userId,
                previousStatus,
                savedUser.getStatus(),
                request.getReason(),
                adminId,
                request.getNotifyUser());
    }

    public ApiResponse<Object> updateUserRole(UUID userId, UpdateUserRoleRequest request, UUID adminId) {
        User user = getUser(userId);

        if (user.getId().equals(adminId)) {
            throw new ForbiddenException("Admins cannot change their own role");
        }

        // Last Admin Check
        if (user.getRole() == UserRole.ADMIN && request.getRole() != UserRole.ADMIN) {
            long adminCount = userRepository.countByRole(UserRole.ADMIN);
            if (adminCount <= 1) {
                throw new BadRequestException("Cannot demote the last admin");
            }
        }

        UserRole previousRole = user.getRole();
        user.setRole(request.getRole());
        userRepository.save(user);

        // Revoke tokens to force re-login
        refreshTokenRepository.deleteByUserId(userId);

        // Audit log
        auditLogService.logAction(adminId, "UPDATE_USER_ROLE",
                "Changed user " + userId + " role from " + previousRole + " to " + request.getRole() + ". Reason: "
                        + request.getReason(),
                userId.toString());

        if (Boolean.TRUE.equals(request.getNotifyUser())) {
            // emailService.sendRoleChangeEmail(user.getEmail(), request.getRole());
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("previousRole", previousRole);
        data.put("newRole", request.getRole());

        return ApiResponse.success("User role updated successfully", data);
    }

    public AdminUserResponse createUser(AdminCreateUserRequest request, UUID adminId) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("User", "email", request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        if (Boolean.TRUE.equals(request.getEmailVerified())) {
            user.setStatus(UserStatus.ACTIVE);
            user.setEmailVerified(true);
            user.setEmailVerifiedAt(Instant.now());
        } else {
            user.setStatus(UserStatus.PENDING_VERIFICATION);
            user.setEmailVerified(false);
        }

        user = userRepository.save(user);

        // Create Profile
        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        userProfileRepository.save(profile);

        // Create Sensitive Data
        UserSensitiveData sensitiveData = new UserSensitiveData();
        sensitiveData.setUser(user);
        userSensitiveDataRepository.save(sensitiveData);

        // Create Preferences
        UserPreference preference = new UserPreference();
        preference.setUser(user);
        userPreferenceRepository.save(preference);

        // Audit Log
        auditLogService.logAction(adminId, "CREATE_USER", "Created user " + user.getEmail(), user.getId().toString());

        if (Boolean.FALSE.equals(request.getEmailVerified())) {
            EmailVerificationToken token = new EmailVerificationToken(user);
            emailVerificationTokenRepository.save(token);
            // emailService.sendVerificationEmail(user.getEmail(), token.getToken());
        } else if (Boolean.TRUE.equals(request.getSendWelcomeEmail())) {
            // emailService.sendWelcomeEmail(user.getEmail());
        }

        return mapToAdminUserResponse(user, profile);
    }

    public ApiResponse<Void> deleteUser(UUID userId, UUID adminId) {
        User user = getUser(userId);

        if (user.getId().equals(adminId)) {
            throw new ForbiddenException("Admins cannot delete themselves");
        }

        // Soft delete logic usually
        // Assuming INACTIVE is enough for now as UserStatus doesn't have DELETED
        // Or if we want to delete: userRepository.delete(user);

        user.setStatus(UserStatus.INACTIVE); // Or add DELETED status
        userRepository.save(user);
        refreshTokenRepository.deleteByUserId(userId);

        auditLogService.logAction(adminId, "DELETE_USER", "Soft deleted user " + userId, userId.toString());

        return ApiResponse.success("User deleted successfully");
    }

    public AdminUserResponse unlockUser(UUID userId, UUID adminId) {
        User user = getUser(userId);
        user.setLockedUntil(null);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        auditLogService.logAction(adminId, "UNLOCK_USER", "Unlocked user " + userId, userId.toString());

        UserProfile profile = userProfileRepository.findById(userId).orElse(null);
        return mapToAdminUserResponse(user, profile);
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboardStatistics() {
        AdminDashboardResponse response = new AdminDashboardResponse();

        response.setTotalUsers(userRepository.count());

        // These might need custom repository methods or streams
        // Doing simpler streams or separate queries for now if repos don't have
        // aggregation
        // ideally: userRepository.countUsersByRole() returns List<Object[]>

        // Mocking aggregation for now, or using what's available
        // Real implementation should add repository methods for efficiency

        response.setPendingVerifications(userRepository.countByStatus(UserStatus.PENDING_VERIFICATION));
        response.setSuspendedAccounts(userRepository.countByStatus(UserStatus.SUSPENDED));

        // TODO: Add proper aggregation queries in UserRepository
        Map<UserRole, Long> roleCounts = new HashMap<>();
        for (UserRole role : UserRole.values()) {
            roleCounts.put(role, userRepository.countByRole(role));
        }
        response.setUsersByRole(roleCounts);

        Map<UserStatus, Long> statusCounts = new HashMap<>();
        for (UserStatus status : UserStatus.values()) {
            statusCounts.put(status, userRepository.countByStatus(status));
        }
        response.setUsersByStatus(statusCounts);

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime today = now.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
        OffsetDateTime weekStart = now.minusWeeks(1);
        OffsetDateTime monthStart = now.minusMonths(1);

        response.setNewUsersToday(userRepository.countByCreatedAtAfter(today));
        response.setNewUsersThisWeek(userRepository.countByCreatedAtAfter(weekStart));
        response.setNewUsersThisMonth(userRepository.countByCreatedAtAfter(monthStart));

        response.setActiveUsersToday(loginHistoryRepository.countDistinctUserIdByCreatedAtAfter(today));
        response.setActiveUsersThisWeek(loginHistoryRepository.countDistinctUserIdByCreatedAtAfter(weekStart));

        PageRequest recentPage = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<AdminUserListResponse> recentUsers = userRepository.findAll(recentPage).getContent()
                .stream()
                .map(user -> {
                    UserProfile profile = userProfileRepository.findById(user.getId()).orElse(null);
                    return mapToAdminUserListResponse(user, profile);
                })
                .collect(Collectors.toList());
        response.setRecentRegistrations(recentUsers);

        return response;
    }

    @Transactional(readOnly = true)
    public PageResponse<Object> getUserLoginHistory(UUID userId, Pageable pageable) {
        Page<LoginHistory> historyPage = loginHistoryRepository.findAllByUser(getUser(userId), pageable);
        // Need mapping if we want DTO, using Object for now as placeholder implies we
        // might assume DTO exists or just return entity
        // Returning entity for now or simpler DTO if needed.
        // Let's assume we map to a simple DTO or return entity
        return PageResponse.of(historyPage.map(h -> (Object) h));
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private PageResponse<AdminUserListResponse> mapToPageResponse(Page<User> usersPage) {
        Page<AdminUserListResponse> responsePage = usersPage.map(user -> {
            UserProfile profile = userProfileRepository.findById(user.getId()).orElse(null);
            return mapToAdminUserListResponse(user, profile);
        });
        return PageResponse.of(responsePage);
    }

    private AdminUserResponse mapToAdminUserResponse(User user, UserProfile profile) {
        AdminUserResponse response = new AdminUserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setEmailVerified(user.getEmailVerified());
        if (user.getEmailVerifiedAt() != null) {
            response.setEmailVerifiedAt(LocalDateTime.ofInstant(user.getEmailVerifiedAt(), ZoneId.systemDefault()));
        }
        if (user.getLastLoginAt() != null) {
            response.setLastLoginAt(LocalDateTime.ofInstant(user.getLastLoginAt(), ZoneId.systemDefault()));
        }
        response.setFailedLoginAttempts(user.getFailedLoginAttempts());
        if (user.getLockedUntil() != null) {
            response.setLockedUntil(LocalDateTime.ofInstant(user.getLockedUntil(), ZoneId.systemDefault()));
        }
        if (user.getPasswordChangedAt() != null) {
            response.setPasswordChangedAt(LocalDateTime.ofInstant(user.getPasswordChangedAt(), ZoneId.systemDefault()));
        }
        if (user.getCreatedAt() != null) {
            response.setCreatedAt(user.getCreatedAt().toLocalDateTime());
        }
        if (user.getUpdatedAt() != null) {
            response.setUpdatedAt(user.getUpdatedAt().toLocalDateTime());
        }

        if (profile != null) {
            AdminUserResponse.AdminUserProfileInfo profileInfo = new AdminUserResponse.AdminUserProfileInfo();
            profileInfo.setFirstName(profile.getFirstName());
            profileInfo.setLastName(profile.getLastName());
            profileInfo.setDisplayName(profile.getDisplayName());
            profileInfo.setAvatarUrl(profile.getAvatarUrl());
            // profileInfo.setPreferredLanguage(profile.getPreferredLanguage()); // If
            // exists
            response.setProfile(profileInfo);
        }
        return response;
    }

    private AdminUserListResponse mapToAdminUserListResponse(User user, UserProfile profile) {
        AdminUserListResponse response = new AdminUserListResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setEmailVerified(user.getEmailVerified());
        if (user.getCreatedAt() != null) {
            response.setCreatedAt(user.getCreatedAt().toLocalDateTime());
        }

        if (profile != null) {
            String first = profile.getFirstName() != null ? profile.getFirstName() : "";
            String last = profile.getLastName() != null ? profile.getLastName() : "";
            response.setFullName((first + " " + last).trim());
            response.setAvatarUrl(profile.getAvatarUrl());
        }
        return response;
    }
}
