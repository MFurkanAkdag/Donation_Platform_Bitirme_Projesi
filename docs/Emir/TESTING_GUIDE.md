# Testing Guide

How to run tests for the Şeffaf Bağış Platformu.

---

## Prerequisites

- Java 21
- Maven 3.8+
- Docker (for integration tests)

---

## Run All Tests

```bash
cd backend
mvn test -Dspring.profiles.active=test
```

---

## Run Unit Tests Only

```bash
mvn test -Dtest="*Test" -DexcludedGroups=integration
```

---

## Run Integration Tests Only

```bash
mvn test -Dtest="*IntegrationTest" -Dspring.profiles.active=test
```

---

## Run Specific Test Class

```bash
mvn test -Dtest="DonationFlowIntegrationTest"
```

---

## Generate Coverage Report

```bash
mvn jacoco:report
```

Report location: `target/site/jacoco/index.html`

---

## Test Configuration

Tests use `application-test.yml` which:
- Uses H2 in-memory database
- Disables Flyway migrations
- Disables schedulers
- Mocks payment provider
- Disables Redis

---

## Integration Test Structure

| Test Class | Tests |
|------------|-------|
| `AuthIntegrationTest` | Authentication flows |
| `DonationFlowIntegrationTest` | Donation lifecycle |
| `PaymentIntegrationTest` | Payment processing |
| `OrganizationVerificationIntegrationTest` | Org verification |
| `EvidenceTransparencyIntegrationTest` | Evidence & scores |
| `ReportHandlingIntegrationTest` | Report workflow |
| `RecurringDonationIntegrationTest` | Recurring donations |
| `ApplicationFlowIntegrationTest` | Beneficiary apps |
| `CampaignLifecycleIntegrationTest` | Campaign lifecycle |

---

## TestDataFactory

Use `TestDataFactory` to create test entities:

```java
@Autowired
private TestDataFactory testDataFactory;

// Create users
User donor = testDataFactory.createDonor();
User admin = testDataFactory.createAdmin();

// Create organizations
Organization org = testDataFactory.createVerifiedOrganization();

// Create campaigns
Campaign campaign = testDataFactory.createApprovedCampaign(org);

// Get auth tokens
String token = testDataFactory.getAuthToken(donor);
```

---

## Troubleshooting

**Tests fail with database errors:**
- Ensure H2 dependency is in pom.xml
- Check `application-test.yml` datasource config

**Integration tests timeout:**
- Docker must be running for Testcontainers
- Increase timeout in test annotations

**Redis connection errors:**
- Redis is disabled in test profile
- Check `spring.data.redis.repositories.enabled: false`
