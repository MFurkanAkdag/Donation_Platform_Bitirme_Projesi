# Şeffaf Bağış Platformu API Documentation

## Overview
This API provides access to the Transparent Donation Platform functionalities including user management, donations, campaigns, and evidence tracking.

**Base URL**: `/api/v1`
**Version**: 1.0.0

## Authentication
Authentication is handled via JWT (JSON Web Tokens).
- **Header**: `Authorization: Bearer <token>`
- **Token Type**: Bearer
- **Expiration**: 15 minutes (Access), 7 days (Refresh)

## Response Format
Standard response wrapper:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2023-12-09T10:00:00"
}
```

## Error Codes
- `400 Bad Request`: Invalid input
- `401 Unauthorized`: Missing or invalid token
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `429 Too Many Requests`: Rate limit exceeded
- `500 Internal Server Error`: Server processing error

---

## Endpoints

### Authentication Module

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/auth/register` | Register a new user | No |
| POST | `/auth/login` | Login and get tokens | No |
| POST | `/auth/refresh` | Refresh access token | No |
| GET | `/auth/verify-email` | Verify email address | No |
| POST | `/auth/forgot-password` | Request password reset | No |
| POST | `/auth/reset-password` | Reset password with token | No |

### User Module

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/users/me/profile` | Get current user profile | Yes |
| PUT | `/users/me/profile` | Update profile details | Yes |
| PUT | `/users/me/sensitive-data` | Add/Update sensitive data (TC, Address) | Yes |
| GET | `/users/me/sensitive-data` | View sensitive data (Masked) | Yes |
| GET | `/users/me/sensitive-data/export` | Export personal data (KVKK) | Yes |
| DELETE | `/users/me` | Delete account | Yes |

### Admin Module

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/admin/dashboard` | Get system statistics | Yes (ADMIN) |
| GET | `/admin/users` | List all users (Paginated) | Yes (ADMIN) |
| GET | `/admin/users/{id}` | Get user details | Yes (ADMIN) |
| PUT | `/admin/users/{id}/status` | Enable/Disable user | Yes (ADMIN) |
| PUT | `/admin/users/{id}/role` | Change user role | Yes (ADMIN) |
| GET | `/admin/audit-logs` | View system audit logs | Yes (ADMIN) |

### System Module

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/public/health` | Health check endpoint | No |
| GET | `/public/settings` | Public system settings | No |

---

## Rate Limiting
- Public endpoints: 60 req/min
- Authenticated endpoints: 300 req/min

## Pagination
List endpoints support `page` (0-based) and `size` parameters.
Example: `?page=0&size=20`
