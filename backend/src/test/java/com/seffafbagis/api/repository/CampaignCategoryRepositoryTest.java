package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.campaign.CampaignCategory;
import com.seffafbagis.api.entity.category.Category;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.CampaignStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CampaignCategoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CampaignCategoryRepository campaignCategoryRepository;

    private Campaign campaign;
    private Category category;

    @BeforeEach
    public void setUp() {
        User user = new User();
        user.setEmail("org-owner-cat@test.com");
        user.setPasswordHash("hash");
        ReflectionTestUtils.setField(user, "createdAt", OffsetDateTime.now());
        ReflectionTestUtils.setField(user, "updatedAt", OffsetDateTime.now());
        user = entityManager.persist(user);

        Organization organization = new Organization();
        organization.setUser(user);
        organization.setLegalName("Test Org");
        organization.setTaxNumber("12345");
        organization.setOrganizationType(com.seffafbagis.api.enums.OrganizationType.FOUNDATION);
        ReflectionTestUtils.setField(organization, "createdAt", OffsetDateTime.now());
        ReflectionTestUtils.setField(organization, "updatedAt", OffsetDateTime.now());
        organization = entityManager.persist(organization);

        campaign = new Campaign();
        campaign.setOrganization(organization);
        campaign.setTitle("Test Campaign");
        campaign.setSlug("test-cat-campaign");
        campaign.setDescription("Desc");
        campaign.setTargetAmount(BigDecimal.valueOf(100));
        campaign.setStatus(CampaignStatus.DRAFT);
        ReflectionTestUtils.setField(campaign, "createdAt", OffsetDateTime.now());
        ReflectionTestUtils.setField(campaign, "updatedAt", OffsetDateTime.now());
        campaign = entityManager.persist(campaign);

        category = new Category();
        category.setName("Education");
        category.setSlug("education");
        category.setDescription("Edu desc");
        ReflectionTestUtils.setField(category, "createdAt", OffsetDateTime.now());
        ReflectionTestUtils.setField(category, "updatedAt", OffsetDateTime.now());
        category = entityManager.persist(category);
    }

    @Test
    public void shouldSaveAndRetrieveCampaignCategory() {
        CampaignCategory cc = new CampaignCategory();
        cc.setCampaign(campaign);
        cc.setCategory(category);
        cc.setIsPrimary(true);

        // Manual setting of ID is not required for BaseEntity
        // cc.getId().setCampaignId(campaign.getId());
        // cc.getId().setCategoryId(category.getId());

        campaignCategoryRepository.save(cc);

        List<CampaignCategory> result = campaignCategoryRepository.findByCampaignId(campaign.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory().getName()).isEqualTo("Education");
        assertThat(result.get(0).getIsPrimary()).isTrue();
    }
}
