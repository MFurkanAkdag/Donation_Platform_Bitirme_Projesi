# API Endpoints Documentation

Complete API reference for the Şeffaf Bağış Platformu backend.

---

## Authentication (`/api/v1/auth`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/register` | Register new user | No |
| POST | `/login` | Login and get tokens | No |
| POST | `/refresh` | Refresh access token | No |
| POST | `/logout` | Logout and invalidate tokens | Yes |
| GET | `/verify-email` | Verify email with token | No |
| POST | `/forgot-password` | Request password reset | No |
| POST | `/reset-password` | Reset password with token | No |

---

## Users (`/api/v1/users`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/me/profile` | Get current user profile | Yes |
| PUT | `/me/profile` | Update current user profile | Yes |
| PUT | `/me/password` | Change password | Yes |
| GET | `/me/preferences` | Get notification preferences | Yes |
| PUT | `/me/preferences` | Update preferences | Yes |

---

## Organizations (`/api/v1/organizations`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/` | List approved organizations | No |
| GET | `/{slug}` | Get organization by slug | No |
| GET | `/search` | Search organizations | No |
| POST | `/` | Create organization | FOUNDATION |
| GET | `/my` | Get my organization | FOUNDATION |
| PUT | `/{id}` | Update organization | Owner |
| POST | `/{id}/documents` | Upload documents | Owner |
| POST | `/{id}/submit` | Submit for verification | Owner |

---

## Campaigns (`/api/v1/campaigns`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/` | List active campaigns | No |
| GET | `/{slug}` | Get campaign by slug | No |
| GET | `/search` | Search campaigns | No |
| GET | `/category/{slug}` | Get by category | No |
| POST | `/` | Create campaign | FOUNDATION |
| PUT | `/{id}` | Update campaign | Owner |
| POST | `/{id}/submit` | Submit for approval | Owner |
| POST | `/{id}/extend` | Request extension | Owner |
| GET | `/{id}/donations` | List campaign donations | No |
| GET | `/{id}/updates` | Get campaign updates | No |
| POST | `/{id}/updates` | Add campaign update | Owner |

---

## Donations (`/api/v1/donations`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/` | Create donation | Yes |
| GET | `/my` | List my donations | Yes |
| GET | `/{id}` | Get donation detail | Owner |
| GET | `/{id}/receipt` | Get donation receipt | Owner |
| POST | `/recurring` | Create recurring donation | Yes |
| GET | `/recurring/my` | List my recurring donations | Yes |
| DELETE | `/recurring/{id}` | Cancel recurring donation | Owner |

---

## Payments (`/api/v1/payments`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/process` | Process payment | Yes |
| GET | `/status/{donationId}` | Get payment status | Yes |
| POST | `/bank-transfer/reference` | Generate bank transfer reference | Yes |
| GET | `/methods` | List payment methods | No |

---

## Evidence (`/api/v1/evidences`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/` | Upload evidence | FOUNDATION |
| GET | `/campaign/{campaignId}` | List campaign evidences | No |
| GET | `/{id}` | Get evidence detail | No |

---

## Transparency (`/api/v1/transparency`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/organization/{id}` | Get transparency score | No |
| GET | `/organization/{id}/history` | Get score history | No |
| GET | `/leaderboard` | Get top organizations | No |

---

## Reports (`/api/v1/reports`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/` | Submit report | Yes |
| GET | `/my` | List my reports | Yes |
| GET | `/{id}` | Get report status | Owner |

---

## Applications (`/api/v1/applications`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/` | Submit application | Yes |
| GET | `/my` | List my applications | Yes |
| GET | `/organization` | List org applications | FOUNDATION |

---

## Notifications (`/api/v1/notifications`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/` | List notifications | Yes |
| GET | `/unread` | List unread | Yes |
| PUT | `/{id}/read` | Mark as read | Yes |
| PUT | `/read-all` | Mark all as read | Yes |

---

## Admin Endpoints (`/api/v1/admin`)

### Organizations
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/organizations` | List all organizations |
| GET | `/organizations?status=PENDING` | Filter by status |
| POST | `/organizations/{id}/verify` | Approve/reject organization |

### Campaigns
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/campaigns` | List all campaigns |
| POST | `/campaigns/{id}/approve` | Approve/reject campaign |

### Evidences
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/evidences` | List evidences |
| POST | `/evidences/{id}/review` | Review evidence |

### Reports
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/reports` | List all reports |
| POST | `/reports/{id}/investigate` | Start investigation |
| POST | `/reports/{id}/resolve` | Resolve report |

### Applications
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/applications` | List all applications |
| POST | `/applications/{id}/review` | Review application |

---

## Categories (`/api/v1/categories`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/` | List categories | No |
| GET | `/{slug}` | Get category | No |
| POST | `/` | Create category | ADMIN |
| PUT | `/{id}` | Update category | ADMIN |

---

## Donation Types (`/api/v1/donation-types`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/` | List donation types | No |
| GET | `/{id}` | Get donation type | No |

---

## Common Response Format

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2024-12-22T20:00:00Z"
}
```

## Error Response Format

```json
{
  "success": false,
  "message": "Error message",
  "errors": ["Validation error 1", "Validation error 2"],
  "timestamp": "2024-12-22T20:00:00Z"
}
```

---

## HTTP Status Codes

| Code | Meaning |
|------|---------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 409 | Conflict |
| 422 | Unprocessable Entity |
| 500 | Internal Server Error |
