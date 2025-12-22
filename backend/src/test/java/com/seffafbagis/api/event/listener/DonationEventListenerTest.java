package com.seffafbagis.api.event.listener;

import com.seffafbagis.api.entity.donation.Donation;
import com.seffafbagis.api.enums.DonationStatus;
import com.seffafbagis.api.event.DonationCompletedEvent;
import com.seffafbagis.api.event.DonationFailedEvent;
import com.seffafbagis.api.repository.DonationRepository;
import com.seffafbagis.api.service.audit.AuditLogService;
import com.seffafbagis.api.service.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DonationEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private AuditLogService auditLogService;

    private DonationEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new DonationEventListener(notificationService, donationRepository, auditLogService);
    }

    @Test
    void handleDonationCompleted_shouldNotifyAndLog() {
        // Arrange
        UUID donationId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        UUID organizationId = UUID.randomUUID();
        UUID donorId = UUID.randomUUID();
        UUID triggeredBy = UUID.randomUUID();

        Donation donation = mock(Donation.class);
        when(donation.getId()).thenReturn(donationId);
        when(donation.getStatus()).thenReturn(DonationStatus.COMPLETED);
        when(donationRepository.findById(donationId)).thenReturn(Optional.of(donation));

        DonationCompletedEvent event = new DonationCompletedEvent(
                triggeredBy, donationId, campaignId, organizationId, donorId, BigDecimal.TEN, null);

        // Act
        listener.handleDonationCompleted(event);

        // Assert
        verify(notificationService).notifyDonationReceived(donation);
        verify(auditLogService).log(eq("donation.completed"), eq(triggeredBy), eq("donation"), eq(donationId), isNull(),
                isNull());
    }

    @Test
    void handleDonationFailed_withDonor_shouldNotifyDonor() {
        // Arrange
        UUID donationId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        UUID donorId = UUID.randomUUID();
        UUID triggeredBy = UUID.randomUUID();
        String failureReason = "Payment declined";

        DonationFailedEvent event = new DonationFailedEvent(
                triggeredBy, donationId, campaignId, donorId, failureReason);

        // Act
        listener.handleDonationFailed(event);

        // Assert
        verify(notificationService).notifySystem(donorId, "Bağış Başarısız",
                "Bağışınız işlenirken bir hata oluştu: " + failureReason);
        verify(auditLogService).log(eq("donation.failed"), eq(triggeredBy), eq("donation"), eq(donationId), isNull(),
                isNull());
    }

    @Test
    void handleDonationFailed_withoutDonor_shouldOnlyLog() {
        // Arrange
        UUID donationId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        UUID triggeredBy = UUID.randomUUID();
        String failureReason = "Payment declined";

        DonationFailedEvent event = new DonationFailedEvent(
                triggeredBy, donationId, campaignId, null, failureReason);

        // Act
        listener.handleDonationFailed(event);

        // Assert
        verify(notificationService, never()).notifySystem(any(), any(), any());
        verify(auditLogService).log(eq("donation.failed"), eq(triggeredBy), eq("donation"), eq(donationId), isNull(),
                isNull());
    }
}
