package com.seffafbagis.api.dto.response.application;

import java.util.Map;

public class ApplicationStatsResponse {

    private long totalApplications;
    private long pending;
    private long inReview;
    private long approved;
    private long rejected;
    private long completed;

    private Map<String, Long> byCategory;
    private Map<String, Long> byCity;

    public ApplicationStatsResponse() {
    }

    public ApplicationStatsResponse(long totalApplications, long pending, long inReview, long approved, long rejected, long completed, Map<String, Long> byCategory, Map<String, Long> byCity) {
        this.totalApplications = totalApplications;
        this.pending = pending;
        this.inReview = inReview;
        this.approved = approved;
        this.rejected = rejected;
        this.completed = completed;
        this.byCategory = byCategory;
        this.byCity = byCity;
    }

    public long getTotalApplications() {
        return totalApplications;
    }

    public void setTotalApplications(long totalApplications) {
        this.totalApplications = totalApplications;
    }

    public long getPending() {
        return pending;
    }

    public void setPending(long pending) {
        this.pending = pending;
    }

    public long getInReview() {
        return inReview;
    }

    public void setInReview(long inReview) {
        this.inReview = inReview;
    }

    public long getApproved() {
        return approved;
    }

    public void setApproved(long approved) {
        this.approved = approved;
    }

    public long getRejected() {
        return rejected;
    }

    public void setRejected(long rejected) {
        this.rejected = rejected;
    }

    public long getCompleted() {
        return completed;
    }

    public void setCompleted(long completed) {
        this.completed = completed;
    }

    public Map<String, Long> getByCategory() {
        return byCategory;
    }

    public void setByCategory(Map<String, Long> byCategory) {
        this.byCategory = byCategory;
    }

    public Map<String, Long> getByCity() {
        return byCity;
    }

    public void setByCity(Map<String, Long> byCity) {
        this.byCity = byCity;
    }

    public static ApplicationStatsResponseBuilder builder() {
        return new ApplicationStatsResponseBuilder();
    }

    public static class ApplicationStatsResponseBuilder {
        private long totalApplications;
        private long pending;
        private long inReview;
        private long approved;
        private long rejected;
        private long completed;
        private Map<String, Long> byCategory;
        private Map<String, Long> byCity;

        public ApplicationStatsResponseBuilder totalApplications(long totalApplications) {
            this.totalApplications = totalApplications;
            return this;
        }

        public ApplicationStatsResponseBuilder pending(long pending) {
            this.pending = pending;
            return this;
        }

        public ApplicationStatsResponseBuilder inReview(long inReview) {
            this.inReview = inReview;
            return this;
        }

        public ApplicationStatsResponseBuilder approved(long approved) {
            this.approved = approved;
            return this;
        }

        public ApplicationStatsResponseBuilder rejected(long rejected) {
            this.rejected = rejected;
            return this;
        }

        public ApplicationStatsResponseBuilder completed(long completed) {
            this.completed = completed;
            return this;
        }

        public ApplicationStatsResponseBuilder byCategory(Map<String, Long> byCategory) {
            this.byCategory = byCategory;
            return this;
        }

        public ApplicationStatsResponseBuilder byCity(Map<String, Long> byCity) {
            this.byCity = byCity;
            return this;
        }

        public ApplicationStatsResponse build() {
            return new ApplicationStatsResponse(totalApplications, pending, inReview, approved, rejected, completed, byCategory, byCity);
        }
    }
}
