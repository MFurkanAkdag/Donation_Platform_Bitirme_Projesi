package com.seffafbagis.api.dto.request.user;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Request DTO for updating KVKK-protected sensitive data.
 * All fields are optional.
 * Validation happens before encryption.
 */
public class UpdateSensitiveDataRequest {

    @Size(min = 11, max = 11, message = "TC Kimlik No must be exactly 11 characters")
    private String tcKimlik;

    @Size(max = 20, message = "Phone number must be at most 20 characters")
    private String phone;

    @Size(max = 500, message = "Address must be at most 500 characters")
    private String address;

    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;

    public String getTcKimlik() {
        return tcKimlik;
    }

    public void setTcKimlik(String tcKimlik) {
        this.tcKimlik = tcKimlik;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
}
