package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.campaign.CampaignFollower;
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
import java.util.UUID;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CampaignFollowerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CampaignFollowerRepository campaignFollowerRepository;

    private Campaign campaign;
    private User user;

    @BeforeEach
    public void setUp() {
        User orgUser = new User();
        orgUser.setEmail("org-owner-follow@test.com");
        orgUser.setPasswordHash("hash");
        ReflectionTestUtils.setField(orgUser, "createdAt", OffsetDateTime.now());
        ReflectionTestUtils.setField(orgUser, "updatedAt", OffsetDateTime.now());
        orgUser = entityManager.persist(orgUser);

        Organization organization = new Organization();
        organization.setUser(orgUser);
        organization.setLegalName("Test Org Follow");
        organization.setTaxNumber("54321");
        organization.setOrganizationType(com.seffafbagis.api.enums.OrganizationType.ASSOCIATION);
        ReflectionTestUtils.setField(organization, "createdAt", OffsetDateTime.now());
        ReflectionTestUtils.setField(organization, "updatedAt", OffsetDateTime.now());
        organization = entityManager.persist(organization);

        campaign = new Campaign();
        campaign.setOrganization(organization);
        campaign.setTitle("Follow Campaign");
        campaign.setSlug("follow-campaign");
        campaign.setDescription("Desc");
        campaign.setTargetAmount(BigDecimal.valueOf(500));
        campaign.setStatus(CampaignStatus.ACTIVE);
        ReflectionTestUtils.setField(campaign, "createdAt", OffsetDateTime.now());
        ReflectionTestUtils.setField(campaign, "updatedAt", OffsetDateTime.now());
        campaign = entityManager.persist(campaign);

        user = new User();
        user.setEmail("follower@test.com");
        user.setPasswordHash("hash");
        ReflectionTestUtils.setField(user, "createdAt", OffsetDateTime.now());
        ReflectionTestUtils.setField(user, "updatedAt", OffsetDateTime.now());
        user = entityManager.persist(user);
    }

    @Test
    public void shouldSaveAndCheckExists() {
        CampaignFollower follower = new CampaignFollower();
        follower.setCampaign(campaign);
        follower.setUser(user);
        follower.setNotifyOnUpdate(true);

        follower.getId().setCampaignId(campaign.getId());
        follower.getId().setUserId(user.getId());

        campaignFollowerRepository.save(follower);

        boolean exists = campaignFollowerRepository.existsByUserIdAndCampaignId(user.getId(), campaign.getId());
        assertThat(exists).isTrue();

        boolean nonExistent = campaignFollowerRepository.existsByUserIdAndCampaignId(UUID.randomUUID(),
                campaign.getId());
        assertThat(nonExistent).isFalse();
    }
}
