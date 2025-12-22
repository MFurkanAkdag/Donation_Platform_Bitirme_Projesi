package com.seffafbagis.api.scheduler;

import com.seffafbagis.api.config.SchedulerProperties;
import com.seffafbagis.api.entity.donation.BankTransferReference;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.repository.BankTransferReferenceRepository;
import com.seffafbagis.api.service.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankTransferExpirySchedulerTest {

    @Mock
    private BankTransferReferenceRepository bankTransferReferenceRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SchedulerProperties schedulerProperties;

    @Mock
    private SchedulerProperties.BankTransferProperties bankTransferProperties;

    @InjectMocks
    private BankTransferExpiryScheduler scheduler;

    @BeforeEach
    void setUp() {
        lenient().when(schedulerProperties.getBankTransfer()).thenReturn(bankTransferProperties);
        lenient().when(bankTransferProperties.isEnabled()).thenReturn(true);
    }

    @Test
    void expirePendingTransfers_ShouldExpireAndNotify() {
        // Arrange
        BankTransferReference ref = new BankTransferReference();
        ref.setId(UUID.randomUUID());
        ref.setReferenceCode("REF123");
        User donor = new User();
        donor.setId(UUID.randomUUID());
        ref.setDonor(donor);

        when(bankTransferReferenceRepository.findByStatusAndExpiresAtBefore(eq("Pending"), any(OffsetDateTime.class)))
                .thenReturn(List.of(ref));

        // Act
        scheduler.expireBankTransfers();

        // Assert
        verify(bankTransferReferenceRepository).save(ref);
        assert "Expired".equals(ref.getStatus());
        verify(notificationService).createNotification(eq(donor.getId()), any(), any(), any(), any());
    }
}
