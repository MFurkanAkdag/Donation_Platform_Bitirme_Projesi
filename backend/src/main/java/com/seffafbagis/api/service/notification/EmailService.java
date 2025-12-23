package com.seffafbagis.api.service.notification;

import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.donation.Donation;
import com.seffafbagis.api.entity.evidence.Evidence;
import com.seffafbagis.api.entity.notification.EmailLog;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.repository.EmailLogRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;
    private final EmailLogRepository emailLogRepository;

    @Async
    public void sendEmail(String to, String subject, String templateName, Map<String, Object> variables, User user) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            Context context = new Context();
            context.setVariables(variables);
            String html = templateEngine.process("email/" + templateName, context);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            helper.setFrom("noreply@seffafbagis.com"); // Configure properly

            javaMailSender.send(message);

            logEmail(to, templateName, subject, "sent", null, user);

        } catch (MessagingException e) {
            log.error("Failed to send email to {}", to, e);
            logEmail(to, templateName, subject, "failed", e.getMessage(), user);
        }
    }

    private void logEmail(String to, String templateName, String subject, String status, String errorMessage,
            User user) {
        try {
            EmailLog emailLog = new EmailLog();
            emailLog.setUser(user);
            emailLog.setEmailTo(to);
            emailLog.setEmailType(templateName); // Using template name as type for now
            emailLog.setTemplateName(templateName);
            emailLog.setSubject(subject);
            emailLog.setStatus(status);
            emailLog.setErrorMessage(errorMessage);
            emailLog.setSentAt(LocalDateTime.now());
            emailLogRepository.save(emailLog);
        } catch (Exception e) {
            log.error("Failed to log email", e);
        }
    }

    public void sendWelcomeEmail(User user) {
        sendEmail(user.getEmail(), "Hoş Geldiniz - Şeffaf Bağış", "welcome",
                Map.of("name", user.getFullName()), user);
    }

    public void sendDonationReceiptEmail(Donation donation) {
        User donor = donation.getDonor();
        Boolean isAnon = donation.getIsAnonymous();
        String donorName = Boolean.TRUE.equals(isAnon) ? "Değerli Bağışçımız"
                : (donor != null ? donor.getFullName() : "Misafir");
        String email = donor != null ? donor.getEmail() : "guest@example.com";

        if (donor != null) {
            sendEmail(email, "Bağış Makbuzu", "donation-receipt",
                    Map.of("name", donorName,
                            "amount", donation.getAmount(),
                            "campaignTitle", donation.getCampaign().getTitle(),
                            "date", donation.getCreatedAt()),
                    donor);
        }
    }

    public void sendCampaignApprovalEmail(Organization org, Campaign campaign, boolean approved, String reason) {
        if (org.getUser() == null)
            return;
        User user = org.getUser();
        String subject = approved ? "Kampanyanız Onaylandı" : "Kampanyanız Reddedildi";
        String template = approved ? "campaign-approved" : "campaign-rejected";
        Map<String, Object> variables = Map.of(
                "name", user.getFullName(),
                "campaignTitle", campaign.getTitle(),
                "reason", reason != null ? reason : "");
        sendEmail(user.getEmail(), subject, template, variables, user);
    }

    public void sendVerificationEmail(String to, String token, User user) {
        sendEmail(to, "E-posta Doğrulama", "verification-email",
                Map.of("name", user.getFullName() != null ? user.getFullName() : "Kullanıcı",
                        "token", token),
                user);
    }

    public void sendPasswordResetEmail(String to, String resetLink, User user) {
        sendEmail(to, "Şifre Sıfırlama", "password-reset",
                Map.of("name", user.getFullName() != null ? user.getFullName() : "Kullanıcı",
                        "resetLink", resetLink),
                user);
    }

    public void sendPasswordChangedEmail(String to, User user) {
        sendEmail(to, "Şifreniz Değiştirildi", "password-changed",
                Map.of("name", user.getFullName() != null ? user.getFullName() : "Kullanıcı"),
                user);
    }

    public void sendEvidenceReminderEmail(Organization org, Campaign campaign, int daysRemaining) {
        if (org.getUser() == null) {
            log.info("Sending evidence reminder to Org: {} for Campaign: {}", org.getLegalName(), campaign.getTitle());
            return;
        }
        User user = org.getUser();
        sendEmail(user.getEmail(), "Kanıt Yükleme Hatırlatması", "evidence-reminder",
                Map.of("name", user.getFullName() != null ? user.getFullName() : org.getLegalName(),
                        "campaignTitle", campaign.getTitle(),
                        "daysRemaining", daysRemaining),
                user);
    }

    public void sendVerificationSuccessEmail(Organization org) {
        if (org.getUser() == null) {
            return;
        }
        User user = org.getUser();
        sendEmail(user.getEmail(), "Kuruluşunuz Onaylandı", "verification-success",
                Map.of("name", user.getFullName() != null ? user.getFullName() : org.getLegalName(),
                        "organizationName", org.getLegalName()),
                user);
    }

    public void retryFailedEmails() {
        // Implementation pending: Requires storing email variables in EmailLog
        log.warn("Retry failed emails triggered but not fully implemented without variable storage.");
    }

}
