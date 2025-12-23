package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.organization.OrganizationContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for OrganizationContact entity.
 */
@Repository
public interface OrganizationContactRepository extends JpaRepository<OrganizationContact, UUID> {

    List<OrganizationContact> findAllByOrganizationId(UUID organizationId);

    List<OrganizationContact> findByOrganizationIdOrderByIsPrimaryDescCreatedAtAsc(UUID organizationId);

    Optional<OrganizationContact> findByOrganizationIdAndIsPrimaryTrue(UUID organizationId);

    long countByOrganizationId(UUID organizationId);

    void deleteAllByOrganizationId(UUID organizationId);
}
