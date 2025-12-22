package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.donation.RecurringDonation;
import com.seffafbagis.api.entity.organization.Organization;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for RecurringDonation entity.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
                "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL;INIT=RUNSCRIPT FROM 'classpath:test-h2-init.sql'"
})
class RecurringDonationRepositoryTest {

        @Autowired
        private TestEntityManager entityManager;

        @Autowired
        private RecurringDonationRepository recurringDonationRepository;

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
        @DisplayName("findByStatusAndNextPaymentDateLessThanEqual should return due recurring donations for scheduler")
        void findByStatusAndNextPaymentDateLessThanEqual_ReturnsDueDonations() {
                LocalDate today = LocalDate.now();
                LocalDate yesterday = today.minusDays(1);
                LocalDate tomorrow = today.plusDays(1);

                // Create recurring donations with different next payment dates
                createRecurringDonation(donor, campaign, new BigDecimal("100.00"), "monthly", yesterday, "active");
                createRecurringDonation(donor, campaign, new BigDecimal("200.00"), "monthly", today, "active");
                createRecurringDonation(donor, campaign, new BigDecimal("300.00"), "monthly", tomorrow, "active"); // Not
                                                                                                                   // due
                                                                                                                   // yet
                createRecurringDonation(donor, campaign, new BigDecimal("400.00"), "monthly", yesterday, "paused"); // Paused

                entityManager.flush();

                List<RecurringDonation> dueDonations = recurringDonationRepository
                                .findByStatusAndNextPaymentDateLessThanEqual("active", today);

                assertThat(dueDonations).hasSize(2);
                assertThat(dueDonations).allMatch(rd -> rd.getStatus().equals("active"));
                assertThat(dueDonations).allMatch(rd -> rd.getNextPaymentDate().isBefore(today.plusDays(1)));
        }

        @Test
        @DisplayName("findByDonorIdAndStatus should return donor's active recurring donations")
        void findByDonorIdAndStatus_ReturnsDonorActiveRecurringDonations() {
                createRecurringDonation(donor, campaign, new BigDecimal("100.00"), "monthly",
                                LocalDate.now().plusDays(15), "active");
                createRecurringDonation(donor, campaign, new BigDecimal("200.00"), "weekly",
                                LocalDate.now().plusDays(7), "active");
                createRecurringDonation(donor, campaign, new BigDecimal("300.00"), "yearly",
                                LocalDate.now().plusMonths(6), "cancelled");

                entityManager.flush();

                List<RecurringDonation> activeDonations = recurringDonationRepository
                                .findByDonorIdAndStatus(donor.getId(), "active");

                assertThat(activeDonations).hasSize(2);
                assertThat(activeDonations).allMatch(rd -> rd.getStatus().equals("active"));
        }

        @Test
        @DisplayName("countByDonorIdAndStatus should return correct count")
        void countByDonorIdAndStatus_ReturnsCorrectCount() {
                createRecurringDonation(donor, campaign, new BigDecimal("100.00"), "monthly",
                                LocalDate.now().plusDays(15), "active");
                createRecurringDonation(donor, campaign, new BigDecimal("200.00"), "weekly",
                                LocalDate.now().plusDays(7), "active");
                createRecurringDonation(donor, campaign, new BigDecimal("300.00"), "yearly",
                                LocalDate.now().plusMonths(6), "paused");

                entityManager.flush();

                long activeCount = recurringDonationRepository.countByDonorIdAndStatus(donor.getId(), "active");
                long pausedCount = recurringDonationRepository.countByDonorIdAndStatus(donor.getId(), "paused");

                assertThat(activeCount).isEqualTo(2);
                assertThat(pausedCount).isEqualTo(1);
        }

        @Test
        @DisplayName("Frequency values should be stored correctly")
        void frequencyValues_ShouldBeStoredCorrectly() {
                createRecurringDonation(donor, campaign, new BigDecimal("100.00"), "weekly", LocalDate.now(), "active");
                createRecurringDonation(donor, campaign, new BigDecimal("200.00"), "monthly", LocalDate.now(),
                                "active");
                createRecurringDonation(donor, campaign, new BigDecimal("300.00"), "yearly", LocalDate.now(), "active");

                entityManager.flush();

                List<RecurringDonation> allDonations = recurringDonationRepository.findByDonorId(donor.getId());

                assertThat(allDonations).hasSize(3);
                assertThat(allDonations.stream().map(RecurringDonation::getFrequency))
                                .containsExactlyInAnyOrder("weekly", "monthly", "yearly");
        }

        @Test
        @DisplayName("totalDonated and paymentCount should update correctly")
        void totalDonatedAndPaymentCount_ShouldUpdateCorrectly() {
                RecurringDonation recurring = createRecurringDonation(
                                donor, campaign, new BigDecimal("100.00"), "monthly", LocalDate.now(), "active");

                // Simulate payment processing
                recurring.setTotalDonated(new BigDecimal("300.00")); // 3 payments
                recurring.setPaymentCount(3);
                recurring.setLastPaymentDate(LocalDate.now());
                recurring.setNextPaymentDate(LocalDate.now().plusMonths(1));

                entityManager.flush();
                entityManager.clear();

                RecurringDonation savedRecurring = recurringDonationRepository.findById(recurring.getId())
                                .orElseThrow();

                assertThat(savedRecurring.getTotalDonated()).isEqualByComparingTo(new BigDecimal("300.00"));
                assertThat(savedRecurring.getPaymentCount()).isEqualTo(3);
                assertThat(savedRecurring.getLastPaymentDate()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("findByCampaignId should return recurring donations for campaign")
        void findByCampaignId_ReturnsCampaignRecurringDonations() {
                createRecurringDonation(donor, campaign, new BigDecimal("100.00"), "monthly", LocalDate.now(),
                                "active");
                createRecurringDonation(donor, campaign, new BigDecimal("200.00"), "monthly", LocalDate.now(),
                                "active");

                entityManager.flush();

                List<RecurringDonation> campaignRecurring = recurringDonationRepository
                                .findByCampaignId(campaign.getId());

                assertThat(campaignRecurring).hasSize(2);
                assertThat(campaignRecurring).allMatch(rd -> rd.getCampaign().getId().equals(campaign.getId()));
        }

        @Test
        @DisplayName("findByOrganizationId should return recurring donations for organization")
        void findByOrganizationId_ReturnsOrganizationRecurringDonations() {
                RecurringDonation recurring = new RecurringDonation();
                recurring.setDonor(donor);
                recurring.setOrganization(organization); // Donate to organization, not campaign
                recurring.setAmount(new BigDecimal("150.00"));
                recurring.setFrequency("monthly");
                recurring.setNextPaymentDate(LocalDate.now().plusMonths(1));
                recurring.setStatus("active");
                recurring.setCurrency("TRY");
                setAuditFields(recurring);
                entityManager.persist(recurring);

                entityManager.flush();

                List<RecurringDonation> orgRecurring = recurringDonationRepository
                                .findByOrganizationId(organization.getId());

                assertThat(orgRecurring).hasSize(1);
                assertThat(orgRecurring.get(0).getOrganization().getId()).isEqualTo(organization.getId());
        }

        private RecurringDonation createRecurringDonation(User donor, Campaign campaign, BigDecimal amount,
                        String frequency, LocalDate nextPaymentDate,
                        String status) {
                RecurringDonation recurring = new RecurringDonation();
                recurring.setDonor(donor);
                recurring.setCampaign(campaign);
                recurring.setAmount(amount);
                recurring.setFrequency(frequency);
                recurring.setNextPaymentDate(nextPaymentDate);
                recurring.setStatus(status);
                recurring.setCurrency("TRY");
                setAuditFields(recurring);
                return entityManager.persist(recurring);
        }

        private void setAuditFields(Object entity) {
                OffsetDateTime now = OffsetDateTime.now();
                ReflectionTestUtils.setField(entity, "createdAt", now);
                ReflectionTestUtils.setField(entity, "updatedAt", now);
        }
}
