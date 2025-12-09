package com.seffafbagis.api.dto.response.admin;

import com.seffafbagis.api.enums.UserRole;
import com.seffafbagis.api.enums.UserStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class AdminDashboardResponse {

    private Long totalUsers;
    private Map<UserRole, Long> usersByRole;
    private Map<UserStatus, Long> usersByStatus;
    private Long newUsersToday;
    private Long newUsersThisWeek;
    private Long newUsersThisMonth;
    private Long activeUsersToday;
    private Long activeUsersThisWeek;
    private Long pendingVerifications;
    private Long suspendedAccounts;
    private List<AdminUserListResponse> recentRegistrations;
    private LocalDateTime generatedAt;

    public AdminDashboardResponse() {
        this.generatedAt = LocalDateTime.now();
    }

    public Long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public Map<UserRole, Long> getUsersByRole() {
        return usersByRole;
    }

    public void setUsersByRole(Map<UserRole, Long> usersByRole) {
        this.usersByRole = usersByRole;
    }

    public Map<UserStatus, Long> getUsersByStatus() {
        return usersByStatus;
    }

    public void setUsersByStatus(Map<UserStatus, Long> usersByStatus) {
        this.usersByStatus = usersByStatus;
    }

    public Long getNewUsersToday() {
        return newUsersToday;
    }

    public void setNewUsersToday(Long newUsersToday) {
        this.newUsersToday = newUsersToday;
    }

    public Long getNewUsersThisWeek() {
        return newUsersThisWeek;
    }

    public void setNewUsersThisWeek(Long newUsersThisWeek) {
        this.newUsersThisWeek = newUsersThisWeek;
    }

    public Long getNewUsersThisMonth() {
        return newUsersThisMonth;
    }

    public void setNewUsersThisMonth(Long newUsersThisMonth) {
        this.newUsersThisMonth = newUsersThisMonth;
    }

    public Long getActiveUsersToday() {
        return activeUsersToday;
    }

    public void setActiveUsersToday(Long activeUsersToday) {
        this.activeUsersToday = activeUsersToday;
    }

    public Long getActiveUsersThisWeek() {
        return activeUsersThisWeek;
    }

    public void setActiveUsersThisWeek(Long activeUsersThisWeek) {
        this.activeUsersThisWeek = activeUsersThisWeek;
    }

    public Long getPendingVerifications() {
        return pendingVerifications;
    }

    public void setPendingVerifications(Long pendingVerifications) {
        this.pendingVerifications = pendingVerifications;
    }

    public Long getSuspendedAccounts() {
        return suspendedAccounts;
    }

    public void setSuspendedAccounts(Long suspendedAccounts) {
        this.suspendedAccounts = suspendedAccounts;
    }

    public List<AdminUserListResponse> getRecentRegistrations() {
        return recentRegistrations;
    }

    public void setRecentRegistrations(List<AdminUserListResponse> recentRegistrations) {
        this.recentRegistrations = recentRegistrations;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}
