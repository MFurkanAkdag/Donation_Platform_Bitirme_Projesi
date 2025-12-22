package com.seffafbagis.api.scheduler;

import com.seffafbagis.api.config.SchedulerProperties;
import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.enums.CampaignStatus;
import com.seffafbagis.api.repository.CampaignRepository;
import com.seffafbagis.api.repository.EvidenceRepository;
import com.seffafbagis.api.service.notification.NotificationService;
import com.seffafbagis.api.service.transparency.TransparencyScoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvidenceReminderSchedulerTest {

    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private EvidenceRepository evidenceRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private TransparencyScoreService transparencyScoreService;
    @Mock
    private SchedulerProperties schedulerProperties;
    @Mock
    private SchedulerProperties.EvidenceReminderProperties evidenceReminderProperties;

    @InjectMocks
    private EvidenceReminderScheduler scheduler;

    @BeforeEach
    void setUp() {
        lenient().when(schedulerProperties.getEvidenceReminder()).thenReturn(evidenceReminderProperties);
        lenient().when(evidenceReminderProperties.isEnabled()).thenReturn(true);
        lenient().when(evidenceReminderProperties.getReminderDays()).thenReturn(List.of(7, 3, 1, 0));
    }

    @Test
    void checkEvidenceDeadlines_ShouldNotifyOnReminderDay() {
        Campaign campaign = new Campaign();
        campaign.setId(UUID.randomUUID());
        campaign.setStatus(CampaignStatus.COMPLETED);
        campaign.setCompletedAt(LocalDateTime.now().minusDays(23).plusHours(1)); // Deadline in 7 days (default 30) +
                                                                                 // buffer
        campaign.setTargetAmount(new BigDecimal("1000"));
        campaign.setCollectedAmount(new BigDecimal("1000"));

        when(campaignRepository.findByStatus(eq(CampaignStatus.COMPLETED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(campaign)));

        when(evidenceRepository.sumAmountSpentByCampaignIdAndStatus(any(), any())).thenReturn(BigDecimal.ZERO);

        scheduler.sendEvidenceReminders();

        // 30 - 23 = 7 days remaining. 7 is in reminder list. Should notify.
        verify(notificationService).notifyEvidenceRequired(eq(campaign), anyInt());
    }
}
