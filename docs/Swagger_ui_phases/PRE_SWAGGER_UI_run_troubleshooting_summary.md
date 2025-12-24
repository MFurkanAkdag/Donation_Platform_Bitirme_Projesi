# Troubleshooting Summary: Backend Startup and Swagger UI Access

This document summarizes the issues encountered and resolved to enable the successful startup of the `seffaf_bagis_backend` service and access to the Swagger UI.

## Overview
The backend service (Spring Boot) was failing to start due to a series of configuration errors, database schema mismatches, and invalid JPA repository definitions. Additionally, a controller URL mapping conflict prevented the application context from loading.

## Resolved Issues

### 1. Database Migrations (Flyway)
*   **Issue:** [V20__add_timestamp_columns_to_all_tables.sql](file:///c:/Users/ogsar/OneDrive/Desktop/bitirme_projesi/Donation_Platform_Bitirme_Projesi/backend/src/main/resources/db/migration/V20__add_timestamp_columns_to_all_tables.sql) failed because it referenced a table named `evidence` instead of `evidences`.
    *   **Fix:** Corrected the table name to `evidences`.
*   **Issue:** Usage of `INET` and `JSONB` types in migrations conflicted with JPA String mappings for [AuditLog](file:///c:/Users/ogsar/OneDrive/Desktop/bitirme_projesi/Donation_Platform_Bitirme_Projesi/backend/src/main/java/com/seffafbagis/api/entity/audit/AuditLog.java#20-97).
    *   **Fix:** Created [V21__fix_audit_log_schema_mismatch.sql](file:///c:/Users/ogsar/OneDrive/Desktop/bitirme_projesi/Donation_Platform_Bitirme_Projesi/backend/src/main/resources/db/migration/V21__fix_audit_log_schema_mismatch.sql) to align types.
*   **Issue:** Join tables (e.g., `campaign_categories`) lacked an [id](file:///c:/Users/ogsar/OneDrive/Desktop/bitirme_projesi/Donation_Platform_Bitirme_Projesi/backend/src/main/java/com/seffafbagis/api/entity/auth/RefreshToken.java#222-232) column required by [BaseEntity](file:///c:/Users/ogsar/OneDrive/Desktop/bitirme_projesi/Donation_Platform_Bitirme_Projesi/backend/src/main/java/com/seffafbagis/api/entity/base/BaseEntity.java#24-88).
    *   **Fix:** Created [V22__add_id_to_join_tables.sql](file:///c:/Users/ogsar/OneDrive/Desktop/bitirme_projesi/Donation_Platform_Bitirme_Projesi/backend/src/main/resources/db/migration/V22__add_id_to_join_tables.sql) to add UUID [id](file:///c:/Users/ogsar/OneDrive/Desktop/bitirme_projesi/Donation_Platform_Bitirme_Projesi/backend/src/main/java/com/seffafbagis/api/entity/auth/RefreshToken.java#222-232) columns.

### 2. Hibernate/JPA Configuration
*   **Issue:** `spring.jpa.hibernate.ddl-auto: validate` caused startup crashes due to minor schema discrepancies (e.g., index naming).
    *   **Fix:** Changed `ddl-auto` to `none` to rely entirely on Flyway migrations, which is safer for production-like environments.

### 3. Missing Configuration Properties
*   **Issue:** Missing [iyzico](file:///c:/Users/ogsar/OneDrive/Desktop/bitirme_projesi/Donation_Platform_Bitirme_Projesi/backend/src/main/java/com/seffafbagis/api/config/IyzicoConfig.java#25-33) usage properties and `app.frontend-url`.
    *   **Fix:** Aadded placeholder values to [application-dev.yml](file:///c:/Users/ogsar/OneDrive/Desktop/bitirme_projesi/Donation_Platform_Bitirme_Projesi/backend/src/main/resources/application-dev.yml).
*   **Issue:** `EncryptionException` due to an invalid/short secret key.
    *   **Fix:** Provided a valid 32-character encryption key in [application-dev.yml](file:///c:/Users/ogsar/OneDrive/Desktop/bitirme_projesi/Donation_Platform_Bitirme_Projesi/backend/src/main/resources/application-dev.yml).

### 4. Invalid Repository Methods (QueryCreationException)
Several JPA repositories defined methods referencing non-existent entity fields.
*   **[UserRepository](file:///c:/Users/ogsar/OneDrive/Desktop/bitirme_projesi/Donation_Platform_Bitirme_Projesi/backend/src/main/java/com/seffafbagis/api/repository/UserRepository.java#22-42):** Removed `findByPhoneNumber` (field does not exist).
*   **[TransactionRepository](file:///c:/Users/ogsar/OneDrive/Desktop/bitirme_projesi/Donation_Platform_Bitirme_Projesi/backend/src/main/java/com/seffafbagis/api/repository/TransactionRepository.java#15-23):** Removed `findByTransactionId` (field does not exist).
*   **[LoginHistoryRepository](file:///c:/Users/ogsar/OneDrive/Desktop/bitirme_projesi/Donation_Platform_Bitirme_Projesi/backend/src/main/java/com/seffafbagis/api/repository/LoginHistoryRepository.java#21-51):** Fixed JPQL to use `h.user.id` instead of `h.userId`.
*   **[OrganizationRepository](file:///c:/Users/ogsar/OneDrive/Desktop/bitirme_projesi/Donation_Platform_Bitirme_Projesi/backend/src/main/java/com/seffafbagis/api/repository/OrganizationRepository.java#19-50):** Fixed JPQL to use `tradeName` instead of `displayName`.
*   **[RefreshTokenRepository](file:///c:/Users/ogsar/OneDrive/Desktop/bitirme_projesi/Donation_Platform_Bitirme_Projesi/backend/src/main/java/com/seffafbagis/api/repository/RefreshTokenRepository.java#18-32):** Changed [findByToken](file:///c:/Users/ogsar/OneDrive/Desktop/bitirme_projesi/Donation_Platform_Bitirme_Projesi/backend/src/main/java/com/seffafbagis/api/repository/PasswordResetTokenRepository.java#22-23) to [findByTokenHash](file:///c:/Users/ogsar/OneDrive/Desktop/bitirme_projesi/Donation_Platform_Bitirme_Projesi/backend/src/main/java/com/seffafbagis/api/repository/PasswordResetTokenRepository.java#22-23).
*   **[ApplicationRepository](file:///c:/Users/ogsar/OneDrive/Desktop/bitirme_projesi/Donation_Platform_Bitirme_Projesi/backend/src/main/java/com/seffafbagis/api/repository/ApplicationRepository.java#17-35):** Changed `findByCampaignId` to [findByAssignedCampaignId](file:///c:/Users/ogsar/OneDrive/Desktop/bitirme_projesi/Donation_Platform_Bitirme_Projesi/backend/src/main/java/com/seffafbagis/api/repository/ApplicationRepository.java#20-21).
*   **[EmailVerificationTokenRepository](file:///c:/Users/ogsar/OneDrive/Desktop/bitirme_projesi/Donation_Platform_Bitirme_Projesi/backend/src/main/java/com/seffafbagis/api/repository/EmailVerificationTokenRepository.java#19-41):** Changed [findByToken](file:///c:/Users/ogsar/OneDrive/Desktop/bitirme_projesi/Donation_Platform_Bitirme_Projesi/backend/src/main/java/com/seffafbagis/api/repository/PasswordResetTokenRepository.java#22-23) to [findByTokenHash](file:///c:/Users/ogsar/OneDrive/Desktop/bitirme_projesi/Donation_Platform_Bitirme_Projesi/backend/src/main/java/com/seffafbagis/api/repository/PasswordResetTokenRepository.java#22-23).
*   **[PasswordResetTokenRepository](file:///c:/Users/ogsar/OneDrive/Desktop/bitirme_projesi/Donation_Platform_Bitirme_Projesi/backend/src/main/java/com/seffafbagis/api/repository/PasswordResetTokenRepository.java#19-41):** Removed unused [findByToken](file:///c:/Users/ogsar/OneDrive/Desktop/bitirme_projesi/Donation_Platform_Bitirme_Projesi/backend/src/main/java/com/seffafbagis/api/repository/PasswordResetTokenRepository.java#22-23).
*   **[OrganizationDocumentRepository](file:///c:/Users/ogsar/OneDrive/Desktop/bitirme_projesi/Donation_Platform_Bitirme_Projesi/backend/src/main/java/com/seffafbagis/api/repository/OrganizationDocumentRepository.java#16-34):** Fixed JPQL to use `expiresAt` instead of `expiryDate`.
*   **[DonationTypeRepository](file:///c:/Users/ogsar/OneDrive/Desktop/bitirme_projesi/Donation_Platform_Bitirme_Projesi/backend/src/main/java/com/seffafbagis/api/repository/DonationTypeRepository.java#15-27):** Removed unused `findByCode` and `existsByCode` ([DonationType](file:///c:/Users/ogsar/OneDrive/Desktop/bitirme_projesi/Donation_Platform_Bitirme_Projesi/backend/src/main/java/com/seffafbagis/api/entity/category/DonationType.java#9-94) uses an Enum `typeCode`).

### 5. Controller Mapping Conflicts
*   **Issue:** `Ambiguous mapping` error. Both [ReportController](file:///c:/Users/ogsar/OneDrive/Desktop/bitirme_projesi/Donation_Platform_Bitirme_Projesi/backend/src/main/java/com/seffafbagis/api/controller/report/ReportController.java#26-167) and [AdminReportController](file:///c:/Users/ogsar/OneDrive/Desktop/bitirme_projesi/Donation_Platform_Bitirme_Projesi/backend/src/main/java/com/seffafbagis/api/controller/admin/AdminReportController.java#20-84) mapped to `/api/v1/admin/reports/{id}`. [ReportController](file:///c:/Users/ogsar/OneDrive/Desktop/bitirme_projesi/Donation_Platform_Bitirme_Projesi/backend/src/main/java/com/seffafbagis/api/controller/report/ReportController.java#26-167) contained legacy admin logic while [AdminReportController](file:///c:/Users/ogsar/OneDrive/Desktop/bitirme_projesi/Donation_Platform_Bitirme_Projesi/backend/src/main/java/com/seffafbagis/api/controller/admin/AdminReportController.java#20-84) was a newer, specialized implementation with Audit Logging.
    *   **Fix:** Commented out the conflicting Admin endpoints in [ReportController](file:///c:/Users/ogsar/OneDrive/Desktop/bitirme_projesi/Donation_Platform_Bitirme_Projesi/backend/src/main/java/com/seffafbagis/api/controller/report/ReportController.java#26-167) to allow [AdminReportController](file:///c:/Users/ogsar/OneDrive/Desktop/bitirme_projesi/Donation_Platform_Bitirme_Projesi/backend/src/main/java/com/seffafbagis/api/controller/admin/AdminReportController.java#20-84) (which is superior) to handle them.

## Final Status
*   **Backend Status:** Started successfully.
*   **Swagger UI:** Accessible at `http://localhost:8080/api/swagger-ui/index.html`.
*   **Database:** Migrations V1-V22 applied successfully.
