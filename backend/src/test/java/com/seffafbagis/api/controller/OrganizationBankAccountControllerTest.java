package com.seffafbagis.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seffafbagis.api.config.SecurityConfig;
import com.seffafbagis.api.dto.request.organization.AddBankAccountRequest;
import com.seffafbagis.api.dto.request.organization.UpdateBankAccountRequest;
import com.seffafbagis.api.dto.response.organization.OrganizationBankAccountResponse;
import com.seffafbagis.api.dto.response.organization.OrganizationDetailResponse;
import com.seffafbagis.api.security.CustomUserDetailsService;
import com.seffafbagis.api.security.JwtAuthenticationEntryPoint;
import com.seffafbagis.api.security.JwtAuthenticationFilter;
import com.seffafbagis.api.security.JwtTokenProvider;
import com.seffafbagis.api.service.organization.OrganizationBankAccountService;
import com.seffafbagis.api.service.organization.OrganizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrganizationBankAccountController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
@ActiveProfiles("test")
public class OrganizationBankAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrganizationBankAccountService bankAccountService;

    @MockBean
    private OrganizationService organizationService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private OrganizationBankAccountResponse bankAccountResponse;
    private OrganizationDetailResponse organizationDetailResponse;
    private final UUID orgId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        bankAccountResponse = new OrganizationBankAccountResponse();
        bankAccountResponse.setId(UUID.randomUUID());
        bankAccountResponse.setBankName("Test Bank");
        bankAccountResponse.setAccountHolder("Test Holder");
        bankAccountResponse.setIban("TR123456789012345678901234");
        bankAccountResponse.setCurrency("TRY");

        organizationDetailResponse = new OrganizationDetailResponse();
        organizationDetailResponse.setId(orgId);
    }

    @Test
    @WithMockUser(roles = "FOUNDATION")
    void getBankAccounts_ShouldReturnList() throws Exception {
        when(organizationService.getMyOrganization()).thenReturn(organizationDetailResponse);
        when(bankAccountService.getBankAccounts(orgId)).thenReturn(Collections.singletonList(bankAccountResponse));

        mockMvc.perform(get("/api/v1/organization/bank-accounts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].bankName").value("Test Bank"));
    }

    @Test
    @WithMockUser(roles = "FOUNDATION")
    void addBankAccount_ValidRequest_ShouldCreate() throws Exception {
        AddBankAccountRequest request = new AddBankAccountRequest();
        request.setBankName("Test Bank");
        request.setAccountHolder("Test Holder");
        request.setIban("TR123456789012345678901234");

        when(bankAccountService.addBankAccount(any(AddBankAccountRequest.class))).thenReturn(bankAccountResponse);

        mockMvc.perform(post("/api/v1/organization/bank-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.iban").value("TR123456789012345678901234"));
    }

    @Test
    @WithMockUser(roles = "FOUNDATION")
    void updateBankAccount_ValidRequest_ShouldUpdate() throws Exception {
        UpdateBankAccountRequest request = new UpdateBankAccountRequest();
        request.setBankName("Updated Bank");
        request.setAccountHolder("Updated Holder");
        request.setIban("TR123456789012345678901234");

        OrganizationBankAccountResponse updatedResponse = new OrganizationBankAccountResponse();
        updatedResponse.setId(bankAccountResponse.getId());
        updatedResponse.setBankName("Updated Bank");

        when(bankAccountService.updateBankAccount(eq(bankAccountResponse.getId()), any(UpdateBankAccountRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/organization/bank-accounts/" + bankAccountResponse.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bankName").value("Updated Bank"));
    }

    @Test
    @WithMockUser(roles = "FOUNDATION")
    void deleteBankAccount_ShouldDelete() throws Exception {
        doNothing().when(bankAccountService).deleteBankAccount(bankAccountResponse.getId());

        mockMvc.perform(delete("/api/v1/organization/bank-accounts/" + bankAccountResponse.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "DONOR")
    void getBankAccounts_WhenDonor_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/organization/bank-accounts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
