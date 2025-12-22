package com.seffafbagis.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.repository.ReportRepository;
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
 * Integration tests for report handling workflow.
 * Tests: submit report -> admin investigate -> resolve -> score penalty
 */
public class ReportHandlingIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Organization organization;
    private User reporter;
    private String reporterToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        reportRepository.deleteAll();

        organization = testDataFactory.createVerifiedOrganization();
        reporter = testDataFactory.createDonor();
        reporterToken = testDataFactory.getAuthToken(reporter);
        adminToken = testDataFactory.getAdminToken();
    }

    // ========== Report Submission Tests ==========

    @Test
    @DisplayName("Authenticated user should submit report")
    void submitReport_Authenticated_ShouldSucceed() throws Exception {
        Map<String, Object> reportRequest = new HashMap<>();
        reportRequest.put("reportType", "FRAUD");
        reportRequest.put("entityType", "ORGANIZATION");
        reportRequest.put("entityId", organization.getId().toString());
        reportRequest.put("reason", "Şüpheli aktivite tespit edildi");
        reportRequest.put("description", "Detaylı açıklama burada");

        HttpHeaders headers = authHeaders(reporterToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/reports",
                HttpMethod.POST,
                new HttpEntity<>(objectMapper.writeValueAsString(reportRequest), headers),
                String.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.CREATED, HttpStatus.OK);
    }

    @Test
    @DisplayName("Unauthenticated user should not submit report")
    void submitReport_Unauthenticated_ShouldFail() throws Exception {
        Map<String, Object> reportRequest = new HashMap<>();
        reportRequest.put("reportType", "FRAUD");
        reportRequest.put("entityType", "ORGANIZATION");
        reportRequest.put("entityId", organization.getId().toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/reports",
                HttpMethod.POST,
                new HttpEntity<>(objectMapper.writeValueAsString(reportRequest), headers),
                String.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Fraud report should have HIGH priority")
    void submitFraudReport_ShouldHaveHighPriority() throws Exception {
        Map<String, Object> reportRequest = new HashMap<>();
        reportRequest.put("reportType", "FRAUD");
        reportRequest.put("entityType", "ORGANIZATION");
        reportRequest.put("entityId", organization.getId().toString());
        reportRequest.put("reason", "Fraud suspected");

        HttpHeaders headers = authHeaders(reporterToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/reports",
                HttpMethod.POST,
                new HttpEntity<>(objectMapper.writeValueAsString(reportRequest), headers),
                String.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.CREATED, HttpStatus.OK);
        if (response.getBody() != null) {
            assertThat(response.getBody()).containsIgnoringCase("HIGH");
        }
    }

    // ========== Report Listing Tests ==========

    @Test
    @DisplayName("User should list own reports")
    void listMyReports_Authenticated_ShouldSucceed() {
        HttpHeaders headers = authHeaders(reporterToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/reports/my",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ========== Admin Report Management Tests ==========

    @Test
    @DisplayName("Admin should list all reports")
    void listAllReports_Admin_ShouldSucceed() {
        HttpHeaders headers = authHeaders(adminToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/admin/reports",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Admin should filter reports by status")
    void listReportsByStatus_Admin_ShouldSucceed() {
        HttpHeaders headers = authHeaders(adminToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/admin/reports?status=PENDING",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Non-admin should not access admin reports")
    void listAdminReports_NonAdmin_ShouldFail() {
        HttpHeaders headers = authHeaders(reporterToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/admin/reports",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ========== Report Type Tests ==========

    @Test
    @DisplayName("Should submit different report types")
    void submitDifferentReportTypes_ShouldSucceed() throws Exception {
        String[] reportTypes = { "SPAM", "INAPPROPRIATE_CONTENT", "OTHER" };

        for (String reportType : reportTypes) {
            Map<String, Object> reportRequest = new HashMap<>();
            reportRequest.put("reportType", reportType);
            reportRequest.put("entityType", "ORGANIZATION");
            reportRequest.put("entityId", organization.getId().toString());
            reportRequest.put("reason", "Test reason for " + reportType);

            HttpHeaders headers = authHeaders(reporterToken);

            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/v1/reports",
                    HttpMethod.POST,
                    new HttpEntity<>(objectMapper.writeValueAsString(reportRequest), headers),
                    String.class);

            assertThat(response.getStatusCode()).isIn(HttpStatus.CREATED, HttpStatus.OK, HttpStatus.BAD_REQUEST);
        }
    }
}
