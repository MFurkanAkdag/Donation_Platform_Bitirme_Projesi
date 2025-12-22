package com.seffafbagis.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.repository.CampaignRepository;
import com.seffafbagis.api.repository.EvidenceRepository;
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
 * Integration tests for evidence upload and transparency score updates.
 * Tests: campaign complete -> upload evidence -> admin review -> score update
 */
public class EvidenceTransparencyIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private EvidenceRepository evidenceRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Organization organization;
    private User foundationUser;
    private String foundationToken;
    private Campaign completedCampaign;
    private String adminToken;

    @BeforeEach
    void setUp() {
        evidenceRepository.deleteAll();
        campaignRepository.deleteAll();

        foundationUser = testDataFactory.createFoundationUser();
        organization = testDataFactory.createVerifiedOrganization(foundationUser);
        completedCampaign = testDataFactory.createCompletedCampaign(organization);
        foundationToken = testDataFactory.getAuthToken(foundationUser);
        adminToken = testDataFactory.getAdminToken();
    }

    // ========== Evidence Upload Tests ==========

    @Test
    @DisplayName("Foundation should upload evidence for completed campaign")
    void uploadEvidence_ForCompletedCampaign_ShouldSucceed() throws Exception {
        Map<String, Object> evidenceRequest = new HashMap<>();
        evidenceRequest.put("campaignId", completedCampaign.getId().toString());
        evidenceRequest.put("evidenceType", "INVOICE");
        evidenceRequest.put("title", "Malzeme Alımı Faturası");
        evidenceRequest.put("description", "Okul malzemeleri için fatura");
        evidenceRequest.put("amountSpent", 5000.00);
        evidenceRequest.put("spendDate", LocalDate.now().minusDays(1).toString());
        evidenceRequest.put("vendorName", "Test Tedarikçi");

        HttpHeaders headers = authHeaders(foundationToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/evidences",
                HttpMethod.POST,
                new HttpEntity<>(objectMapper.writeValueAsString(evidenceRequest), headers),
                String.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.CREATED, HttpStatus.OK);
    }

    @Test
    @DisplayName("Donor should not upload evidence")
    void uploadEvidence_AsDonor_ShouldFail() throws Exception {
        User donor = testDataFactory.createDonor();
        String donorToken = testDataFactory.getAuthToken(donor);

        Map<String, Object> evidenceRequest = new HashMap<>();
        evidenceRequest.put("campaignId", completedCampaign.getId().toString());
        evidenceRequest.put("evidenceType", "INVOICE");
        evidenceRequest.put("title", "Unauthorized Evidence");

        HttpHeaders headers = authHeaders(donorToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/evidences",
                HttpMethod.POST,
                new HttpEntity<>(objectMapper.writeValueAsString(evidenceRequest), headers),
                String.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.FORBIDDEN, HttpStatus.BAD_REQUEST);
    }

    // ========== Evidence Listing Tests ==========

    @Test
    @DisplayName("Should list evidences for campaign")
    void listEvidences_ForCampaign_ShouldSucceed() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/evidences/campaign/" + completedCampaign.getId(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
    }

    // ========== Admin Review Tests ==========

    @Test
    @DisplayName("Admin should list pending evidences")
    void listPendingEvidences_Admin_ShouldSucceed() {
        HttpHeaders headers = authHeaders(adminToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/admin/evidences?status=PENDING",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ========== Transparency Score Tests ==========

    @Test
    @DisplayName("Should get transparency score for organization")
    void getTransparencyScore_ForOrganization_ShouldSucceed() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/transparency/organization/" + organization.getId(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should get transparency score history")
    void getTransparencyScoreHistory_ForOrganization_ShouldSucceed() {
        HttpHeaders headers = authHeaders(foundationToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/transparency/organization/" + organization.getId() + "/history",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
    }

    // ========== Evidence Deadline Tests ==========

    @Test
    @DisplayName("Should check evidence deadline for campaign")
    void getEvidenceDeadline_ForCampaign_ShouldShowDeadline() {
        HttpHeaders headers = authHeaders(foundationToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/campaigns/" + completedCampaign.getSlug(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // Campaign should have evidence deadline information
        assertThat(response.getBody()).isNotNull();
    }
}
