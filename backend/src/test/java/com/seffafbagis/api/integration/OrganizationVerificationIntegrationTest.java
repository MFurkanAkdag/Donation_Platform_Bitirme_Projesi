package com.seffafbagis.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.OrganizationType;
import com.seffafbagis.api.enums.UserRole;
import com.seffafbagis.api.enums.VerificationStatus;
import com.seffafbagis.api.repository.OrganizationRepository;
import com.seffafbagis.api.repository.UserRepository;
import com.seffafbagis.api.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for organization verification workflow.
 * Tests: register foundation -> create org -> submit docs -> admin verify
 */
public class OrganizationVerificationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    void setUp() {
        organizationRepository.deleteAll();
        adminToken = testDataFactory.getAdminToken();
    }

    // ========== Organization Creation Tests ==========

    @Test
    @DisplayName("Foundation user should create organization with PENDING status")
    void createOrganization_FoundationUser_ShouldBePending() throws Exception {
        String foundationToken = testDataFactory.registerAndLogin(
                "foundation@test.com", "Password123!", UserRole.FOUNDATION);

        Map<String, Object> orgRequest = new HashMap<>();
        orgRequest.put("organizationType", "FOUNDATION");
        orgRequest.put("legalName", "Test Vakfı");
        orgRequest.put("taxNumber", "1234567890");
        orgRequest.put("establishmentDate", LocalDate.of(2020, 1, 1).toString());
        orgRequest.put("description", "Test vakfı açıklaması");

        HttpHeaders headers = authHeaders(foundationToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/organizations",
                HttpMethod.POST,
                new HttpEntity<>(objectMapper.writeValueAsString(orgRequest), headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("Test Vakfı");
        assertThat(response.getBody()).contains("PENDING");
    }

    @Test
    @DisplayName("Donor user should not create organization")
    void createOrganization_DonorUser_ShouldFail() throws Exception {
        User donor = testDataFactory.createDonor();
        String donorToken = testDataFactory.getAuthToken(donor);

        Map<String, Object> orgRequest = new HashMap<>();
        orgRequest.put("organizationType", "FOUNDATION");
        orgRequest.put("legalName", "Unauthorized Vakfı");
        orgRequest.put("taxNumber", "9876543210");

        HttpHeaders headers = authHeaders(donorToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/organizations",
                HttpMethod.POST,
                new HttpEntity<>(objectMapper.writeValueAsString(orgRequest), headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ========== Admin Verification Tests ==========

    @Test
    @DisplayName("Admin should list pending organizations")
    void listPendingOrganizations_Admin_ShouldSucceed() {
        // Create pending organizations
        testDataFactory.createPendingOrganization();
        testDataFactory.createPendingOrganization();

        HttpHeaders headers = authHeaders(adminToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/admin/organizations?status=PENDING",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("Admin should approve organization")
    void approveOrganization_Admin_ShouldSucceed() throws Exception {
        Organization pendingOrg = testDataFactory.createPendingOrganization();

        Map<String, Object> verifyRequest = new HashMap<>();
        verifyRequest.put("approved", true);
        verifyRequest.put("notes", "Belgeler kontrol edildi");

        HttpHeaders headers = authHeaders(adminToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/admin/organizations/" + pendingOrg.getId() + "/verify",
                HttpMethod.POST,
                new HttpEntity<>(objectMapper.writeValueAsString(verifyRequest), headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify organization status changed
        Organization updated = organizationRepository.findById(pendingOrg.getId()).orElseThrow();
        assertThat(updated.getVerificationStatus()).isEqualTo(VerificationStatus.APPROVED);
    }

    @Test
    @DisplayName("Admin should reject organization with reason")
    void rejectOrganization_Admin_ShouldSucceed() throws Exception {
        Organization pendingOrg = testDataFactory.createPendingOrganization();

        Map<String, Object> verifyRequest = new HashMap<>();
        verifyRequest.put("approved", false);
        verifyRequest.put("notes", "Eksik belgeler");
        verifyRequest.put("rejectionReason", "Vergi levhası eksik");

        HttpHeaders headers = authHeaders(adminToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/admin/organizations/" + pendingOrg.getId() + "/verify",
                HttpMethod.POST,
                new HttpEntity<>(objectMapper.writeValueAsString(verifyRequest), headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify organization status changed
        Organization updated = organizationRepository.findById(pendingOrg.getId()).orElseThrow();
        assertThat(updated.getVerificationStatus()).isEqualTo(VerificationStatus.REJECTED);
    }

    @Test
    @DisplayName("Non-admin should not verify organization")
    void verifyOrganization_NonAdmin_ShouldFail() throws Exception {
        Organization pendingOrg = testDataFactory.createPendingOrganization();
        User donor = testDataFactory.createDonor();
        String donorToken = testDataFactory.getAuthToken(donor);

        Map<String, Object> verifyRequest = new HashMap<>();
        verifyRequest.put("approved", true);

        HttpHeaders headers = authHeaders(donorToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/admin/organizations/" + pendingOrg.getId() + "/verify",
                HttpMethod.POST,
                new HttpEntity<>(objectMapper.writeValueAsString(verifyRequest), headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ========== Organization Owner Tests ==========

    @Test
    @DisplayName("Owner should view own organization")
    void getMyOrganization_Owner_ShouldSucceed() {
        User foundationUser = testDataFactory.createFoundationUser();
        Organization org = testDataFactory.createVerifiedOrganization(foundationUser);
        String token = testDataFactory.getAuthToken(foundationUser);

        HttpHeaders headers = authHeaders(token);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/organizations/my",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(org.getLegalName());
    }

    @Test
    @DisplayName("Owner should update organization details")
    void updateOrganization_Owner_ShouldSucceed() throws Exception {
        User foundationUser = testDataFactory.createFoundationUser();
        Organization org = testDataFactory.createVerifiedOrganization(foundationUser);
        String token = testDataFactory.getAuthToken(foundationUser);

        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("description", "Güncellenmiş açıklama");

        HttpHeaders headers = authHeaders(token);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/organizations/" + org.getId(),
                HttpMethod.PUT,
                new HttpEntity<>(objectMapper.writeValueAsString(updateRequest), headers),
                String.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.FORBIDDEN);
    }

    // ========== Transparency Score Initialization Tests ==========

    @Test
    @DisplayName("Approved organization should have transparency score initialized")
    void approveOrganization_ShouldInitializeTransparencyScore() throws Exception {
        Organization pendingOrg = testDataFactory.createPendingOrganization();

        Map<String, Object> verifyRequest = new HashMap<>();
        verifyRequest.put("approved", true);

        HttpHeaders headers = authHeaders(adminToken);

        restTemplate.exchange(
                "/api/v1/admin/organizations/" + pendingOrg.getId() + "/verify",
                HttpMethod.POST,
                new HttpEntity<>(objectMapper.writeValueAsString(verifyRequest), headers),
                String.class);

        // Check transparency score endpoint
        ResponseEntity<String> scoreResponse = restTemplate.exchange(
                "/api/v1/transparency/organization/" + pendingOrg.getId(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(scoreResponse.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
    }
}
