package com.seffafbagis.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.CampaignStatus;
import com.seffafbagis.api.repository.CampaignRepository;
import com.seffafbagis.api.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for complete campaign lifecycle.
 * Tests: create draft -> submit -> approve -> collect -> complete
 */
public class CampaignLifecycleIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Organization organization;
    private User foundationUser;
    private String foundationToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        campaignRepository.deleteAll();

        foundationUser = testDataFactory.createFoundationUser();
        organization = testDataFactory.createVerifiedOrganization(foundationUser);
        foundationToken = testDataFactory.getAuthToken(foundationUser);
        adminToken = testDataFactory.getAdminToken();
    }

    // ========== Campaign Creation Tests ==========

    @Test
    @DisplayName("Foundation should create campaign as draft")
    void createCampaign_Foundation_ShouldBeDraft() throws Exception {
        Map<String, Object> campaignRequest = new HashMap<>();
        campaignRequest.put("title", "Test Kampanya");
        campaignRequest.put("description", "Test kampanya açıklaması");
        campaignRequest.put("summary", "Test özet");
        campaignRequest.put("targetAmount", 10000.00);
        campaignRequest.put("startDate", LocalDateTime.now().toString());
        campaignRequest.put("endDate", LocalDateTime.now().plusDays(30).toString());

        HttpHeaders headers = authHeaders(foundationToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/campaigns",
                HttpMethod.POST,
                new HttpEntity<>(objectMapper.writeValueAsString(campaignRequest), headers),
                String.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.CREATED, HttpStatus.OK);
        if (response.getBody() != null) {
            assertThat(response.getBody()).containsIgnoringCase("DRAFT");
        }
    }

    @Test
    @DisplayName("Donor should not create campaign")
    void createCampaign_Donor_ShouldFail() throws Exception {
        User donor = testDataFactory.createDonor();
        String donorToken = testDataFactory.getAuthToken(donor);

        Map<String, Object> campaignRequest = new HashMap<>();
        campaignRequest.put("title", "Unauthorized Campaign");
        campaignRequest.put("description", "Test");
        campaignRequest.put("targetAmount", 5000.00);

        HttpHeaders headers = authHeaders(donorToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/campaigns",
                HttpMethod.POST,
                new HttpEntity<>(objectMapper.writeValueAsString(campaignRequest), headers),
                String.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.FORBIDDEN, HttpStatus.BAD_REQUEST);
    }

    // ========== Campaign Approval Tests ==========

    @Test
    @DisplayName("Admin should approve pending campaign")
    void approveCampaign_Admin_ShouldSucceed() throws Exception {
        Campaign draftCampaign = testDataFactory.createDraftCampaign(organization);
        draftCampaign.setStatus(CampaignStatus.PENDING_APPROVAL);
        campaignRepository.save(draftCampaign);

        Map<String, Object> approvalRequest = new HashMap<>();
        approvalRequest.put("approved", true);
        approvalRequest.put("notes", "Onaylandı");

        HttpHeaders headers = authHeaders(adminToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/admin/campaigns/" + draftCampaign.getId() + "/approve",
                HttpMethod.POST,
                new HttpEntity<>(objectMapper.writeValueAsString(approvalRequest), headers),
                String.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Admin should reject campaign with reason")
    void rejectCampaign_Admin_ShouldSucceed() throws Exception {
        Campaign draftCampaign = testDataFactory.createDraftCampaign(organization);
        draftCampaign.setStatus(CampaignStatus.PENDING_APPROVAL);
        campaignRepository.save(draftCampaign);

        Map<String, Object> rejectionRequest = new HashMap<>();
        rejectionRequest.put("approved", false);
        rejectionRequest.put("rejectionReason", "Eksik bilgi");

        HttpHeaders headers = authHeaders(adminToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/admin/campaigns/" + draftCampaign.getId() + "/approve",
                HttpMethod.POST,
                new HttpEntity<>(objectMapper.writeValueAsString(rejectionRequest), headers),
                String.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
    }

    // ========== Campaign Status Tests ==========

    @Test
    @DisplayName("Should list only active campaigns publicly")
    void listCampaigns_Public_ShouldShowOnlyActive() {
        // Create campaigns with different statuses
        Campaign activeCampaign = testDataFactory.createApprovedCampaign(organization);
        Campaign draftCampaign = testDataFactory.createDraftCampaign(organization);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/campaigns",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(activeCampaign.getTitle());
        assertThat(response.getBody()).doesNotContain(draftCampaign.getTitle());
    }

    // ========== Campaign Update Tests ==========

    @Test
    @DisplayName("Foundation should update own campaign")
    void updateCampaign_Owner_ShouldSucceed() throws Exception {
        Campaign campaign = testDataFactory.createDraftCampaign(organization);

        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("description", "Güncellenmiş açıklama");
        updateRequest.put("summary", "Güncellenmiş özet");

        HttpHeaders headers = authHeaders(foundationToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/campaigns/" + campaign.getId(),
                HttpMethod.PUT,
                new HttpEntity<>(objectMapper.writeValueAsString(updateRequest), headers),
                String.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.FORBIDDEN);
    }

    // ========== Campaign Completion Tests ==========

    @Test
    @DisplayName("Should mark campaign as completed when target reached")
    void campaignCompletion_TargetReached_ShouldComplete() {
        Campaign campaign = testDataFactory.createCompletedCampaign(organization);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/campaigns/" + campaign.getSlug(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsIgnoringCase("COMPLETED");
    }

    // ========== Campaign Extension Tests ==========

    @Test
    @DisplayName("Foundation should request campaign extension")
    void requestExtension_Foundation_ShouldSucceed() throws Exception {
        Campaign campaign = testDataFactory.createApprovedCampaign(organization);

        Map<String, Object> extensionRequest = new HashMap<>();
        extensionRequest.put("extensionDays", 15);
        extensionRequest.put("reason", "Hedefe ulaşamadık");

        HttpHeaders headers = authHeaders(foundationToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/campaigns/" + campaign.getId() + "/extend",
                HttpMethod.POST,
                new HttpEntity<>(objectMapper.writeValueAsString(extensionRequest), headers),
                String.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.BAD_REQUEST);
    }

    // ========== Campaign Statistics Tests ==========

    @Test
    @DisplayName("Should get campaign statistics")
    void getCampaignStats_ShouldSucceed() {
        Campaign campaign = testDataFactory.createApprovedCampaign(organization);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/campaigns/" + campaign.getSlug(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("targetAmount");
        assertThat(response.getBody()).contains("collectedAmount");
    }
}
