# PHASE 17.0: INTEGRATION TESTING & FINAL POLISH

## Context and Background

You are working on the "Şeffaf Bağış Platformu" (Transparent Donation Platform), a graduation project for Fatih Sultan Mehmet Vakıf University.

**Project Stack:** Java 17, Spring Boot 3.x, PostgreSQL 15+, Redis, Maven

**Current Phase:** 17.0 - Integration Testing & Final Polish (FINAL PHASE)

**Previous Phases Completed:**
- Phase 1.0-15.0: All foundation modules ✅
- Phase 16.0: Event System & Integration ✅

---

## Objective

Complete comprehensive integration testing of all modules, ensure all components work together seamlessly, fix any remaining issues, and prepare the backend for production deployment.

---

## What This Phase Will Solve

1. **End-to-End Testing**: Verify complete user journeys work
2. **Cross-Module Integration**: Ensure all modules communicate correctly
3. **Data Integrity**: Verify database operations and constraints
4. **API Contract Validation**: All endpoints return expected responses
5. **Performance Baseline**: Basic load testing
6. **Documentation**: API documentation completeness
7. **Code Quality**: Final review and cleanup

---

## Integration Test Scenarios

### Location: `src/test/java/com/seffafbagis/api/integration/`

---

### 1. Complete Donation Flow Test

**File:** `DonationFlowIntegrationTest.java`

**Scenario:** Full donation lifecycle from campaign discovery to receipt

```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DonationFlowIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private TestDataFactory testDataFactory;
    
    @Test
    @DisplayName("Complete donation flow: browse → donate → payment → receipt")
    void testCompleteDonationFlow() throws Exception {
        // 1. Setup: Create verified organization with approved campaign
        Organization org = testDataFactory.createVerifiedOrganization();
        Campaign campaign = testDataFactory.createApprovedCampaign(org);
        User donor = testDataFactory.createDonor();
        String donorToken = testDataFactory.getAuthToken(donor);
        
        // 2. Browse campaigns (public)
        mockMvc.perform(get("/api/v1/campaigns")
                .param("status", "ACTIVE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].id").value(campaign.getId().toString()));
        
        // 3. View campaign detail (public)
        mockMvc.perform(get("/api/v1/campaigns/" + campaign.getSlug()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value(campaign.getTitle()))
            .andExpect(jsonPath("$.data.targetAmount").exists());
        
        // 4. Create donation (authenticated)
        CreateDonationRequest donationRequest = new CreateDonationRequest();
        donationRequest.setCampaignId(campaign.getId());
        donationRequest.setAmount(new BigDecimal("100.00"));
        donationRequest.setDonationTypeId(testDataFactory.getGeneralDonationType().getId());
        donationRequest.setAnonymous(false);
        
        MvcResult donationResult = mockMvc.perform(post("/api/v1/donations")
                .header("Authorization", "Bearer " + donorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(donationRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.status").value("PENDING"))
            .andReturn();
        
        UUID donationId = extractIdFromResponse(donationResult);
        
        // 5. Process payment
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setDonationId(donationId);
        paymentRequest.setCardHolderName("Test User");
        paymentRequest.setCardNumber("5528790000000008"); // Iyzico test card
        paymentRequest.setExpireMonth("12");
        paymentRequest.setExpireYear("2030");
        paymentRequest.setCvc("123");
        
        mockMvc.perform(post("/api/v1/payments/process")
                .header("Authorization", "Bearer " + donorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("SUCCESS"));
        
        // 6. Verify donation completed
        mockMvc.perform(get("/api/v1/donations/" + donationId)
                .header("Authorization", "Bearer " + donorToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("COMPLETED"));
        
        // 7. Verify campaign stats updated
        mockMvc.perform(get("/api/v1/campaigns/" + campaign.getSlug()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.collectedAmount").value(100.00))
            .andExpect(jsonPath("$.data.donorCount").value(1));
        
        // 8. Verify receipt generated
        mockMvc.perform(get("/api/v1/donations/" + donationId + "/receipt")
                .header("Authorization", "Bearer " + donorToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.receiptNumber").exists());
        
        // 9. Verify notification created
        mockMvc.perform(get("/api/v1/notifications/unread")
                .header("Authorization", "Bearer " + donorToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[?(@.type=='DONATION_COMPLETED')]").exists());
    }
}
```

---

### 2. Organization Verification Flow Test

**File:** `OrganizationVerificationIntegrationTest.java`

**Scenario:** Organization registration through verification

```java
@Test
@DisplayName("Organization verification flow: register → submit docs → admin review → approved")
void testOrganizationVerificationFlow() throws Exception {
    // 1. Register as foundation user
    RegisterRequest registerRequest = new RegisterRequest();
    registerRequest.setEmail("foundation@test.com");
    registerRequest.setPassword("SecurePass123!");
    registerRequest.setRole(UserRole.FOUNDATION);
    
    MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isCreated())
        .andReturn();
    
    String foundationToken = extractTokenFromResponse(registerResult);
    
    // 2. Create organization
    CreateOrganizationRequest orgRequest = new CreateOrganizationRequest();
    orgRequest.setOrganizationType(OrganizationType.FOUNDATION);
    orgRequest.setLegalName("Test Vakfı");
    orgRequest.setTaxNumber("1234567890");
    // ... other fields
    
    MvcResult orgResult = mockMvc.perform(post("/api/v1/organizations")
            .header("Authorization", "Bearer " + foundationToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(orgRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.verificationStatus").value("PENDING"))
        .andReturn();
    
    UUID orgId = extractIdFromResponse(orgResult);
    
    // 3. Upload required documents
    // Tax certificate, authorization, etc.
    mockMvc.perform(post("/api/v1/organizations/" + orgId + "/documents")
            .header("Authorization", "Bearer " + foundationToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(createDocumentRequest("tax_certificate")))
        .andExpect(status().isCreated());
    
    // 4. Submit for verification
    mockMvc.perform(post("/api/v1/organizations/" + orgId + "/submit")
            .header("Authorization", "Bearer " + foundationToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.verificationStatus").value("IN_REVIEW"));
    
    // 5. Admin approves
    String adminToken = testDataFactory.getAdminToken();
    
    VerifyOrganizationRequest verifyRequest = new VerifyOrganizationRequest();
    verifyRequest.setApproved(true);
    
    mockMvc.perform(post("/api/v1/admin/organizations/" + orgId + "/verify")
            .header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(verifyRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.verificationStatus").value("APPROVED"));
    
    // 6. Verify transparency score initialized
    mockMvc.perform(get("/api/v1/transparency/organization/" + orgId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.currentScore").value(50.00));
    
    // 7. Organization can now create campaigns
    CreateCampaignRequest campaignRequest = testDataFactory.createCampaignRequest();
    
    mockMvc.perform(post("/api/v1/campaigns")
            .header("Authorization", "Bearer " + foundationToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(campaignRequest)))
        .andExpect(status().isCreated());
}
```

---

### 3. Evidence & Transparency Score Flow Test

**File:** `EvidenceTransparencyIntegrationTest.java`

**Scenario:** Campaign completion → evidence upload → score update

```java
@Test
@DisplayName("Evidence flow: campaign complete → upload evidence → admin review → score update")
void testEvidenceAndTransparencyFlow() throws Exception {
    // 1. Setup: Create completed campaign
    Organization org = testDataFactory.createVerifiedOrganization();
    Campaign campaign = testDataFactory.createCompletedCampaign(org);
    String foundationToken = testDataFactory.getAuthToken(org.getUser());
    String adminToken = testDataFactory.getAdminToken();
    
    BigDecimal initialScore = getTransparencyScore(org.getId());
    
    // 2. Upload evidence
    CreateEvidenceRequest evidenceRequest = new CreateEvidenceRequest();
    evidenceRequest.setCampaignId(campaign.getId());
    evidenceRequest.setEvidenceType(EvidenceType.INVOICE);
    evidenceRequest.setTitle("Malzeme Alımı Faturası");
    evidenceRequest.setAmountSpent(campaign.getCollectedAmount());
    evidenceRequest.setSpendDate(LocalDate.now().minusDays(1));
    evidenceRequest.setVendorName("Test Tedarikçi");
    evidenceRequest.setDocuments(List.of(createDocumentData()));
    
    MvcResult evidenceResult = mockMvc.perform(post("/api/v1/evidences")
            .header("Authorization", "Bearer " + foundationToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(evidenceRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.status").value("PENDING"))
        .andReturn();
    
    UUID evidenceId = extractIdFromResponse(evidenceResult);
    
    // 3. Admin approves evidence
    ReviewEvidenceRequest reviewRequest = new ReviewEvidenceRequest();
    reviewRequest.setApproved(true);
    
    mockMvc.perform(post("/api/v1/admin/evidences/" + evidenceId + "/review")
            .header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(reviewRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("APPROVED"));
    
    // 4. Verify transparency score increased
    BigDecimal newScore = getTransparencyScore(org.getId());
    assertTrue(newScore.compareTo(initialScore) > 0, 
        "Score should increase after evidence approval");
    
    // 5. Verify score history entry created
    mockMvc.perform(get("/api/v1/transparency/organization/" + org.getId() + "/history")
            .header("Authorization", "Bearer " + foundationToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].changeReason").value("EVIDENCE_APPROVED_ON_TIME"));
}
```

---

### 4. Report & Fraud Handling Test

**File:** `ReportHandlingIntegrationTest.java`

**Scenario:** Report submission → admin review → action taken

```java
@Test
@DisplayName("Report flow: submit fraud report → investigate → resolve → score penalty")
void testFraudReportFlow() throws Exception {
    // 1. Setup
    Organization org = testDataFactory.createVerifiedOrganization();
    Campaign campaign = testDataFactory.createApprovedCampaign(org);
    User reporter = testDataFactory.createDonor();
    String reporterToken = testDataFactory.getAuthToken(reporter);
    String adminToken = testDataFactory.getAdminToken();
    
    BigDecimal initialScore = getTransparencyScore(org.getId());
    
    // 2. Submit fraud report
    CreateReportRequest reportRequest = new CreateReportRequest();
    reportRequest.setReportType(ReportType.FRAUD);
    reportRequest.setEntityType(ReportEntityType.ORGANIZATION);
    reportRequest.setEntityId(org.getId());
    reportRequest.setReason("Şüpheli aktivite tespit edildi");
    reportRequest.setDescription("Detaylı açıklama...");
    
    MvcResult reportResult = mockMvc.perform(post("/api/v1/reports")
            .header("Authorization", "Bearer " + reporterToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(reportRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.priority").value("HIGH")) // Auto-assigned for fraud
        .andReturn();
    
    UUID reportId = extractIdFromResponse(reportResult);
    
    // 3. Admin starts investigation
    mockMvc.perform(post("/api/v1/admin/reports/" + reportId + "/investigate")
            .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("INVESTIGATING"));
    
    // 4. Admin resolves (upholds fraud report)
    ResolveReportRequest resolveRequest = new ResolveReportRequest();
    resolveRequest.setResolution(ReportStatus.RESOLVED);
    resolveRequest.setResolutionNotes("Fraud confirmed after investigation");
    resolveRequest.setTakeAction(true);
    resolveRequest.setActionType("SUSPEND");
    
    mockMvc.perform(post("/api/v1/admin/reports/" + reportId + "/resolve")
            .header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(resolveRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("RESOLVED"));
    
    // 5. Verify transparency score decreased by 15
    BigDecimal newScore = getTransparencyScore(org.getId());
    BigDecimal expectedScore = initialScore.subtract(new BigDecimal("15"));
    assertEquals(expectedScore, newScore, "Score should decrease by 15 for upheld fraud");
    
    // 6. Verify reporter notified
    mockMvc.perform(get("/api/v1/notifications/unread")
            .header("Authorization", "Bearer " + reporterToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[?(@.title contains 'Şikayet')]").exists());
}
```

---

### 5. Recurring Donation Test

**File:** `RecurringDonationIntegrationTest.java`

```java
@Test
@DisplayName("Recurring donation: create → process → verify history")
void testRecurringDonationFlow() throws Exception {
    // Setup and test recurring donation lifecycle
}
```

---

### 6. Application (Beneficiary) Flow Test

**File:** `ApplicationFlowIntegrationTest.java`

```java
@Test
@DisplayName("Application flow: submit → review → approve → assign to campaign")
void testApplicationFlow() throws Exception {
    // Setup and test beneficiary application lifecycle
}
```

---

## Test Data Factory

**File:** `src/test/java/com/seffafbagis/api/util/TestDataFactory.java`

```java
@Component
public class TestDataFactory {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private CampaignRepository campaignRepository;
    
    @Autowired
    private JwtService jwtService;
    
    public User createDonor() {
        User user = new User();
        user.setEmail("donor_" + UUID.randomUUID() + "@test.com");
        user.setPasswordHash(passwordEncoder.encode("Test123!"));
        user.setRole(UserRole.DONOR);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);
        return userRepository.save(user);
    }
    
    public Organization createVerifiedOrganization() {
        User user = createFoundationUser();
        Organization org = new Organization();
        org.setUser(user);
        org.setOrganizationType(OrganizationType.FOUNDATION);
        org.setLegalName("Test Vakfı " + UUID.randomUUID());
        org.setTaxNumber(generateTaxNumber());
        org.setVerificationStatus(VerificationStatus.APPROVED);
        return organizationRepository.save(org);
    }
    
    public Campaign createApprovedCampaign(Organization org) {
        Campaign campaign = new Campaign();
        campaign.setOrganization(org);
        campaign.setTitle("Test Kampanya " + UUID.randomUUID());
        campaign.setSlug(SlugGenerator.generate(campaign.getTitle()));
        campaign.setDescription("Test açıklama");
        campaign.setTargetAmount(new BigDecimal("10000"));
        campaign.setCollectedAmount(BigDecimal.ZERO);
        campaign.setStatus(CampaignStatus.ACTIVE);
        campaign.setStartDate(LocalDateTime.now());
        campaign.setEndDate(LocalDateTime.now().plusDays(30));
        return campaignRepository.save(campaign);
    }
    
    public Campaign createCompletedCampaign(Organization org) {
        Campaign campaign = createApprovedCampaign(org);
        campaign.setStatus(CampaignStatus.COMPLETED);
        campaign.setCollectedAmount(campaign.getTargetAmount());
        campaign.setCompletedAt(LocalDateTime.now());
        return campaignRepository.save(campaign);
    }
    
    public String getAuthToken(User user) {
        return jwtService.generateToken(user);
    }
    
    public String getAdminToken() {
        User admin = userRepository.findByRole(UserRole.ADMIN).stream()
            .findFirst()
            .orElseGet(this::createAdmin);
        return getAuthToken(admin);
    }
    
    // ... more helper methods
}
```

---

## Test Configuration

**File:** `src/test/resources/application-test.yml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/seffaf_bagis_test
    username: test_user
    password: test_password
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  
  redis:
    host: localhost
    port: 6379

# Disable schedulers in tests
scheduler:
  recurring-donation:
    enabled: false
  bank-transfer:
    enabled: false
  evidence-reminder:
    enabled: false
  cleanup:
    enabled: false

# Use mock payment provider
payment:
  provider: mock
```

---

## API Documentation Verification

### Swagger/OpenAPI Check

Verify all endpoints are documented:

```java
@Test
void verifyAllEndpointsDocumented() {
    // Fetch OpenAPI spec
    MvcResult result = mockMvc.perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andReturn();
    
    String openApiJson = result.getResponse().getContentAsString();
    
    // Verify key endpoints exist
    assertTrue(openApiJson.contains("/api/v1/campaigns"));
    assertTrue(openApiJson.contains("/api/v1/donations"));
    assertTrue(openApiJson.contains("/api/v1/organizations"));
    assertTrue(openApiJson.contains("/api/v1/evidences"));
    // ... more assertions
}
```

---

## Final Checklist Items

### Code Quality
- [ ] Remove all TODO comments or create issues
- [ ] Ensure consistent code formatting
- [ ] Remove unused imports
- [ ] Check for code duplication
- [ ] Verify all methods have proper JavaDoc

### Security Review
- [ ] All endpoints have proper authorization
- [ ] Sensitive data is encrypted
- [ ] SQL injection prevention (parameterized queries)
- [ ] XSS prevention in responses
- [ ] Rate limiting configured
- [ ] CORS properly configured

### Performance
- [ ] Database indexes verified
- [ ] N+1 query issues resolved
- [ ] Pagination implemented on all list endpoints
- [ ] Redis caching working

### Error Handling
- [ ] All exceptions properly caught
- [ ] User-friendly error messages
- [ ] Proper HTTP status codes
- [ ] Error logging in place

### Documentation
- [ ] README updated with setup instructions
- [ ] API documentation complete
- [ ] Database schema documented
- [ ] Environment variables documented

---

## Files to Create

### Test Files
1. `DonationFlowIntegrationTest.java`
2. `OrganizationVerificationIntegrationTest.java`
3. `EvidenceTransparencyIntegrationTest.java`
4. `ReportHandlingIntegrationTest.java`
5. `RecurringDonationIntegrationTest.java`
6. `ApplicationFlowIntegrationTest.java`
7. `AuthenticationIntegrationTest.java`
8. `CampaignLifecycleIntegrationTest.java`

### Utility Files
9. `TestDataFactory.java`
10. `IntegrationTestBase.java` (base class with common setup)

### Configuration
11. `application-test.yml` (if not exists)

### Documentation
12. `docs/Emir/API_ENDPOINTS.md` - Complete API reference
13. `docs/Emir/INTEGRATION_GUIDE.md` - How modules integrate
14. `docs/Emir/TESTING_GUIDE.md` - How to run tests

---

## Testing Requirements

### Run All Tests
```bash
mvn test -Dspring.profiles.active=test
```

### Run Integration Tests Only
```bash
mvn test -Dtest="*IntegrationTest" -Dspring.profiles.active=test
```

### Generate Test Coverage Report
```bash
mvn jacoco:report
```

Target: >80% code coverage for service layer

---

## Success Criteria

- [ ] All 8 integration test classes created
- [ ] TestDataFactory with all helper methods
- [ ] DonationFlowIntegrationTest passes
- [ ] OrganizationVerificationIntegrationTest passes
- [ ] EvidenceTransparencyIntegrationTest passes
- [ ] ReportHandlingIntegrationTest passes
- [ ] RecurringDonationIntegrationTest passes
- [ ] ApplicationFlowIntegrationTest passes
- [ ] All unit tests still pass
- [ ] Code coverage >80% for services
- [ ] No critical SonarQube issues
- [ ] API documentation complete
- [ ] All endpoints return correct status codes
- [ ] Error handling verified
- [ ] Performance acceptable (<500ms response time)

---

## Result File Requirement

After completing this phase, create:
**Location:** `docs/Emir/step_results/phase_17.0_result.md`

Include:
1. Summary
2. Files created
3. Test coverage report
4. Integration test results (pass/fail table)
5. Performance metrics
6. Issues found and fixed
7. Final code quality assessment
8. Deployment readiness checklist
9. Known limitations
10. Recommendations for future improvements
11. Success criteria checklist

---

## Important Notes

1. **Do NOT use ternary operators** - Use if-else
2. **@Transactional on tests** - Rollback after each test
3. **Test isolation** - Tests should not depend on each other
4. **Mock external services** - Iyzico should use mock in tests
5. **Clean test data** - Use unique identifiers to avoid conflicts

---

## Dependencies

All previous phases must be complete:
- Phase 1-16 fully implemented and unit tested
- Database migrations applied
- All services functional

External:
- Test database (PostgreSQL)
- Test Redis instance
- Mock payment provider

---

## Estimated Duration

3 days

---

## Project Completion

🎉 **Congratulations!** After completing this phase, Emir's backend portion of the Şeffaf Bağış Platformu will be complete.

### Summary of Emir's Contribution:
- **17 Phases** completed
- **~200 files** created
- **42 days** of development
- **42%** of total backend

### Modules Delivered:
1. Category & Donation Type
2. Organization (Entity, Service, Controller)
3. Campaign (Entity, Service, Controller)
4. Donation (Core, Extended)
5. Payment (Iyzico Integration)
6. Evidence
7. Transparency Score
8. Application (Beneficiary)
9. Notification
10. Report
11. Scheduler
12. Event System
13. Integration Testing

### Ready for:
- Frontend integration
- UAT testing
- Production deployment
