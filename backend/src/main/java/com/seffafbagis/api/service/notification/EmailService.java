package com.seffafbagis.api.service.notification;

import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.entity.user.UserProfile;
import com.seffafbagis.api.repository.EmailLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Email Service.
 * 
 * Handles all transactional email sending using Thymeleaf templates.
 * 
 * Features:
 * - HTML email templates with Thymeleaf
 * - Email logging for audit trail
 * - Graceful error handling
 * - Support for multiple email types (verification, password reset, welcome,
 * etc.)
 * 
 * Configuration Properties:
 * - app.mail.from-address: Sender email address
 * - app.mail.from-name: Sender name
 * - app.base-url: Base URL for email links (e.g., https://example.com)
 * 
 * @author Furkan
 * @version 1.0
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final EmailLogRepository emailLogRepository;

    @Value("${app.mail.from-address:noreply@seffafbagis.com}")
    private String fromAddress;

    @Value("${app.mail.from-name:Şeffaf Bağış Platformu}")
    private String fromName;

    @Value("${app.base-url:https://seffafbagis.com}")
    private String baseUrl;

    /**
     * Constructor injection.
     */
    @Autowired
    public EmailService(
            JavaMailSender mailSender,
            TemplateEngine templateEngine,
            EmailLogRepository emailLogRepository) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.emailLogRepository = emailLogRepository;
    }

    /**
     * Sends a generic email using Thymeleaf template.
     * 
     * Flow:
     * 1. Process template with variables
     * 2. Create and send MimeMessage
     * 3. Log email send (success or failure)
     * 
     * @param request Email request with recipient, subject, template, and variables
     */
    public void sendEmail(EmailRequest request) {
        try {
            // Step 1: Create MimeMessage
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Step 2: Set message properties
            helper.setFrom(fromAddress, fromName);
            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());

            // Step 3: Process template
            Context context = new Context(new Locale("tr", "TR"));
            context.setVariables(request.getTemplateVariables());

            String htmlContent = templateEngine.process(request.getTemplateName(), context);
            helper.setText(htmlContent, true); // true = HTML content

            // Step 4: Send email
            mailSender.send(message);

            logger.info("Email sent successfully - To: {}, Type: {}", request.getTo(), request.getEmailType());

            // Step 5: Log success (if needed for audit trail)
            // emailLogRepository.log(request.getTo(), request.getEmailType(), "sent", null,
            // request.getUserId());

        } catch (MessagingException | java.io.UnsupportedEncodingException ex) {
            logger.error("Failed to send email - To: {}, Type: {}, Error: {}",
                    request.getTo(), request.getEmailType(), ex.getMessage(), ex);

            // Log failure to database
            // emailLogRepository.log(request.getTo(), request.getEmailType(), "failed",
            // ex.getMessage(), request.getUserId());

            // Optionally re-throw or handle gracefully
            throw new RuntimeException("Failed to send email", ex);
        }
    }

    /**
     * Sends email verification email.
     * 
     * Sent when user registers a new account.
     * Email contains link to verify email address.
     * Link is valid for 24 hours.
     * 
     * @param toEmail Recipient email address
     * @param token   Verification token (plain, not hashed)
     * @param user    User entity
     */
    public void sendVerificationEmail(String toEmail, String token, User user) {
        logger.info("Preparing verification email for: {}", toEmail);

        // Build verification URL
        String verificationUrl = String.format("%s/verify-email?token=%s", baseUrl, token);

        // Prepare template variables
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", formatUserName(user));
        variables.put("verificationUrl", verificationUrl);
        variables.put("expiryHours", 24);
        variables.put("supportEmail", fromAddress);

        // Create email request
        EmailRequest request = new EmailRequest();
        request.setTo(toEmail);
        request.setSubject("Şeffaf Bağış Platformu - E-posta Doğrulama");
        request.setTemplateName("email/email-verification");
        request.setTemplateVariables(variables);
        request.setEmailType("VERIFICATION");
        request.setUserId(user.getId());

        // Send email
        sendEmail(request);
    }

    /**
     * Sends password reset email.
     * 
     * Sent when user requests password reset (forgot password).
     * Email contains link to reset password.
     * Link is valid for 1 hour.
     * 
     * @param toEmail Recipient email address
     * @param token   Password reset token (plain, not hashed)
     * @param user    User entity
     */
    public void sendPasswordResetEmail(String toEmail, String token, User user) {
        logger.info("Preparing password reset email for: {}", toEmail);

        // Build reset URL
        String resetUrl = String.format("%s/reset-password?token=%s", baseUrl, token);

        // Prepare template variables
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", formatUserName(user));
        variables.put("resetUrl", resetUrl);
        variables.put("expiryMinutes", 60);
        variables.put("supportEmail", fromAddress);

        // Create email request
        EmailRequest request = new EmailRequest();
        request.setTo(toEmail);
        request.setSubject("Şeffaf Bağış Platformu - Şifre Sıfırlama");
        request.setTemplateName("email/password-reset");
        request.setTemplateVariables(variables);
        request.setEmailType("PASSWORD_RESET");
        request.setUserId(user.getId());

        // Send email
        sendEmail(request);
    }

    /**
     * Sends welcome email.
     * 
     * Sent after email is successfully verified.
     * Welcomes user and provides instructions for platform usage.
     * 
     * @param toEmail Recipient email address
     * @param user    User entity
     * @param profile User profile (optional)
     */
    public void sendWelcomeEmail(String toEmail, User user, UserProfile profile) {
        logger.info("Preparing welcome email for: {}", toEmail);

        // Prepare template variables
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", formatUserName(user));
        variables.put("loginUrl", String.format("%s/login", baseUrl));
        variables.put("dashboardUrl", String.format("%s/dashboard", baseUrl));
        variables.put("supportEmail", fromAddress);

        // Create email request
        EmailRequest request = new EmailRequest();
        request.setTo(toEmail);
        request.setSubject("Şeffaf Bağış Platformu'na Hoş Geldiniz!");
        request.setTemplateName("email/welcome");
        request.setTemplateVariables(variables);
        request.setEmailType("WELCOME");
        request.setUserId(user.getId());

        // Send email
        sendEmail(request);
    }

    /**
     * Sends password changed notification email.
     * 
     * Sent after password is successfully changed.
     * Notifies user that password was changed.
     * Contains security notice to contact support if not authorized.
     * 
     * @param toEmail Recipient email address
     * @param user    User entity
     */
    public void sendPasswordChangedEmail(String toEmail, User user) {
        logger.info("Preparing password changed notification email for: {}", toEmail);

        // Format current date/time
        String changeTime = ZonedDateTime.now().format(
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss ZZZZZ", new Locale("tr", "TR")));

        // Prepare template variables
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", formatUserName(user));
        variables.put("changeTime", changeTime);
        variables.put("supportUrl", String.format("%s/support", baseUrl));
        variables.put("supportEmail", fromAddress);

        // Create email request
        EmailRequest request = new EmailRequest();
        request.setTo(toEmail);
        request.setSubject("Şeffaf Bağış Platformu - Şifreniz Değiştirildi");
        request.setTemplateName("email/password-changed");
        request.setTemplateVariables(variables);
        request.setEmailType("PASSWORD_CHANGED");
        request.setUserId(user.getId());

        // Send email
        sendEmail(request);
    }

    /**
     * Formats user name for email templates.
     * 
     * Uses profile first and last name if available, otherwise uses email.
     * 
     * @param user User entity
     * @return Formatted user name
     */
    private String formatUserName(User user) {
        if (user.getProfile() != null) {
            UserProfile profile = user.getProfile();
            if (profile.getFirstName() != null && !profile.getFirstName().isBlank()) {
                return profile.getFirstName();
            }
        }
        return user.getEmail().split("@")[0]; // Use part before @ if no profile name
    }

    /**
     * Inner class for email request details.
     * 
     * Encapsulates all information needed to send an email.
     */
    public static class EmailRequest {
        private String to;
        private String subject;
        private String templateName;
        private Map<String, Object> templateVariables;
        private String emailType;
        private java.util.UUID userId;

        // Getters and setters
        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getTemplateName() {
            return templateName;
        }

        public void setTemplateName(String templateName) {
            this.templateName = templateName;
        }

        public Map<String, Object> getTemplateVariables() {
            return templateVariables;
        }

        public void setTemplateVariables(Map<String, Object> templateVariables) {
            this.templateVariables = templateVariables;
        }

        public String getEmailType() {
            return emailType;
        }

        public void setEmailType(String emailType) {
            this.emailType = emailType;
        }

        public java.util.UUID getUserId() {
            return userId;
        }

        public void setUserId(java.util.UUID userId) {
            this.userId = userId;
        }
    }
}
