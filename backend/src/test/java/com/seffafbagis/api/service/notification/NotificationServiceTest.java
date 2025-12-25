package com.seffafbagis.api.service.notification;

import com.seffafbagis.api.dto.response.notification.NotificationListResponse;
import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.donation.Donation;
import com.seffafbagis.api.entity.evidence.Evidence;
import com.seffafbagis.api.entity.notification.Notification;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.entity.user.UserPreference;
import com.seffafbagis.api.enums.NotificationType;
import com.seffafbagis.api.repository.NotificationRepository;
import com.seffafbagis.api.repository.OrganizationRepository;
import com.seffafbagis.api.repository.UserPreferenceRepository;
import com.seffafbagis.api.repository.UserRepository;
import com.seffafbagis.api.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserPreferenceRepository userPreferenceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationService notificationService;

    private User user;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
    }

    @Test
    void getMyNotifications_ShouldReturnNotifications() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(userId));

            Notification notification = new Notification();
            notification.setId(UUID.randomUUID());
            notification.setTitle("Title");
            notification.setMessage("Message");
            notification.setType(NotificationType.SYSTEM.name());
            ReflectionTestUtils.setField(notification, "createdAt", OffsetDateTime.now());
            notification.setRead(false);

            Page<Notification> page = new PageImpl<>(List.of(notification));
            when(notificationRepository.findByUserIdOrderByCreatedAtDesc(any(UUID.class), any(Pageable.class)))
                    .thenReturn(page);
            when(notificationRepository.countByUserIdAndIsReadFalse(userId)).thenReturn(1L);

            NotificationListResponse response = notificationService.getMyNotifications(Pageable.unpaged());

            assertNotNull(response);
            assertEquals(1, response.getNotifications().size());
            assertEquals(1L, response.getUnreadCount());
            assertEquals("Title", response.getNotifications().get(0).getTitle());
        }
    }

    @Test
    void createNotification_ShouldSaveNotification() {
        when(userRepository.getReferenceById(userId)).thenReturn(user);

        notificationService.createNotification(userId, NotificationType.SYSTEM, "Test Title", "Test Message", null);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());

        Notification savedNotification = notificationCaptor.getValue();
        assertEquals(userId, savedNotification.getUserId());
        assertEquals("Test Title", savedNotification.getTitle());
        assertEquals("Test Message", savedNotification.getMessage());
        assertEquals(NotificationType.SYSTEM.name(), savedNotification.getType());
    }

    @Test
    void notifyDonationReceived_ShouldCreateNotification() {
        Organization organization = new Organization();
        organization.setUser(user);

        Campaign campaign = new Campaign();
        campaign.setId(UUID.randomUUID());
        campaign.setTitle("Campaign Title");
        campaign.setOrganization(organization);

        Donation donation = new Donation();
        donation.setId(UUID.randomUUID());
        donation.setAmount(BigDecimal.TEN);
        donation.setCampaign(campaign);
        donation.setDonor(user); // Donor is also user for test simplicity

        when(userRepository.getReferenceById(userId)).thenReturn(user);

        notificationService.notifyDonationReceived(donation);

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void notifyDonationCompleted_ShouldCreateNotificationAndSendReceiptEmail() {
        Campaign campaign = new Campaign();
        campaign.setId(UUID.randomUUID());
        campaign.setTitle("Campaign Title");

        Donation donation = new Donation();
        donation.setId(UUID.randomUUID());
        donation.setAmount(BigDecimal.TEN);
        donation.setCampaign(campaign);
        donation.setDonor(user);
        donation.setReceipt(new com.seffafbagis.api.entity.Receipt()); // Trigger receipt email condition

        when(userRepository.getReferenceById(userId)).thenReturn(user);

        // Mock preferences to allow email
        UserPreference prefs = new UserPreference();
        prefs.setEmailNotifications(true);
        when(userPreferenceRepository.findByUserId(userId)).thenReturn(Optional.of(prefs));

        notificationService.notifyDonationCompleted(donation);

        verify(notificationRepository).save(any(Notification.class));
        verify(emailService).sendDonationReceiptEmail(donation);
    }

    @Test
    void notifyScoreChange_ShouldFetchOrgAndNotify() {
        UUID orgId = UUID.randomUUID();
        Organization organization = new Organization();
        organization.setId(orgId);
        organization.setUser(user);

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
        when(userRepository.getReferenceById(userId)).thenReturn(user);

        notificationService.notifyScoreChange(orgId, BigDecimal.ZERO, BigDecimal.TEN);

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void markAsRead_ShouldCallRepository() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(userId));
            UUID notificationId = UUID.randomUUID();

            notificationService.markAsRead(notificationId);

            verify(notificationRepository).markAsRead(notificationId, userId);
        }
    }
}
