package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.user.UserSensitiveData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSensitiveDataRepository extends JpaRepository<UserSensitiveData, UUID> {
    Optional<UserSensitiveData> findByUserId(UUID userId);
}
