# Deployment Guide

## System Requirements
- **Java**: JDK 17 or higher
- **Database**: PostgreSQL 15+
- **Cache**: Redis 6+
- **Build Tool**: Maven 3.8+

## Environment Variables
See `ENV.md` for a complete list of required environment variables.

## Local Deployment (Manual)

1. **Build the Application**:
   ```bash
   mvn clean package -DskipTests
   ```

2. **Run the Application**:
   ```bash
   java -jar target/seffaf-bagis-api-1.0.0-SNAPSHOT.jar
   ```

   Ensure Postgres and Redis are running locally.

## Docker Deployment

1. **Build Docker Image**:
   ```bash
   docker build -t seffaf-bagis-api .
   ```

2. **Run with Docker Compose** (Recommended):
   Create a `docker-compose.yml` file:
   ```yaml
   version: '3.8'
   services:
     app:
       image: seffaf-bagis-api
       ports:
         - "8080:8080"
       environment:
         - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/seffaf_db
         - SPRING_PROFILES_ACTIVE=prod
       depends_on:
         - postgres
         - redis
     
     postgres:
       image: postgres:15
       environment:
         - POSTGRES_DB=seffaf_db
         - POSTGRES_PASSWORD=secret
         
     redis:
       image: redis:alpine
   ```

3. **Start Services**:
   ```bash
   docker-compose up -d
   ```

## Health Checks
- **Endpoint**: `/actuator/health` or `/api/v1/public/health`
- **Expected Response**: `{"status": "UP"}` or `200 OK`

## Logs
Logs are written to console (stdout) and optionally to file if configured in `application.properties`.
In Docker: `docker logs -f <container_id>`

## Database Migrations
Flyway is enabled by default. Database schemas will be automatically migrated on application startup.
