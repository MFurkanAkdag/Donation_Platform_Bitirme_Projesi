package com.seffafbagis.api.service.evidence;

import com.seffafbagis.api.dto.mapper.EvidenceMapper;
import com.seffafbagis.api.dto.request.evidence.CreateEvidenceRequest;
import com.seffafbagis.api.dto.request.evidence.UpdateEvidenceRequest;
import com.seffafbagis.api.dto.response.evidence.EvidenceResponse;
import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.evidence.Evidence;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.CampaignStatus;
import com.seffafbagis.api.enums.EvidenceStatus;
import com.seffafbagis.api.enums.EvidenceType;
import com.seffafbagis.api.exception.BadRequestException;
import com.seffafbagis.api.exception.ForbiddenException;
import com.seffafbagis.api.repository.CampaignRepository;
import com.seffafbagis.api.repository.EvidenceRepository;
import com.seffafbagis.api.service.user.UserService;
import com.seffafbagis.api.security.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvidenceServiceTest {

    @Mock
    private EvidenceRepository evidenceRepository;
    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private EvidenceDocumentService evidenceDocumentService;
    @Mock
    private EvidenceMapper evidenceMapper;
    @Mock
    private UserService userService;

    @InjectMocks
    private EvidenceService evidenceService;

    private MockedStatic<SecurityUtils> securityUtilsMock;
    private User currentUser;
    private Campaign campaign;
    private Organization organization;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);

        currentUser = new User();
        currentUser.setId(UUID.randomUUID());
        currentUser.setEmail("test@foundation.com");

        organization = new Organization();
        organization.setId(UUID.randomUUID());
        organization.setUser(currentUser);

        campaign = new Campaign();
        campaign.setId(UUID.randomUUID());
        campaign.setOrganization(organization);
        campaign.setStatus(CampaignStatus.COMPLETED);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    void uploadEvidence_Success() {
        UUID campaignId = campaign.getId();
        CreateEvidenceRequest request = new CreateEvidenceRequest();
        request.setCampaignId(campaignId);
        request.setEvidenceType(EvidenceType.INVOICE.name());
        request.setTitle("Test Evidence");
        request.setAmountSpent(new BigDecimal("1000"));
        request.setSpendDate(LocalDate.now());
        request.setDocuments(new ArrayList<>()); // Empty list for simplicity

        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(currentUser.getId()));
        when(userService.getUserById(currentUser.getId())).thenReturn(currentUser);
        when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));

        Evidence evidence = new Evidence();
        evidence.setId(UUID.randomUUID());
        evidence.setStatus(EvidenceStatus.PENDING);

        when(evidenceMapper.toEntity(any(), any(), any())).thenReturn(evidence);
        when(evidenceRepository.save(any(Evidence.class))).thenReturn(evidence);

        EvidenceResponse response = new EvidenceResponse();
        response.setId(evidence.getId());
        response.setStatus(EvidenceStatus.PENDING.name());

        when(evidenceMapper.toResponse(evidence)).thenReturn(response);

        EvidenceResponse result = evidenceService.uploadEvidence(request);

        assertNotNull(result);
        assertEquals(EvidenceStatus.PENDING.name(), result.getStatus());
        verify(evidenceRepository).save(any(Evidence.class));
    }

    @Test
    void uploadEvidence_Fail_NotCompleted() {
        campaign.setStatus(CampaignStatus.ACTIVE); // Not COMPLETED

        UUID campaignId = campaign.getId();
        CreateEvidenceRequest request = new CreateEvidenceRequest();
        request.setCampaignId(campaignId);

        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(currentUser.getId()));
        when(userService.getUserById(currentUser.getId())).thenReturn(currentUser);
        when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));

        assertThrows(BadRequestException.class, () -> evidenceService.uploadEvidence(request));
    }

    @Test
    void uploadEvidence_Fail_NotOwner() {
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());

        // Setup campaign belonging to currentUser, but current user is otherUser
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(otherUser.getId()));
        when(userService.getUserById(otherUser.getId())).thenReturn(otherUser);
        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        CreateEvidenceRequest request = new CreateEvidenceRequest();
        request.setCampaignId(campaign.getId());

        assertThrows(ForbiddenException.class, () -> evidenceService.uploadEvidence(request));
    }

    @Test
    void updateEvidence_Fail_ApprovedEvidence() {
        Evidence evidence = new Evidence();
        evidence.setId(UUID.randomUUID());
        evidence.setCampaign(campaign);
        evidence.setUploadedBy(currentUser);
        evidence.setStatus(EvidenceStatus.APPROVED);

        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(currentUser.getId()));
        when(userService.getUserById(currentUser.getId())).thenReturn(currentUser);
        when(evidenceRepository.findById(evidence.getId())).thenReturn(Optional.of(evidence));

        UpdateEvidenceRequest request = new UpdateEvidenceRequest();
        request.setTitle("New Title");

        assertThrows(BadRequestException.class, () -> evidenceService.updateEvidence(evidence.getId(), request));
    }

    @Test
    void isDeadlineMissed_True_WhenPastDeadline() {
        campaign.setCompletedAt(LocalDateTime.now().minusDays(20));
        campaign.setEvidenceDeadlineDays(15);

        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        boolean result = evidenceService.isDeadlineMissed(campaign.getId());

        assertEquals(true, result);
    }

    @Test
    void isDeadlineMissed_False_WhenBeforeDeadline() {
        campaign.setCompletedAt(LocalDateTime.now().minusDays(5));
        campaign.setEvidenceDeadlineDays(15);

        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        boolean result = evidenceService.isDeadlineMissed(campaign.getId());

        assertEquals(false, result);
    }
}
