package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.UserRole;
import com.seffafbagis.api.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(UserRole role);

    Page<User> findAllByRole(UserRole role, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.status = :status")
    Page<User> findAllByStatus(@Param("status") String status, Pageable pageable);

    Optional<User> findByPhoneNumber(String phoneNumber);

    long countByRole(UserRole role);

    long countByStatus(UserStatus status);

    long countByCreatedAtAfter(OffsetDateTime date);
}
