package com.seffafbagis.api.service.transparency;

import com.seffafbagis.api.dto.response.transparency.TransparencyScoreResponse;
import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.evidence.Evidence;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.transparency.TransparencyScore;
import com.seffafbagis.api.entity.transparency.TransparencyScoreHistory;
import com.seffafbagis.api.repository.CampaignRepository;
import com.seffafbagis.api.repository.EvidenceRepository;
import com.seffafbagis.api.repository.OrganizationRepository;
import com.seffafbagis.api.repository.TransparencyScoreHistoryRepository;
import com.seffafbagis.api.repository.TransparencyScoreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransparencyScoreServiceTest {

    @Mock
    private TransparencyScoreRepository scoreRepository;
    @Mock
    private TransparencyScoreHistoryRepository historyRepository;
    @Mock
    private TransparencyScoreCalculator calculator;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private EvidenceRepository evidenceRepository;

    @InjectMocks
    private TransparencyScoreService transparencyScoreService;

    @Test
    void initializeScore_ShouldCreateScore_WhenNotExists() {
        UUID orgId = UUID.randomUUID();
        Organization organization = new Organization();
        organization.setId(orgId);

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
        when(scoreRepository.findByOrganizationId(orgId)).thenReturn(Optional.empty());

        transparencyScoreService.initializeScore(orgId);

        verify(scoreRepository).save(any(TransparencyScore.class));
        verify(historyRepository).save(any(TransparencyScoreHistory.class));
    }

    @Test
    void initializeScore_ShouldDoNothing_WhenExists() {
        UUID orgId = UUID.randomUUID();
        Organization organization = new Organization();
        organization.setId(orgId);

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
        when(scoreRepository.findByOrganizationId(orgId)).thenReturn(Optional.of(new TransparencyScore()));

        transparencyScoreService.initializeScore(orgId);

        verify(scoreRepository, never()).save(any(TransparencyScore.class));
    }

    @Test
    void canCreateCampaign_ShouldReturnTrue_WhenScoreIsHighEnough() {
        UUID orgId = UUID.randomUUID();
        TransparencyScore score = TransparencyScore.builder()
                .currentScore(new BigDecimal("50.00"))
                .build();

        when(scoreRepository.findByOrganizationId(orgId)).thenReturn(Optional.of(score));

        boolean result = transparencyScoreService.canCreateCampaign(orgId);

        assertTrue(result);
    }

    @Test
    void canCreateCampaign_ShouldReturnFalse_WhenScoreIsLow() {
        UUID orgId = UUID.randomUUID();
        TransparencyScore score = TransparencyScore.builder()
                .currentScore(new BigDecimal("30.00"))
                .build();

        when(scoreRepository.findByOrganizationId(orgId)).thenReturn(Optional.of(score));

        boolean result = transparencyScoreService.canCreateCampaign(orgId);

        assertFalse(result);
    }

    @Test
    void onEvidenceApproved_ShouldUpdateScore() {
        UUID evidenceId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();

        Organization org = new Organization();
        org.setId(orgId);

        Campaign campaign = new Campaign();
        campaign.setOrganization(org);

        Evidence evidence = new Evidence();
        evidence.setId(evidenceId);
        evidence.setCampaign(campaign);

        TransparencyScore score = TransparencyScore.builder()
                .organization(org)
                .currentScore(new BigDecimal("50.00"))
                .approvedEvidences(0)
                .onTimeReports(0)
                .build();
        score.setId(UUID.randomUUID());

        when(evidenceRepository.findById(evidenceId)).thenReturn(Optional.of(evidence));
        when(scoreRepository.findByOrganizationId(orgId)).thenReturn(Optional.of(score));
        when(calculator.calculateChange(anyString())).thenReturn(new BigDecimal("5.00"));
        when(calculator.calculateNewScore(any(), any())).thenReturn(new BigDecimal("55.00"));

        transparencyScoreService.onEvidenceApproved(evidenceId, true);

        verify(scoreRepository).save(score);
        verify(historyRepository).save(any(TransparencyScoreHistory.class));
        assertEquals(new BigDecimal("55.00"), score.getCurrentScore());
        assertEquals(1, score.getApprovedEvidences());
        assertEquals(1, score.getOnTimeReports());
    }
}
