package com.seffafbagis.api.security;

import com.seffafbagis.api.enums.UserRole;
import com.seffafbagis.api.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

/**
 * Convenience helpers for accessing the authenticated user.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<CustomUserDetails> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails customUserDetails) {
            return Optional.of(customUserDetails);
        }
        return Optional.empty();
    }

    public static Optional<UUID> getCurrentUserId() {
        return getCurrentUser().map(CustomUserDetails::getId);
    }

    public static Optional<String> getCurrentUserEmail() {
        return getCurrentUser().map(CustomUserDetails::getUsername);
    }

    public static Optional<UserRole> getCurrentUserRole() {
        return getCurrentUser().map(CustomUserDetails::getRole);
    }

    public static CustomUserDetails getCurrentUserOrThrow() {
        return getCurrentUser().orElseThrow(() -> new UnauthorizedException("Authentication required"));
    }

    public static boolean isAuthenticated() {
        return getCurrentUser().isPresent();
    }

    public static boolean hasRole(UserRole role) {
        return getCurrentUserRole().map(role::equals).orElse(false);
    }

    public static boolean isAdmin() {
        return hasRole(UserRole.ADMIN);
    }

    public static boolean isFoundation() {
        return hasRole(UserRole.FOUNDATION);
    }

    public static boolean isDonor() {
        return hasRole(UserRole.DONOR);
    }

    public static boolean isCurrentUser(UUID userId) {
        return getCurrentUserId().map(id -> id.equals(userId)).orElse(false);
    }
}
