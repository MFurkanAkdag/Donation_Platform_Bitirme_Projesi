package com.seffafbagis.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seffafbagis.api.config.SecurityConfig;
import com.seffafbagis.api.dto.request.organization.AddContactRequest;
import com.seffafbagis.api.dto.request.organization.UpdateContactRequest;
import com.seffafbagis.api.dto.response.organization.OrganizationContactResponse;
import com.seffafbagis.api.dto.response.organization.OrganizationDetailResponse;
import com.seffafbagis.api.security.CustomUserDetailsService;
import com.seffafbagis.api.security.JwtAuthenticationEntryPoint;
import com.seffafbagis.api.security.JwtAuthenticationFilter;
import com.seffafbagis.api.security.JwtTokenProvider;
import com.seffafbagis.api.service.organization.OrganizationContactService;
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

@WebMvcTest(OrganizationContactController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
@ActiveProfiles("test")
public class OrganizationContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrganizationContactService contactService;

    @MockBean
    private OrganizationService organizationService; // Needed for getContacts

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private OrganizationContactResponse contactResponse;
    private OrganizationDetailResponse organizationDetailResponse;
    private final UUID orgId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        contactResponse = new OrganizationContactResponse();
        contactResponse.setId(UUID.randomUUID());
        contactResponse.setContactName("John Doe");
        contactResponse.setEmail("john.doe@example.com");
        contactResponse.setPhone("1234567890");
        contactResponse.setContactType("primary");

        organizationDetailResponse = new OrganizationDetailResponse();
        organizationDetailResponse.setId(orgId);
    }

    @Test
    @WithMockUser(roles = "FOUNDATION")
    void getContacts_ShouldReturnList() throws Exception {
        when(organizationService.getMyOrganization()).thenReturn(organizationDetailResponse);
        when(contactService.getContacts(orgId)).thenReturn(Collections.singletonList(contactResponse));

        mockMvc.perform(get("/api/v1/organization/contacts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].contactName").value("John Doe"));
    }

    @Test
    @WithMockUser(roles = "FOUNDATION")
    void addContact_ValidRequest_ShouldCreate() throws Exception {
        AddContactRequest request = new AddContactRequest();
        request.setContactName("John Doe");
        request.setEmail("john.doe@example.com");
        request.setPhone("1234567890");
        request.setContactType("primary");

        when(contactService.addContact(any(AddContactRequest.class))).thenReturn(contactResponse);

        mockMvc.perform(post("/api/v1/organization/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"));
    }

    @Test
    @WithMockUser(roles = "FOUNDATION")
    void updateContact_ValidRequest_ShouldUpdate() throws Exception {
        UpdateContactRequest request = new UpdateContactRequest();
        request.setContactName("Jane Doe");
        request.setEmail("jane.doe@example.com");
        request.setContactType("support");

        OrganizationContactResponse updatedResponse = new OrganizationContactResponse();
        updatedResponse.setId(contactResponse.getId());
        updatedResponse.setContactName("Jane Doe");
        updatedResponse.setContactType("support");

        when(contactService.updateContact(eq(contactResponse.getId()), any(UpdateContactRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/organization/contacts/" + contactResponse.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.contactName").value("Jane Doe"));
    }

    @Test
    @WithMockUser(roles = "FOUNDATION")
    void deleteContact_ShouldDelete() throws Exception {
        doNothing().when(contactService).deleteContact(contactResponse.getId());

        mockMvc.perform(delete("/api/v1/organization/contacts/" + contactResponse.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "DONOR")
    void getContacts_WhenDonor_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/organization/contacts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
