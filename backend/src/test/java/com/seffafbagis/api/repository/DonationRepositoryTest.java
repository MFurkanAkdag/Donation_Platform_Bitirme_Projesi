package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.donation.Donation;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.CampaignStatus;
import com.seffafbagis.api.enums.DonationStatus;
import com.seffafbagis.api.enums.OrganizationType;
import com.seffafbagis.api.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for Donation entity.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL;INIT=RUNSCRIPT FROM 'classpath:test-h2-init.sql'"
})
class DonationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DonationRepository donationRepository;

    private Campaign campaign;
    private User donor;
    private Organization organization;

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
    @DisplayName("sumAmountByCampaignIdAndStatus should return correct sum for completed donations")
    void sumAmountByCampaignIdAndStatus_ReturnsCorrectSum() {
        // Create donations with COMPLETED status
        createDonation(campaign, donor, new BigDecimal("100.50"), DonationStatus.COMPLETED, false);
        createDonation(campaign, donor, new BigDecimal("250.25"), DonationStatus.COMPLETED, false);
        createDonation(campaign, donor, new BigDecimal("75.00"), DonationStatus.COMPLETED, false);

        // Create a PENDING donation (should not be counted)
        createDonation(campaign, donor, new BigDecimal("500.00"), DonationStatus.PENDING, false);

        entityManager.flush();

        // Test sum calculation
        BigDecimal sum = donationRepository.sumAmountByCampaignIdAndStatus(campaign.getId(), DonationStatus.COMPLETED);

        assertThat(sum).isEqualByComparingTo(new BigDecimal("425.75")); // 100.50 + 250.25 + 75.00
    }

    @Test
    @DisplayName("sumAmountByCampaignIdAndStatus should return zero when no donations")
    void sumAmountByCampaignIdAndStatus_ReturnsZeroWhenNoDonations() {
        BigDecimal sum = donationRepository.sumAmountByCampaignIdAndStatus(campaign.getId(), DonationStatus.COMPLETED);

        assertThat(sum).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("findByCampaignIdAndIsAnonymousFalseAndStatus should return non-anonymous donations")
    void findByCampaignIdAndIsAnonymousFalseAndStatus_ReturnsNonAnonymousDonations() {
        // Create non-anonymous donations
        createDonation(campaign, donor, new BigDecimal("100.00"), DonationStatus.COMPLETED, false);
        createDonation(campaign, donor, new BigDecimal("200.00"), DonationStatus.COMPLETED, false);

        // Create anonymous donations (should be excluded)
        createDonation(campaign, null, new BigDecimal("300.00"), DonationStatus.COMPLETED, true);
        createDonation(campaign, donor, new BigDecimal("400.00"), DonationStatus.COMPLETED, true);

        entityManager.flush();

        Page<Donation> donations = donationRepository.findByCampaignIdAndIsAnonymousFalseAndStatus(
                campaign.getId(), DonationStatus.COMPLETED, PageRequest.of(0, 10));

        assertThat(donations.getContent()).hasSize(2);
        assertThat(donations.getContent()).allMatch(d -> !d.getIsAnonymous());
    }

    @Test
    @DisplayName("Anonymous donations should have null donor and isAnonymous true")
    void anonymousDonations_ShouldHaveNullDonorAndIsAnonymousTrue() {
        Donation anonymousDonation = createDonation(campaign, null, new BigDecimal("100.00"), DonationStatus.COMPLETED,
                true);
        anonymousDonation.setDonorDisplayName("Anonymous Donor");
        entityManager.flush();

        List<Donation> donations = donationRepository.findByCampaignIdAndStatus(campaign.getId(),
                DonationStatus.COMPLETED);

        assertThat(donations).hasSize(1);
        Donation savedDonation = donations.get(0);
        assertThat(savedDonation.getDonor()).isNull();
        assertThat(savedDonation.getIsAnonymous()).isTrue();
        assertThat(savedDonation.getDonorDisplayName()).isEqualTo("Anonymous Donor");
    }

    @Test
    @DisplayName("findTopDonationsByCampaignId should return donations ordered by amount DESC")
    void findTopDonationsByCampaignId_ReturnsOrderedByAmountDesc() {
        createDonation(campaign, donor, new BigDecimal("100.00"), DonationStatus.COMPLETED, false);
        createDonation(campaign, donor, new BigDecimal("500.00"), DonationStatus.COMPLETED, false);
        createDonation(campaign, donor, new BigDecimal("250.00"), DonationStatus.COMPLETED, false);

        entityManager.flush();

        Page<Donation> topDonations = donationRepository.findTopDonationsByCampaignId(
                campaign.getId(), DonationStatus.COMPLETED, PageRequest.of(0, 10));

        assertThat(topDonations.getContent()).hasSize(3);
        assertThat(topDonations.getContent().get(0).getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(topDonations.getContent().get(1).getAmount()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(topDonations.getContent().get(2).getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("countDistinctDonorsByCampaignIdAndStatus should return unique donor count")
    void countDistinctDonorsByCampaignIdAndStatus_ReturnsUniqueDonorCount() {
        // Create another donor
        User donor2 = new User();
        donor2.setEmail("donor2-" + System.currentTimeMillis() + "@test.com");
        donor2.setPasswordHash("hashedpassword");
        donor2.setRole(UserRole.DONOR);
        setAuditFields(donor2);
        donor2 = entityManager.persist(donor2);

        // Multiple donations from same donor
        createDonation(campaign, donor, new BigDecimal("100.00"), DonationStatus.COMPLETED, false);
        createDonation(campaign, donor, new BigDecimal("200.00"), DonationStatus.COMPLETED, false);
        // Single donation from another donor
        createDonation(campaign, donor2, new BigDecimal("300.00"), DonationStatus.COMPLETED, false);
        // Anonymous donation (should not be counted)
        createDonation(campaign, null, new BigDecimal("400.00"), DonationStatus.COMPLETED, true);

        entityManager.flush();

        long distinctDonors = donationRepository.countDistinctDonorsByCampaignIdAndStatus(
                campaign.getId(), DonationStatus.COMPLETED);

        assertThat(distinctDonors).isEqualTo(2); // Only 2 distinct donors
    }

    @Test
    @DisplayName("BigDecimal precision should be maintained for amounts")
    void bigDecimalPrecision_ShouldBeMaintained() {
        BigDecimal preciseAmount = new BigDecimal("12345.67");
        Donation donation = createDonation(campaign, donor, preciseAmount, DonationStatus.COMPLETED, false);
        entityManager.flush();
        entityManager.clear();

        Donation savedDonation = donationRepository.findById(donation.getId()).orElseThrow();
        assertThat(savedDonation.getAmount()).isEqualByComparingTo(preciseAmount);
    }

    private Donation createDonation(Campaign campaign, User donor, BigDecimal amount,
            DonationStatus status, boolean isAnonymous) {
        Donation donation = new Donation();
        donation.setCampaign(campaign);
        donation.setDonor(donor);
        donation.setAmount(amount);
        donation.setStatus(status);
        donation.setIsAnonymous(isAnonymous);
        donation.setCurrency("TRY");
        setAuditFields(donation);
        return entityManager.persist(donation);
    }

    private void setAuditFields(Object entity) {
        OffsetDateTime now = OffsetDateTime.now();
        ReflectionTestUtils.setField(entity, "createdAt", now);
        ReflectionTestUtils.setField(entity, "updatedAt", now);
    }
}
