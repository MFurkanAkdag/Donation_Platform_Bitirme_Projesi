package com.seffafbagis.api.util;

import com.seffafbagis.api.dto.request.auth.LoginRequest;
import com.seffafbagis.api.dto.request.auth.RegisterRequest;
import com.seffafbagis.api.dto.response.auth.AuthResponse;
import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.category.Category;
import com.seffafbagis.api.entity.category.DonationType;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.CampaignStatus;
import com.seffafbagis.api.enums.DonationTypeCode;
import com.seffafbagis.api.enums.OrganizationType;
import com.seffafbagis.api.enums.UserRole;
import com.seffafbagis.api.enums.VerificationStatus;
import com.seffafbagis.api.repository.CampaignRepository;
import com.seffafbagis.api.repository.CategoryRepository;
import com.seffafbagis.api.repository.DonationTypeRepository;
import com.seffafbagis.api.repository.OrganizationRepository;
import com.seffafbagis.api.repository.UserRepository;
import com.seffafbagis.api.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Factory for creating test data in integration tests.
 * Centralizes test entity creation to avoid duplication across test classes.
 */
@Component
public class TestDataFactory {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DonationTypeRepository donationTypeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String DEFAULT_PASSWORD = "Test123!";

    // ========== User Creation Methods ==========

    /**
     * Creates a donor user with ACTIVE status and verified email.
     */
    public User createDonor() {
        return createDonor("donor_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com");
    }

    /**
     * Creates a donor user with the specified email.
     */
    public User createDonor(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setRole(UserRole.DONOR);
        user.setEmailVerified(true);
        user.activate();
        return userRepository.save(user);
    }

    /**
     * Creates a foundation user with ACTIVE status.
     */
    public User createFoundationUser() {
        return createFoundationUser("foundation_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com");
    }

    /**
     * Creates a foundation user with the specified email.
     */
    public User createFoundationUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setRole(UserRole.FOUNDATION);
        user.setEmailVerified(true);
        user.activate();
        return userRepository.save(user);
    }

    /**
     * Creates an admin user with ACTIVE status.
     */
    public User createAdmin() {
        return createAdmin("admin_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com");
    }

    /**
     * Creates an admin user with the specified email.
     */
    public User createAdmin(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setRole(UserRole.ADMIN);
        user.setEmailVerified(true);
        user.activate();
        return userRepository.save(user);
    }

    // ========== Organization Creation Methods ==========

    /**
     * Creates a verified (APPROVED) organization with a foundation user.
     */
    public Organization createVerifiedOrganization() {
        User user = createFoundationUser();
        return createVerifiedOrganization(user);
    }

    /**
     * Creates a verified (APPROVED) organization for the specified user.
     */
    public Organization createVerifiedOrganization(User user) {
        Organization org = new Organization();
        org.setUser(user);
        org.setOrganizationType(OrganizationType.FOUNDATION);
        org.setLegalName("Test Vakfı " + UUID.randomUUID().toString().substring(0, 8));
        org.setTaxNumber(generateTaxNumber());
        org.setVerificationStatus(VerificationStatus.APPROVED);
        org.setFeatured(false);
        org.setResubmissionCount(0);
        return organizationRepository.save(org);
    }

    /**
     * Creates a pending organization awaiting verification.
     */
    public Organization createPendingOrganization() {
        User user = createFoundationUser();
        return createPendingOrganization(user);
    }

    /**
     * Creates a pending organization for the specified user.
     */
    public Organization createPendingOrganization(User user) {
        Organization org = new Organization();
        org.setUser(user);
        org.setOrganizationType(OrganizationType.FOUNDATION);
        org.setLegalName("Pending Vakfı " + UUID.randomUUID().toString().substring(0, 8));
        org.setTaxNumber(generateTaxNumber());
        org.setVerificationStatus(VerificationStatus.PENDING);
        org.setFeatured(false);
        org.setResubmissionCount(0);
        return organizationRepository.save(org);
    }

    // ========== Campaign Creation Methods ==========

    /**
     * Creates an approved (ACTIVE) campaign.
     */
    public Campaign createApprovedCampaign(Organization org) {
        Campaign campaign = new Campaign();
        campaign.setOrganization(org);
        campaign.setTitle("Test Kampanya " + UUID.randomUUID().toString().substring(0, 8));
        campaign.setSlug(generateSlug(campaign.getTitle()));
        campaign.setDescription("Test kampanya açıklaması");
        campaign.setSummary("Test özet");
        campaign.setTargetAmount(new BigDecimal("10000.00"));
        campaign.setCollectedAmount(BigDecimal.ZERO);
        campaign.setDonorCount(0);
        campaign.setStatus(CampaignStatus.ACTIVE);
        campaign.setStartDate(LocalDateTime.now());
        campaign.setEndDate(LocalDateTime.now().plusDays(30));
        campaign.setCurrency("TRY");
        return campaignRepository.save(campaign);
    }

    /**
     * Creates a draft campaign.
     */
    public Campaign createDraftCampaign(Organization org) {
        Campaign campaign = new Campaign();
        campaign.setOrganization(org);
        campaign.setTitle("Draft Kampanya " + UUID.randomUUID().toString().substring(0, 8));
        campaign.setSlug(generateSlug(campaign.getTitle()));
        campaign.setDescription("Draft kampanya açıklaması");
        campaign.setSummary("Draft özet");
        campaign.setTargetAmount(new BigDecimal("5000.00"));
        campaign.setCollectedAmount(BigDecimal.ZERO);
        campaign.setDonorCount(0);
        campaign.setStatus(CampaignStatus.DRAFT);
        campaign.setStartDate(LocalDateTime.now());
        campaign.setEndDate(LocalDateTime.now().plusDays(30));
        campaign.setCurrency("TRY");
        return campaignRepository.save(campaign);
    }

    /**
     * Creates a completed campaign with collected amount equal to target.
     */
    public Campaign createCompletedCampaign(Organization org) {
        Campaign campaign = createApprovedCampaign(org);
        campaign.setStatus(CampaignStatus.COMPLETED);
        campaign.setCollectedAmount(campaign.getTargetAmount());
        campaign.setDonorCount(10);
        campaign.setCompletedAt(LocalDateTime.now());
        return campaignRepository.save(campaign);
    }

    // ========== Category & Donation Type Methods ==========

    /**
     * Creates a category (e.g., Eğitim, Sağlık).
     */
    public Category createCategory(String name) {
        Category category = new Category();
        category.setName(name);
        category.setSlug(generateSlug(name));
        category.setDescription(name + " kategori açıklaması");
        category.setActive(true);
        category.setDisplayOrder(1);
        return categoryRepository.save(category);
    }

    /**
     * Creates or retrieves a general donation type.
     */
    public DonationType getOrCreateGeneralDonationType() {
        return donationTypeRepository.findAll().stream()
                .filter(dt -> DonationTypeCode.GENEL.equals(dt.getTypeCode()))
                .findFirst()
                .orElseGet(() -> {
                    DonationType dt = new DonationType();
                    dt.setTypeCode(DonationTypeCode.GENEL);
                    dt.setName("Genel Bağış");
                    dt.setDescription("Genel amaçlı bağış");
                    dt.setActive(true);
                    return donationTypeRepository.save(dt);
                });
    }

    // ========== Authentication Methods ==========

    /**
     * Gets a JWT token for the specified user.
     */
    public String getAuthToken(User user) {
        return jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
    }

    /**
     * Gets a JWT token for an admin user (creates one if needed).
     */
    public String getAdminToken() {
        User admin = userRepository.findByRole(UserRole.ADMIN).stream()
                .findFirst()
                .orElseGet(this::createAdmin);
        return getAuthToken(admin);
    }

    /**
     * Registers a user via API and returns the auth token.
     */
    public String registerAndLogin(String email, String password, UserRole role) {
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

        // Activate user directly
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

    // ========== Helper Methods ==========

    /**
     * Generates a unique tax number.
     */
    private String generateTaxNumber() {
        return "TAX" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4);
    }

    /**
     * Generates a URL-friendly slug from text.
     */
    private String generateSlug(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                + "-" + UUID.randomUUID().toString().substring(0, 6);
    }

    /**
     * Returns the default test password.
     */
    public String getDefaultPassword() {
        return DEFAULT_PASSWORD;
    }

    /**
     * Cleans up all test data.
     */
    public void cleanupAll() {
        campaignRepository.deleteAll();
        organizationRepository.deleteAll();
        donationTypeRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }
}
