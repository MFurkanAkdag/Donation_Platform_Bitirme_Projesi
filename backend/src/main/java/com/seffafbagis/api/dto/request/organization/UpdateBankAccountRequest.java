package com.seffafbagis.api.dto.request.organization;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateBankAccountRequest {

    @Size(max = 100)
    private String bankName;

    @Size(max = 5)
    private String bankCode;

    @Size(max = 100)
    private String branchName;

    @Size(max = 10)
    private String branchCode;

    @Size(max = 100)
    private String branchCity;

    @Size(max = 100)
    private String branchDistrict;

    @Size(max = 255)
    private String accountHolder;

    @Size(max = 30)
    private String accountNumber;

    @Size(min = 26, max = 34, message = "IBAN must be between 26 and 34 characters")
    @Pattern(regexp = "^TR[0-9]{24}$", message = "Invalid Turkish IBAN format")
    private String iban;

    @Size(max = 3)
    private String currency;

    @Size(max = 50)
    private String accountType;

    private Boolean isPrimary;

    // Manual Getters
    public String getBankName() {
        return bankName;
    }

    public String getBankCode() {
        return bankCode;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public String getBranchCity() {
        return branchCity;
    }

    public String getBranchDistrict() {
        return branchDistrict;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getIban() {
        return iban;
    }

    public String getCurrency() {
        return currency;
    }

    public String getAccountType() {
        return accountType;
    }

    public Boolean getIsPrimary() {
        return isPrimary;
    }
}
