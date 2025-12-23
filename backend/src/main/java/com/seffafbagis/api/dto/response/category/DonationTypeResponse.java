package com.seffafbagis.api.dto.response.category;

import com.seffafbagis.api.enums.DonationTypeCode;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class DonationTypeResponse {
    private UUID id;
    private DonationTypeCode typeCode;
    private String name;
    private String nameEn;
    private String description;
    private String rules;
    private BigDecimal minimumAmount;
    private Boolean isActive;
    private OffsetDateTime createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public DonationTypeCode getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(DonationTypeCode typeCode) {
        this.typeCode = typeCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    public BigDecimal getMinimumAmount() {
        return minimumAmount;
    }

    public void setMinimumAmount(BigDecimal minimumAmount) {
        this.minimumAmount = minimumAmount;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
