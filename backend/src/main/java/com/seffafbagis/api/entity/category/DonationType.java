package com.seffafbagis.api.entity.category;

import com.seffafbagis.api.entity.base.BaseEntity;
import com.seffafbagis.api.enums.DonationTypeCode;
import jakarta.persistence.*;
import java.io.Serial;
import java.math.BigDecimal;

@Entity
@Table(name = "donation_types")
public class DonationType extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_code", nullable = false, unique = true)
    private DonationTypeCode typeCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "name_en", length = 100)
    private String nameEn;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String rules;

    @Column(name = "minimum_amount", precision = 12, scale = 2)
    private BigDecimal minimumAmount;

    @Column(name = "is_active")
    private Boolean isActive = true;

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
}
