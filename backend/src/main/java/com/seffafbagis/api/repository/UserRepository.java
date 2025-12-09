package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.UserRole;
import com.seffafbagis.api.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * User entity için Repository interface.
 * 
 * JpaRepository'den extend ederek temel CRUD işlemleri otomatik sağlanır.
 * Özel sorgular için method isimlendirme kuralları veya @Query kullanılır.
 * 
 * Method isimlendirme örnekleri:
 * - findByEmail -> SELECT * FROM users WHERE email = ?
 * - findByRoleAndStatus -> SELECT * FROM users WHERE role = ? AND status = ?
 * - existsByEmail -> SELECT COUNT(*) > 0 FROM users WHERE email = ?
 * 
 * @author Furkan
 * @version 1.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    // ==================== TEMEL SORGULAR ====================

    /**
     * E-posta adresine göre kullanıcı bulur.
     * 
     * @param email E-posta adresi
     * @return User veya boş Optional
     */
    Optional<User> findByEmail(String email);

    /**
     * E-posta adresinin kayıtlı olup olmadığını kontrol eder.
     * 
     * @param email E-posta adresi
     * @return Kayıtlı ise true
     */
    boolean existsByEmail(String email);

    /**
     * Belirli bir role sahip kullanıcıları bulur.
     * 
     * @param role Kullanıcı rolü
     * @return Kullanıcı listesi
     */
    List<User> findByRole(UserRole role);

    /**
     * Belirli bir duruma sahip kullanıcıları bulur.
     * 
     * @param status Kullanıcı durumu
     * @return Kullanıcı listesi
     */
    List<User> findByStatus(UserStatus status);

    /**
     * Rol ve duruma göre kullanıcıları bulur.
     * 
     * @param role   Kullanıcı rolü
     * @param status Kullanıcı durumu
     * @return Kullanıcı listesi
     */
    List<User> findByRoleAndStatus(UserRole role, UserStatus status);

    // ==================== SAYFALAMA İLE SORGULAR ====================

    /**
     * Belirli bir role sahip kullanıcıları sayfalama ile bulur.
     * 
     * @param role     Kullanıcı rolü
     * @param pageable Sayfalama bilgisi
     * @return Sayfalanmış kullanıcı listesi
     */
    Page<User> findByRole(UserRole role, Pageable pageable);

    /**
     * Belirli bir duruma sahip kullanıcıları sayfalama ile bulur.
     * 
     * @param status   Kullanıcı durumu
     * @param pageable Sayfalama bilgisi
     * @return Sayfalanmış kullanıcı listesi
     */
    Page<User> findByStatus(UserStatus status, Pageable pageable);

    /**
     * Rol ve duruma göre kullanıcıları sayfalama ile bulur.
     * 
     * @param role     Kullanıcı rolü
     * @param status   Kullanıcı durumu
     * @param pageable Sayfalama bilgisi
     * @return Sayfalanmış kullanıcı listesi
     */
    Page<User> findByRoleAndStatus(UserRole role, UserStatus status, Pageable pageable);

    // ==================== E-POSTA DOĞRULAMA ====================

    /**
     * E-postası doğrulanmamış kullanıcıları bulur.
     * 
     * @return Kullanıcı listesi
     */
    List<User> findByEmailVerifiedFalse();

    /**
     * E-postası doğrulanmış kullanıcıları bulur.
     * 
     * @return Kullanıcı listesi
     */
    List<User> findByEmailVerifiedTrue();

    // ==================== TARİH BAZLI SORGULAR ====================

    /**
     * Belirli bir tarihten sonra kayıt olan kullanıcıları bulur.
     * 
     * @param date Başlangıç tarihi
     * @return Kullanıcı listesi
     */
    List<User> findByCreatedAtAfter(OffsetDateTime date);

    /**
     * Belirli bir tarih aralığında kayıt olan kullanıcıları bulur.
     * 
     * @param startDate Başlangıç tarihi
     * @param endDate   Bitiş tarihi
     * @return Kullanıcı listesi
     */
    List<User> findByCreatedAtBetween(OffsetDateTime startDate, OffsetDateTime endDate);

    /**
     * Belirli bir tarihten sonra giriş yapan kullanıcıları bulur.
     * 
     * @param date Başlangıç tarihi
     * @return Kullanıcı listesi
     */
    List<User> findByLastLoginAtAfter(Instant date);

    /**
     * Hiç giriş yapmamış kullanıcıları bulur.
     * 
     * @return Kullanıcı listesi
     */
    List<User> findByLastLoginAtIsNull();

    // ==================== SAYIM SORGULARI ====================

    /**
     * Belirli bir role sahip kullanıcı sayısını döndürür.
     * 
     * @param role Kullanıcı rolü
     * @return Kullanıcı sayısı
     */
    long countByRole(UserRole role);

    /**
     * Belirli bir duruma sahip kullanıcı sayısını döndürür.
     * 
     * @param status Kullanıcı durumu
     * @return Kullanıcı sayısı
     */
    long countByStatus(UserStatus status);

    /**
     * E-postası doğrulanmış kullanıcı sayısını döndürür.
     * 
     * @return Kullanıcı sayısı
     */
    long countByEmailVerifiedTrue();

    /**
     * Belirli bir tarihten sonra kayıt olan kullanıcı sayısını döndürür.
     * 
     * @param date Başlangıç tarihi
     * @return Kullanıcı sayısı
     */
    long countByCreatedAtAfter(OffsetDateTime date);

    // ==================== ÖZEL SORGULAR (JPQL) ====================

    /**
     * E-posta adresine göre kullanıcı bulur (case-insensitive).
     * 
     * @param email E-posta adresi
     * @return User veya boş Optional
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<User> findByEmailIgnoreCase(@Param("email") String email);

    /**
     * Aktif ve e-postası doğrulanmış kullanıcıları bulur.
     * 
     * @param pageable Sayfalama bilgisi
     * @return Sayfalanmış kullanıcı listesi
     */
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' AND u.emailVerified = true")
    Page<User> findActiveAndVerifiedUsers(Pageable pageable);

    /**
     * Arama terimine göre kullanıcı arar.
     * E-posta adresinde arama yapar.
     * 
     * @param searchTerm Arama terimi
     * @param pageable   Sayfalama bilgisi
     * @return Sayfalanmış kullanıcı listesi
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchByEmail(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Belirli bir tarihten önce giriş yapmayan aktif kullanıcıları bulur.
     * Pasif kullanıcı tespiti için kullanılabilir.
     * 
     * @param date Tarih
     * @return Kullanıcı listesi
     */
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' AND (u.lastLoginAt IS NULL OR u.lastLoginAt < :date)")
    List<User> findInactiveUsers(@Param("date") Instant date);

    // ==================== GÜNCELLEME SORGULARI ====================

    /**
     * Kullanıcının son giriş tarihini günceller.
     * 
     * @param userId    Kullanıcı ID
     * @param loginTime Giriş zamanı
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.id = :userId")
    void updateLastLoginAt(@Param("userId") UUID userId, @Param("loginTime") Instant loginTime);

    /**
     * Kullanıcının durumunu günceller.
     * 
     * @param userId Kullanıcı ID
     * @param status Yeni durum
     */
    @Modifying
    @Query("UPDATE User u SET u.status = :status WHERE u.id = :userId")
    void updateStatus(@Param("userId") UUID userId, @Param("status") UserStatus status);

    /**
     * Kullanıcının e-posta doğrulama durumunu günceller.
     * 
     * @param userId     Kullanıcı ID
     * @param verified   Doğrulama durumu
     * @param verifiedAt Doğrulama zamanı
     */
    @Modifying
    @Query("UPDATE User u SET u.emailVerified = :verified, u.emailVerifiedAt = :verifiedAt WHERE u.id = :userId")
    void updateEmailVerification(
            @Param("userId") UUID userId,
            @Param("verified") boolean verified,
            @Param("verifiedAt") Instant verifiedAt);

    /**
     * Kullanıcının şifresini günceller.
     * 
     * @param userId       Kullanıcı ID
     * @param passwordHash Yeni şifre hash'i
     */
    @Modifying
    @Query("UPDATE User u SET u.passwordHash = :passwordHash WHERE u.id = :userId")
    void updatePassword(@Param("userId") UUID userId, @Param("passwordHash") String passwordHash);
}
