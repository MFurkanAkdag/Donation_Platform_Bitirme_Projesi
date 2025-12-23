package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.organization.OrganizationBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for OrganizationBankAccount entity.
 */
@Repository
public interface OrganizationBankAccountRepository extends JpaRepository<OrganizationBankAccount, UUID> {

    List<OrganizationBankAccount> findAllByOrganizationId(UUID organizationId);

    List<OrganizationBankAccount> findByOrganizationIdOrderByIsPrimaryDescCreatedAtAsc(UUID organizationId);

    Optional<OrganizationBankAccount> findByIban(String iban);

    Optional<OrganizationBankAccount> findByOrganizationIdAndIsPrimaryTrue(UUID organizationId);

    boolean existsByIban(String iban);

    boolean existsByIbanAndIdNot(String iban, UUID excludeId);

    long countByOrganizationId(UUID organizationId);
}
