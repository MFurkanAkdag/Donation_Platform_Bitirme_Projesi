# Backend & Docker Setup Context

**Date:** 2024-12-23
**Status:** Operational

## Overview
The Donation Platform backend is containerized using Docker and Docker Compose. It connects to PostgreSQL and Redis services.

## Access Points
- **Backend API:** `http://localhost:8080/api`
- **Swagger UI:** `http://localhost:8080/api/swagger-ui/index.html` (Enabled in `dev` profile)
- **PostgreSQL:** Port `5432` (User/Pass: `postgres`/`postgres`, DB: `seffaf_bagis_db`)
- **Redis:** Port `6379`

## Setup & Commands
### Prerequisites
- Docker Desktop installed and running.

### Commands
- **Start Application:**
  ```bash
  cd docker
  docker-compose up -d --build
  ```
- **Stop Application:**
  ```bash
  docker-compose down
  ```
- **Reset Database (Fresh Start):**
  ```bash
  docker-compose down -v
  docker-compose up -d --build
  ```

## Implementation Details & Fixes
The following issues were encountered and resolved during the initial containerization:

1.  **Duplicate Controller Conflict:**
    - **Issue:** `ConflictingBeanDefinitionException` due to `OrganizationDocumentController` existing in two packages.
    - **Fix:** Deleted the placeholder class in `com.seffafbagis.api.controller.organization`.

2.  **Invalid AspectJ Pointcut:**
    - **Issue:** `AuditAspect` referenced a non-existent `UserSensitiveDataService`, preventing startup.
    - **Fix:** Removed the invalid `@Pointcut` and associated `@AfterReturning` advice in `AuditAspect.java`.

3.  **JWT Configuration Injection:**
    - **Issue:** `JwtConfig` failed to bind properties because it was annotated with `@Configuration` (causing bean conflict).
    - **Fix:** Removed `@Configuration` from `JwtConfig.java` to allow `@EnableConfigurationProperties` to function correctly.

4.  **JWT Secret Encoding:**
    - **Issue:** `decoders.BASE64` failed to decode the default plain-text secret.
    - **Fix:** Updated `application.yml` and `application-dev.yml` to use a valid Base64-encoded secret.

5.  **Flyway Migration Conflict:**
    - **Issue:** `V19__add_default_bank_account_to_campaigns.sql` failed because the column `default_bank_account_id` was already added by `V16`.
    - **Fix:** Deleted the redundant `V19` migration script and the backup file `V1_to_V19.sql`.

## Configuration Files
- **Dockerfile:** `backend/Dockerfile` (Multi-stage build: Maven -> JRE)
- **Compose:** `docker/docker-compose.yml` (Services: postgres, redis, backend)
- **App Config:** `backend/src/main/resources/application.yml` & `application-dev.yml`
