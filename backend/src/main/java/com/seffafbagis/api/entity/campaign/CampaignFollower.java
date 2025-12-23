package com.seffafbagis.api.entity.campaign;

import com.seffafbagis.api.entity.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Represents a user following a campaign.
 */
@Entity
@Table(name = "campaign_followers", indexes = {
                @Index(name = "idx_campaign_followers_campaign", columnList = "campaign_id"),
                @Index(name = "idx_campaign_followers_user", columnList = "user_id")
}, uniqueConstraints = {
                @UniqueConstraint(columnNames = { "campaign_id", "user_id" })
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignFollower {

        @EmbeddedId
        private CampaignFollowerId id;

        @ManyToOne(fetch = FetchType.LAZY)
        @MapsId("campaignId")
        @JoinColumn(name = "campaign_id", nullable = false)
        private Campaign campaign;

        @ManyToOne(fetch = FetchType.LAZY)
        @MapsId("userId")
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

        @Builder.Default
        @Column(name = "notify_updates")
        private Boolean notifyUpdates = true;

        @Builder.Default
        @Column(name = "notify_milestones")
        private Boolean notifyMilestones = true;

        @Builder.Default
        @Column(name = "notify_on_update")
        private Boolean notifyOnUpdate = true;

        @Builder.Default
        @Column(name = "notify_on_complete")
        private Boolean notifyOnComplete = true;

        @Column(name = "created_at")
        private OffsetDateTime createdAt;

        @PrePersist
        protected void onCreate() {
                createdAt = OffsetDateTime.now();
        }
}
