package com.seffafbagis.api.scheduler;

import com.seffafbagis.api.config.SchedulerProperties;
import com.seffafbagis.api.repository.EmailLogRepository;
import com.seffafbagis.api.repository.NotificationRepository;
import com.seffafbagis.api.repository.PasswordResetTokenRepository;
import com.seffafbagis.api.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CleanupSchedulerTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private EmailLogRepository emailLogRepository;
    @Mock
    private SchedulerProperties schedulerProperties;
    @Mock
    private SchedulerProperties.CleanupProperties cleanupProperties;

    @InjectMocks
    private CleanupScheduler scheduler;

    @BeforeEach
    void setUp() {
        lenient().when(schedulerProperties.getCleanup()).thenReturn(cleanupProperties);
        lenient().when(cleanupProperties.isEnabled()).thenReturn(true);
        lenient().when(cleanupProperties.getNotificationRetentionDays()).thenReturn(90);
    }

    @Test
    void performCleanup_ShouldDeleteExpiredData() {
        scheduler.performCleanup();

        verify(refreshTokenRepository).deleteExpiredTokens(any(Instant.class));
        verify(passwordResetTokenRepository).deleteExpiredTokens(any(Instant.class));
        verify(notificationRepository).deleteByIsReadTrueAndCreatedAtBefore(any(OffsetDateTime.class));
        verify(emailLogRepository).deleteBySentAtBefore(any(LocalDateTime.class));
    }

    @Test
    void performCleanup_ShouldDoNothingIfDisabled() {
        when(cleanupProperties.isEnabled()).thenReturn(false);

        scheduler.performCleanup();

        verify(refreshTokenRepository, never()).deleteExpiredTokens(any());
        verify(passwordResetTokenRepository, never()).deleteExpiredTokens(any());
    }
}
