package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.enums.CampaignStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

/**
 * Repository for Campaign entity.
 */
@Repository
public interface CampaignRepository extends JpaRepository<Campaign, UUID>, JpaSpecificationExecutor<Campaign> {

        Optional<Campaign> findBySlug(String slug);

        Page<Campaign> findAllByStatus(CampaignStatus status, Pageable pageable);

        Page<Campaign> findByStatus(CampaignStatus status, Pageable pageable);

        Page<Campaign> findAllByOrganizationId(UUID organizationId, Pageable pageable);

        Page<Campaign> findByOrganizationId(UUID organizationId, Pageable pageable);

        List<Campaign> findAllByOrganizationId(UUID organizationId);

        List<Campaign> findByOrganizationIdAndStatus(UUID organizationId, CampaignStatus status);

        long countByOrganizationId(UUID organizationId);

        long countByStatus(CampaignStatus status);

        @Query("SELECT c FROM Campaign c WHERE c.status = :status AND c.isFeatured = true")
        List<Campaign> findFeaturedCampaigns(@Param("status") CampaignStatus status);

        List<Campaign> findByIsFeaturedTrueAndStatus(CampaignStatus status);

        List<Campaign> findByIsUrgentTrueAndStatus(CampaignStatus status);

        @Query("SELECT c FROM Campaign c JOIN c.categories cc WHERE cc.category.slug = :categorySlug AND c.status = :status")
        Page<Campaign> findByCategorySlugAndStatus(@Param("categorySlug") String categorySlug,
                        @Param("status") CampaignStatus status,
                        Pageable pageable);

        @Query("SELECT c FROM Campaign c WHERE c.status = :status AND (LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
        Page<Campaign> searchByKeyword(@Param("keyword") String keyword,
                        @Param("status") CampaignStatus status,
                        Pageable pageable);

        List<Campaign> findByEndDateBeforeAndStatus(java.time.LocalDateTime endDate, CampaignStatus status);

        boolean existsBySlug(String slug);
}
