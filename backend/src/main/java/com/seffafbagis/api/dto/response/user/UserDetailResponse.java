package com.seffafbagis.api.dto.response.user;

import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.entity.user.UserPreference;
import com.seffafbagis.api.entity.user.UserProfile;
import com.seffafbagis.api.entity.user.UserSensitiveData;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Complete user information (combines all user data).
 */
public class UserDetailResponse {

    private UserResponse user;
    private UserProfileResponse profile;
    private UserPreferenceResponse preferences;
    private UserSensitiveDataResponse sensitiveData;
    private UserStatistics statistics;

    public static UserDetailResponse of(UserResponse user, UserProfileResponse profile,
            UserPreferenceResponse preferences, UserSensitiveDataResponse sensitiveData) {
        UserDetailResponse response = new UserDetailResponse();
        response.setUser(user);
        response.setProfile(profile);
        response.setPreferences(preferences);
        response.setSensitiveData(sensitiveData);
        // Statistics would be populated by a separate service call
        return response;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }

    public UserProfileResponse getProfile() {
        return profile;
    }

    public void setProfile(UserProfileResponse profile) {
        this.profile = profile;
    }

    public UserPreferenceResponse getPreferences() {
        return preferences;
    }

    public void setPreferences(UserPreferenceResponse preferences) {
        this.preferences = preferences;
    }

    public UserSensitiveDataResponse getSensitiveData() {
        return sensitiveData;
    }

    public void setSensitiveData(UserSensitiveDataResponse sensitiveData) {
        this.sensitiveData = sensitiveData;
    }

    public UserStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(UserStatistics statistics) {
        this.statistics = statistics;
    }

    public static class UserStatistics {
        private Integer totalDonations;
        private BigDecimal totalDonationAmount;
        private Integer favoriteOrganizationsCount;
        private LocalDate memberSince;

        public Integer getTotalDonations() {
            return totalDonations;
        }

        public void setTotalDonations(Integer totalDonations) {
            this.totalDonations = totalDonations;
        }

        public BigDecimal getTotalDonationAmount() {
            return totalDonationAmount;
        }

        public void setTotalDonationAmount(BigDecimal totalDonationAmount) {
            this.totalDonationAmount = totalDonationAmount;
        }

        public Integer getFavoriteOrganizationsCount() {
            return favoriteOrganizationsCount;
        }

        public void setFavoriteOrganizationsCount(Integer favoriteOrganizationsCount) {
            this.favoriteOrganizationsCount = favoriteOrganizationsCount;
        }

        public LocalDate getMemberSince() {
            return memberSince;
        }

        public void setMemberSince(LocalDate memberSince) {
            this.memberSince = memberSince;
        }
    }
}
