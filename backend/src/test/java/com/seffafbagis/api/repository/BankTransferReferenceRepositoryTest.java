package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.donation.BankTransferReference;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.organization.OrganizationBankAccount;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.CampaignStatus;
import com.seffafbagis.api.enums.OrganizationType;
import com.seffafbagis.api.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for BankTransferReference entity.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL;INIT=RUNSCRIPT FROM 'classpath:test-h2-init.sql'"
})
class BankTransferReferenceRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BankTransferReferenceRepository bankTransferReferenceRepository;

    private Campaign campaign;
    private User donor;
    private Organization organization;
    private OrganizationBankAccount bankAccount;

    @BeforeEach
    void setUp() {
        // Create user for organization
        User orgOwner = new User();
        orgOwner.setEmail("org-owner-" + System.currentTimeMillis() + "@test.com");
        orgOwner.setPasswordHash("hash");
        orgOwner.setRole(UserRole.FOUNDATION);
        setAuditFields(orgOwner);
        orgOwner = entityManager.persist(orgOwner);

        // Create organization
        organization = new Organization();
        organization.setUser(orgOwner);
        organization.setLegalName("Test Organization");
        organization.setTaxNumber("1234567890");
        organization.setOrganizationType(OrganizationType.ASSOCIATION);
        organization.setDescription("Test Description");
        setAuditFields(organization);
        organization = entityManager.persist(organization);

        // Create bank account
        bankAccount = new OrganizationBankAccount();
        bankAccount.setOrganization(organization);
        bankAccount.setAccountHolder("Test Bank Account"); // Fixed: was setAccountName
        bankAccount.setBankName("Test Bank");
        bankAccount.setIban("TR330006100519786457841326");
        bankAccount.setPrimary(true); // Fixed: was setIsPrimary
        setAuditFields(bankAccount);
        bankAccount = entityManager.persist(bankAccount);

        // Create user (donor)
        donor = new User();
        donor.setEmail("donor-" + System.currentTimeMillis() + "@test.com");
        donor.setPasswordHash("hashedpassword");
        donor.setRole(UserRole.DONOR);
        setAuditFields(donor);
        donor = entityManager.persist(donor);

        // Create campaign
        campaign = new Campaign();
        campaign.setOrganization(organization);
        campaign.setTitle("Test Campaign");
        campaign.setSlug("test-campaign-" + System.currentTimeMillis());
        campaign.setDescription("Test description");
        campaign.setTargetAmount(new BigDecimal("10000"));
        campaign.setStartDate(LocalDateTime.now().minusDays(1));
        campaign.setEndDate(LocalDateTime.now().plusDays(30));
        campaign.setStatus(CampaignStatus.ACTIVE);
        setAuditFields(campaign);
        campaign = entityManager.persist(campaign);

        entityManager.flush();
    }

    @Test
    @DisplayName("findByStatusAndExpiresAtBefore should return expired pending references for cleanup scheduler")
    void findByStatusAndExpiresAtBefore_ReturnsExpiredReferences() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime yesterday = now.minusDays(1);
        OffsetDateTime tomorrow = now.plusDays(1);

        // Create expired pending references
        createBankTransferReference("SBP-REF-001", campaign, yesterday, "pending");
        createBankTransferReference("SBP-REF-002", campaign, yesterday.minusHours(6), "pending");

        // Create not yet expired reference
        createBankTransferReference("SBP-REF-003", campaign, tomorrow, "pending");

        // Create expired but already matched (should be excluded)
        createBankTransferReference("SBP-REF-004", campaign, yesterday, "matched");

        entityManager.flush();

        List<BankTransferReference> expiredRefs = bankTransferReferenceRepository
                .findByStatusAndExpiresAtBefore("pending", now);

        assertThat(expiredRefs).hasSize(2);
        assertThat(expiredRefs).allMatch(ref -> ref.getStatus().equals("pending"));
        assertThat(expiredRefs).allMatch(ref -> ref.getExpiresAt().isBefore(now));
    }

    @Test
    @DisplayName("findByReferenceCodeAndStatus should find matching reference")
    void findByReferenceCodeAndStatus_FindsMatchingReference() {
        createBankTransferReference("SBP-20241215-12345", campaign, OffsetDateTime.now().plusDays(1), "pending");

        entityManager.flush();

        Optional<BankTransferReference> found = bankTransferReferenceRepository
                .findByReferenceCodeAndStatus("SBP-20241215-12345", "pending");

        assertThat(found).isPresent();
        assertThat(found.get().getReferenceCode()).isEqualTo("SBP-20241215-12345");
        assertThat(found.get().getStatus()).isEqualTo("pending");
    }

    @Test
    @DisplayName("existsByReferenceCode should return true for existing code")
    void existsByReferenceCode_ReturnsTrueForExistingCode() {
        createBankTransferReference("SBP-UNIQUE-CODE", campaign, OffsetDateTime.now().plusDays(1), "pending");

        entityManager.flush();

        boolean exists = bankTransferReferenceRepository.existsByReferenceCode("SBP-UNIQUE-CODE");
        boolean notExists = bankTransferReferenceRepository.existsByReferenceCode("SBP-NONEXISTENT");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("JSONB bankAccountSnapshot should serialize and deserialize correctly")
    void jsonbBankAccountSnapshot_ShouldSerializeAndDeserializeCorrectly() {
        BankTransferReference ref = new BankTransferReference();
        ref.setReferenceCode("SBP-JSONB-TEST");
        ref.setCampaign(campaign);
        ref.setBankAccount(bankAccount);
        ref.setExpectedAmount(new BigDecimal("500.00"));
        ref.setExpiresAt(OffsetDateTime.now().plusDays(7));
        ref.setStatus("pending");

        // Create bank account snapshot
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("bankName", "Test Bank");
        snapshot.put("iban", "TR330006100519786457841326");
        snapshot.put("accountName", "Test Bank Account");
        snapshot.put("branchCode", "1234");
        ref.setBankAccountSnapshot(snapshot);

        setAuditFields(ref);
        entityManager.persist(ref);

        entityManager.flush();
        entityManager.clear();

        BankTransferReference savedRef = bankTransferReferenceRepository.findByReferenceCode("SBP-JSONB-TEST")
                .orElseThrow();

        assertThat(savedRef.getBankAccountSnapshot()).isNotNull();
        assertThat(savedRef.getBankAccountSnapshot().get("bankName")).isEqualTo("Test Bank");
        assertThat(savedRef.getBankAccountSnapshot().get("iban")).isEqualTo("TR330006100519786457841326");
        assertThat(savedRef.getBankAccountSnapshot().get("accountName")).isEqualTo("Test Bank Account");
        assertThat(savedRef.getBankAccountSnapshot().get("branchCode")).isEqualTo("1234");
    }

    @Test
    @DisplayName("findByDonorId should return donor's bank transfer references")
    void findByDonorId_ReturnsDonorReferences() {
        BankTransferReference ref = createBankTransferReference("SBP-DONOR-REF", campaign,
                OffsetDateTime.now().plusDays(7), "pending");
        ref.setDonor(donor);
        entityManager.flush();

        List<BankTransferReference> donorRefs = bankTransferReferenceRepository.findByDonorId(donor.getId());

        assertThat(donorRefs).hasSize(1);
        assertThat(donorRefs.get(0).getDonor().getId()).isEqualTo(donor.getId());
    }

    @Test
    @DisplayName("findByCampaignId should return campaign's bank transfer references")
    void findByCampaignId_ReturnsCampaignReferences() {
        createBankTransferReference("SBP-CAMP-001", campaign, OffsetDateTime.now().plusDays(7), "pending");
        createBankTransferReference("SBP-CAMP-002", campaign, OffsetDateTime.now().plusDays(7), "pending");

        entityManager.flush();

        List<BankTransferReference> campaignRefs = bankTransferReferenceRepository.findByCampaignId(campaign.getId());

        assertThat(campaignRefs).hasSize(2);
        assertThat(campaignRefs).allMatch(ref -> ref.getCampaign().getId().equals(campaign.getId()));
    }

    @Test
    @DisplayName("Status values should be stored correctly")
    void statusValues_ShouldBeStoredCorrectly() {
        createBankTransferReference("SBP-PENDING", campaign, OffsetDateTime.now().plusDays(7), "pending");
        createBankTransferReference("SBP-MATCHED", campaign, OffsetDateTime.now().plusDays(7), "matched");
        createBankTransferReference("SBP-EXPIRED", campaign, OffsetDateTime.now().minusDays(1), "expired");

        entityManager.flush();

        List<BankTransferReference> pending = bankTransferReferenceRepository.findByStatus("pending");
        List<BankTransferReference> matched = bankTransferReferenceRepository.findByStatus("matched");
        List<BankTransferReference> expired = bankTransferReferenceRepository.findByStatus("expired");

        assertThat(pending).hasSize(1);
        assertThat(matched).hasSize(1);
        assertThat(expired).hasSize(1);
    }

    private BankTransferReference createBankTransferReference(String referenceCode, Campaign campaign,
            OffsetDateTime expiresAt, String status) {
        BankTransferReference ref = new BankTransferReference();
        ref.setReferenceCode(referenceCode);
        ref.setCampaign(campaign);
        ref.setBankAccount(bankAccount);
        ref.setExpectedAmount(new BigDecimal("100.00"));
        ref.setExpiresAt(expiresAt);
        ref.setStatus(status);
        setAuditFields(ref);
        return entityManager.persist(ref);
    }

    private void setAuditFields(Object entity) {
        OffsetDateTime now = OffsetDateTime.now();
        ReflectionTestUtils.setField(entity, "createdAt", now);
        ReflectionTestUtils.setField(entity, "updatedAt", now);
    }
}
