package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.OrganizationType;
import com.seffafbagis.api.enums.UserRole;
import com.seffafbagis.api.enums.VerificationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@org.springframework.test.context.TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.generate-ddl=true",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL"
})
class OrganizationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Test
    void findByUserId_ShouldReturnOrganization() {
        // Arrange
        User user = new User("test@example.com", "hash", UserRole.FOUNDATION);
        entityManager.persist(user);

        Organization org = new Organization();
        org.setUser(user);
        org.setLegalName("Test Foundation");
        org.setOrganizationType(OrganizationType.FOUNDATION);
        org.setTaxNumber("1234567890");
        entityManager.persist(org);
        entityManager.flush();

        // Act
        Optional<Organization> found = organizationRepository.findByUserId(user.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getLegalName()).isEqualTo("Test Foundation");
    }

    @Test
    void findByVerificationStatus_ShouldReturnMatchingOrganizations() {
        // Arrange
        User user1 = new User("test1@example.com", "hash", UserRole.FOUNDATION);
        entityManager.persist(user1);

        Organization org1 = new Organization();
        org1.setUser(user1);
        org1.setLegalName("Pending Org");
        org1.setOrganizationType(OrganizationType.FOUNDATION);
        org1.setVerificationStatus(VerificationStatus.PENDING);
        entityManager.persist(org1);

        User user2 = new User("test2@example.com", "hash", UserRole.FOUNDATION);
        entityManager.persist(user2);

        Organization org2 = new Organization();
        org2.setUser(user2);
        org2.setLegalName("Approved Org");
        org2.setOrganizationType(OrganizationType.FOUNDATION);
        org2.setVerificationStatus(VerificationStatus.APPROVED);
        entityManager.persist(org2);

        entityManager.flush();

        // Act
        List<Organization> pendingOrgs = organizationRepository.findByVerificationStatus(VerificationStatus.PENDING);

        // Assert
        assertThat(pendingOrgs).hasSize(1);
        assertThat(pendingOrgs.get(0).getLegalName()).isEqualTo("Pending Org");
    }

    @Test
    void searchByKeyword_ShouldReturnMatchingOrganizations() {
        // Arrange
        User user = new User("test@example.com", "hash", UserRole.FOUNDATION);
        entityManager.persist(user);

        Organization org = new Organization();
        org.setUser(user);
        org.setLegalName("Special Charity Foundation");
        org.setOrganizationType(OrganizationType.FOUNDATION);
        org.setVerificationStatus(VerificationStatus.APPROVED);
        entityManager.persist(org);
        entityManager.flush();

        // Act
        Page<Organization> results = organizationRepository.searchByKeyword(
                "Charity",
                VerificationStatus.APPROVED,
                PageRequest.of(0, 10));

        // Assert
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getLegalName()).isEqualTo("Special Charity Foundation");
    }

    @Test
    void existsByTaxNumber_ShouldReturnTrueIfExists() {
        // Arrange
        User user = new User("test@example.com", "hash", UserRole.FOUNDATION);
        entityManager.persist(user);

        Organization org = new Organization();
        org.setUser(user);
        org.setLegalName("Test Sync");
        org.setOrganizationType(OrganizationType.FOUNDATION);
        org.setTaxNumber("9876543210");
        entityManager.persist(org);
        entityManager.flush();

        // Act
        boolean exists = organizationRepository.existsByTaxNumber("9876543210");

        // Assert
        assertThat(exists).isTrue();
    }
}
