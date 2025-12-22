package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.organization.OrganizationBankAccount;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.OrganizationType;
import com.seffafbagis.api.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class OrganizationBankAccountRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrganizationBankAccountRepository bankAccountRepository;

    @Test
    void findByIban_ShouldReturnAccount() {
        // Arrange
        User user = new User("test@example.com", "hash", UserRole.FOUNDATION);
        entityManager.persist(user);

        Organization org = new Organization();
        org.setUser(user);
        org.setLegalName("Test Org");
        org.setOrganizationType(OrganizationType.FOUNDATION);
        entityManager.persist(org);

        OrganizationBankAccount account = new OrganizationBankAccount();
        account.setOrganization(org);
        account.setBankName("Test Bank");
        account.setAccountHolder("Test Holder");
        account.setIban("TR123456");
        entityManager.persist(account);
        entityManager.flush();

        // Act
        Optional<OrganizationBankAccount> result = bankAccountRepository.findByIban("TR123456");

        // Assert
        assertThat(result).isPresent();
    }

    @Test
    void existsByIban_ShouldReturnTrue_WhenExists() {
        // Arrange
        User user = new User("test@example.com", "hash", UserRole.FOUNDATION);
        entityManager.persist(user);

        Organization org = new Organization();
        org.setUser(user);
        org.setLegalName("Test Org");
        org.setOrganizationType(OrganizationType.FOUNDATION);
        entityManager.persist(org);

        OrganizationBankAccount account = new OrganizationBankAccount();
        account.setOrganization(org);
        account.setBankName("Test Bank");
        account.setAccountHolder("Test Holder");
        account.setIban("TR987654");
        entityManager.persist(account);
        entityManager.flush();

        // Act
        boolean exists = bankAccountRepository.existsByIban("TR987654");

        // Assert
        assertThat(exists).isTrue();
    }
}
