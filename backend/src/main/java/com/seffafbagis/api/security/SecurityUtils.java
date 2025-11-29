package com.seffafbagis.api.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Güvenlik yardımcı sınıfı.
 * 
 * Bu sınıf, SecurityContext'ten kullanıcı bilgilerine
 * kolay erişim sağlayan statik metodlar içerir.
 * 
 * @author Furkan
 * @version 1.0
 */
@Component
public class SecurityUtils {

    /**
     * Private constructor - utility class olduğu için instance oluşturulamaz.
     */
    private SecurityUtils() {
        // Utility class
    }

    /**
     * Mevcut kullanıcının ID'sini döndürür.
     * 
     * @return Kullanıcı UUID veya null
     */
    public static UUID getCurrentUserId() {
        CustomUserDetails userDetails = getCurrentUserDetails();
        if (userDetails == null) {
            return null;
        }
        return userDetails.getId();
    }

    /**
     * Mevcut kullanıcının e-posta adresini döndürür.
     * 
     * @return E-posta adresi veya null
     */
    public static String getCurrentUserEmail() {
        CustomUserDetails userDetails = getCurrentUserDetails();
        if (userDetails == null) {
            return null;
        }
        return userDetails.getUsername();
    }

    /**
     * Mevcut kullanıcının rolünü döndürür.
     * 
     * @return Rol adı veya null
     */
    public static String getCurrentUserRole() {
        CustomUserDetails userDetails = getCurrentUserDetails();
        if (userDetails == null) {
            return null;
        }
        return userDetails.getRole();
    }

    /**
     * Kullanıcının giriş yapıp yapmadığını kontrol eder.
     * 
     * @return Giriş yapmışsa true
     */
    public static boolean isAuthenticated() {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.isAuthenticated();
    }

    /**
     * Mevcut kullanıcının admin olup olmadığını kontrol eder.
     * 
     * @return Admin ise true
     */
    public static boolean isAdmin() {
        String role = getCurrentUserRole();
        if (role == null) {
            return false;
        }
        return "ADMIN".equals(role);
    }

    /**
     * Mevcut kullanıcının vakıf/dernek olup olmadığını kontrol eder.
     * 
     * @return Foundation ise true
     */
    public static boolean isFoundation() {
        String role = getCurrentUserRole();
        if (role == null) {
            return false;
        }
        return "FOUNDATION".equals(role);
    }

    /**
     * Mevcut kullanıcının bağışçı olup olmadığını kontrol eder.
     * 
     * @return Donor ise true
     */
    public static boolean isDonor() {
        String role = getCurrentUserRole();
        if (role == null) {
            return false;
        }
        return "DONOR".equals(role);
    }

    /**
     * Verilen kullanıcı ID'sinin mevcut kullanıcıya ait olup olmadığını kontrol eder.
     * 
     * @param userId Kontrol edilecek kullanıcı ID
     * @return Aynı kullanıcı ise true
     */
    public static boolean isCurrentUser(UUID userId) {
        UUID currentUserId = getCurrentUserId();
        if (currentUserId == null || userId == null) {
            return false;
        }
        return currentUserId.equals(userId);
    }

    /**
     * Verilen kullanıcı ID'sinin mevcut kullanıcıya ait olduğunu veya
     * mevcut kullanıcının admin olduğunu kontrol eder.
     * 
     * @param userId Kontrol edilecek kullanıcı ID
     * @return Aynı kullanıcı veya admin ise true
     */
    public static boolean isCurrentUserOrAdmin(UUID userId) {
        if (isAdmin()) {
            return true;
        }
        return isCurrentUser(userId);
    }

    /**
     * Mevcut kullanıcının CustomUserDetails nesnesini döndürür.
     * 
     * @return CustomUserDetails veya null
     */
    public static CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal == null) {
            return null;
        }

        // Principal CustomUserDetails mi kontrol et
        if (principal instanceof CustomUserDetails) {
            return (CustomUserDetails) principal;
        }

        return null;
    }

    /**
     * SecurityContext'ten Authentication nesnesini alır.
     * 
     * @return Authentication veya null
     */
    private static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
