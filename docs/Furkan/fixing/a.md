# DTO Refactoring ve Derleme Hatalarının Düzeltilmesi

**Tarih:** 23 Aralık 2024  
**Sonuç:** ✅ BUILD SUCCESS

---

## 📋 Genel Bakış

Bu oturumda, mevcut DTO'ları Lombok anotasyonlarını kullanacak şekilde refactor ettik ve projede bulunan çeşitli derleme hatalarını düzelttik. Amaç, boilerplate kodu azaltmak, tip güvenliğini sağlamak ve projenin başarılı bir şekilde derlenmesini garantilemekti.

---

## 🔧 Yapılan Değişiklikler

### 1. Lombok DTO Dönüşümleri

Aşağıdaki DTO dosyaları Lombok anotasyonları (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`) kullanacak şekilde güncellendi:

#### Request DTO'ları

| Dosya | Eklenen Anotasyonlar | Açıklama |
|-------|---------------------|----------|
| `CreateEvidenceRequest.java` | `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` | Manuel getter/setter kaldırıldı |
| `CreateApplicationRequest.java` | `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` | Manuel getter/setter kaldırıldı |
| `CreateCampaignRequest.java` | `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` | Manuel getter/setter kaldırıldı |
| `UpdateCampaignRequest.java` | `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` | Manuel getter/setter kaldırıldı |
| `CreateOrganizationRequest.java` | `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` | Manuel getter/setter kaldırıldı |
| `UpdateOrganizationRequest.java` | `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` | Manuel getter/setter kaldırıldı |
| `ReviewEvidenceRequest.java` | `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` | Manuel getter/setter kaldırıldı |
| `UpdateProfileRequest.java` | `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` | Manuel getter/setter kaldırıldı |

#### Response DTO'ları

| Dosya | Eklenen Anotasyonlar | Açıklama |
|-------|---------------------|----------|
| `EvidenceResponse.java` | `@Data`, `@SuperBuilder`, `@NoArgsConstructor`, `@AllArgsConstructor` | Inheritance için `@SuperBuilder` kullanıldı |
| `EvidenceDetailResponse.java` | `@Data`, `@SuperBuilder`, `@NoArgsConstructor`, `@AllArgsConstructor` | `EvidenceResponse`'dan inherit ettiği için `@SuperBuilder` |
| `OrganizationStatistics.java` | `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` | Manuel getter/setter kaldırıldı |
| `OrganizationDetailResponse.java` | `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` | Tüm alanlar ile yeniden yazıldı |
| `OrganizationListResponse.java` | `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` | Mapper uyumluluğu için güncellendi |
| `OrganizationSummaryResponse.java` | `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` | Manuel builder kaldırıldı |

---

### 2. Entity Düzeltmeleri

#### TransparencyScore.java

**Sorun:** Service katmanı entity'de olmayan metotları çağırıyordu.

**Çözüm:** Eksik alanlar eklendi ve Lombok'a dönüştürüldü.

```java
// Eklenen alanlar:
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "organization_id", nullable = false, unique = true)
private Organization organization;

@Column(name = "rejected_evidences")
private Integer rejectedEvidences = 0;

@Column(name = "on_time_reports")
private Integer onTimeReports = 0;

@Column(name = "late_reports")
private Integer lateReports = 0;

@Column(name = "last_calculated_at")
private LocalDateTime lastCalculatedAt;
```

#### CampaignFollower.java

**Sorun:** `CampaignFollowerService` bildirim ayarı için eksik alanları çağırıyordu.

**Çözüm:** Eksik alanlar eklendi:

```java
@Column(name = "notify_on_update")
private Boolean notifyOnUpdate = true;

@Column(name = "notify_on_complete")
private Boolean notifyOnComplete = true;
```

---

### 3. Exception Düzeltmeleri

#### ConflictException.java

**Sorun:** `OrganizationService` sadece mesaj parametresi ile exception fırlatıyordu, ancak mevcut constructor'lar bunu desteklemiyordu.

**Çözüm:** Yeni constructor eklendi:

```java
public ConflictException(String message) {
    super(message);
    this.resourceName = null;
    this.fieldName = null;
    this.fieldValue = null;
}
```

---

### 4. Repository Düzeltmeleri

#### ApplicationRepository.java

**Sorun:** Eksik metotlar ve String yerine enum kullanımı gerekiyordu.

**Değişiklikler:**
- `findByStatus(ApplicationStatus status, Pageable pageable)` eklendi
- `findByApplicantIdOrderByCreatedAtDesc(UUID applicantId)` eklendi
- `findByAssignedOrganizationId(UUID organizationId, Pageable pageable)` eklendi
- Tüm `String status` parametreleri `ApplicationStatus` enum'a dönüştürüldü

#### LoginHistoryRepository.java

**Sorun:** Service'in kullandığı metotlar eksikti.

**Eklenen metotlar:**
- `findAllByUserOrderByCreatedAtDesc(User user, Pageable pageable)`
- `countByUserAndLoginStatusAndCreatedAtAfter(User user, String loginStatus, OffsetDateTime createdAt)`
- `deleteAllByCreatedAtBefore(OffsetDateTime before)`

#### ReportRepository.java

**Sorun:** String tipler yerine enum tipleri kullanılmalıydı.

**Değişiklikler:**
- `ReportStatus status` ve `ReportPriority priority` enum tipleri kullanıldı

#### CampaignRepository.java

**Sorun:** Service'in kullandığı birçok metot eksikti.

**Eklenen metotlar:**
- `findByStatus(CampaignStatus status, Pageable pageable)`
- `findByOrganizationId(UUID organizationId, Pageable pageable)`
- `findByOrganizationIdAndStatus(UUID organizationId, CampaignStatus status)`
- `findByIsFeaturedTrueAndStatus(CampaignStatus status)`
- `findByIsUrgentTrueAndStatus(CampaignStatus status)`
- `findByCategorySlugAndStatus(String categorySlug, CampaignStatus status, Pageable pageable)`
- `searchByKeyword(String keyword, CampaignStatus status, Pageable pageable)`

#### CampaignImageRepository.java

**Sorun:** Eksik metot.

**Eklenen metot:**
- `findByCampaignIdOrderByDisplayOrderAsc(UUID campaignId)`

#### PasswordResetTokenRepository.java

**Sorun:** Token hash ve toplu işlem metotları eksikti.

**Eklenen metotlar:**
- `findByTokenHash(String tokenHash)`
- `markAllUnusedTokensAsUsed(UUID userId, Instant now)`
- `deleteExpiredTokens(Instant now)` (overload)

#### EmailVerificationTokenRepository.java

**Sorun:** Service'in kullandığı metotlar eksikti.

**Eklenen metotlar:**
- `findByTokenHash(String tokenHash)`
- `countRecentTokens(UUID userId, LocalDateTime since)`
- `markAllUnverifiedTokensAsVerified(UUID userId, LocalDateTime now)`

#### EmailLogRepository.java

**Sorun:** Eksik metotlar.

**Eklenen metotlar:**
- `deleteBySentAtBefore(LocalDateTime before)`
- `findByUserId(UUID userId, Pageable pageable)`
- `findByEmailType(String emailType, Pageable pageable)`
- `findByStatus(String status, Pageable pageable)`

---

### 5. Service/Controller Düzeltmeleri

#### AdminReportService.java

**Sorunlar:**
1. `LocalDateTime.now()` yerine `OffsetDateTime.now()` kullanılmalıydı
2. String değerler enum tiplerine dönüştürülmeliydi

**Çözümler:**
- Tüm `LocalDateTime` kullanımları `OffsetDateTime`'a dönüştürüldü
- `ReportStatus.valueOf()` ve `ReportPriority.valueOf()` ile enum dönüşümleri eklendi

#### AdminReportController.java

**Sorun:** `getReportsByPriority` metodu String alıyordu ama service enum bekliyordu.

**Çözüm:**
```java
@GetMapping("/priority/{priority}")
public ResponseEntity<PageResponse<ReportResponse>> getReportsByPriority(
        @PathVariable String priority, Pageable pageable) {
    ReportPriority priorityEnum = ReportPriority.valueOf(priority.toUpperCase());
    return ResponseEntity.ok(adminReportService.getReportsByPriority(priorityEnum, pageable));
}
```

---

## 📁 Değiştirilen Dosyaların Tam Listesi

### DTO'lar
- `backend/src/main/java/com/seffafbagis/api/dto/request/evidence/CreateEvidenceRequest.java`
- `backend/src/main/java/com/seffafbagis/api/dto/request/application/CreateApplicationRequest.java`
- `backend/src/main/java/com/seffafbagis/api/dto/request/campaign/CreateCampaignRequest.java`
- `backend/src/main/java/com/seffafbagis/api/dto/request/campaign/UpdateCampaignRequest.java`
- `backend/src/main/java/com/seffafbagis/api/dto/request/organization/CreateOrganizationRequest.java`
- `backend/src/main/java/com/seffafbagis/api/dto/request/organization/UpdateOrganizationRequest.java`
- `backend/src/main/java/com/seffafbagis/api/dto/request/evidence/ReviewEvidenceRequest.java`
- `backend/src/main/java/com/seffafbagis/api/dto/request/user/UpdateProfileRequest.java`
- `backend/src/main/java/com/seffafbagis/api/dto/response/evidence/EvidenceResponse.java`
- `backend/src/main/java/com/seffafbagis/api/dto/response/evidence/EvidenceDetailResponse.java`
- `backend/src/main/java/com/seffafbagis/api/dto/response/organization/OrganizationStatistics.java`
- `backend/src/main/java/com/seffafbagis/api/dto/response/organization/OrganizationDetailResponse.java`
- `backend/src/main/java/com/seffafbagis/api/dto/response/organization/OrganizationListResponse.java`
- `backend/src/main/java/com/seffafbagis/api/dto/response/organization/OrganizationSummaryResponse.java`

### Entity'ler
- `backend/src/main/java/com/seffafbagis/api/entity/transparency/TransparencyScore.java`
- `backend/src/main/java/com/seffafbagis/api/entity/campaign/CampaignFollower.java`

### Exception'lar
- `backend/src/main/java/com/seffafbagis/api/exception/ConflictException.java`

### Repository'ler
- `backend/src/main/java/com/seffafbagis/api/repository/ApplicationRepository.java`
- `backend/src/main/java/com/seffafbagis/api/repository/LoginHistoryRepository.java`
- `backend/src/main/java/com/seffafbagis/api/repository/ReportRepository.java`
- `backend/src/main/java/com/seffafbagis/api/repository/CampaignRepository.java`
- `backend/src/main/java/com/seffafbagis/api/repository/CampaignImageRepository.java`
- `backend/src/main/java/com/seffafbagis/api/repository/PasswordResetTokenRepository.java`
- `backend/src/main/java/com/seffafbagis/api/repository/EmailVerificationTokenRepository.java`
- `backend/src/main/java/com/seffafbagis/api/repository/EmailLogRepository.java`

### Service/Controller'lar
- `backend/src/main/java/com/seffafbagis/api/service/admin/AdminReportService.java`
- `backend/src/main/java/com/seffafbagis/api/controller/admin/AdminReportController.java`

---

## ⚠️ Notlar

1. **IDE Lint Uyarıları:** Derleme başarılı olsa da IDE'de bazı uyarılar görülebilir (kullanılmayan import'lar, null safety uyarıları). Bunlar derleme hatası değildir ve opsiyonel olarak düzeltilebilir.

2. **@SuperBuilder Kullanımı:** Miras alan DTO'larda (`EvidenceDetailResponse` extends `EvidenceResponse`) MapStruct uyumluluğu için `@SuperBuilder` kullanılmalıdır.

3. **Enum Dönüşümleri:** Repository metotlarında String yerine enum tipleri kullanılması tip güvenliğini artırır ve derleme zamanında hata yakalanmasını sağlar.

---

## ✅ Doğrulama

```bash
mvn clean compile
# Sonuç: BUILD SUCCESS
```

Proje başarıyla derleniyor ve tüm hatalar çözüldü.
