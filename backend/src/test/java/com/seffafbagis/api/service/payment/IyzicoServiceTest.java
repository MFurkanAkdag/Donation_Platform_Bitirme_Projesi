package com.seffafbagis.api.service.payment;

import com.iyzipay.Options;
import com.iyzipay.model.ThreedsInitialize;
import com.seffafbagis.api.dto.request.payment.PaymentRequest;
import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.donation.Donation;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.entity.user.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IyzicoServiceTest {

    @Mock
    private Options iyzicoOptions;

    @InjectMocks
    private IyzicoService iyzicoService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(iyzicoService, "callbackUrl", "http://localhost:8080/callback");
    }

    @Test
    void create3DSPayment_Success() {
        // Prepare data
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        UserProfile profile = new UserProfile();
        profile.setFirstName("John");
        profile.setLastName("Doe");
        user.setProfile(profile);

        Campaign campaign = new Campaign();
        campaign.setTitle("Test Campaign");

        Donation donation = new Donation();
        donation.setId(UUID.randomUUID());
        donation.setAmount(new BigDecimal("100.00"));
        donation.setCampaign(campaign);

        PaymentRequest request = new PaymentRequest();
        request.setCardHolderName("John Doe");
        request.setCardNumber("1234567890123456");
        request.setExpireMonth("12");
        request.setExpireYear("2030");
        request.setCvc("123");

        // Mock ThreedsInitialize static creation
        // Since Iyzico SDK uses static methods, we need Mockito's mockStatic or try to
        // rely on behavior if possible/integration test.
        // Pure unit testing static SDK calls is hard without PowerMock or Mockito
        // static.
        // Here we try Mockito static.

        try (var mockedStatic = Mockito.mockStatic(ThreedsInitialize.class)) {
            ThreedsInitialize mockResponse = new ThreedsInitialize();
            mockResponse.setStatus("success");
            mockResponse.setHtmlContent("<div>3DS Content</div>");
            mockResponse.setPaymentId("payment123");

            mockedStatic.when(() -> ThreedsInitialize.create(any(), any())).thenReturn(mockResponse);

            ThreedsInitialize result = iyzicoService.create3DSPayment(request, donation, user);

            assertNotNull(result);
            assertEquals("success", result.getStatus());
            assertEquals("<div>3DS Content</div>", result.getHtmlContent());
        }
    }
}
