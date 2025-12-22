package com.seffafbagis.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seffafbagis.api.dto.request.organization.CreateOrganizationRequest;
import com.seffafbagis.api.dto.request.organization.UpdateOrganizationRequest;
import com.seffafbagis.api.dto.response.common.PageResponse;
import com.seffafbagis.api.dto.response.organization.OrganizationDetailResponse;
import com.seffafbagis.api.dto.response.organization.OrganizationListResponse;
import com.seffafbagis.api.dto.response.organization.OrganizationResponse;
import com.seffafbagis.api.dto.response.organization.OrganizationSummaryResponse;
import com.seffafbagis.api.enums.OrganizationType;
import com.seffafbagis.api.service.organization.OrganizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.seffafbagis.api.config.SecurityConfig;
import com.seffafbagis.api.security.CustomUserDetailsService;
import com.seffafbagis.api.security.JwtAuthenticationEntryPoint;
import com.seffafbagis.api.security.JwtAuthenticationFilter;
import com.seffafbagis.api.security.JwtTokenProvider;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(OrganizationController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
@ActiveProfiles("test")
class OrganizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrganizationService organizationService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    // JwtAuthenticationFilter is now a real bean imported via @Import

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private OrganizationListResponse listResponse;
    private OrganizationDetailResponse detailResponse;
    private OrganizationResponse organizationResponse;

    @BeforeEach
    void setUp() {
        listResponse = new OrganizationListResponse();
        listResponse.setId(UUID.randomUUID());
        listResponse.setLegalName("Test Org");
        listResponse.setOrganizationType(OrganizationType.FOUNDATION);

        detailResponse = new OrganizationDetailResponse();
        detailResponse.setId(UUID.randomUUID());
        detailResponse.setLegalName("Test Org Detail");

        organizationResponse = new OrganizationResponse();
        organizationResponse.setId(UUID.randomUUID());
        organizationResponse.setLegalName("Test Org Created");
    }

    @Test
    void getAllOrganizations_ShouldReturnPage() throws Exception {
        Page<OrganizationListResponse> page = new PageImpl<>(Collections.singletonList(listResponse));
        when(organizationService.getApprovedOrganizations(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/organizations")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data[0].legalName").value("Test Org"));
    }

    @Test
    void getOrganizationById_ShouldReturnDetail() throws Exception {
        when(organizationService.getOrganizationPublicDetail(any(UUID.class))).thenReturn(detailResponse);

        mockMvc.perform(get("/api/v1/organizations/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.legalName").value("Test Org Detail"));
    }

    @Test
    @WithMockUser(roles = "FOUNDATION")
    void createOrganization_WhenFoundation_ShouldCreate() throws Exception {
        CreateOrganizationRequest request = new CreateOrganizationRequest();
        request.setLegalName("Test Org Created");
        request.setOrganizationType(OrganizationType.FOUNDATION);
        request.setTaxNumber("1234567890");

        when(organizationService.createOrganization(any(CreateOrganizationRequest.class)))
                .thenReturn(organizationResponse);

        mockMvc.perform(post("/api/v1/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.legalName").value("Test Org Created"));
    }

    @Test
    @WithMockUser(roles = "DONOR")
    void createOrganization_WhenDonor_ShouldReturnForbidden() throws Exception {
        CreateOrganizationRequest request = new CreateOrganizationRequest();
        request.setLegalName("Test Org");
        request.setOrganizationType(OrganizationType.FOUNDATION);

        mockMvc.perform(post("/api/v1/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "FOUNDATION")
    void getMyOrganization_ShouldReturnDetail() throws Exception {
        when(organizationService.getMyOrganization()).thenReturn(detailResponse);

        mockMvc.perform(get("/api/v1/organizations/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNotEmpty());
    }
}
