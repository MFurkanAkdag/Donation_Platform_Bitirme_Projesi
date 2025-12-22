package com.seffafbagis.api.service.donation;

import com.seffafbagis.api.dto.request.donation.CreateRecurringDonationRequest;
import com.seffafbagis.api.dto.request.donation.UpdateRecurringDonationRequest;
import com.seffafbagis.api.dto.response.donation.RecurringDonationResponse;
import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.donation.RecurringDonation;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.repository.CampaignRepository;
import com.seffafbagis.api.repository.DonationTypeRepository;
import com.seffafbagis.api.repository.OrganizationRepository;
import com.seffafbagis.api.repository.RecurringDonationRepository;
import com.seffafbagis.api.repository.UserRepository;
import com.seffafbagis.api.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecurringDonationServiceTest {

    @Mock
    private RecurringDonationRepository recurringDonationRepository;
    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private DonationTypeRepository donationTypeRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RecurringDonationService recurringDonationService;

    private User user;
    private Campaign campaign;
    private RecurringDonation recurringDonation;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());

        campaign = new Campaign();
        campaign.setId(UUID.randomUUID());
        campaign.setOrganization(new Organization());

        recurringDonation = new RecurringDonation();
        recurringDonation.setId(UUID.randomUUID());
        recurringDonation.setDonor(user);
        recurringDonation.setCampaign(campaign);
        recurringDonation.setAmount(BigDecimal.valueOf(100));
        recurringDonation.setFrequency("monthly");
        recurringDonation.setStatus("active");
        recurringDonation.setNextPaymentDate(LocalDate.now().plusMonths(1));
    }

    @Test
    void createRecurringDonation_ShouldCreateActiveSubscription() {
        CreateRecurringDonationRequest request = CreateRecurringDonationRequest.builder()
                .campaignId(campaign.getId())
                .amount(BigDecimal.valueOf(100))
                .frequency("monthly")
                .currency("TRY")
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(user.getId()));
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));
            when(recurringDonationRepository.save(any(RecurringDonation.class))).thenAnswer(i -> {
                RecurringDonation d = i.getArgument(0);
                d.setId(UUID.randomUUID());
                ReflectionTestUtils.setField(d, "createdAt", java.time.OffsetDateTime.now());
                return d;
            });

            RecurringDonationResponse response = recurringDonationService.createRecurringDonation(request);

            assertNotNull(response);
            assertEquals("active", response.getStatus());
            assertEquals(LocalDate.now().plusMonths(1), response.getNextPaymentDate());
            verify(recurringDonationRepository).save(any(RecurringDonation.class));
        }
    }

    @Test
    void pauseRecurringDonation_ShouldUpdateStatus() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(user.getId()));
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(recurringDonationRepository.findById(recurringDonation.getId()))
                    .thenReturn(Optional.of(recurringDonation));

            recurringDonationService.pauseRecurringDonation(recurringDonation.getId());

            assertEquals("paused", recurringDonation.getStatus());
            verify(recurringDonationRepository).save(recurringDonation);
        }
    }

    @Test
    void resumeRecurringDonation_ShouldRecalculateDate() {
        recurringDonation.setStatus("paused");
        // Set old date
        recurringDonation.setNextPaymentDate(LocalDate.now().minusDays(10));

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(user.getId()));
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(recurringDonationRepository.findById(recurringDonation.getId()))
                    .thenReturn(Optional.of(recurringDonation));

            recurringDonationService.resumeRecurringDonation(recurringDonation.getId());

            assertEquals("active", recurringDonation.getStatus());
            // Should be calculated from today
            assertEquals(LocalDate.now().plusMonths(1), recurringDonation.getNextPaymentDate());
            verify(recurringDonationRepository).save(recurringDonation);
        }
    }

    @Test
    void handlePaymentFailure_ShouldPauseAfter3Failures() {
        recurringDonation.setFailureCount(2);
        when(recurringDonationRepository.findById(recurringDonation.getId()))
                .thenReturn(Optional.of(recurringDonation));

        recurringDonationService.handlePaymentFailure(recurringDonation.getId(), "Error");

        assertEquals(3, recurringDonation.getFailureCount());
        assertEquals("paused", recurringDonation.getStatus());
        verify(recurringDonationRepository).save(recurringDonation);
    }

    @Test
    void handlePaymentSuccess_ShouldResetFailuresAndAdvanceDate() {
        recurringDonation.setFailureCount(2);
        recurringDonation.setTotalDonated(BigDecimal.ZERO);
        recurringDonation.setPaymentCount(0);

        when(recurringDonationRepository.findById(recurringDonation.getId()))
                .thenReturn(Optional.of(recurringDonation));

        recurringDonationService.handlePaymentSuccess(recurringDonation.getId(), BigDecimal.valueOf(100));

        assertEquals(0, recurringDonation.getFailureCount());
        assertEquals(1, recurringDonation.getPaymentCount());
        assertEquals(BigDecimal.valueOf(100), recurringDonation.getTotalDonated());
        assertEquals(LocalDate.now().plusMonths(1), recurringDonation.getNextPaymentDate()); // Calculated from today
        verify(recurringDonationRepository).save(recurringDonation);
    }

    @Test
    void nextPaymentDate_ShouldCalculateCorrectly_ForWeeklyFrequency() {
        CreateRecurringDonationRequest request = CreateRecurringDonationRequest.builder()
                .campaignId(campaign.getId())
                .amount(BigDecimal.valueOf(50))
                .frequency("weekly")
                .currency("TRY")
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(user.getId()));
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));
            when(recurringDonationRepository.save(any(RecurringDonation.class))).thenAnswer(i -> {
                RecurringDonation d = i.getArgument(0);
                d.setId(UUID.randomUUID());
                ReflectionTestUtils.setField(d, "createdAt", java.time.OffsetDateTime.now());
                return d;
            });

            RecurringDonationResponse response = recurringDonationService.createRecurringDonation(request);

            assertEquals(LocalDate.now().plusWeeks(1), response.getNextPaymentDate());
        }
    }

    @Test
    void nextPaymentDate_ShouldCalculateCorrectly_ForYearlyFrequency() {
        CreateRecurringDonationRequest request = CreateRecurringDonationRequest.builder()
                .campaignId(campaign.getId())
                .amount(BigDecimal.valueOf(1000))
                .frequency("yearly")
                .currency("TRY")
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(user.getId()));
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));
            when(recurringDonationRepository.save(any(RecurringDonation.class))).thenAnswer(i -> {
                RecurringDonation d = i.getArgument(0);
                d.setId(UUID.randomUUID());
                ReflectionTestUtils.setField(d, "createdAt", java.time.OffsetDateTime.now());
                return d;
            });

            RecurringDonationResponse response = recurringDonationService.createRecurringDonation(request);

            assertEquals(LocalDate.now().plusYears(1), response.getNextPaymentDate());
        }
    }
}
