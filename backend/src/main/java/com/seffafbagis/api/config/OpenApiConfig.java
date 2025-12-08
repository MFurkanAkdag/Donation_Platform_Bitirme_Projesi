package com.seffafbagis.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configures the OpenAPI specification and Swagger UI metadata for the REST API.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI donationPlatformOpenAPI() {
        return new OpenAPI()
                .info(buildApiInfo())
                .components(buildSecurityComponents())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .tags(defaultTags());
    }

    private Info buildApiInfo() {
        return new Info()
                .title("Şeffaf Bağış Platformu API")
                .description("Transparent Donation Platform REST API Documentation")
                .version("1.0.0")
                .contact(buildContact())
                .license(new License().name("MIT").url("https://opensource.org/licenses/MIT"));
    }

    private Contact buildContact() {
        return new Contact()
                .name("Furkan")
                .email("furkan@seffafbagis.org")
                .url("https://seffafbagis.org");
    }

    private Components buildSecurityComponents() {
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Provide the JWT token obtained from the Auth endpoints. The 'Bearer' prefix is added automatically.");

        return new Components().addSecuritySchemes(SECURITY_SCHEME_NAME, bearerScheme);
    }

    private List<Tag> defaultTags() {
        return List.of(
                createTag("Auth", "Authentication and authorization use cases"),
                createTag("Users", "User profile and preference operations"),
                createTag("Admin", "Administrative endpoints"),
                createTag("Campaigns", "Donation campaign lifecycle"),
                createTag("Donations", "Donation processing and receipts"),
                createTag("Organizations", "Foundation and NGO management")
        );
    }

    private Tag createTag(String name, String description) {
        Tag tag = new Tag();
        tag.setName(name);
        tag.setDescription(description);
        return tag;
    }
}
