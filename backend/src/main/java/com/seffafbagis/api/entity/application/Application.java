package com.seffafbagis.api.entity.application;

import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.category.Category;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.entity.base.BaseEntity;
import com.seffafbagis.api.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "applications")
@Getter
@Setter
public class Application extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id")
    private User applicant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "requested_amount")
    private BigDecimal requestedAmount;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50) default 'PENDING'")
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(name = "location_city")
    private String locationCity;

    @Column(name = "location_district")
    private String locationDistrict;

    @Column(name = "household_size")
    private Integer householdSize;

    @Column(name = "urgency_level")
    private Integer urgencyLevel = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_organization_id")
    private Organization assignedOrganization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_campaign_id")
    private Campaign assignedCampaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApplicationDocument> documents = new ArrayList<>();
}
