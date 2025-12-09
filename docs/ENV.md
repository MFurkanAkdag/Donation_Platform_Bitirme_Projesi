# Environment Variables

These variables must be set in your `.env` file or environment configuration.

## Database
| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | JDBC URL for PostgreSQL | `jdbc:postgresql://localhost:5432/seffaf_bagis_db` |
| `SPRING_DATASOURCE_USERNAME` | DB Username | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | DB Password | `mysecretpassword` |

## Redis
| Variable | Description | Example |
|----------|-------------|---------|
| `REDIS_HOST` | Redis Host | `localhost` |
| `REDIS_PORT` | Redis Port | `6379` |
| `REDIS_PASSWORD` | Redis Password (Optional) | `redispassword` |

## JWT Security
| Variable | Description | Example |
|----------|-------------|---------|
| `JWT_SECRET` | Secret key for signing tokens (Min 32 chars) | `v3ryS3cr3tK3yF0rJWTG3n3rat10n!!!` |
| `JWT_ACCESS_EXPIRATION` | Access token lifetime (ms) | `900000` (15 min) |
| `JWT_REFRESH_EXPIRATION` | Refresh token lifetime (ms) | `604800000` (7 days) |

## Email Configuration
| Variable | Description | Example |
|----------|-------------|---------|
| `MAIL_HOST` | SMTP Host | `smtp.gmail.com` |
| `MAIL_PORT` | SMTP Port | `587` |
| `MAIL_USERNAME` | SMTP User | `info@seffafbagis.org` |
| `MAIL_PASSWORD` | SMTP Password | `app-specific-password` |
| `MAIL_FROM` | From Info | `noreply@seffafbagis.org` |

## Encryption
| Variable | Description | Example |
|----------|-------------|---------|
| `ENCRYPTION_SECRET_KEY` | AES Key (32/16 chars) | `MySuperSecretEncryptionKey123456` |

## Others
| Variable | Description | Example |
|----------|-------------|---------|
| `CORS_ALLOWED_ORIGINS` | Allowed Frontend URL | `http://localhost:3000` |
| `UPLOAD_DIR` | Directory for file uploads | `./uploads` |
| `SPRING_PROFILES_ACTIVE`| Active Profile | `dev` or `prod` |
