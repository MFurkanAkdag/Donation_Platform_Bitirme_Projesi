package com.seffafbagis.api.service.campaign;

import com.seffafbagis.api.dto.mapper.CampaignMapper;
import com.seffafbagis.api.dto.request.campaign.CreateCampaignRequest;
import com.seffafbagis.api.dto.response.campaign.CampaignResponse;
import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.enums.CampaignStatus;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.VerificationStatus;
import com.seffafbagis.api.exception.BadRequestException;
import com.seffafbagis.api.exception.ForbiddenException;
import com.seffafbagis.api.repository.*;
import com.seffafbagis.api.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {

    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private CampaignMapper campaignMapper;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private DonationTypeRepository donationTypeRepository;
    @Mock
    private CampaignCategoryRepository campaignCategoryRepository;
    @Mock
    private CampaignDonationTypeRepository campaignDonationTypeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TransparencyScoreRepository transparencyScoreRepository;

    @InjectMocks
    private CampaignService campaignService;

    private User testUser;
    private Organization testOrg;
    private Campaign testCampaign;

    @BeforeEach
    void setUp() {
        testUser = new User();
        ReflectionTestUtils.setField(testUser, "id", UUID.randomUUID());

        testOrg = new Organization();
        ReflectionTestUtils.setField(testOrg, "id", UUID.randomUUID());
        testOrg.setVerificationStatus(VerificationStatus.APPROVED);

        testCampaign = new Campaign();
        ReflectionTestUtils.setField(testCampaign, "id", UUID.randomUUID());
        testCampaign.setTitle("Test Campaign");
        testCampaign.setOrganization(testOrg);
        testCampaign.setStatus(CampaignStatus.DRAFT);
    }

    @Nested
    @DisplayName("CreateCampaign Tests")
    class CreateCampaignTests {

        @Test
        @DisplayName("Should successfully create a campaign")
        void createCampaign_Success() {
            CreateCampaignRequest request = new CreateCampaignRequest();
            request.setTitle("Test Campaign");
            request.setTargetAmount(BigDecimal.TEN);

            Campaign campaign = new Campaign();
            campaign.setTitle("Test Campaign");
            campaign.setStatus(CampaignStatus.DRAFT);

            try (MockedStatic<SecurityUtils> utilities = Mockito.mockStatic(SecurityUtils.class)) {
                utilities.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(testUser.getId()));

                when(organizationRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testOrg));
                when(campaignMapper.toEntity(request)).thenReturn(campaign);
                when(campaignRepository.save(any(Campaign.class))).thenReturn(campaign);
                when(campaignMapper.toResponse(campaign)).thenReturn(new CampaignResponse());
                when(userRepository.getReferenceById(testUser.getId())).thenReturn(testUser);
                when(campaignRepository.findBySlug(anyString())).thenReturn(Optional.empty());

                CampaignResponse response = campaignService.createCampaign(request);

                assertNotNull(response);
                verify(campaignRepository).save(any(Campaign.class));
            }
        }

        @Test
        @DisplayName("Should fail to create campaign with unverified organization")
        void createCampaign_Fail_UnverifiedOrg() {
            testOrg.setVerificationStatus(VerificationStatus.PENDING);
            CreateCampaignRequest request = new CreateCampaignRequest();

            try (MockedStatic<SecurityUtils> utilities = Mockito.mockStatic(SecurityUtils.class)) {
                utilities.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(testUser.getId()));
                when(organizationRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testOrg));

                assertThrows(ForbiddenException.class, () -> campaignService.createCampaign(request));
            }
        }

        @Test
        @DisplayName("Should generate unique slug when duplicate exists")
        void createCampaign_GeneratesUniqueSlug() {
            CreateCampaignRequest request = new CreateCampaignRequest();
            request.setTitle("Test Campaign");
            request.setTargetAmount(BigDecimal.TEN);

            Campaign existingCampaign = new Campaign();
            existingCampaign.setSlug("test-campaign");

            Campaign newCampaign = new Campaign();
            newCampaign.setTitle("Test Campaign");
            newCampaign.setStatus(CampaignStatus.DRAFT);

            try (MockedStatic<SecurityUtils> utilities = Mockito.mockStatic(SecurityUtils.class)) {
                utilities.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(testUser.getId()));

                when(organizationRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testOrg));
                when(campaignMapper.toEntity(request)).thenReturn(newCampaign);
                when(userRepository.getReferenceById(testUser.getId())).thenReturn(testUser);

                // First call returns existing campaign (slug exists), second call returns empty
                // (unique slug found)
                when(campaignRepository.findBySlug("test-campaign")).thenReturn(Optional.of(existingCampaign));
                when(campaignRepository.findBySlug("test-campaign-1")).thenReturn(Optional.empty());
                when(campaignRepository.save(any(Campaign.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(campaignMapper.toResponse(any(Campaign.class))).thenReturn(new CampaignResponse());

                campaignService.createCampaign(request);

                ArgumentCaptor<Campaign> captor = ArgumentCaptor.forClass(Campaign.class);
                verify(campaignRepository).save(captor.capture());
                assertEquals("test-campaign-1", captor.getValue().getSlug());
            }
        }
    }

    @Nested
    @DisplayName("Status Transition Tests")
    class StatusTransitionTests {

        @Test
        @DisplayName("Should submit DRAFT campaign for approval")
        void submitForApproval_Success() {
            testCampaign.setStatus(CampaignStatus.DRAFT);

            try (MockedStatic<SecurityUtils> utilities = Mockito.mockStatic(SecurityUtils.class)) {
                utilities.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(testUser.getId()));

                when(campaignRepository.findById(testCampaign.getId())).thenReturn(Optional.of(testCampaign));
                when(organizationRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testOrg));
                when(campaignRepository.save(any(Campaign.class))).thenReturn(testCampaign);

                campaignService.submitForApproval(testCampaign.getId());

                assertEquals(CampaignStatus.PENDING_APPROVAL, testCampaign.getStatus());
                verify(campaignRepository).save(testCampaign);
            }
        }

        @Test
        @DisplayName("Should fail to submit non-DRAFT campaign")
        void submitForApproval_Fail_NotDraft() {
            testCampaign.setStatus(CampaignStatus.ACTIVE);

            try (MockedStatic<SecurityUtils> utilities = Mockito.mockStatic(SecurityUtils.class)) {
                utilities.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(testUser.getId()));

                when(campaignRepository.findById(testCampaign.getId())).thenReturn(Optional.of(testCampaign));
                when(organizationRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testOrg));

                assertThrows(BadRequestException.class, () -> campaignService.submitForApproval(testCampaign.getId()));
            }
        }

        @Test
        @DisplayName("Should pause ACTIVE campaign")
        void pauseCampaign_Success() {
            testCampaign.setStatus(CampaignStatus.ACTIVE);

            try (MockedStatic<SecurityUtils> utilities = Mockito.mockStatic(SecurityUtils.class)) {
                utilities.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(testUser.getId()));

                when(campaignRepository.findById(testCampaign.getId())).thenReturn(Optional.of(testCampaign));
                when(organizationRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testOrg));
                when(campaignRepository.save(any(Campaign.class))).thenReturn(testCampaign);

                campaignService.pauseCampaign(testCampaign.getId());

                assertEquals(CampaignStatus.PAUSED, testCampaign.getStatus());
            }
        }

        @Test
        @DisplayName("Should resume PAUSED campaign")
        void resumeCampaign_Success() {
            testCampaign.setStatus(CampaignStatus.PAUSED);

            try (MockedStatic<SecurityUtils> utilities = Mockito.mockStatic(SecurityUtils.class)) {
                utilities.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(testUser.getId()));

                when(campaignRepository.findById(testCampaign.getId())).thenReturn(Optional.of(testCampaign));
                when(organizationRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testOrg));
                when(campaignRepository.save(any(Campaign.class))).thenReturn(testCampaign);

                campaignService.resumeCampaign(testCampaign.getId());

                assertEquals(CampaignStatus.ACTIVE, testCampaign.getStatus());
            }
        }

        @Test
        @DisplayName("Should complete ACTIVE campaign")
        void completeCampaign_Success() {
            testCampaign.setStatus(CampaignStatus.ACTIVE);

            try (MockedStatic<SecurityUtils> utilities = Mockito.mockStatic(SecurityUtils.class)) {
                utilities.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(testUser.getId()));

                when(campaignRepository.findById(testCampaign.getId())).thenReturn(Optional.of(testCampaign));
                when(organizationRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testOrg));
                when(campaignRepository.save(any(Campaign.class))).thenReturn(testCampaign);

                campaignService.completeCampaign(testCampaign.getId());

                assertEquals(CampaignStatus.COMPLETED, testCampaign.getStatus());
                assertNotNull(testCampaign.getCompletedAt());
            }
        }
    }

    @Nested
    @DisplayName("Delete Campaign Tests")
    class DeleteCampaignTests {

        @Test
        @DisplayName("Should delete DRAFT campaign")
        void deleteCampaign_Success_Draft() {
            testCampaign.setStatus(CampaignStatus.DRAFT);

            try (MockedStatic<SecurityUtils> utilities = Mockito.mockStatic(SecurityUtils.class)) {
                utilities.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(testUser.getId()));

                when(campaignRepository.findById(testCampaign.getId())).thenReturn(Optional.of(testCampaign));
                when(organizationRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testOrg));

                campaignService.deleteCampaign(testCampaign.getId());

                verify(campaignRepository).delete(eq(testCampaign));
            }
        }

        @Test
        @DisplayName("Should fail to delete non-DRAFT campaign")
        void deleteCampaign_Fail_NonDraft() {
            testCampaign.setStatus(CampaignStatus.ACTIVE);

            try (MockedStatic<SecurityUtils> utilities = Mockito.mockStatic(SecurityUtils.class)) {
                utilities.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(testUser.getId()));

                when(campaignRepository.findById(testCampaign.getId())).thenReturn(Optional.of(testCampaign));
                when(organizationRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testOrg));

                assertThrows(BadRequestException.class, () -> campaignService.deleteCampaign(testCampaign.getId()));
                verify(campaignRepository, never()).delete(any(Campaign.class));
            }
        }

        @Test
        @DisplayName("Should fail to delete COMPLETED campaign")
        void deleteCampaign_Fail_Completed() {
            testCampaign.setStatus(CampaignStatus.COMPLETED);

            try (MockedStatic<SecurityUtils> utilities = Mockito.mockStatic(SecurityUtils.class)) {
                utilities.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(testUser.getId()));

                when(campaignRepository.findById(testCampaign.getId())).thenReturn(Optional.of(testCampaign));
                when(organizationRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testOrg));

                assertThrows(BadRequestException.class, () -> campaignService.deleteCampaign(testCampaign.getId()));
            }
        }
    }

    @Nested
    @DisplayName("Donation Stats Tests")
    class DonationStatsTests {

        @Test
        @DisplayName("Should increment donation stats correctly")
        void incrementDonationStats_Success() {
            testCampaign.setStatus(CampaignStatus.ACTIVE);
            testCampaign.setCollectedAmount(BigDecimal.valueOf(100));
            testCampaign.setDonorCount(5);
            testCampaign.setTargetAmount(BigDecimal.valueOf(1000));

            when(campaignRepository.findById(testCampaign.getId())).thenReturn(Optional.of(testCampaign));
            when(campaignRepository.save(any(Campaign.class))).thenReturn(testCampaign);

            campaignService.incrementDonationStats(testCampaign.getId(), BigDecimal.valueOf(50));

            assertEquals(BigDecimal.valueOf(150), testCampaign.getCollectedAmount());
            assertEquals(6, testCampaign.getDonorCount());
            verify(campaignRepository, atLeast(1)).save(testCampaign);
        }

        @Test
        @DisplayName("Should auto-complete campaign when target is reached")
        void incrementDonationStats_AutoComplete_WhenTargetReached() {
            testCampaign.setStatus(CampaignStatus.ACTIVE);
            testCampaign.setCollectedAmount(BigDecimal.valueOf(900));
            testCampaign.setDonorCount(10);
            testCampaign.setTargetAmount(BigDecimal.valueOf(1000));

            when(campaignRepository.findById(testCampaign.getId())).thenReturn(Optional.of(testCampaign));
            when(campaignRepository.save(any(Campaign.class))).thenReturn(testCampaign);

            campaignService.incrementDonationStats(testCampaign.getId(), BigDecimal.valueOf(200));

            assertEquals(CampaignStatus.COMPLETED, testCampaign.getStatus());
            assertNotNull(testCampaign.getCompletedAt());
        }

        @Test
        @DisplayName("Should initialize stats from null values")
        void incrementDonationStats_Success_FromNull() {
            testCampaign.setStatus(CampaignStatus.ACTIVE);
            testCampaign.setCollectedAmount(null);
            testCampaign.setDonorCount(null);
            testCampaign.setTargetAmount(BigDecimal.valueOf(1000));

            when(campaignRepository.findById(testCampaign.getId())).thenReturn(Optional.of(testCampaign));
            when(campaignRepository.save(any(Campaign.class))).thenReturn(testCampaign);

            campaignService.incrementDonationStats(testCampaign.getId(), BigDecimal.valueOf(50));

            assertEquals(BigDecimal.valueOf(50), testCampaign.getCollectedAmount());
            assertEquals(1, testCampaign.getDonorCount());
        }
    }
}
