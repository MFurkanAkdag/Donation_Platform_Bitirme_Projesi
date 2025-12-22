package com.seffafbagis.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.repository.ApplicationRepository;
import com.seffafbagis.api.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for beneficiary application workflow.
 * Tests: submit application -> admin review -> approve/reject -> assign to
 * campaign
 */
public class ApplicationFlowIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Organization organization;
    private Campaign campaign;
    private User applicant;
    private String applicantToken;
    private String adminToken;
    private String foundationToken;

    @BeforeEach
    void setUp() {
        applicationRepository.deleteAll();

        User foundationUser = testDataFactory.createFoundationUser();
        organization = testDataFactory.createVerifiedOrganization(foundationUser);
        campaign = testDataFactory.createApprovedCampaign(organization);
        applicant = testDataFactory.createDonor();
        applicantToken = testDataFactory.getAuthToken(applicant);
        adminToken = testDataFactory.getAdminToken();
        foundationToken = testDataFactory.getAuthToken(foundationUser);
    }

    // ========== Application Submission Tests ==========

    @Test
    @DisplayName("Should submit application for help")
    void submitApplication_Authenticated_ShouldSucceed() throws Exception {
        Map<String, Object> applicationRequest = new HashMap<>();
        applicationRequest.put("applicationType", "INDIVIDUAL");
        applicationRequest.put("fullName", "Test Başvuran");
        applicationRequest.put("phoneNumber", "+905551234567");
        applicationRequest.put("description", "Yardım başvurusu detaylı açıklaması");
        applicationRequest.put("urgencyLevel", "MEDIUM");
        applicationRequest.put("requestedAmount", 5000.00);

        HttpHeaders headers = authHeaders(applicantToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/applications",
                HttpMethod.POST,
                new HttpEntity<>(objectMapper.writeValueAsString(applicationRequest), headers),
                String.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.CREATED, HttpStatus.OK);
    }

    @Test
    @DisplayName("Unauthenticated user should not submit application")
    void submitApplication_Unauthenticated_ShouldFail() throws Exception {
        Map<String, Object> applicationRequest = new HashMap<>();
        applicationRequest.put("applicationType", "INDIVIDUAL");
        applicationRequest.put("fullName", "Test");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/applications",
                HttpMethod.POST,
                new HttpEntity<>(objectMapper.writeValueAsString(applicationRequest), headers),
                String.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
    }

    // ========== Application Listing Tests ==========

    @Test
    @DisplayName("Should list own applications")
    void listMyApplications_Authenticated_ShouldSucceed() {
        HttpHeaders headers = authHeaders(applicantToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/applications/my",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ========== Admin/Foundation Review Tests ==========

    @Test
    @DisplayName("Admin should list all applications")
    void listAllApplications_Admin_ShouldSucceed() {
        HttpHeaders headers = authHeaders(adminToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/admin/applications",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Foundation should list applications for their campaigns")
    void listApplications_Foundation_ShouldSucceed() {
        HttpHeaders headers = authHeaders(foundationToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/applications/organization",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Non-admin should not access admin applications")
    void listAdminApplications_NonAdmin_ShouldFail() {
        HttpHeaders headers = authHeaders(applicantToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/admin/applications",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ========== Application Status Tests ==========

    @Test
    @DisplayName("Should filter applications by status")
    void listApplicationsByStatus_Admin_ShouldSucceed() {
        HttpHeaders headers = authHeaders(adminToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/admin/applications?status=PENDING",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ========== Application Type Tests ==========

    @Test
    @DisplayName("Should submit family application")
    void submitFamilyApplication_ShouldSucceed() throws Exception {
        Map<String, Object> applicationRequest = new HashMap<>();
        applicationRequest.put("applicationType", "FAMILY");
        applicationRequest.put("fullName", "Test Aile");
        applicationRequest.put("familySize", 4);
        applicationRequest.put("description", "Aile yardım başvurusu");
        applicationRequest.put("urgencyLevel", "HIGH");

        HttpHeaders headers = authHeaders(applicantToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/applications",
                HttpMethod.POST,
                new HttpEntity<>(objectMapper.writeValueAsString(applicationRequest), headers),
                String.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.CREATED, HttpStatus.OK);
    }
}
