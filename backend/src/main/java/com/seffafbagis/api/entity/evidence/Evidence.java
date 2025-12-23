package com.seffafbagis.api.entity.evidence;

import com.seffafbagis.api.entity.base.BaseEntity;
import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.EvidenceStatus;
import com.seffafbagis.api.enums.EvidenceType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "evidences")
@Getter
@Setter
public class Evidence extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Enumerated(EnumType.STRING)
    @Column(name = "evidence_type", nullable = false)
    private EvidenceType evidenceType;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "amount_spent", precision = 12, scale = 2)
    private BigDecimal amountSpent;

    @Column(name = "spend_date")
    private LocalDate spendDate;

    @Column(name = "vendor_name")
    private String vendorName;

    @Column(name = "vendor_tax_number", length = 20)
    private String vendorTaxNumber;

    @Column(name = "invoice_number", length = 100)
    private String invoiceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EvidenceStatus status = EvidenceStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    @Column(name = "uploaded_at")
    private OffsetDateTime uploadedAt;

    @OneToMany(mappedBy = "evidence", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EvidenceDocument> documents = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.uploadedAt == null) {
            this.uploadedAt = OffsetDateTime.now();
        }
        if (this.status == null) {
            this.status = EvidenceStatus.PENDING;
        }
    }
}
