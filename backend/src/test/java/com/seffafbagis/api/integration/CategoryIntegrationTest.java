package com.seffafbagis.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seffafbagis.api.dto.request.category.CreateCategoryRequest;
import com.seffafbagis.api.dto.request.category.UpdateCategoryRequest;
import com.seffafbagis.api.entity.category.Category;
import com.seffafbagis.api.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.jwt.secret=MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=",
        "app.encryption.secret-key=12345678901234567890123456789012",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.generate-ddl=true",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "iyzico.api-key=dummy-key",
        "iyzico.secret-key=dummy-secret",
        "iyzico.base-url=https://sandbox-api.iyzipay.com",
        "iyzico.callback-url=http://localhost:8080/api/v1/payments/callback",
        "app.frontend-url=http://localhost:3000"
})
@Transactional
class CategoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // Mocking legacy services to prevent ApplicationContext failure due to missing
    // dependencies
    @org.springframework.boot.test.mock.mockito.MockBean
    private com.seffafbagis.api.service.admin.AdminUserService adminUserService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.seffafbagis.api.service.admin.AdminOrganizationService adminOrganizationService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.seffafbagis.api.service.admin.AdminCampaignService adminCampaignService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.seffafbagis.api.service.system.SystemSettingService systemSettingService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.seffafbagis.api.service.notification.EmailService emailService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.seffafbagis.api.service.campaign.CampaignService campaignService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.seffafbagis.api.service.organization.OrganizationService organizationService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.boot.test.web.client.TestRestTemplate testRestTemplate;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_WhenAdmin_ShouldCreateCategory() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Education");
        request.setDescription("Education category");
        request.setDisplayOrder(1);

        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name", is("Education")))
                .andExpect(jsonPath("$.data.slug", is("education")));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createCategory_WhenUser_ShouldReturnForbidden() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Education");

        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllCategories_ShouldReturnActiveCategories() throws Exception {
        Category category = new Category();
        category.setName("Health");
        category.setSlug("health");
        category.setActive(true);
        categoryRepository.save(category);

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name", is("Health")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCategory_ShouldUpdateAndReturn() throws Exception {
        Category category = new Category();
        category.setName("Old Name");
        category.setSlug("old-name");
        category = categoryRepository.save(category);

        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setName("New Name");

        mockMvc.perform(put("/api/v1/categories/" + category.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name", is("New Name")))
                .andExpect(jsonPath("$.data.slug", is("new-name")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_ShouldDeactivate() throws Exception {
        Category category = new Category();
        category.setName("To Delete");
        category.setSlug("to-delete");
        category = categoryRepository.save(category);

        mockMvc.perform(delete("/api/v1/categories/" + category.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/categories/" + category.getId()))
                .andExpect(jsonPath("$.data.active", is(false)));
    }
}
