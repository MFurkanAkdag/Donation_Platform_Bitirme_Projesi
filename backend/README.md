# Şeffaf Bağış Platformu - Backend API

## 📋 İçindekiler
- [Gereksinimler](#gereksinimler)
- [Kurulum](#kurulum)
- [Ortam Değişkenleri](#ortam-değişkenleri)
- [Veritabanı Kurulumu](#veritabanı-kurulumu)
- [Uygulamayı Çalıştırma](#uygulamayı-çalıştırma)
- [API Dokümantasyonu](#api-dokümantasyonu)
- [Geliştirme Notları](#geliştirme-notları)

## 🔧 Gereksinimler

### Minimum Sürümler
- **Java**: 17 veya daha yüksek
- **Maven**: 3.8.1 veya daha yüksek
- **PostgreSQL**: 12 veya daha yüksek
- **Redis**: 6.0 veya daha yüksek (cache ve session yönetimi için)

### Harici Servisler (Opsiyonel)
- **Iyzico**: Ödeme işlemleri için
- **AWS S3**: Dosya depolama için
- **Gmail SMTP**: E-posta gönderimi için

## 📦 Kurulum

### 1. Projeyi Klonlayın
```bash
git clone <repository-url>
cd backend
```

### 2. Maven Bağımlılıklarını İndirin
```bash
mvn clean install
```

### 3. Ortam Değişkenlerini Ayarlayın
```bash
# .env.example dosyasını .env olarak kopyalayın
cp .env.example .env

# .env dosyasını açıp gerçek değerleri doldurun
# (Aşağıdaki bölüme bakın)
```

## 🔐 Ortam Değişkenleri

### Gerekli Değişkenler

#### Database Configuration
```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/seffaf_bagis_db
SPRING_DATASOURCE_USERNAME=seffaf_user
SPRING_DATASOURCE_PASSWORD=your_secure_password
```

#### JWT Configuration
```env
APP_JWT_SECRET=your_very_long_secret_key_at_least_32_chars_change_in_production_now!
```

> ⚠️ **UYARI**: JWT Secret en az 32 karakter olmalı. Şu komutu kullanarak güvenli bir key oluşturun:
> ```bash
> openssl rand -base64 32
> ```

#### Redis Configuration
```env
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379
SPRING_DATA_REDIS_PASSWORD=
```

#### Mail Configuration (Gmail örneği)
```env
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your_email@gmail.com
SPRING_MAIL_PASSWORD=your_app_password_here
```

> 💡 **İpucu**: Gmail kullanıyorsanız:
> 1. 2FA (İki Faktörlü Kimlik Doğrulama) etkinleştirin
> 2. [App Passwords](https://myaccount.google.com/apppasswords) sayfasından uygulama şifresi oluşturun

#### CORS Configuration
```env
APP_CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001
```

#### Payment Gateway (Iyzico)
```env
IYZICO_API_KEY=your_iyzico_api_key
IYZICO_SECRET_KEY=your_iyzico_secret_key
```

### Opsiyonel Değişkenler

#### AWS S3 Configuration
```env
AWS_ACCESS_KEY_ID=your_aws_key
AWS_SECRET_ACCESS_KEY=your_aws_secret
AWS_S3_BUCKET_NAME=seffaf-bagis-files
AWS_S3_REGION=eu-west-1
```

## 🗄️ Veritabanı Kurulumu

### PostgreSQL Kurulumu

#### 1. PostgreSQL Sunucusunu Başlatın
```bash
# macOS (Homebrew)
brew services start postgresql

# Ubuntu/Debian
sudo service postgresql start

# Windows
# PostgreSQL Application -> Start Server
```

#### 2. Veritabanını ve Kullanıcıyı Oluşturun
```bash
# PostgreSQL CLI'ye bağlanın
psql -U postgres

# Veritabanını oluşturun
CREATE DATABASE seffaf_bagis_db;

# Kullanıcıyı oluşturun
CREATE USER seffaf_user WITH PASSWORD 'your_secure_password';

# İzinleri verin
ALTER ROLE seffaf_user SET client_encoding TO 'utf8';
ALTER ROLE seffaf_user SET default_transaction_isolation TO 'read committed';
ALTER ROLE seffaf_user SET default_transaction_deferrable TO on;
ALTER ROLE seffaf_user SET default_transaction_read_only TO off;
GRANT ALL PRIVILEGES ON DATABASE seffaf_bagis_db TO seffaf_user;

# Çıkın
\q
```

#### 3. Redis Kurulumu
```bash
# macOS (Homebrew)
brew install redis
brew services start redis

# Ubuntu/Debian
sudo apt-get install redis-server
sudo service redis-server start

# Windows
# WSL2 içinde Redis kurun veya Docker kullanın:
docker run -d -p 6379:6379 redis:latest
```

### Docker Kullanarak (Önerilen)
```bash
cd docker
docker-compose up -d
```

Bu, PostgreSQL ve Redis'i başlatacaktır.

## 🚀 Uygulamayı Çalıştırma

### 1. Development Modunda Çalıştırın
```bash
# Maven kullanarak
mvn spring-boot:run

# veya IDE'den çalıştırın (IntelliJ IDEA vb.)
# SeffafBagisApplication.java dosyasını sağ tıkla > Run
```

### 2. Production Build Oluşturun
```bash
# JAR dosyası oluşturun
mvn clean package

# Oluşturulan JAR'ı çalıştırın
java -jar target/seffaf-bagis-api-1.0.0-SNAPSHOT.jar
```

### 3. Specipik Profil ile Çalıştırın
```bash
# Development profili
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Production profili
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"

# Test profili
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=test"
```

## 📚 API Dokümantasyonu

### Swagger UI
Uygulama çalışmaya başladığında Swagger UI'ye şu adresten erişebilirsiniz:

```
http://localhost:8080/swagger-ui.html
```

### OpenAPI JSON
OpenAPI spesifikasyonuna şu adresten ulaşabilirsiniz:

```
http://localhost:8080/v3/api-docs
```

### Ana Endpoint'ler

#### Authentication
- `POST /api/v1/auth/login` - Giriş yap
- `POST /api/v1/auth/register` - Kayıt ol
- `POST /api/v1/auth/refresh-token` - Token yenile
- `POST /api/v1/auth/forgot-password` - Şifremi unuttum
- `POST /api/v1/auth/reset-password` - Şifreyi sıfırla

#### Campaigns
- `GET /api/v1/campaigns` - Tüm kampanyaları listele
- `GET /api/v1/campaigns/{id}` - Kampanya detaylarını al
- `POST /api/v1/campaigns` - Yeni kampanya oluştur
- `PUT /api/v1/campaigns/{id}` - Kampanyayı güncelle
- `DELETE /api/v1/campaigns/{id}` - Kampanyayı sil

#### Donations
- `POST /api/v1/donations` - Bağış yap
- `GET /api/v1/donations` - Bağışlarımı listele
- `GET /api/v1/donations/{id}` - Bağış detaylarını al

#### Organizations
- `GET /api/v1/organizations` - Kuruluşları listele
- `GET /api/v1/organizations/{id}` - Kuruluş detaylarını al
- `POST /api/v1/organizations` - Kuruluş kaydı yap

Daha fazla endpoint için Swagger UI'ye bakın.

## 🧪 Testleri Çalıştırma

```bash
# Tüm testleri çalıştırın
mvn test

# Spesipik test sınıfını çalıştırın
mvn test -Dtest=UserServiceTest

# Coverage raporu oluşturun
mvn jacoco:report

# Coverage raporu: target/site/jacoco/index.html
```

## 🛠️ Geliştirme Notları

### Code Style
- Google Java Style Guide'ı takip ederiz
- Lombok'u boilerplate azaltmak için kullanıyoruz
- MapStruct'ı DTO mapping'i için kullanıyoruz

### Paket Yapısı
```
com.seffafbagis.api
├── config/              # Spring yapılandırmaları
├── controller/          # REST Controller'lar
├── service/             # İş mantığı
├── repository/          # Veritabanı erişimi
├── entity/              # JPA Entity'ler
├── dto/                 # Data Transfer Objects
├── exception/           # Custom Exception'lar
├── security/            # JWT ve Security
├── scheduler/           # Planlı görevler
└── util/                # Utility fonksiyonları
```

### Database Migrations (Flyway)
Veritabanı değişiklikleri `V1__*.sql`, `V2__*.sql` vb. dosyalarla versiyonlanır.

Yeni migration eklemek için:
1. `src/main/resources/db/migration/` dizinine dosya ekleyin
2. Dosya adını `V{number}__{description}.sql` formatında verin
3. Uygulamayı başlatın - Flyway otomatik olarak çalıştıracaktır

### Logging
- **DEBUG**: Geliştirme sırasında detaylı bilgiler
- **INFO**: Önemli olaylar
- **WARN**: Uyarılar (önerilen): 
- **ERROR**: Hatalar

### Performance Tuning
1. **Database Connection Pool**: HikariCP kullanıyoruz
2. **Caching**: Redis ile ORM sorguları cache'liyoruz
3. **Batch Operations**: Batch işlemler için Hibernate yapılandırması yapıyoruz

## 🚨 Sorun Giderme

### Veritabanı Bağlantı Hatası
```
ERROR: org.postgresql.util.PSQLException: Connection to localhost:5432 refused
```

**Çözüm:**
```bash
# PostgreSQL çalışıyor mu kontrol edin
psql -U postgres -c "SELECT 1"

# Eğer başlamadıysa başlatın
brew services start postgresql
# veya
docker-compose up -d
```

### Redis Bağlantı Hatası
```
ERROR: redis.clients.jedis.exceptions.JedisConnectionException
```

**Çözüm:**
```bash
# Redis'i başlatın
brew services start redis
# veya
docker-compose up -d redis
```

### JWT Secret Hatası
```
ERROR: JWT secret boş olamaz veya en az 32 karakter olmalı
```

**Çözüm:**
1. `.env` dosyasında `APP_JWT_SECRET` değerini kontrol edin
2. Değeri şu komutla oluşturun:
```bash
openssl rand -base64 32
```
3. Oluşturulan değeri `.env` dosyasında `APP_JWT_SECRET` değerine yapıştırın

### Port Zaten Kullanımda
```
ERROR: Address already in use
```

**Çözüm:**
```bash
# Port'u açıyor olanı bul ve kapat
lsof -i :8080
# Veya başka port kullan
java -jar target/seffaf-bagis-api-1.0.0-SNAPSHOT.jar --server.port=8081
```

## 📖 Kaynaklar

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Redis Documentation](https://redis.io/documentation)
- [JWT Introduction](https://jwt.io/introduction)
- [OpenAPI Specification](https://swagger.io/specification/)

## 👥 Katkıda Bulunma

Katkılarınız hoş geldiniz! Lütfen:
1. Feature branch oluşturun
2. Değişikliklerinizi commit edin
3. Pull Request gönderin

## 📝 Lisans

Bu proje MIT License altında lisanslanmıştır.

## 📧 İletişim

Sorular veya öneriler için lütfen iletişim kurun:
- Email: dev@seffafbagis.com
- Website: https://seffafbagis.com
