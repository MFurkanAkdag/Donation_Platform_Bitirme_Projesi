# ŞEFFAF BAĞIŞ PLATFORMU - JAVA PROJE YAPISI


## PROJE DİZİN YAPISI

```
seffaf-bagis-platform/
│
├── 📁 backend/                              # Spring Boot Uygulaması
│   ├── 📁 src/
│   │   ├── 📁 main/
│   │   │   ├── 📁 java/
│   │   │   │   └── 📁 com/
│   │   │   │       └── 📁 seffafbagis/
│   │   │   │           └── 📁 api/
│   │   │   │               │
│   │   │   │               ├── 📄 SeffafBagisApplication.java
│   │   │   │               │
│   │   │   │               ├── 📁 config/                    # Yapılandırma
│   │   │   │               │   ├── 📄 SecurityConfig.java
│   │   │   │               │   ├── 📄 JwtConfig.java
│   │   │   │               │   ├── 📄 CorsConfig.java
│   │   │   │               │   ├── 📄 RedisConfig.java
│   │   │   │               │   ├── 📄 OpenApiConfig.java
│   │   │   │               │   └── 📄 AuditConfig.java
│   │   │   │               │
│   │   │   │               ├── 📁 controller/                # API Endpoints
│   │   │   │               │   ├── 📁 auth/
│   │   │   │               │   │   └── 📄 AuthController.java
│   │   │   │               │   ├── 📁 user/
│   │   │   │               │   │   ├── 📄 UserController.java
│   │   │   │               │   │   └── 📄 UserProfileController.java
│   │   │   │               │   ├── 📁 organization/
│   │   │   │               │   │   ├── 📄 OrganizationController.java
│   │   │   │               │   │   └── 📄 OrganizationDocumentController.java
│   │   │   │               │   ├── 📁 campaign/
│   │   │   │               │   │   ├── 📄 CampaignController.java
│   │   │   │               │   │   ├── 📄 CampaignUpdateController.java
│   │   │   │               │   │   └── 📄 CampaignImageController.java
│   │   │   │               │   ├── 📁 donation/
│   │   │   │               │   │   ├── 📄 DonationController.java
│   │   │   │               │   │   ├── 📄 RecurringDonationController.java
│   │   │   │               │   │   └── 📄 BankTransferController.java
│   │   │   │               │   ├── 📁 payment/
│   │   │   │               │   │   └── 📄 PaymentController.java
│   │   │   │               │   ├── 📁 evidence/
│   │   │   │               │   │   └── 📄 EvidenceController.java
│   │   │   │               │   ├── 📁 transparency/
│   │   │   │               │   │   └── 📄 TransparencyController.java
│   │   │   │               │   ├── 📁 application/
│   │   │   │               │   │   └── 📄 ApplicationController.java
│   │   │   │               │   ├── 📁 notification/
│   │   │   │               │   │   └── 📄 NotificationController.java
│   │   │   │               │   ├── 📁 report/
│   │   │   │               │   │   └── 📄 ReportController.java
│   │   │   │               │   ├── 📁 category/
│   │   │   │               │   │   ├── 📄 CategoryController.java
│   │   │   │               │   │   └── 📄 DonationTypeController.java
│   │   │   │               │   └── 📁 admin/
│   │   │   │               │       ├── 📄 AdminUserController.java
│   │   │   │               │       ├── 📄 AdminOrganizationController.java
│   │   │   │               │       ├── 📄 AdminCampaignController.java
│   │   │   │               │       └── 📄 AdminReportController.java
│   │   │   │               │
│   │   │   │               ├── 📁 service/                   # İş Mantığı
│   │   │   │               │   ├── 📁 auth/
│   │   │   │               │   │   ├── 📄 AuthService.java
│   │   │   │               │   │   ├── 📄 JwtService.java
│   │   │   │               │   │   └── 📄 PasswordResetService.java
│   │   │   │               │   ├── 📁 user/
│   │   │   │               │   │   ├── 📄 UserService.java
│   │   │   │               │   │   ├── 📄 UserProfileService.java
│   │   │   │               │   │   └── 📄 UserPreferenceService.java
│   │   │   │               │   ├── 📁 organization/
│   │   │   │               │   │   ├── 📄 OrganizationService.java
│   │   │   │               │   │   ├── 📄 OrganizationContactService.java
│   │   │   │               │   │   ├── 📄 OrganizationDocumentService.java
│   │   │   │               │   │   └── 📄 OrganizationBankAccountService.java
│   │   │   │               │   ├── 📁 campaign/
│   │   │   │               │   │   ├── 📄 CampaignService.java
│   │   │   │               │   │   ├── 📄 CampaignUpdateService.java
│   │   │   │               │   │   ├── 📄 CampaignImageService.java
│   │   │   │               │   │   └── 📄 CampaignFollowerService.java
│   │   │   │               │   ├── 📁 donation/
│   │   │   │               │   │   ├── 📄 DonationService.java
│   │   │   │               │   │   ├── 📄 RecurringDonationService.java
│   │   │   │               │   │   ├── 📄 DonationReceiptService.java
│   │   │   │               │   │   └── 📄 BankTransferService.java
│   │   │   │               │   ├── 📁 payment/
│   │   │   │               │   │   ├── 📄 PaymentService.java
│   │   │   │               │   │   ├── 📄 IyzicoService.java
│   │   │   │               │   │   └── 📄 TransactionService.java
│   │   │   │               │   ├── 📁 evidence/
│   │   │   │               │   │   ├── 📄 EvidenceService.java
│   │   │   │               │   │   └── 📄 EvidenceDocumentService.java
│   │   │   │               │   ├── 📁 transparency/
│   │   │   │               │   │   ├── 📄 TransparencyScoreService.java
│   │   │   │               │   │   └── 📄 TransparencyScoreCalculator.java
│   │   │   │               │   ├── 📁 application/
│   │   │   │               │   │   ├── 📄 ApplicationService.java
│   │   │   │               │   │   └── 📄 ApplicationDocumentService.java
│   │   │   │               │   ├── 📁 notification/
│   │   │   │               │   │   ├── 📄 NotificationService.java
│   │   │   │               │   │   └── 📄 EmailService.java
│   │   │   │               │   ├── 📁 report/
│   │   │   │               │   │   └── 📄 ReportService.java
│   │   │   │               │   ├── 📁 category/
│   │   │   │               │   │   ├── 📄 CategoryService.java
│   │   │   │               │   │   └── 📄 DonationTypeService.java
│   │   │   │               │   ├── 📁 storage/
│   │   │   │               │   │   └── 📄 FileStorageService.java
│   │   │   │               │   ├── 📁 encryption/
│   │   │   │               │   │   └── 📄 EncryptionService.java
│   │   │   │               │   └── 📁 audit/
│   │   │   │               │       └── 📄 AuditLogService.java
│   │   │   │               │
│   │   │   │               ├── 📁 repository/                # Veritabanı Erişim
│   │   │   │               │   ├── 📄 UserRepository.java
│   │   │   │               │   ├── 📄 UserProfileRepository.java
│   │   │   │               │   ├── 📄 UserSensitiveDataRepository.java
│   │   │   │               │   ├── 📄 UserPreferenceRepository.java
│   │   │   │               │   ├── 📄 OrganizationRepository.java
│   │   │   │               │   ├── 📄 OrganizationContactRepository.java
│   │   │   │               │   ├── 📄 OrganizationDocumentRepository.java
│   │   │   │               │   ├── 📄 OrganizationBankAccountRepository.java
│   │   │   │               │   ├── 📄 CategoryRepository.java
│   │   │   │               │   ├── 📄 DonationTypeRepository.java
│   │   │   │               │   ├── 📄 CampaignRepository.java
│   │   │   │               │   ├── 📄 CampaignCategoryRepository.java
│   │   │   │               │   ├── 📄 CampaignDonationTypeRepository.java
│   │   │   │               │   ├── 📄 CampaignUpdateRepository.java
│   │   │   │               │   ├── 📄 CampaignImageRepository.java
│   │   │   │               │   ├── 📄 CampaignFollowerRepository.java
│   │   │   │               │   ├── 📄 DonationRepository.java
│   │   │   │               │   ├── 📄 TransactionRepository.java
│   │   │   │               │   ├── 📄 DonationReceiptRepository.java
│   │   │   │               │   ├── 📄 RecurringDonationRepository.java
│   │   │   │               │   ├── 📄 BankTransferReferenceRepository.java
│   │   │   │               │   ├── 📄 EvidenceRepository.java
│   │   │   │               │   ├── 📄 EvidenceDocumentRepository.java
│   │   │   │               │   ├── 📄 TransparencyScoreRepository.java
│   │   │   │               │   ├── 📄 TransparencyScoreHistoryRepository.java
│   │   │   │               │   ├── 📄 ApplicationRepository.java
│   │   │   │               │   ├── 📄 ApplicationDocumentRepository.java
│   │   │   │               │   ├── 📄 NotificationRepository.java
│   │   │   │               │   ├── 📄 AuditLogRepository.java
│   │   │   │               │   ├── 📄 EmailLogRepository.java
│   │   │   │               │   ├── 📄 ReportRepository.java
│   │   │   │               │   ├── 📄 RefreshTokenRepository.java
│   │   │   │               │   ├── 📄 PasswordResetTokenRepository.java
│   │   │   │               │   ├── 📄 FavoriteOrganizationRepository.java
│   │   │   │               │   └── 📄 SystemSettingRepository.java
│   │   │   │               │
│   │   │   │               ├── 📁 entity/                    # Veritabanı Tabloları
│   │   │   │               │   ├── 📁 user/
│   │   │   │               │   │   ├── 📄 User.java
│   │   │   │               │   │   ├── 📄 UserProfile.java
│   │   │   │               │   │   ├── 📄 UserSensitiveData.java
│   │   │   │               │   │   └── 📄 UserPreference.java
│   │   │   │               │   ├── 📁 organization/
│   │   │   │               │   │   ├── 📄 Organization.java
│   │   │   │               │   │   ├── 📄 OrganizationContact.java
│   │   │   │               │   │   ├── 📄 OrganizationDocument.java
│   │   │   │               │   │   └── 📄 OrganizationBankAccount.java
│   │   │   │               │   ├── 📁 campaign/
│   │   │   │               │   │   ├── 📄 Campaign.java
│   │   │   │               │   │   ├── 📄 CampaignCategory.java
│   │   │   │               │   │   ├── 📄 CampaignDonationType.java
│   │   │   │               │   │   ├── 📄 CampaignUpdate.java
│   │   │   │               │   │   ├── 📄 CampaignImage.java
│   │   │   │               │   │   └── 📄 CampaignFollower.java
│   │   │   │               │   ├── 📁 donation/
│   │   │   │               │   │   ├── 📄 Donation.java
│   │   │   │               │   │   ├── 📄 Transaction.java
│   │   │   │               │   │   ├── 📄 DonationReceipt.java
│   │   │   │               │   │   ├── 📄 RecurringDonation.java
│   │   │   │               │   │   └── 📄 BankTransferReference.java
│   │   │   │               │   ├── 📁 evidence/
│   │   │   │               │   │   ├── 📄 Evidence.java
│   │   │   │               │   │   └── 📄 EvidenceDocument.java
│   │   │   │               │   ├── 📁 transparency/
│   │   │   │               │   │   ├── 📄 TransparencyScore.java
│   │   │   │               │   │   └── 📄 TransparencyScoreHistory.java
│   │   │   │               │   ├── 📁 application/
│   │   │   │               │   │   ├── 📄 Application.java
│   │   │   │               │   │   └── 📄 ApplicationDocument.java
│   │   │   │               │   ├── 📁 category/
│   │   │   │               │   │   ├── 📄 Category.java
│   │   │   │               │   │   └── 📄 DonationType.java
│   │   │   │               │   ├── 📁 notification/
│   │   │   │               │   │   ├── 📄 Notification.java
│   │   │   │               │   │   └── 📄 EmailLog.java
│   │   │   │               │   ├── 📁 audit/
│   │   │   │               │   │   └── 📄 AuditLog.java
│   │   │   │               │   ├── 📁 report/
│   │   │   │               │   │   └── 📄 Report.java
│   │   │   │               │   ├── 📁 auth/
│   │   │   │               │   │   ├── 📄 RefreshToken.java
│   │   │   │               │   │   └── 📄 PasswordResetToken.java
│   │   │   │               │   ├── 📁 system/
│   │   │   │               │   │   └── 📄 SystemSetting.java
│   │   │   │               │   ├── 📁 favorite/
│   │   │   │               │   │   └── 📄 FavoriteOrganization.java
│   │   │   │               │   └── 📁 base/
│   │   │   │               │       └── 📄 BaseEntity.java
│   │   │   │               │
│   │   │   │               ├── 📁 dto/                       # Data Transfer Objects
│   │   │   │               │   ├── 📁 request/
│   │   │   │               │   │   ├── 📁 auth/
│   │   │   │               │   │   │   ├── 📄 LoginRequest.java
│   │   │   │               │   │   │   ├── 📄 RegisterRequest.java
│   │   │   │               │   │   │   ├── 📄 RefreshTokenRequest.java
│   │   │   │               │   │   │   ├── 📄 PasswordResetRequest.java
│   │   │   │               │   │   │   └── 📄 ChangePasswordRequest.java
│   │   │   │               │   │   ├── 📁 user/
│   │   │   │               │   │   │   ├── 📄 UpdateProfileRequest.java
│   │   │   │               │   │   │   ├── 📄 UpdatePreferencesRequest.java
│   │   │   │               │   │   │   └── 📄 UpdateSensitiveDataRequest.java
│   │   │   │               │   │   ├── 📁 organization/
│   │   │   │               │   │   │   ├── 📄 CreateOrganizationRequest.java
│   │   │   │               │   │   │   ├── 📄 UpdateOrganizationRequest.java
│   │   │   │               │   │   │   ├── 📄 AddContactRequest.java
│   │   │   │               │   │   │   ├── 📄 AddDocumentRequest.java
│   │   │   │               │   │   │   └── 📄 AddBankAccountRequest.java
│   │   │   │               │   │   ├── 📁 campaign/
│   │   │   │               │   │   │   ├── 📄 CreateCampaignRequest.java
│   │   │   │               │   │   │   ├── 📄 UpdateCampaignRequest.java
│   │   │   │               │   │   │   ├── 📄 AddCampaignUpdateRequest.java
│   │   │   │               │   │   │   └── 📄 AddCampaignImageRequest.java
│   │   │   │               │   │   ├── 📁 donation/
│   │   │   │               │   │   │   ├── 📄 CreateDonationRequest.java
│   │   │   │               │   │   │   ├── 📄 CreateRecurringDonationRequest.java
│   │   │   │               │   │   │   └── 📄 InitiateBankTransferRequest.java
│   │   │   │               │   │   ├── 📁 payment/
│   │   │   │               │   │   │   ├── 📄 PaymentRequest.java
│   │   │   │               │   │   │   └── 📄 CardInfoRequest.java
│   │   │   │               │   │   ├── 📁 evidence/
│   │   │   │               │   │   │   ├── 📄 CreateEvidenceRequest.java
│   │   │   │               │   │   │   └── 📄 ReviewEvidenceRequest.java
│   │   │   │               │   │   ├── 📁 application/
│   │   │   │               │   │   │   └── 📄 CreateApplicationRequest.java
│   │   │   │               │   │   ├── 📁 report/
│   │   │   │               │   │   │   └── 📄 CreateReportRequest.java
│   │   │   │               │   │   └── 📁 admin/
│   │   │   │               │   │       ├── 📄 VerifyOrganizationRequest.java
│   │   │   │               │   │       ├── 📄 ApproveCampaignRequest.java
│   │   │   │               │   │       └── 📄 ResolveReportRequest.java
│   │   │   │               │   │
│   │   │   │               │   ├── 📁 response/
│   │   │   │               │   │   ├── 📁 auth/
│   │   │   │               │   │   │   ├── 📄 AuthResponse.java
│   │   │   │               │   │   │   └── 📄 TokenResponse.java
│   │   │   │               │   │   ├── 📁 user/
│   │   │   │               │   │   │   ├── 📄 UserResponse.java
│   │   │   │               │   │   │   ├── 📄 UserProfileResponse.java
│   │   │   │               │   │   │   └── 📄 UserPreferenceResponse.java
│   │   │   │               │   │   ├── 📁 organization/
│   │   │   │               │   │   │   ├── 📄 OrganizationResponse.java
│   │   │   │               │   │   │   ├── 📄 OrganizationDetailResponse.java
│   │   │   │               │   │   │   ├── 📄 OrganizationListResponse.java
│   │   │   │               │   │   │   └── 📄 OrganizationSummaryResponse.java
│   │   │   │               │   │   ├── 📁 campaign/
│   │   │   │               │   │   │   ├── 📄 CampaignResponse.java
│   │   │   │               │   │   │   ├── 📄 CampaignDetailResponse.java
│   │   │   │               │   │   │   ├── 📄 CampaignListResponse.java
│   │   │   │               │   │   │   ├── 📄 CampaignUpdateResponse.java
│   │   │   │               │   │   │   └── 📄 CampaignStatsResponse.java
│   │   │   │               │   │   ├── 📁 donation/
│   │   │   │               │   │   │   ├── 📄 DonationResponse.java
│   │   │   │               │   │   │   ├── 📄 DonationListResponse.java
│   │   │   │               │   │   │   ├── 📄 RecurringDonationResponse.java
│   │   │   │               │   │   │   ├── 📄 BankTransferInfoResponse.java
│   │   │   │               │   │   │   └── 📄 DonationReceiptResponse.java
│   │   │   │               │   │   ├── 📁 payment/
│   │   │   │               │   │   │   └── 📄 PaymentResultResponse.java
│   │   │   │               │   │   ├── 📁 evidence/
│   │   │   │               │   │   │   ├── 📄 EvidenceResponse.java
│   │   │   │               │   │   │   └── 📄 EvidenceListResponse.java
│   │   │   │               │   │   ├── 📁 transparency/
│   │   │   │               │   │   │   ├── 📄 TransparencyScoreResponse.java
│   │   │   │               │   │   │   └── 📄 ScoreHistoryResponse.java
│   │   │   │               │   │   ├── 📁 category/
│   │   │   │               │   │   │   ├── 📄 CategoryResponse.java
│   │   │   │               │   │   │   └── 📄 DonationTypeResponse.java
│   │   │   │               │   │   ├── 📁 notification/
│   │   │   │               │   │   │   └── 📄 NotificationResponse.java
│   │   │   │               │   │   └── 📁 common/
│   │   │   │               │   │       ├── 📄 ApiResponse.java
│   │   │   │               │   │       ├── 📄 PageResponse.java
│   │   │   │               │   │       └── 📄 ErrorResponse.java
│   │   │   │               │   │
│   │   │   │               │   └── 📁 mapper/
│   │   │   │               │       ├── 📄 UserMapper.java
│   │   │   │               │       ├── 📄 OrganizationMapper.java
│   │   │   │               │       ├── 📄 CampaignMapper.java
│   │   │   │               │       ├── 📄 DonationMapper.java
│   │   │   │               │       ├── 📄 EvidenceMapper.java
│   │   │   │               │       └── 📄 CategoryMapper.java
│   │   │   │               │
│   │   │   │               ├── 📁 enums/                     # Enum Tipleri
│   │   │   │               │   ├── 📄 UserRole.java
│   │   │   │               │   ├── 📄 UserStatus.java
│   │   │   │               │   ├── 📄 OrganizationType.java
│   │   │   │               │   ├── 📄 VerificationStatus.java
│   │   │   │               │   ├── 📄 CampaignStatus.java
│   │   │   │               │   ├── 📄 DonationTypeCode.java
│   │   │   │               │   ├── 📄 DonationStatus.java
│   │   │   │               │   ├── 📄 PaymentMethod.java
│   │   │   │               │   ├── 📄 EvidenceType.java
│   │   │   │               │   ├── 📄 EvidenceStatus.java
│   │   │   │               │   ├── 📄 ApplicationStatus.java
│   │   │   │               │   ├── 📄 NotificationType.java
│   │   │   │               │   ├── 📄 ReportType.java
│   │   │   │               │   └── 📄 ReportStatus.java
│   │   │   │               │
│   │   │   │               ├── 📁 exception/                 # Hata Yönetimi
│   │   │   │               │   ├── 📄 GlobalExceptionHandler.java
│   │   │   │               │   ├── 📄 ResourceNotFoundException.java
│   │   │   │               │   ├── 📄 BadRequestException.java
│   │   │   │               │   ├── 📄 UnauthorizedException.java
│   │   │   │               │   ├── 📄 ForbiddenException.java
│   │   │   │               │   ├── 📄 ConflictException.java
│   │   │   │               │   ├── 📄 PaymentException.java
│   │   │   │               │   ├── 📄 FileStorageException.java
│   │   │   │               │   └── 📄 EncryptionException.java
│   │   │   │               │
│   │   │   │               ├── 📁 security/                  # Güvenlik
│   │   │   │               │   ├── 📄 JwtTokenProvider.java
│   │   │   │               │   ├── 📄 JwtAuthenticationFilter.java
│   │   │   │               │   ├── 📄 JwtAuthenticationEntryPoint.java
│   │   │   │               │   ├── 📄 CustomUserDetails.java
│   │   │   │               │   ├── 📄 CustomUserDetailsService.java
│   │   │   │               │   └── 📄 SecurityUtils.java
│   │   │   │               │
│   │   │   │               ├── 📁 scheduler/                 # Zamanlanmış Görevler
│   │   │   │               │   ├── 📄 RecurringDonationScheduler.java
│   │   │   │               │   ├── 📄 BankTransferExpiryScheduler.java
│   │   │   │               │   ├── 📄 EvidenceReminderScheduler.java
│   │   │   │               │   └── 📄 TransparencyScoreScheduler.java
│   │   │   │               │
│   │   │   │               ├── 📁 validator/                 # Özel Validasyonlar
│   │   │   │               │   ├── 📄 IbanValidator.java
│   │   │   │               │   ├── 📄 TcKimlikValidator.java
│   │   │   │               │   ├── 📄 PhoneValidator.java
│   │   │   │               │   └── 📄 PasswordValidator.java
│   │   │   │               │
│   │   │   │               ├── 📁 event/                     # Event-Driven
│   │   │   │               │   ├── 📄 DonationCreatedEvent.java
│   │   │   │               │   ├── 📄 CampaignCompletedEvent.java
│   │   │   │               │   ├── 📄 EvidenceApprovedEvent.java
│   │   │   │               │   └── 📁 listener/
│   │   │   │               │       ├── 📄 DonationEventListener.java
│   │   │   │               │       ├── 📄 CampaignEventListener.java
│   │   │   │               │       └── 📄 EvidenceEventListener.java
│   │   │   │               │
│   │   │   │               └── 📁 util/                      # Yardımcı Sınıflar
│   │   │   │                   ├── 📄 SlugGenerator.java
│   │   │   │                   ├── 📄 ReferenceCodeGenerator.java
│   │   │   │                   ├── 📄 ReceiptNumberGenerator.java
│   │   │   │                   └── 📄 DateUtils.java
│   │   │   │
│   │   │   └── 📁 resources/
│   │   │       ├── 📄 application.yml                        # Ana yapılandırma
│   │   │       ├── 📄 application-dev.yml                    # Development
│   │   │       ├── 📄 application-prod.yml                   # Production
│   │   │       ├── 📄 application-test.yml                   # Test
│   │   │       ├── 📁 db/
│   │   │       │   └── 📁 migration/                         # Flyway migrations
│   │   │       │       ├── 📄 V1__create_enum_types.sql
│   │   │       │       ├── 📄 V2__create_user_tables.sql
│   │   │       │       ├── 📄 V3__create_organization_tables.sql
│   │   │       │       ├── 📄 V4__create_category_tables.sql
│   │   │       │       ├── 📄 V5__create_campaign_tables.sql
│   │   │       │       ├── 📄 V6__create_donation_tables.sql
│   │   │       │       ├── 📄 V7__create_evidence_tables.sql
│   │   │       │       ├── 📄 V8__create_application_tables.sql
│   │   │       │       ├── 📄 V9__create_notification_tables.sql
│   │   │       │       ├── 📄 V10__create_auth_tables.sql
│   │   │       │       ├── 📄 V11__create_system_tables.sql
│   │   │       │       ├── 📄 V12__create_indexes.sql
│   │   │       │       ├── 📄 V13__create_triggers.sql
│   │   │       │       ├── 📄 V14__create_views.sql
│   │   │       │       └── 📄 V15__insert_initial_data.sql
│   │   │       ├── 📁 templates/
│   │   │       │   └── 📁 email/                             # E-posta şablonları
│   │   │       │       ├── 📄 welcome.html
│   │   │       │       ├── 📄 donation-receipt.html
│   │   │       │       ├── 📄 password-reset.html
│   │   │       │       ├── 📄 evidence-reminder.html
│   │   │       │       └── 📄 verification-success.html
│   │   │       ├── 📄 messages.properties                    # i18n Türkçe
│   │   │       ├── 📄 messages_en.properties                 # i18n İngilizce
│   │   │       └── 📄 ValidationMessages.properties          # Validasyon mesajları
│   │   │
│   │   └── 📁 test/
│   │       ├── 📁 java/
│   │       │   └── 📁 com/
│   │       │       └── 📁 seffafbagis/
│   │       │           └── 📁 api/
│   │       │               ├── 📁 controller/
│   │       │               │   ├── 📄 AuthControllerTest.java
│   │       │               │   ├── 📄 CampaignControllerTest.java
│   │       │               │   └── 📄 DonationControllerTest.java
│   │       │               ├── 📁 service/
│   │       │               │   ├── 📄 UserServiceTest.java
│   │       │               │   ├── 📄 CampaignServiceTest.java
│   │       │               │   ├── 📄 DonationServiceTest.java
│   │       │               │   └── 📄 TransparencyScoreServiceTest.java
│   │       │               ├── 📁 repository/
│   │       │               │   ├── 📄 UserRepositoryTest.java
│   │       │               │   └── 📄 CampaignRepositoryTest.java
│   │       │               └── 📁 integration/
│   │       │                   ├── 📄 AuthIntegrationTest.java
│   │       │                   ├── 📄 DonationFlowIntegrationTest.java
│   │       │                   └── 📄 PaymentIntegrationTest.java
│   │       └── 📁 resources/
│   │           ├── 📄 application-test.yml
│   │           └── 📁 fixtures/
│   │               ├── 📄 users.json
│   │               ├── 📄 campaigns.json
│   │               └── 📄 donations.json
│   │
│   ├── 📄 pom.xml                                           # Maven bağımlılıkları
│   ├── 📄 Dockerfile                                        # Docker image
│   ├── 📄 .env.example                                      # Örnek environment
│   └── 📄 README.md                                         # Backend dokümantasyonu
│
├── 📁 frontend/                             # Next.js Uygulaması
│   ├── 📁 src/
│   │   ├── 📁 app/                          # App Router (Next.js 14)
│   │   │   ├── 📄 layout.tsx
│   │   │   ├── 📄 page.tsx                  # Ana sayfa
│   │   │   ├── 📄 globals.css
│   │   │   ├── 📁 (auth)/                   # Auth gruplandırma
│   │   │   │   ├── 📁 login/
│   │   │   │   │   └── 📄 page.tsx
│   │   │   │   ├── 📁 register/
│   │   │   │   │   └── 📄 page.tsx
│   │   │   │   ├── 📁 forgot-password/
│   │   │   │   │   └── 📄 page.tsx
│   │   │   │   └── 📁 reset-password/
│   │   │   │       └── 📄 page.tsx
│   │   │   ├── 📁 (main)/                   # Ana layout grubu
│   │   │   │   ├── 📄 layout.tsx
│   │   │   │   ├── 📁 campaigns/
│   │   │   │   │   ├── 📄 page.tsx          # Kampanya listesi
│   │   │   │   │   └── 📁 [slug]/
│   │   │   │   │       └── 📄 page.tsx      # Kampanya detay
│   │   │   │   ├── 📁 organizations/
│   │   │   │   │   ├── 📄 page.tsx
│   │   │   │   │   └── 📁 [id]/
│   │   │   │   │       └── 📄 page.tsx
│   │   │   │   ├── 📁 categories/
│   │   │   │   │   └── 📁 [slug]/
│   │   │   │   │       └── 📄 page.tsx
│   │   │   │   ├── 📁 donate/
│   │   │   │   │   └── 📁 [campaignId]/
│   │   │   │   │       ├── 📄 page.tsx      # Bağış formu
│   │   │   │   │       └── 📁 success/
│   │   │   │   │           └── 📄 page.tsx  # Başarılı bağış
│   │   │   │   └── 📁 about/
│   │   │   │       └── 📄 page.tsx
│   │   │   ├── 📁 dashboard/                # Kullanıcı paneli
│   │   │   │   ├── 📄 layout.tsx
│   │   │   │   ├── 📄 page.tsx              # Dashboard ana sayfa
│   │   │   │   ├── 📁 profile/
│   │   │   │   │   └── 📄 page.tsx
│   │   │   │   ├── 📁 donations/
│   │   │   │   │   └── 📄 page.tsx          # Bağış geçmişim
│   │   │   │   ├── 📁 recurring/
│   │   │   │   │   └── 📄 page.tsx          # Tekrarlayan bağışlar
│   │   │   │   ├── 📁 following/
│   │   │   │   │   └── 📄 page.tsx          # Takip ettiklerim
│   │   │   │   ├── 📁 favorites/
│   │   │   │   │   └── 📄 page.tsx          # Favori vakıflar
│   │   │   │   ├── 📁 notifications/
│   │   │   │   │   └── 📄 page.tsx
│   │   │   │   └── 📁 settings/
│   │   │   │       └── 📄 page.tsx
│   │   │   ├── 📁 foundation/               # Vakıf paneli
│   │   │   │   ├── 📄 layout.tsx
│   │   │   │   ├── 📄 page.tsx
│   │   │   │   ├── 📁 campaigns/
│   │   │   │   │   ├── 📄 page.tsx          # Kampanyalarım
│   │   │   │   │   ├── 📁 new/
│   │   │   │   │   │   └── 📄 page.tsx      # Yeni kampanya
│   │   │   │   │   └── 📁 [id]/
│   │   │   │   │       ├── 📄 page.tsx      # Kampanya düzenle
│   │   │   │   │       └── 📁 evidences/
│   │   │   │   │           └── 📄 page.tsx  # Kanıt yükle
│   │   │   │   ├── 📁 donations/
│   │   │   │   │   └── 📄 page.tsx          # Gelen bağışlar
│   │   │   │   ├── 📁 transparency/
│   │   │   │   │   └── 📄 page.tsx          # Şeffaflık skoru
│   │   │   │   ├── 📁 documents/
│   │   │   │   │   └── 📄 page.tsx          # Belgelerim
│   │   │   │   ├── 📁 bank-accounts/
│   │   │   │   │   └── 📄 page.tsx          # Banka hesapları
│   │   │   │   └── 📁 settings/
│   │   │   │       └── 📄 page.tsx
│   │   │   ├── 📁 admin/                    # Admin paneli
│   │   │   │   ├── 📄 layout.tsx
│   │   │   │   ├── 📄 page.tsx
│   │   │   │   ├── 📁 users/
│   │   │   │   │   └── 📄 page.tsx
│   │   │   │   ├── 📁 organizations/
│   │   │   │   │   ├── 📄 page.tsx          # Vakıf listesi
│   │   │   │   │   └── 📁 pending/
│   │   │   │   │       └── 📄 page.tsx      # Onay bekleyenler
│   │   │   │   ├── 📁 campaigns/
│   │   │   │   │   ├── 📄 page.tsx
│   │   │   │   │   └── 📁 pending/
│   │   │   │   │       └── 📄 page.tsx
│   │   │   │   ├── 📁 evidences/
│   │   │   │   │   └── 📄 page.tsx          # Kanıt inceleme
│   │   │   │   ├── 📁 reports/
│   │   │   │   │   └── 📄 page.tsx          # Şikayetler
│   │   │   │   ├── 📁 bank-transfers/
│   │   │   │   │   └── 📄 page.tsx          # Havale eşleştirme
│   │   │   │   └── 📁 settings/
│   │   │   │       └── 📄 page.tsx          # Sistem ayarları
│   │   │   ├── 📁 apply/                    # Yardım başvurusu
│   │   │   │   └── 📄 page.tsx
│   │   │   └── 📁 api/                      # API routes (opsiyonel)
│   │   │       └── 📁 auth/
│   │   │           └── 📄 [...nextauth]/route.ts
│   │   │
│   │   ├── 📁 components/                   # React Bileşenleri
│   │   │   ├── 📁 ui/                       # Temel UI bileşenleri
│   │   │   │   ├── 📄 Button.tsx
│   │   │   │   ├── 📄 Input.tsx
│   │   │   │   ├── 📄 Card.tsx
│   │   │   │   ├── 📄 Modal.tsx
│   │   │   │   ├── 📄 Badge.tsx
│   │   │   │   ├── 📄 Avatar.tsx
│   │   │   │   ├── 📄 Dropdown.tsx
│   │   │   │   ├── 📄 Tabs.tsx
│   │   │   │   ├── 📄 Progress.tsx
│   │   │   │   ├── 📄 Skeleton.tsx
│   │   │   │   ├── 📄 Toast.tsx
│   │   │   │   └── 📄 Spinner.tsx
│   │   │   ├── 📁 layout/
│   │   │   │   ├── 📄 Header.tsx
│   │   │   │   ├── 📄 Footer.tsx
│   │   │   │   ├── 📄 Sidebar.tsx
│   │   │   │   ├── 📄 Navbar.tsx
│   │   │   │   └── 📄 MobileMenu.tsx
│   │   │   ├── 📁 campaign/
│   │   │   │   ├── 📄 CampaignCard.tsx
│   │   │   │   ├── 📄 CampaignList.tsx
│   │   │   │   ├── 📄 CampaignDetail.tsx
│   │   │   │   ├── 📄 CampaignProgress.tsx
│   │   │   │   ├── 📄 CampaignUpdates.tsx
│   │   │   │   ├── 📄 CampaignGallery.tsx
│   │   │   │   ├── 📄 DonorList.tsx
│   │   │   │   └── 📄 CampaignForm.tsx
│   │   │   ├── 📁 donation/
│   │   │   │   ├── 📄 DonationForm.tsx
│   │   │   │   ├── 📄 DonationTypeSelector.tsx
│   │   │   │   ├── 📄 PaymentForm.tsx
│   │   │   │   ├── 📄 BankTransferInfo.tsx
│   │   │   │   ├── 📄 DonationHistory.tsx
│   │   │   │   ├── 📄 RecurringDonationCard.tsx
│   │   │   │   └── 📄 DonationReceipt.tsx
│   │   │   ├── 📁 organization/
│   │   │   │   ├── 📄 OrganizationCard.tsx
│   │   │   │   ├── 📄 OrganizationDetail.tsx
│   │   │   │   ├── 📄 TransparencyBadge.tsx
│   │   │   │   ├── 📄 TransparencyScore.tsx
│   │   │   │   └── 📄 VerificationBadge.tsx
│   │   │   ├── 📁 evidence/
│   │   │   │   ├── 📄 EvidenceCard.tsx
│   │   │   │   ├── 📄 EvidenceList.tsx
│   │   │   │   ├── 📄 EvidenceUploadForm.tsx
│   │   │   │   └── 📄 EvidenceReviewPanel.tsx
│   │   │   ├── 📁 auth/
│   │   │   │   ├── 📄 LoginForm.tsx
│   │   │   │   ├── 📄 RegisterForm.tsx
│   │   │   │   ├── 📄 ForgotPasswordForm.tsx
│   │   │   │   └── 📄 ProtectedRoute.tsx
│   │   │   ├── 📁 category/
│   │   │   │   ├── 📄 CategoryCard.tsx
│   │   │   │   ├── 📄 CategoryList.tsx
│   │   │   │   └── 📄 CategoryFilter.tsx
│   │   │   ├── 📁 notification/
│   │   │   │   ├── 📄 NotificationBell.tsx
│   │   │   │   ├── 📄 NotificationList.tsx
│   │   │   │   └── 📄 NotificationItem.tsx
│   │   │   └── 📁 common/
│   │   │       ├── 📄 SearchBar.tsx
│   │   │       ├── 📄 Pagination.tsx
│   │   │       ├── 📄 EmptyState.tsx
│   │   │       ├── 📄 ErrorBoundary.tsx
│   │   │       ├── 📄 LoadingState.tsx
│   │   │       └── 📄 ConfirmDialog.tsx
│   │   │
│   │   ├── 📁 lib/                          # Yardımcı fonksiyonlar
│   │   │   ├── 📄 api.ts                    # Axios instance
│   │   │   ├── 📄 auth.ts                   # Auth helpers
│   │   │   ├── 📄 utils.ts                  # Genel utilities
│   │   │   ├── 📄 formatters.ts             # Tarih, para formatları
│   │   │   └── 📄 validations.ts            # Form validasyonları
│   │   │
│   │   ├── 📁 hooks/                        # Custom React hooks
│   │   │   ├── 📄 useAuth.ts
│   │   │   ├── 📄 useCampaigns.ts
│   │   │   ├── 📄 useDonations.ts
│   │   │   ├── 📄 useNotifications.ts
│   │   │   ├── 📄 useOrganization.ts
│   │   │   └── 📄 useDebounce.ts
│   │   │
│   │   ├── 📁 store/                        # State management (Zustand)
│   │   │   ├── 📄 authStore.ts
│   │   │   ├── 📄 cartStore.ts              # Bağış sepeti
│   │   │   └── 📄 notificationStore.ts
│   │   │
│   │   ├── 📁 types/                        # TypeScript tipleri
│   │   │   ├── 📄 user.ts
│   │   │   ├── 📄 campaign.ts
│   │   │   ├── 📄 donation.ts
│   │   │   ├── 📄 organization.ts
│   │   │   ├── 📄 evidence.ts
│   │   │   ├── 📄 category.ts
│   │   │   ├── 📄 notification.ts
│   │   │   └── 📄 api.ts
│   │   │
│   │   └── 📁 constants/                    # Sabit değerler
│   │       ├── 📄 routes.ts
│   │       ├── 📄 config.ts
│   │       └── 📄 messages.ts
│   │
│   ├── 📁 public/
│   │   ├── 📁 images/
│   │   │   ├── 📄 logo.svg
│   │   │   ├── 📄 hero-bg.jpg
│   │   │   └── 📁 icons/
│   │   └── 📁 fonts/
│   │
│   ├── 📄 package.json
│   ├── 📄 next.config.js
│   ├── 📄 tailwind.config.ts
│   ├── 📄 tsconfig.json
│   ├── 📄 .env.local.example
│   ├── 📄 Dockerfile
│   └── 📄 README.md
│
├── 📁 docs/                                 # Dokümantasyon
│   ├── 📄 API.md                            # API dokümantasyonu
│   ├── 📄 DATABASE.md                       # Veritabanı şeması
│   ├── 📄 DEPLOYMENT.md                     # Deployment kılavuzu
│   ├── 📄 DEVELOPMENT.md                    # Geliştirme kılavuzu
│   └── 📄 ARCHITECTURE.md                   # Mimari kararlar
│
├── 📁 docker/                               # Docker yapılandırmaları
│   ├── 📄 docker-compose.yml                # Geliştirme ortamı
│   ├── 📄 docker-compose.prod.yml           # Production
│   ├── 📁 nginx/
│   │   └── 📄 nginx.conf
│   └── 📁 postgres/
│       └── 📄 init.sql
│
├── 📁 scripts/                              # Yardımcı scriptler
│   ├── 📄 setup.sh                          # İlk kurulum
│   ├── 📄 seed.sh                           # Veritabanı seed
│   └── 📄 backup.sh                         # Yedekleme
│
├── 📄 .gitignore
├── 📄 README.md
└── 📄 LICENSE
```


