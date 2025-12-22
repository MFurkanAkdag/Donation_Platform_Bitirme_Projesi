package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.organization.OrganizationDocument;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.OrganizationType;
import com.seffafbagis.api.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class OrganizationDocumentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrganizationDocumentRepository documentRepository;

    @Test
    void findByOrganizationIdAndDocumentType_ShouldReturnCorrectDocuments() {
        // Arrange
        User user = new User("test@example.com", "hash", UserRole.FOUNDATION);
        entityManager.persist(user);

        Organization org = new Organization();
        org.setUser(user);
        org.setLegalName("Test Org");
        org.setOrganizationType(OrganizationType.FOUNDATION);
        entityManager.persist(org);

        OrganizationDocument doc = new OrganizationDocument();
        doc.setOrganization(org);
        doc.setDocumentType("tax_certificate");
        doc.setDocumentName("Tax.pdf");
        doc.setFileUrl("http://url");
        entityManager.persist(doc);
        entityManager.flush();

        // Act
        List<OrganizationDocument> result = documentRepository.findByOrganizationIdAndDocumentType(org.getId(),
                "tax_certificate");

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void findExpiringDocuments_ShouldReturnExpiringVerifiedDocuments() {
        // Arrange
        User user = new User("test@example.com", "hash", UserRole.FOUNDATION);
        entityManager.persist(user);

        Organization org = new Organization();
        org.setUser(user);
        org.setLegalName("Test Org");
        org.setOrganizationType(OrganizationType.FOUNDATION);
        entityManager.persist(org);

        OrganizationDocument doc1 = new OrganizationDocument();
        doc1.setOrganization(org);
        doc1.setDocumentType("auth");
        doc1.setDocumentName("Expiring.pdf");
        doc1.setFileUrl("url");
        doc1.setVerified(true);
        doc1.setExpiresAt(LocalDate.now().plusDays(5));
        entityManager.persist(doc1);

        OrganizationDocument doc2 = new OrganizationDocument();
        doc2.setOrganization(org);
        doc2.setDocumentType("auth");
        doc2.setDocumentName("Valid.pdf");
        doc2.setFileUrl("url");
        doc2.setVerified(true);
        doc2.setExpiresAt(LocalDate.now().plusDays(20));
        entityManager.persist(doc2);

        entityManager.flush();

        // Act
        List<OrganizationDocument> result = documentRepository.findExpiringDocuments(LocalDate.now().plusDays(10));

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDocumentName()).isEqualTo("Expiring.pdf");
    }
}
