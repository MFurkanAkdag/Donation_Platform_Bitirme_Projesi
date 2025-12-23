package com.seffafbagis.api.controller.donation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seffafbagis.api.dto.request.donation.CreateDonationRequest;
import com.seffafbagis.api.service.donation.DonationReceiptService;
import com.seffafbagis.api.service.donation.DonationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DonationController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for simple check or configure them
// Using addFilters = false to avoid complex security setup in unit/slice test
// For full integration test we would use @SpringBootTest but that's heavy
public class DonationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DonationService donationService;

    @MockBean
    private com.seffafbagis.api.security.JwtTokenProvider jwtTokenProvider;

    @MockBean
    private com.seffafbagis.api.security.CustomUserDetailsService customUserDetailsService;

    @MockBean
    private com.seffafbagis.api.security.JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private com.seffafbagis.api.service.donation.DonationReceiptService donationReceiptService; // Needed as it is
                                                                                                // injected in
                                                                                                // controller

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void createDonation_ShouldReturnCreated_WhenValidRequest() throws Exception {
        UUID campaignId = UUID.randomUUID();
        CreateDonationRequest request = new CreateDonationRequest();
        request.setCampaignId(campaignId);
        request.setAmount(BigDecimal.valueOf(100));

        request.setDonorDisplayName("Test Donor");

        when(donationService.createDonation(any(CreateDonationRequest.class))).thenReturn(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/donations")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser
    void createDonation_ShouldReturnBadRequest_WhenAmountIsInvalid() throws Exception {
        UUID campaignId = UUID.randomUUID();
        CreateDonationRequest request = new CreateDonationRequest();
        request.setCampaignId(campaignId);
        // Minimal amount is 1.00 as per DTO validation
        request.setAmount(BigDecimal.valueOf(0.5));

        mockMvc.perform(post("/api/v1/donations")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
