package com.seffafbagis.api.service.notification;

import com.seffafbagis.api.dto.response.notification.NotificationCountResponse;
import com.seffafbagis.api.dto.response.notification.NotificationListResponse;
import com.seffafbagis.api.dto.response.notification.NotificationResponse;
import com.seffafbagis.api.entity.application.Application;
import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.campaign.CampaignUpdate;
import com.seffafbagis.api.entity.donation.Donation;
import com.seffafbagis.api.entity.evidence.Evidence;
import com.seffafbagis.api.entity.notification.Notification;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.entity.user.UserPreference;
import com.seffafbagis.api.enums.NotificationType;
import com.seffafbagis.api.repository.NotificationRepository;
import com.seffafbagis.api.repository.UserPreferenceRepository;
import com.seffafbagis.api.repository.UserRepository;
import com.seffafbagis.api.repository.OrganizationRepository;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.exception.ResourceNotFoundException;
import com.seffafbagis.api.exception.UnauthorizedException;
import com.seffafbagis.api.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public NotificationListResponse getMyNotifications(Pageable pageable) {
        UUID userId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new UnauthorizedException("User not found"));
        Page<Notification> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(userId);

        List<NotificationResponse> responses = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return NotificationListResponse.builder()
                .unreadCount(unreadCount)
                .notifications(responses)
                .build();
    }

    @Transactional(readOnly = true)
    public NotificationListResponse getUnreadNotifications(Pageable pageable) {
        UUID userId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new UnauthorizedException("User not found"));
        Page<Notification> page = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId,
                pageable);

        long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(userId);

        List<NotificationResponse> responses = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return NotificationListResponse.builder()
                .unreadCount(unreadCount)
                .notifications(responses)
                .build();
    }

    @Transactional(readOnly = true)
    public NotificationCountResponse getUnreadCount() {
        UUID userId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new UnauthorizedException("User not found"));
        long unread = notificationRepository.countByUserIdAndIsReadFalse(userId);
        long total = notificationRepository.count();
        return new NotificationCountResponse(total, unread);
    }

    @Transactional
    public void markAsRead(UUID notificationId) {
        UUID userId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new UnauthorizedException("User not found"));
        notificationRepository.markAsRead(notificationId, userId);
    }

    @Transactional
    public void markAllAsRead() {
        UUID userId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new UnauthorizedException("User not found"));
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Transactional
    public void deleteNotification(UUID notificationId) {
        UUID userId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new UnauthorizedException("User not found"));
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Bildirim bulunamadı"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bu bildirimi silme yetkiniz yok");
        }

        notificationRepository.delete(notification);
    }

    @Transactional
    public void createNotification(UUID userId, NotificationType type, String title, String message,
            Map<String, Object> data) {
        User user = userRepository.getReferenceById(userId);

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setData(data);

        notificationRepository.save(notification);
    }

    public void sendNotification(UUID userId, String title, String message) {
        createNotification(userId, NotificationType.SYSTEM, title, message, null);
    }

    @Transactional
    public void notifyDonationReceived(Donation donation) {
        if (donation.getCampaign() == null || donation.getCampaign().getOrganization() == null) {
            return;
        }

        User orgOwner = donation.getCampaign().getOrganization().getUser();
        UUID userId = orgOwner.getId();

        String donorName = Boolean.TRUE.equals(donation.getIsAnonymous()) ? "Anonim"
                : (donation.getDonor() != null ? donation.getDonor().getFullName() : "Bir bağışçı");

        Map<String, Object> data = Map.of(
                "donationId", donation.getId(),
                "campaignId", donation.getCampaign().getId(),
                "campaignTitle", donation.getCampaign().getTitle(),
                "amount", donation.getAmount(),
                "donorName", donorName);

        String title = "Bağış Alındı";
        String message = donation.getCampaign().getTitle() + " kampanyasına " +
                donation.getAmount() + " TL bağış yapıldı.";

        createNotification(userId, NotificationType.DONATION_RECEIVED, title, message, data);

        // Check preferences and send email if applicable (Not specified in EmailService
        // list, maybe future scope)
    }

    @Transactional
    public void notifyDonationCompleted(Donation donation) {
        if (donation.getDonor() == null) {
            // Anonymous donation without user account, maybe just email if email captured?
            // But Notification entity requires User.
            // If anonymous, rely on EmailService direct call from DonationService or here
            // if checking user fails.
            return;
        }

        User donor = donation.getDonor();
        UUID userId = donor.getId();

        Map<String, Object> data = Map.of(
                "donationId", donation.getId(),
                "campaignId", donation.getCampaign().getId(),
                "campaignTitle", donation.getCampaign().getTitle(),
                "amount", donation.getAmount());

        String title = "Bağış Tamamlandı";
        String message = "Bağışınız başarıyla tamamlandı. Teşekkür ederiz!";

        createNotification(userId, NotificationType.DONATION_COMPLETED, title, message, data);

        // Send Receipt Email
        checkAndSendEmail(userId, () -> {
            if (donation.getReceipt() != null) {
                emailService.sendDonationReceiptEmail(donation);
            }
        });
    }

    @Transactional
    public void notifyEvidenceRequired(Campaign campaign, int daysRemaining) {
        if (campaign.getOrganization() == null)
            return;

        User orgOwner = campaign.getOrganization().getUser();
        UUID userId = orgOwner.getId();

        Map<String, Object> data = Map.of(
                "campaignId", campaign.getId(),
                "daysRemaining", daysRemaining);

        String title = "Kanıt Yükleme Hatırlatması";
        String message = campaign.getTitle() + " kampanyası için kanıt yüklemek üzere " + daysRemaining
                + " gününüz kaldı.";

        createNotification(userId, NotificationType.EVIDENCE_REQUIRED, title, message, data);

        checkAndSendEmail(userId,
                () -> emailService.sendEvidenceReminderEmail(campaign.getOrganization(), campaign, daysRemaining));
    }

    @Transactional
    public void notifyEvidenceApproved(Evidence evidence) {
        if (evidence.getCampaign() == null)
            return;

        User orgOwner = evidence.getCampaign().getOrganization().getUser();

        Map<String, Object> data = Map.of(
                "evidenceId", evidence.getId(),
                "campaignId", evidence.getCampaign().getId());

        createNotification(
                orgOwner.getId(),
                NotificationType.EVIDENCE_APPROVED,
                "Kanıt Onaylandı",
                "Yüklediğiniz kanıt onaylandı.",
                data);

        // No email specified for approval in prompt list, but good practice.
    }

    @Transactional
    public void notifyEvidenceRejected(Evidence evidence, String reason) {
        if (evidence.getCampaign() == null)
            return;

        User orgOwner = evidence.getCampaign().getOrganization().getUser();

        Map<String, Object> data = Map.of(
                "evidenceId", evidence.getId(),
                "campaignId", evidence.getCampaign().getId(),
                "reason", reason);

        createNotification(
                orgOwner.getId(),
                NotificationType.EVIDENCE_REJECTED,
                "Kanıt Reddedildi",
                "Yüklediğiniz kanıt reddedildi. Sebep: " + reason,
                data);
    }

    @Transactional
    public void notifyCampaignApproved(Campaign campaign) {
        if (campaign.getOrganization() == null)
            return;
        User user = campaign.getOrganization().getUser();

        createNotification(
                user.getId(),
                NotificationType.CAMPAIGN_APPROVED,
                "Kampanya Onaylandı",
                "Kampanyanız onaylandı ve yayına alındı.",
                Map.of("campaignId", campaign.getId()));

        checkAndSendEmail(user.getId(),
                () -> emailService.sendCampaignApprovalEmail(campaign.getOrganization(), campaign, true, null));
    }

    @Transactional
    public void notifyCampaignRejected(Campaign campaign, String reason) {
        if (campaign.getOrganization() == null)
            return;
        User user = campaign.getOrganization().getUser();

        createNotification(
                user.getId(),
                NotificationType.CAMPAIGN_REJECTED,
                "Kampanya Reddedildi",
                "Kampanyanız reddedildi. Sebep: " + reason,
                Map.of("campaignId", campaign.getId(), "reason", reason));

        checkAndSendEmail(user.getId(),
                () -> emailService.sendCampaignApprovalEmail(campaign.getOrganization(), campaign, false, reason));
    }

    @Transactional
    public void notifyCampaignCompleted(Campaign campaign) {
        if (campaign.getOrganization() == null)
            return;
        User user = campaign.getOrganization().getUser();

        Map<String, Object> data = Map.of(
                "campaignId", campaign.getId(),
                "campaignTitle", campaign.getTitle(),
                "collectedAmount",
                campaign.getCollectedAmount() != null ? campaign.getCollectedAmount() : BigDecimal.ZERO,
                "donorCount", campaign.getDonorCount() != null ? campaign.getDonorCount() : 0);

        createNotification(
                user.getId(),
                NotificationType.CAMPAIGN_COMPLETED,
                "Kampanya Tamamlandı",
                "Tebrikler! Kampanyanız hedeflenen tutara ulaştı.",
                data);

        // Notify user via preference? Add if specific email needed.
    }

    @Transactional
    public void notifyAllUsers(String title, String message) {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            createNotification(user.getId(), NotificationType.SYSTEM, title, message, null);
        }
    }

    @Transactional
    public void notifyScoreChange(UUID organizationId, BigDecimal oldScore, BigDecimal newScore) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", organizationId.toString()));

        User user = organization.getUser();

        String message = String.format("Şeffaflık skorunuz değişti. Eski Skor: %s, Yeni Skor: %s", oldScore, newScore);
        Map<String, Object> data = Map.of(
                "organizationId", organizationId,
                "oldScore", oldScore,
                "newScore", newScore);

        createNotification(user.getId(), NotificationType.SCORE_CHANGE, "Skor Güncellemesi", message, data);
    }

    @Transactional
    public void notifyCampaignUpdate(CampaignUpdate update) {
        if (update.getCampaign() == null || update.getCampaign().getOrganization() == null) {
            return;
        }

        // Notify all donors of this campaign about the update
        Campaign campaign = update.getCampaign();
        String title = "Kampanya Güncellemesi";
        String message = campaign.getTitle() + " kampanyasında yeni bir güncelleme var: " + update.getTitle();

        Map<String, Object> data = Map.of(
                "campaignId", campaign.getId(),
                "updateId", update.getId(),
                "updateTitle", update.getTitle());

        // Notify organization owner
        User orgOwner = campaign.getOrganization().getUser();
        createNotification(orgOwner.getId(), NotificationType.CAMPAIGN_UPDATE, title, message, data);
    }

    @Transactional
    public void notifyApplicationUpdate(Application application) {
        if (application.getApplicant() == null) {
            return;
        }

        User applicant = application.getApplicant();

        String statusMessage;
        switch (application.getStatus()) {
            case APPROVED:
                statusMessage = "onaylandı";
                break;
            case REJECTED:
                statusMessage = "reddedildi";
                break;
            case IN_REVIEW:
                statusMessage = "incelemeye alındı";
                break;
            case COMPLETED:
                statusMessage = "tamamlandı";
                break;
            default:
                statusMessage = "güncellendi";
        }

        String title = "Başvuru Güncellendi";
        String message = "\"" + application.getTitle() + "\" başlıklı başvurunuz " + statusMessage + ".";

        Map<String, Object> data = Map.of(
                "applicationId", application.getId(),
                "applicationTitle", application.getTitle(),
                "status", application.getStatus().name());

        createNotification(applicant.getId(), NotificationType.APPLICATION_UPDATE, title, message, data);
    }

    @Transactional
    public void notifySystem(UUID userId, String title, String message) {
        createNotification(userId, NotificationType.SYSTEM, title, message, null);
    }

    @Transactional
    public void deleteOldNotifications(int daysOld) {
        UUID userId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new UnauthorizedException("User not found"));
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        notificationRepository.deleteByUserIdAndCreatedAtBefore(userId, cutoffDate);
    }

    private void checkAndSendEmail(UUID userId, Runnable emailAction) {
        UserPreference prefs = userPreferenceRepository.findByUserId(userId).orElse(null);
        if (prefs == null || Boolean.TRUE.equals(prefs.getEmailNotifications())) {
            try {
                emailAction.run();
            } catch (Exception e) {
                log.error("Failed to send email notif for user {}: {}", userId, e.getMessage());
            }
        }
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .typeName(getTypeNameInTurkish(notification.getType()))
                .title(notification.getTitle())
                .message(notification.getMessage())
                .data(notification.getData())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .timeAgo(calculateTimeAgo(notification.getCreatedAt()))
                .build();
    }

    private String getTypeNameInTurkish(NotificationType type) {
        switch (type) {
            case DONATION_RECEIVED:
                return "Bağış Alındı";
            case DONATION_COMPLETED:
                return "Bağış Tamamlandı";
            case CAMPAIGN_UPDATE:
                return "Kampanya Güncellemesi";
            case CAMPAIGN_COMPLETED:
                return "Kampanya Tamamlandı";
            case CAMPAIGN_APPROVED:
                return "Kampanya Onaylandı";
            case CAMPAIGN_REJECTED:
                return "Kampanya Reddedildi";
            case EVIDENCE_REQUIRED:
                return "Kanıt Yükleme Hatırlatması";
            case EVIDENCE_APPROVED:
                return "Kanıt Onaylandı";
            case EVIDENCE_REJECTED:
                return "Kanıt Reddedildi";
            case SCORE_CHANGE:
                return "Şeffaflık Skoru Değişti";
            case APPLICATION_UPDATE:
                return "Başvuru Güncellendi";
            case SYSTEM:
                return "Sistem Bildirimi";
            default:
                return type.name();
        }
    }

    public String calculateTimeAgo(OffsetDateTime createdAt) {
        long minutes = ChronoUnit.MINUTES.between(createdAt, OffsetDateTime.now());

        if (minutes < 1) {
            return "Az önce";
        }
        if (minutes < 60) {
            return minutes + " dakika önce";
        }

        long hours = minutes / 60;
        if (hours < 24) {
            return hours + " saat önce";
        }

        long days = hours / 24;
        if (days < 7) {
            return days + " gün önce";
        }

        long weeks = days / 7;
        if (weeks < 4) {
            return weeks + " hafta önce";
        }

        long months = days / 30;
        if (months < 12) {
            return months + " ay önce";
        }

        return (days / 365) + " yıl önce";
    }
}
