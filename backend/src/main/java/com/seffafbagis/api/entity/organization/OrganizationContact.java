package com.seffafbagis.api.entity.organization;

import com.seffafbagis.api.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "organization_contacts")
public class OrganizationContact extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "contact_type", nullable = false)
    private String contactType; // 'primary', 'support', 'press'

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(name = "city")
    private String city;

    @Column(name = "district")
    private String district;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "country")
    private String country = "Türkiye";

    @Column(name = "is_primary")
    private Boolean isPrimary = false;

    // We can't use createdAt from BaseEntity if we want to manually control it via
    // PrePersist or if it maps differently?
    // BaseEntity uses OffsetDateTime and has @CreatedDate.
    // The prompt asks for `private LocalDateTime createdAt;` and `@PrePersist`.
    // BaseEntity already has `protected OffsetDateTime createdAt;`.
    // However, the existing schema V3 has `created_at TIMESTAMPTZ`. BaseEntity maps
    // to `TIMESTAMPTZ` with `OffsetDateTime`.
    // The prompt explicitly asked for `private LocalDateTime createdAt;` BUT
    // extends `BaseEntity`.
    // IF I extend BaseEntity, I inherit createdAt.
    // BaseEntity uses OffsetDateTime.
    // Prompt asks for LocalDateTime.
    // Usually it's better to stick to BaseEntity's field to avoid shadowing and
    // confusion.
    // However, the prompt specifically included a code snippet with `@PrePersist`.
    // "Add @PrePersist for createdAt".
    // BaseEntity uses `@EntityListeners(AuditingEntityListener.class)` and
    // `@CreatedDate`.
    // So BaseEntity handles this automatically.
    // I should probably skip the manual `createdAt` field and `@PrePersist` if
    // BaseEntity handles it.
    // BUT the prompt says "Key implementation details: Extend BaseEntity".
    // AND "Fields to include: ... private LocalDateTime createdAt;".
    // If I include it, it will shadow the BaseEntity one or cause conflict.
    // Let's check Organization.java I just created. defining createdAt in
    // Organization? No, Organization extends BaseEntity.
    // Organization table in V3 has `created_at`.
    // BaseEntity maps it.
    // The user's prompt might be slightly inconsistent with "Extend BaseEntity" vs
    // "Define createdAt manually".
    // However, for OrganizationContact, the table also has `created_at`.
    // I will use BaseEntity's createdAt and NOT define a new one, to be consistent
    // with the project's architecture (Furkan's work).
    // I will explicitly NOT add the manual `createdAt` field and `@PrePersist`
    // block, relying on BaseEntity.
    // Wait, the prompt says "Add @PrePersist for createdAt".
    // If I don't adds it, the user might complain I didn't follow instructions.
    // But if I add it, it breaks compilation or runtime (duplicate column mapping).
    // I will assume the prompt meant "Ensure createdAt is handled", but since I
    // extend BaseEntity, it is handled.
    // EXCEPT: BaseEntity uses `OffsetDateTime`. The prompt asks for
    // `LocalDateTime`.
    // DB type is `TIMESTAMPTZ`, which maps better to `OffsetDateTime` or
    // `ZonedDateTime`.
    // User might have copy-pasted a snippet that uses LocalDateTime.
    // I will respect BaseEntity as the source of truth for the project.

    // Getters and Setters

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public String getContactType() {
        return contactType;
    }

    public void setContactType(String contactType) {
        this.contactType = contactType;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Boolean getPrimary() {
        return isPrimary;
    }

    public void setPrimary(Boolean primary) {
        isPrimary = primary;
    }
}
