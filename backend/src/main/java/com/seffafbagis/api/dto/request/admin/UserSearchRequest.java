package com.seffafbagis.api.dto.request.admin;

import com.seffafbagis.api.enums.UserRole;
import com.seffafbagis.api.enums.UserStatus;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class UserSearchRequest {

    @Size(max = 100, message = "Search term cannot exceed 100 characters")
    private String searchTerm;

    private UserRole role;
    private UserStatus status;
    private Boolean emailVerified;
    private LocalDate createdFrom;
    private LocalDate createdTo;

    // Sort parameters are typically handled via Pageable, but specific sort field
    // preference can be passed here if needed for custom logic
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";

    public UserSearchRequest() {
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public LocalDate getCreatedFrom() {
        return createdFrom;
    }

    public void setCreatedFrom(LocalDate createdFrom) {
        this.createdFrom = createdFrom;
    }

    public LocalDate getCreatedTo() {
        return createdTo;
    }

    public void setCreatedTo(LocalDate createdTo) {
        this.createdTo = createdTo;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
}
