package com.seffafbagis.api.service.notification;

import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.donation.Donation;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.entity.donation.DonationReceipt;
import com.seffafbagis.api.entity.user.UserProfile;
import com.seffafbagis.api.repository.EmailLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.context.i18n.LocaleContextHolder;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @Mock
    private EmailLogRepository emailLogRepository;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        when(mailSender.createMimeMessage())
                .thenAnswer(invocation -> new MimeMessage(Session.getDefaultInstance(new Properties())));
    }

    @Test
    void sendWelcomeEmail_ShouldSendEmail() {
        // No need to mock mimeMessage here, setup handles it
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Body</html>");

        User user = new User();
        user.setEmail("test@example.com");
        UserProfile profile = new UserProfile(user);
        profile.setFirstName("Test");
        profile.setLastName("User");
        user.setProfile(profile);

        emailService.sendWelcomeEmail(user);

        verify(mailSender).send(any(MimeMessage.class));
        verify(emailLogRepository).save(any());
    }

    @Test
    void sendDonationReceiptEmail_ShouldSendEmail() {
        // MimeMessage handled by setUp
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Body</html>");

        User donor = new User();
        donor.setEmail("donor@example.com");

        Campaign campaign = new Campaign();
        campaign.setTitle("Help");

        Donation donation = new Donation();
        donation.setDonor(donor);
        donation.setCampaign(campaign);
        donation.setAmount(BigDecimal.TEN);
        donation.setReceipt(new DonationReceipt());
        ReflectionTestUtils.setField(donation, "createdAt", OffsetDateTime.now());

        emailService.sendDonationReceiptEmail(donation);

        verify(mailSender).send(any(MimeMessage.class));
        verify(emailLogRepository).save(any());
    }

    @Test
    void sendCampaignApprovalEmail_ShouldSendEmail() {
        // MimeMessage handled by setUp
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Body</html>");

        User orgUser = new User();
        orgUser.setEmail("org@example.com");

        Organization organization = new Organization();
        organization.setUser(orgUser);

        Campaign campaign = new Campaign();
        campaign.setTitle("Campaign");

        emailService.sendCampaignApprovalEmail(organization, campaign, true, null);

        verify(mailSender).send(any(MimeMessage.class));
        verify(emailLogRepository).save(any());
    }
}
