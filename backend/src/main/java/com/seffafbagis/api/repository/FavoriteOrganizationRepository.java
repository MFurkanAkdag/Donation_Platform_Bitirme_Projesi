package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.favorite.FavoriteOrganization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FavoriteOrganizationRepository extends JpaRepository<FavoriteOrganization, UUID> {
    List<FavoriteOrganization> findAllByUserId(UUID userId);

    boolean existsByUserIdAndOrganizationId(UUID userId, UUID organizationId);

    Optional<FavoriteOrganization> findByUserIdAndOrganizationId(UUID userId, UUID organizationId);

    long countByOrganizationId(UUID organizationId);
}
