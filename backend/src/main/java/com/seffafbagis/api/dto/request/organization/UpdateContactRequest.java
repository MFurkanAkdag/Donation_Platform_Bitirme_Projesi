package com.seffafbagis.api.dto.request.organization;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateContactRequest {

    @Pattern(regexp = "^(primary|support|press|other)$", message = "Invalid contact type")
    private String contactType;

    @Size(max = 100)
    private String contactName;

    @Email(message = "Invalid email format")
    @Size(max = 255)
    private String email;

    @Size(max = 20)
    private String phone;

    @Size(max = 255)
    private String addressLine1;

    @Size(max = 255)
    private String addressLine2;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String district;

    @Size(max = 10)
    private String postalCode;

    @Size(max = 100)
    private String country;

    private Boolean isPrimary;

    // Manual Getters
    public String getContactType() {
        return contactType;
    }

    public String getContactName() {
        return contactName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCountry() {
        return country;
    }

    public Boolean getIsPrimary() {
        return isPrimary;
    }
}
