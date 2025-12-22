package com.seffafbagis.api.scheduler;

import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.enums.CampaignStatus;
import com.seffafbagis.api.repository.CampaignRepository;
import com.seffafbagis.api.service.notification.NotificationService;
import com.seffafbagis.api.service.transparency.TransparencyScoreService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignStatusSchedulerTest {

    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private TransparencyScoreService transparencyScoreService;

    @InjectMocks
    private CampaignStatusScheduler scheduler;

    @Test
    void autoCompleteSuccessfulCampaigns_ShouldCompleteWhenTargetReached() {
        Campaign campaign = new Campaign();
        campaign.setId(UUID.randomUUID());
        campaign.setTargetAmount(new BigDecimal("1000"));
        campaign.setCollectedAmount(new BigDecimal("1000"));

        when(campaignRepository.findByStatus(eq(CampaignStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(campaign)));

        scheduler.autoCompleteSuccessfulCampaigns();

        verify(campaignRepository).save(campaign);
        assert campaign.getStatus() == CampaignStatus.COMPLETED;
        verify(notificationService).notifyCampaignCompleted(campaign);
    }
}
