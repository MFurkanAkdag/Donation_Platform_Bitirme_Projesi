package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.organization.OrganizationContact;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.OrganizationType;
import com.seffafbagis.api.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class OrganizationContactRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrganizationContactRepository contactRepository;

    @Test
    void findByOrganizationId_ShouldReturnContacts() {
        // Arrange
        User user = new User("test@example.com", "hash", UserRole.FOUNDATION);
        entityManager.persist(user);

        Organization org = new Organization();
        org.setUser(user);
        org.setLegalName("Test Org");
        org.setOrganizationType(OrganizationType.FOUNDATION);
        entityManager.persist(org);

        OrganizationContact contact = new OrganizationContact();
        contact.setOrganization(org);
        contact.setContactType("primary");
        contact.setEmail("contact@test.com");
        entityManager.persist(contact);
        entityManager.flush();

        // Act
        List<OrganizationContact> result = contactRepository
                .findByOrganizationIdOrderByIsPrimaryDescCreatedAtAsc(org.getId());

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("contact@test.com");
    }

    @Test
    void findByOrganizationIdAndIsPrimaryTrue_ShouldReturnPrimaryContact() {
        // Arrange
        User user = new User("test@example.com", "hash", UserRole.FOUNDATION);
        entityManager.persist(user);

        Organization org = new Organization();
        org.setUser(user);
        org.setLegalName("Test Org");
        org.setOrganizationType(OrganizationType.FOUNDATION);
        entityManager.persist(org);

        OrganizationContact primary = new OrganizationContact();
        primary.setOrganization(org);
        primary.setContactType("primary");
        primary.setPrimary(true);
        entityManager.persist(primary);

        OrganizationContact secondary = new OrganizationContact();
        secondary.setOrganization(org);
        secondary.setContactType("support");
        secondary.setPrimary(false);
        entityManager.persist(secondary);

        entityManager.flush();

        // Act
        Optional<OrganizationContact> result = contactRepository.findByOrganizationIdAndIsPrimaryTrue(org.getId());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getContactType()).isEqualTo("primary");
    }
}
