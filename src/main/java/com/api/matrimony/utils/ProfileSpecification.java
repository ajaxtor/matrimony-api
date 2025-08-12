package com.api.matrimony.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.api.matrimony.entity.User;
import com.api.matrimony.entity.UserProfile;
import com.api.matrimony.enums.UserType;
import com.api.matrimony.request.SearchRequest;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

@Component
public class ProfileSpecification {

    public Specification<UserProfile> searchProfiles(Long currentUserId, UserType oppositeType, SearchRequest criteria) {
        return (root, query, cb) -> {
            // Join with users table
            Join<UserProfile, User> userJoin = root.join("user");

            List<Predicate> predicates = new ArrayList<>();

            // Exclude current user
            predicates.add(cb.notEqual(userJoin.get("id"), currentUserId));

            // Opposite gender type
            predicates.add(cb.equal(userJoin.get("userType"), oppositeType));

            // City / State / Religion / Caste / SubCaste
            if (criteria.getCity() != null && !criteria.getCity().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("city")), criteria.getCity().toLowerCase()));
            }
            if (criteria.getState() != null && !criteria.getState().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("state")), criteria.getState().toLowerCase()));
            }
            if (criteria.getReligion() != null && !criteria.getReligion().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("religion")), criteria.getReligion().toLowerCase()));
            }
            if (criteria.getCaste() != null && !criteria.getCaste().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("caste")), criteria.getCaste().toLowerCase()));
            }
            if (criteria.getSubCaste() != null && !criteria.getSubCaste().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("subCaste")), criteria.getSubCaste().toLowerCase()));
            }

            // Height / Weight range
            if (criteria.getMinHeight() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("height"), criteria.getMinHeight()));
            }
            if (criteria.getMaxHeight() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("height"), criteria.getMaxHeight()));
            }
            if (criteria.getMinWeight() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("weight"), criteria.getMinWeight()));
            }
            if (criteria.getMaxWeight() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("weight"), criteria.getMaxWeight()));
            }

            // Marital status / Education / Occupation / Diet
            if (criteria.getMaritalStatus() != null) {
                predicates.add(cb.equal(root.get("maritalStatus"), criteria.getMaritalStatus()));
            }
            if (criteria.getEducation() != null && !criteria.getEducation().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("education")), criteria.getEducation().toLowerCase()));
            }
            if (criteria.getOccupation() != null && !criteria.getOccupation().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("occupation")), criteria.getOccupation().toLowerCase()));
            }
            if (criteria.getDiet() != null && !criteria.getDiet().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("diet")), criteria.getDiet().toLowerCase()));
            }

            // Income range
            if (criteria.getMinIncome() != null && criteria.getMinIncome().compareTo(BigDecimal.ZERO) > 0) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("income"), criteria.getMinIncome()));
            }
            if (criteria.getMaxIncome() != null && criteria.getMaxIncome().compareTo(BigDecimal.ZERO) > 0) {
                predicates.add(cb.lessThanOrEqualTo(root.get("income"), criteria.getMaxIncome()));
            }

            // ✅ Age range filter using MySQL TIMESTAMPDIFF YEAR without quotes
            if (criteria.getMinAge() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        cb.function(
                                "TIMESTAMPDIFF", Integer.class,
                                cb.literal("YEAR"),
                                root.get("dateOfBirth"),
                                cb.currentDate()
                        ),
                        criteria.getMinAge()
                ));
            }
            if (criteria.getMaxAge() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        cb.function(
                                "TIMESTAMPDIFF", Integer.class,
                                cb.literal("YEAR"),
                                root.get("dateOfBirth"),
                                cb.currentDate()
                        ),
                        criteria.getMaxAge()
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
