package com.seffafbagis.api.scheduler;

import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.enums.CampaignStatus;
import com.seffafbagis.api.enums.EvidenceStatus;
import com.seffafbagis.api.repository.CampaignRepository;
import com.seffafbagis.api.repository.EvidenceRepository;
import com.seffafbagis.api.repository.OrganizationRepository;
import com.seffafbagis.api.service.transparency.TransparencyScoreService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransparencyScoreSchedulerTest {

    @Mock
    private TransparencyScoreService transparencyScoreService;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private EvidenceRepository evidenceRepository;
    @Mock
    private CampaignRepository campaignRepository;

    @InjectMocks
    private TransparencyScoreScheduler scheduler;

    @Test
    void updateTransparencyScores_ShouldRunAllChecks() {
        when(campaignRepository.findByStatus(eq(CampaignStatus.COMPLETED), any(Pageable.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        scheduler.updateTransparencyScores();
        // Since methods are public but called from inside, simple verify might not work
        // if not using spy or if logic is complex
        // But here we are unit testing the scheduler which blindly calls private/public
        // methods.
        // The scheduler iterates repositories.
        verify(organizationRepository).findAll();
        verify(campaignRepository).findByStatus(eq(CampaignStatus.COMPLETED), any(Pageable.class));
    }

    @Test
    void applyConsistencyBonus_ShouldIterateOrganizations() {
        Organization org = new Organization();
        org.setId(UUID.randomUUID());
        when(organizationRepository.findAll()).thenReturn(List.of(org));

        scheduler.applyConsistencyBonus();

        // Currently logic is a placeholder but it should run without error
        verify(organizationRepository).findAll();
    }

    @Test
    void penalizeMissedDeadlines_ShouldPenalizeWhenDeadlineMissedAndEvidenceInsufficient() {
        Campaign campaign = new Campaign();
        campaign.setId(UUID.randomUUID());
        campaign.setStatus(CampaignStatus.COMPLETED);
        // Completed 20 days ago, default deadline 15 days -> missed by 5 days
        campaign.setCompletedAt(LocalDateTime.now().minusDays(20));
        campaign.setTargetAmount(new BigDecimal("1000"));
        campaign.setCollectedAmount(new BigDecimal("1000"));

        when(campaignRepository.findByStatus(eq(CampaignStatus.COMPLETED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(campaign)));

        // Spent 0
        when(evidenceRepository.sumAmountSpentByCampaignIdAndStatus(campaign.getId(), EvidenceStatus.APPROVED))
                .thenReturn(BigDecimal.ZERO);

        scheduler.penalizeMissedDeadlines();

        verify(transparencyScoreService).onEvidenceMissedDeadline(campaign.getId());
    }

    @Test
    void penalizeMissedDeadlines_ShouldNotPenalizeWhenEvidenceSufficient() {
        Campaign campaign = new Campaign();
        campaign.setId(UUID.randomUUID());
        campaign.setStatus(CampaignStatus.COMPLETED);
        campaign.setCompletedAt(LocalDateTime.now().minusDays(20));
        campaign.setTargetAmount(new BigDecimal("1000"));
        campaign.setCollectedAmount(new BigDecimal("1000"));

        when(campaignRepository.findByStatus(eq(CampaignStatus.COMPLETED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(campaign)));

        // Spent 1000 (Full)
        when(evidenceRepository.sumAmountSpentByCampaignIdAndStatus(campaign.getId(), EvidenceStatus.APPROVED))
                .thenReturn(new BigDecimal("1000"));

        scheduler.penalizeMissedDeadlines();

        verify(transparencyScoreService, never()).onEvidenceMissedDeadline(any());
    }
}
