package com.seffafbagis.api.service.donation;

import com.seffafbagis.api.dto.request.donation.InitiateBankTransferRequest;
import com.seffafbagis.api.dto.request.donation.MatchBankTransferRequest;
import com.seffafbagis.api.dto.response.donation.BankTransferInfoResponse;
import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.donation.BankTransferReference;
import com.seffafbagis.api.entity.donation.Donation;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.organization.OrganizationBankAccount;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.DonationStatus;
import com.seffafbagis.api.repository.BankTransferReferenceRepository;
import com.seffafbagis.api.repository.CampaignRepository;
import com.seffafbagis.api.repository.DonationRepository;
import com.seffafbagis.api.repository.DonationTypeRepository;
import com.seffafbagis.api.repository.OrganizationBankAccountRepository;
import com.seffafbagis.api.repository.UserRepository;
import com.seffafbagis.api.service.notification.NotificationService;
import com.seffafbagis.api.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BankTransferServiceTest {

    @Mock
    private BankTransferReferenceRepository bankTransferReferenceRepository;
    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private OrganizationBankAccountRepository bankAccountRepository;
    @Mock
    private DonationTypeRepository donationTypeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DonationService donationService;
    @Mock
    private DonationRepository donationRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BankTransferService bankTransferService;

    private User user;
    private Campaign campaign;
    private OrganizationBankAccount bankAccount;
    private BankTransferReference reference;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());

        Organization org = new Organization();
        org.setId(UUID.randomUUID());

        campaign = new Campaign();
        campaign.setId(UUID.randomUUID());
        campaign.setOrganization(org);
        campaign.setStatus(com.seffafbagis.api.enums.CampaignStatus.ACTIVE);

        bankAccount = new OrganizationBankAccount();
        bankAccount.setPrimary(true);
        bankAccount.setBankName("Test Bank");
        bankAccount.setIban("TR1234");
        bankAccount.setAccountHolder("Test Account");

        reference = new BankTransferReference();
        reference.setId(UUID.randomUUID());
        reference.setReferenceCode("SBP-TEST-123");
        reference.setCampaign(campaign);
        reference.setOrganization(org);
        reference.setDonor(user);
        reference.setStatus("pending");
        reference.setExpiresAt(OffsetDateTime.now().plusDays(7));
    }

    @Test
    void initiateBankTransfer_ShouldGenerateReferenceAndSnapshot() {
        InitiateBankTransferRequest request = InitiateBankTransferRequest.builder()
                .campaignId(campaign.getId())
                .amount(BigDecimal.valueOf(500))
                .senderName("John Doe")
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(user.getId()));
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));
            when(bankAccountRepository.findByOrganizationIdOrderByIsPrimaryDescCreatedAtAsc(any()))
                    .thenReturn(Collections.singletonList(bankAccount));
            when(bankTransferReferenceRepository.existsByReferenceCode(anyString())).thenReturn(false);
            when(bankTransferReferenceRepository.save(any(BankTransferReference.class)))
                    .thenAnswer(i -> i.getArgument(0));

            BankTransferInfoResponse response = bankTransferService.initiateBankTransfer(request);

            assertNotNull(response);
            assertNotNull(response.getReferenceCode());
            assertEquals("Test Bank", response.getBankName());
            verify(bankTransferReferenceRepository).save(any(BankTransferReference.class));
        }
    }

    @Test
    void matchBankTransfer_ShouldCreateCompletedDonationAndMatchRef() {
        MatchBankTransferRequest request = MatchBankTransferRequest.builder()
                .referenceCode(reference.getReferenceCode())
                .actualAmount(BigDecimal.valueOf(500))
                .build();

        when(bankTransferReferenceRepository.findByReferenceCode(reference.getReferenceCode()))
                .thenReturn(Optional.of(reference));
        when(donationRepository.save(any(Donation.class))).thenAnswer(i -> {
            Donation d = i.getArgument(0);
            d.setId(UUID.randomUUID());
            return d;
        });

        bankTransferService.matchBankTransfer(request);

        assertEquals("matched", reference.getStatus());
        assertNotNull(reference.getMatchedDonation());
        verify(donationRepository).save(any(Donation.class));
        verify(donationService).completeDonation(any());
        verify(bankTransferReferenceRepository).save(reference);
    }

    @Test
    void cancelBankTransfer_ShouldCancelPending() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(user.getId()));
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(bankTransferReferenceRepository.findByReferenceCode(reference.getReferenceCode()))
                    .thenReturn(Optional.of(reference));

            bankTransferService.cancelBankTransfer(reference.getReferenceCode());

            assertEquals("cancelled", reference.getStatus());
            verify(bankTransferReferenceRepository).save(reference);
        }
    }
}
