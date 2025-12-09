package com.seffafbagis.api.specification;

import com.seffafbagis.api.dto.request.admin.UserSearchRequest;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.entity.user.UserProfile;
import com.seffafbagis.api.enums.UserRole;
import com.seffafbagis.api.enums.UserStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

public class UserSpecification {

    public static Specification<User> withSearchTerm(String searchTerm) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(searchTerm)) {
                return cb.conjunction();
            }
            String likePattern = "%" + searchTerm.toLowerCase() + "%";
            Join<User, UserProfile> profileJoin = root.join("profile", JoinType.LEFT);

            return cb.or(
                    cb.like(cb.lower(root.get("email")), likePattern),
                    cb.like(cb.lower(profileJoin.get("firstName")), likePattern),
                    cb.like(cb.lower(profileJoin.get("lastName")), likePattern));
        };
    }

    public static Specification<User> withRole(UserRole role) {
        return (root, query, cb) -> {
            if (role == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("role"), role);
        };
    }

    public static Specification<User> withStatus(UserStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<User> withEmailVerified(Boolean emailVerified) {
        return (root, query, cb) -> {
            if (emailVerified == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("emailVerified"), emailVerified);
        };
    }

    public static Specification<User> withCreatedBetween(LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            if (from == null && to == null) {
                return cb.conjunction();
            }
            if (from != null && to != null) {
                return cb.between(root.get("createdAt").as(LocalDate.class), from, to);
            }
            if (from != null) {
                return cb.greaterThanOrEqualTo(root.get("createdAt").as(LocalDate.class), from);
            }
            return cb.lessThanOrEqualTo(root.get("createdAt").as(LocalDate.class), to);
        };
    }

    public static Specification<User> buildSpecification(UserSearchRequest request) {
        return Specification.where(withSearchTerm(request.getSearchTerm()))
                .and(withRole(request.getRole()))
                .and(withStatus(request.getStatus()))
                .and(withEmailVerified(request.getEmailVerified()))
                .and(withCreatedBetween(request.getCreatedFrom(), request.getCreatedTo()));
    }
}
