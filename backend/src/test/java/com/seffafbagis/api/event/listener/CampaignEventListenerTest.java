package com.seffafbagis.api.event.listener;

import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.event.CampaignApprovedEvent;
import com.seffafbagis.api.event.CampaignRejectedEvent;
import com.seffafbagis.api.repository.CampaignRepository;
import com.seffafbagis.api.service.audit.AuditLogService;
import com.seffafbagis.api.service.notification.NotificationService;
import com.seffafbagis.api.service.transparency.TransparencyScoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CampaignEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private TransparencyScoreService transparencyScoreService;

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private AuditLogService auditLogService;

    private CampaignEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new CampaignEventListener(
                notificationService, transparencyScoreService, campaignRepository, auditLogService);
    }

    @Test
    void handleCampaignApproved_shouldNotifyAndLog() {
        // Arrange
        UUID campaignId = UUID.randomUUID();
        UUID organizationId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        Campaign campaign = mock(Campaign.class);
        when(campaign.getId()).thenReturn(campaignId);
        when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));

        CampaignApprovedEvent event = new CampaignApprovedEvent(
                adminId, campaignId, organizationId, adminId);

        // Act
        listener.handleCampaignApproved(event);

        // Assert
        verify(notificationService).notifyCampaignApproved(campaign);
        verify(auditLogService).log(eq("campaign.approved"), eq(adminId), eq("campaign"), eq(campaignId), isNull(),
                isNull());
    }

    @Test
    void handleCampaignRejected_shouldNotifyAndLog() {
        // Arrange
        UUID campaignId = UUID.randomUUID();
        UUID organizationId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        String reason = "Insufficient documentation";

        Campaign campaign = mock(Campaign.class);
        when(campaign.getId()).thenReturn(campaignId);
        when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));

        CampaignRejectedEvent event = new CampaignRejectedEvent(
                adminId, campaignId, organizationId, reason);

        // Act
        listener.handleCampaignRejected(event);

        // Assert
        verify(notificationService).notifyCampaignRejected(campaign, reason);
        verify(auditLogService).log(eq("campaign.rejected"), eq(adminId), eq("campaign"), eq(campaignId), isNull(),
                isNull());
    }
}
