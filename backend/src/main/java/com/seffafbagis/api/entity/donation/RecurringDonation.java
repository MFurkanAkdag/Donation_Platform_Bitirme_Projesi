package com.seffafbagis.api.entity.donation;

import com.seffafbagis.api.entity.base.BaseEntity;
import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.category.DonationType;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a recurring donation subscription.
 * Allows donors to make repeated donations on a schedule.
 */
@Entity
@Getter
@Setter
@Table(name = "recurring_donations", indexes = {
        @Index(name = "idx_recurring_donor", columnList = "donor_id"),
        @Index(name = "idx_recurring_status", columnList = "status"),
        @Index(name = "idx_recurring_next_payment", columnList = "next_payment_date")
})
public class RecurringDonation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false)
    private User donor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign; // Nullable - can donate to organization directly

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization; // Nullable - can donate to campaign

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donation_type_id")
    private DonationType donationType;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 3)
    private String currency = "TRY";

    @Column(nullable = false, length = 20)
    private String frequency; // 'weekly', 'monthly', 'yearly'

    @Column(name = "next_payment_date", nullable = false)
    private LocalDate nextPaymentDate;

    @Column(name = "last_payment_date")
    private LocalDate lastPaymentDate;

    @Column(name = "total_donated", precision = 12, scale = 2)
    private BigDecimal totalDonated = BigDecimal.ZERO;

    @Column(name = "payment_count")
    private Integer paymentCount = 0;

    @Column(length = 20)
    private String status = "active"; // 'active', 'paused', 'cancelled'

    @Column(name = "card_token")
    private String cardToken;

    @Column(name = "failure_count")
    private Integer failureCount = 0;

    @Column(name = "last_error_message", columnDefinition = "TEXT")
    private String lastErrorMessage;

}
