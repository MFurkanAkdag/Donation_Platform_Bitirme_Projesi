package com.seffafbagis.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * OpenAPI (Swagger) dokümantasyon yapılandırması.
 * 
 * Bu yapılandırma sayesinde:
 * - API endpoint'leri otomatik dokümante edilir
 * - Swagger UI üzerinden API test edilebilir
 * - JWT token ile kimlik doğrulama yapılabilir
 * 
 * Swagger UI erişim adresi: http://localhost:8080/swagger-ui.html
 * OpenAPI JSON: http://localhost:8080/v3/api-docs
 * 
 * @author Furkan
 * @version 1.0
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:Şeffaf Bağış API}")
    private String applicationName;

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * JWT güvenlik şeması adı.
     * Controller'larda @SecurityRequirement ile kullanılır.
     */
    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    /**
     * OpenAPI yapılandırması.
     * 
     * @return Yapılandırılmış OpenAPI nesnesi
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(createApiInfo())
                .servers(createServerList())
                .components(createSecurityComponents())
                .addSecurityItem(createSecurityRequirement())
                .tags(createTags());
    }

    /**
     * API bilgilerini oluşturur.
     * 
     * @return Info nesnesi
     */
    private Info createApiInfo() {
        return new Info()
                .title("Şeffaf Bağış Platformu API")
                .description(createApiDescription())
                .version("1.0.0")
                .contact(createContact())
                .license(createLicense());
    }

    /**
     * API açıklaması.
     * 
     * @return API açıklama metni
     */
    private String createApiDescription() {
        StringBuilder description = new StringBuilder();
        description.append("# Şeffaf Bağış Platformu REST API\n\n");
        description.append("Bu API, şeffaf bağış platformunun backend servislerini sağlar.\n\n");
        description.append("## Özellikler\n");
        description.append("- **Kullanıcı Yönetimi**: Kayıt, giriş, profil yönetimi\n");
        description.append("- **Vakıf/Dernek Yönetimi**: Organizasyon kayıt ve doğrulama\n");
        description.append("- **Kampanya Yönetimi**: Bağış kampanyaları oluşturma ve takip\n");
        description.append("- **Bağış İşlemleri**: Güvenli ödeme ve bağış takibi\n");
        description.append("- **Şeffaflık Skorlaması**: Vakıf güvenilirlik değerlendirmesi\n\n");
        description.append("## Kimlik Doğrulama\n");
        description.append("API, JWT (JSON Web Token) tabanlı kimlik doğrulama kullanır.\n");
        description.append("1. `/api/v1/auth/login` endpoint'ine kullanıcı bilgilerini gönderin\n");
        description.append("2. Dönen `accessToken` değerini alın\n");
        description.append("3. Diğer isteklerde `Authorization: Bearer {token}` header'ı ekleyin\n");
        return description.toString();
    }

    /**
     * İletişim bilgilerini oluşturur.
     * 
     * @return Contact nesnesi
     */
    private Contact createContact() {
        Contact contact = new Contact();
        contact.setName("Şeffaf Bağış Platformu Geliştirme Ekibi");
        contact.setEmail("dev@seffafbagis.com");
        contact.setUrl("https://seffafbagis.com");
        return contact;
    }

    /**
     * Lisans bilgilerini oluşturur.
     * 
     * @return License nesnesi
     */
    private License createLicense() {
        License license = new License();
        license.setName("MIT License");
        license.setUrl("https://opensource.org/licenses/MIT");
        return license;
    }

    /**
     * Sunucu listesini oluşturur.
     * 
     * @return Server listesi
     */
    private List<Server> createServerList() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:" + serverPort);
        localServer.setDescription("Geliştirme Sunucusu");

        Server productionServer = new Server();
        productionServer.setUrl("https://api.seffafbagis.com");
        productionServer.setDescription("Production Sunucusu");

        return Arrays.asList(localServer, productionServer);
    }

    /**
     * Güvenlik bileşenlerini oluşturur.
     * JWT Bearer token için gerekli yapılandırma.
     * 
     * @return Components nesnesi
     */
    private Components createSecurityComponents() {
        SecurityScheme securityScheme = new SecurityScheme();
        securityScheme.setType(SecurityScheme.Type.HTTP);
        securityScheme.setScheme("bearer");
        securityScheme.setBearerFormat("JWT");
        securityScheme.setDescription("JWT token'ınızı buraya girin. 'Bearer' öneki otomatik eklenir.");

        Components components = new Components();
        components.addSecuritySchemes(SECURITY_SCHEME_NAME, securityScheme);
        return components;
    }

    /**
     * Güvenlik gereksinimini oluşturur.
     * 
     * @return SecurityRequirement nesnesi
     */
    private SecurityRequirement createSecurityRequirement() {
        SecurityRequirement securityRequirement = new SecurityRequirement();
        securityRequirement.addList(SECURITY_SCHEME_NAME);
        return securityRequirement;
    }

    /**
     * API tag'lerini oluşturur.
     * Controller'ları gruplamak için kullanılır.
     * 
     * @return Tag listesi
     */
    private List<Tag> createTags() {
        return Arrays.asList(
            createTag("Auth", "Kimlik doğrulama işlemleri (Login, Register, Token)"),
            createTag("Users", "Kullanıcı yönetimi işlemleri"),
            createTag("Organizations", "Vakıf/Dernek yönetimi"),
            createTag("Campaigns", "Bağış kampanyaları"),
            createTag("Donations", "Bağış işlemleri"),
            createTag("Payments", "Ödeme işlemleri"),
            createTag("Evidence", "Harcama kanıtları"),
            createTag("Transparency", "Şeffaflık skorları"),
            createTag("Applications", "Yardım başvuruları"),
            createTag("Categories", "Kategori ve bağış türleri"),
            createTag("Notifications", "Bildirim yönetimi"),
            createTag("Admin", "Yönetici işlemleri"),
            createTag("System", "Sistem ayarları")
        );
    }

    /**
     * Tek bir tag oluşturur.
     * 
     * @param name Tag adı
     * @param description Tag açıklaması
     * @return Tag nesnesi
     */
    private Tag createTag(String name, String description) {
        Tag tag = new Tag();
        tag.setName(name);
        tag.setDescription(description);
        return tag;
    }
}
