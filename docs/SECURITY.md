# Security Documentation

## Overview
This document outlines the security measures implemented in the **Şeffaf Bağış Platformu** to protect user data, ensure integrity, and comply with KVKK/GDPR regulations.

## Authentication & Authorization
- **JWT (JSON Web Tokens)**: Used for stateless authentication.
  - Access Token: Short-lived (15 mins), used for API access.
  - Refresh Token: Long-lived (7 days), used to obtain new access tokens, stored securely.
- **Role-Based Access Control (RBAC)**:
  - `DONOR`: Standard user privileges (Profile, Donations).
  - `ADMIN`: Elevated privileges (User Mgmt, Verification, System Settings).

## Data Protection
### Encryption at Rest
- **Passwords**: Hashed using **BCrypt** (strength 10). Original passwords are never stored.
- **Sensitive Data**: Fields like TC Identity Number, Address, and Phone Number are encrypted using **AES-256-GCM** before being stored in the database.
- **Keys**: Encryption keys are managed via environment variables and never committed to version control.

### Encryption in Transit
- **TLS/SSL**: All API communication is forced over HTTPS (in production).

## Input Validation & Sanitization
- **SQL Injection**: Prevented using JPA Criteria Builder and Parameterized Queries / Prepared Statements.
- **XSS (Cross-Site Scripting)**: Inputs are validated using Bean Validation (@NotNull, @Size, @Pattern). Output encoding is enforced by the client applications, and risky content is sanitized on entry where applicable.

## Account Security
- **Account Lockout**: Accounts are temporarily locked after 5 failed login attempts to prevent Brute Force attacks.
- **Session Revocation**: Password reset or account deletion triggers revocation of all active refresh tokens.

## Audit Logging
- **Action Tracking**: Critical actions (data export, status change, deletion) are logged in an immutable `audit_logs` table.
- **Details**: Logs include Actor ID, Action Type, Target Resource, IP Address, and Timestamp.

## Vulnerability Management
- Dependencies are regularly scanned for known vulnerabilities.
- API endpoints are rate-limited to prevent DoS attacks.

## Incident Response
In case of a suspected breach:
1. Revoke all refresh tokens immediately.
2. Rotate encryption keys (requires data migration tool).
3. Notify affected users as per KVKK requirements (within 72 hours).
