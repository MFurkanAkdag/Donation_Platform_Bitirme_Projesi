package com.seffafbagis.api.service.donation;

import com.seffafbagis.api.dto.mapper.DonationMapper;
import com.seffafbagis.api.dto.request.donation.CreateDonationRequest;
import com.seffafbagis.api.dto.request.donation.RefundRequest;
import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.donation.Donation;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.CampaignStatus;
import com.seffafbagis.api.enums.DonationStatus;
import com.seffafbagis.api.exception.BadRequestException;
import com.seffafbagis.api.repository.CampaignRepository;
import com.seffafbagis.api.repository.DonationRepository;
import com.seffafbagis.api.repository.DonationTypeRepository;
import com.seffafbagis.api.repository.UserRepository;
import com.seffafbagis.api.service.campaign.CampaignService;
import com.seffafbagis.api.service.notification.NotificationService;
import com.seffafbagis.api.service.system.SystemSettingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DonationServiceTest {

    @Mock
    private DonationRepository donationRepository;
    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DonationTypeRepository donationTypeRepository;
    @Mock
    private DonationMapper donationMapper;
    @Mock
    private CampaignService campaignService;
    @Mock
    private com.seffafbagis.api.service.receipt.ReceiptService receiptService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;
    @Mock
    private SystemSettingService systemSettingService;

    @InjectMocks
    private DonationService donationService;

    private Campaign campaign;
    private Donation donation;
    private CreateDonationRequest request;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext(); // Ensure clean state
        campaign = new Campaign();
        campaign.setId(UUID.randomUUID());
        campaign.setStatus(CampaignStatus.ACTIVE);

        request = new CreateDonationRequest();
        request.setCampaignId(campaign.getId());
        request.setAmount(BigDecimal.valueOf(100));
        request.setCurrency("TRY");

        donation = new Donation();
        donation.setId(UUID.randomUUID());
        donation.setCampaign(campaign);
        donation.setAmount(BigDecimal.valueOf(100));
        donation.setStatus(DonationStatus.PENDING);

        // Mock System Settings
        when(systemSettingService.getSettingValueOrDefault(eq("min_donation_amount"), anyString())).thenReturn("10");

        // Mock Receipt Service
        when(receiptService.createReceipt(any())).thenAnswer(invocation -> {
            Donation d = invocation.getArgument(0);
            com.seffafbagis.api.entity.Receipt r = new com.seffafbagis.api.entity.Receipt();
            r.setDonation(d);
            return r;
        });
    }

    @Test
    void createDonation_ShouldCreatePendingDonation() {
        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));
        when(donationMapper.toEntity(any(), any(), any(), any())).thenReturn(donation);
        when(donationRepository.save(any(Donation.class))).thenReturn(donation);

        UUID donationId = donationService.createDonation(request);

        assertNotNull(donationId);
        verify(donationRepository).save(any(Donation.class));
        verify(eventPublisher).publishEvent(any(com.seffafbagis.api.event.DonationCreatedEvent.class));
    }

    @Test
    void createDonation_ShouldThrowException_WhenCampaignNotActive() {
        campaign.setStatus(CampaignStatus.COMPLETED);
        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        assertThrows(BadRequestException.class, () -> donationService.createDonation(request));
    }

    @Test
    void createDonation_ShouldThrowException_WhenAmountBelowMinimum() {
        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));
        request.setAmount(BigDecimal.ONE); // Below 10

        assertThrows(BadRequestException.class, () -> donationService.createDonation(request));
    }

    @Test
    void completeDonation_ShouldUpdateStatsAndNotify() {
        when(donationRepository.findById(donation.getId())).thenReturn(Optional.of(donation));

        // Mock organization user for notification
        User orgUser = new User();
        orgUser.setId(UUID.randomUUID());
        com.seffafbagis.api.entity.organization.Organization org = new com.seffafbagis.api.entity.organization.Organization();
        org.setUser(orgUser);
        org.setLegalName("Test Org"); // Ensure legal name is set if accessed
        campaign.setOrganization(org);
        campaign.setTitle("Test Campaign");

        donationService.completeDonation(donation.getId());

        assertEquals(DonationStatus.COMPLETED, donation.getStatus());
        verify(campaignService).incrementDonationStats(campaign.getId(), donation.getAmount());
        verify(receiptService).createReceipt(donation);
        verify(eventPublisher).publishEvent(any(com.seffafbagis.api.event.DonationCompletedEvent.class));
    }

    // Helper to mock security context
    private void mockSecurityContext(UUID userId) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        com.seffafbagis.api.security.CustomUserDetails userDetails = new com.seffafbagis.api.security.CustomUserDetails(
                userId, "test@example.com", "pass", com.seffafbagis.api.enums.UserRole.DONOR,
                com.seffafbagis.api.enums.UserStatus.ACTIVE, true);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void requestRefund_ShouldThrowException_WhenOutside14DayWindow() {
        // Setup a completed donation older than 14 days
        User donorUser = new User();
        donorUser.setId(UUID.randomUUID());

        Donation oldDonation = new Donation();
        oldDonation.setId(UUID.randomUUID());
        oldDonation.setCampaign(campaign);
        oldDonation.setAmount(BigDecimal.valueOf(100));
        oldDonation.setStatus(DonationStatus.COMPLETED);
        oldDonation.setDonor(donorUser);
        oldDonation.setRefundStatus("none");

        // Set createdAt to 15 days ago
        org.springframework.test.util.ReflectionTestUtils.setField(oldDonation, "createdAt",
                OffsetDateTime.now().minusDays(15));

        when(donationRepository.findById(oldDonation.getId())).thenReturn(Optional.of(oldDonation));
        mockSecurityContext(donorUser.getId());

        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setDonationId(oldDonation.getId());
        refundRequest.setReason("Test refund reason");

        assertThrows(BadRequestException.class, () -> donationService.requestRefund(refundRequest));
    }

    @Test
    void createDonation_ShouldHaveNullDonorId_WhenAnonymousOrNotLoggedIn() {
        // Test that donation without authenticated user has null donor_id
        request.setIsAnonymous(true);

        Donation anonymousDonation = new Donation();
        anonymousDonation.setId(UUID.randomUUID());
        anonymousDonation.setCampaign(campaign);
        anonymousDonation.setAmount(request.getAmount());
        anonymousDonation.setDonor(null); // No donor linked
        anonymousDonation.setIsAnonymous(true);

        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));
        when(donationMapper.toEntity(any(), any(), isNull(), any())).thenReturn(anonymousDonation);
        when(donationRepository.save(any(Donation.class))).thenReturn(anonymousDonation);

        UUID donationId = donationService.createDonation(request);

        assertNotNull(donationId);
        assertNull(anonymousDonation.getDonor()); // Verify donor is null
        verify(donationMapper).toEntity(any(), any(), isNull(), any()); // Verify mapper called with null donor
    }
}
