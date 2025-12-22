package com.seffafbagis.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seffafbagis.api.config.SecurityConfig;
import com.seffafbagis.api.dto.request.organization.AddDocumentRequest;
import com.seffafbagis.api.dto.response.organization.OrganizationDetailResponse;
import com.seffafbagis.api.dto.response.organization.OrganizationDocumentResponse;
import com.seffafbagis.api.security.CustomUserDetailsService;
import com.seffafbagis.api.security.JwtAuthenticationEntryPoint;
import com.seffafbagis.api.security.JwtAuthenticationFilter;
import com.seffafbagis.api.security.JwtTokenProvider;
import com.seffafbagis.api.service.organization.OrganizationDocumentService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrganizationDocumentController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
@ActiveProfiles("test")
public class OrganizationDocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrganizationDocumentService documentService;

    @MockBean
    private OrganizationService organizationService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private OrganizationDocumentResponse documentResponse;
    private OrganizationDetailResponse organizationDetailResponse;
    private final UUID orgId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        documentResponse = new OrganizationDocumentResponse();
        documentResponse.setId(UUID.randomUUID());
        documentResponse.setDocumentType("tax_certificate");
        documentResponse.setDocumentName("Tax Cert");
        documentResponse.setFileUrl("http://example.com/file.pdf");

        organizationDetailResponse = new OrganizationDetailResponse();
        organizationDetailResponse.setId(orgId);
    }

    @Test
    @WithMockUser(roles = "FOUNDATION")
    void getDocuments_ShouldReturnList() throws Exception {
        when(organizationService.getMyOrganization()).thenReturn(organizationDetailResponse);
        when(documentService.getDocuments(orgId)).thenReturn(Collections.singletonList(documentResponse));

        mockMvc.perform(get("/api/v1/organization/documents")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].documentName").value("Tax Cert"));
    }

    @Test
    @WithMockUser(roles = "FOUNDATION")
    void addDocument_ValidRequest_ShouldCreate() throws Exception {
        AddDocumentRequest request = new AddDocumentRequest();
        request.setDocumentType("tax_certificate");
        request.setDocumentName("Tax Cert");
        request.setFileUrl("http://example.com/file.pdf");

        when(documentService.addDocument(any(AddDocumentRequest.class))).thenReturn(documentResponse);

        mockMvc.perform(post("/api/v1/organization/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.documentName").value("Tax Cert"));
    }

    @Test
    @WithMockUser(roles = "FOUNDATION")
    void deleteDocument_ShouldDelete() throws Exception {
        doNothing().when(documentService).deleteDocument(documentResponse.getId());

        mockMvc.perform(delete("/api/v1/organization/documents/" + documentResponse.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "DONOR")
    void getDocuments_WhenDonor_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/organization/documents")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
