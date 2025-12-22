package com.seffafbagis.api.integration;

import com.seffafbagis.api.dto.request.auth.RegisterRequest;
import com.seffafbagis.api.dto.request.auth.LoginRequest;
import com.seffafbagis.api.dto.request.organization.CreateOrganizationRequest;
import com.seffafbagis.api.dto.response.auth.AuthResponse;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.OrganizationType;
import com.seffafbagis.api.enums.UserRole;
import com.seffafbagis.api.enums.VerificationStatus;
import com.seffafbagis.api.repository.OrganizationRepository;
import com.seffafbagis.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Organization module (Phase 3.0)
 * Tests full verification workflow, public endpoints, and owner endpoints
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class OrganizationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    private String foundationToken;
    private User foundationUser;

    @BeforeEach
    void setUp() {
        organizationRepository.deleteAll();

        // Create FOUNDATION user via API and get token
        foundationToken = registerAndLoginUser("foundation@test.com", "Password123!", UserRole.FOUNDATION);
        foundationUser = userRepository.findByEmail("foundation@test.com").orElseThrow();
    }

    // ========== Public Endpoints Tests ==========

    @Test
    void listApprovedOrganizations_ShouldReturnOnlyApproved() {
        // Create approved organization directly in DB
        Organization approved = createOrganizationEntity(foundationUser, "Approved Org", VerificationStatus.APPROVED);
        organizationRepository.save(approved);

        // Create pending organization with a different user
        String anotherToken = registerAndLoginUser("another@test.com", "Password123!", UserRole.FOUNDATION);
        User anotherUser = userRepository.findByEmail("another@test.com").orElseThrow();
        Organization pending = createOrganizationEntity(anotherUser, "Pending Org", VerificationStatus.PENDING);
        organizationRepository.save(pending);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/organizations",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("Approved Org");
        assertThat(response.getBody()).doesNotContain("Pending Org");
    }

    @Test
    void searchOrganizations_ShouldFindByKeyword() {
        Organization org = createOrganizationEntity(foundationUser, "Hayata Destek Vakfı", VerificationStatus.APPROVED);
        organizationRepository.save(org);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/organizations/search?keyword=Hayata",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("Hayata Destek Vakfı");
    }

    // ========== Owner Endpoints Tests (FOUNDATION role) ==========

    @Test
    void createOrganization_WithFoundationRole_ShouldSucceed() {
        CreateOrganizationRequest request = new CreateOrganizationRequest();
        request.setOrganizationType(OrganizationType.FOUNDATION);
        request.setLegalName("Test Vakfı");
        request.setTaxNumber("1234567890");
        request.setEstablishmentDate(LocalDate.of(2020, 1, 1));
        request.setDescription("Test vakfı açıklaması");

        HttpHeaders headers = authHeaders(foundationToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/organizations",
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("Test Vakfı");

        // Verify organization was created with PENDING status
        Organization created = organizationRepository.findByUserId(foundationUser.getId()).orElse(null);
        assertThat(created).isNotNull();
        assertThat(created.getVerificationStatus()).isEqualTo(VerificationStatus.PENDING);
    }

    @Test
    void createOrganization_WithDonorRole_ShouldFail() {
        // Create donor user and get token
        String donorToken = registerAndLoginUser("donor@test.com", "Password123!", UserRole.DONOR);

        CreateOrganizationRequest request = new CreateOrganizationRequest();
        request.setOrganizationType(OrganizationType.FOUNDATION);
        request.setLegalName("Test Vakfı");
        request.setTaxNumber("9876543210");

        HttpHeaders headers = authHeaders(donorToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/organizations",
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getMyOrganization_WhenExists_ShouldReturnDetail() {
        Organization org = createOrganizationEntity(foundationUser, "My Organization", VerificationStatus.PENDING);
        organizationRepository.save(org);

        HttpHeaders headers = authHeaders(foundationToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/organizations/my",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("My Organization");
    }

    @Test
    void getMyOrganization_WhenNotExists_ShouldReturn404() {
        HttpHeaders headers = authHeaders(foundationToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/organizations/my",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ========== Helper Methods ==========

    private Organization createOrganizationEntity(User user, String legalName, VerificationStatus status) {
        Organization organization = new Organization();
        organization.setUser(user);
        organization.setOrganizationType(OrganizationType.FOUNDATION);
        organization.setLegalName(legalName);
        organization.setTaxNumber("TAX" + UUID.randomUUID().toString().substring(0, 8));
        organization.setVerificationStatus(status);
        organization.setFeatured(false);
        organization.setResubmissionCount(0);
        return organization;
    }

    /**
     * Registers a user via API, activates them, and returns their access token.
     */
    private String registerAndLoginUser(String email, String password, UserRole role) {
        // Register
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);
        registerRequest.setConfirmPassword(password);
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");
        registerRequest.setRole(role);
        registerRequest.setAcceptTerms(true);
        registerRequest.setAcceptKvkk(true);

        restTemplate.postForEntity("/api/v1/auth/register", registerRequest, String.class);

        // Activate user directly in DB
        User user = userRepository.findByEmail(email).orElseThrow();
        user.activate();
        userRepository.save(user);

        // Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/v1/auth/login",
                loginRequest,
                AuthResponse.class);

        if (response.getBody() != null && response.getBody().getAccessToken() != null) {
            return response.getBody().getAccessToken();
        }
        throw new RuntimeException("Failed to login user: " + email);
    }
}
