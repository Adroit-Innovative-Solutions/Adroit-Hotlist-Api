package com.adroit.hotlistmicroservice.utils;

import com.adroit.hotlistmicroservice.model.Consultant;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ConsultantSpecifications {

    public static Specification<Consultant> createSearchSpecification(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction();
            }

            String pattern = "%" + keyword.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();

            // ✅ string fields
            predicates.add(cb.like(cb.lower(root.get("consultantId")), pattern));
            predicates.add(cb.like(cb.lower(root.get("name")), pattern));
            predicates.add(cb.like(cb.lower(root.get("emailId")), pattern));
            predicates.add(cb.like(cb.lower(root.get("grade")), pattern));
            predicates.add(cb.like(cb.lower(root.get("marketingContact")), pattern));
            predicates.add(cb.like(cb.lower(root.get("personalContact")), pattern));
            predicates.add(cb.like(cb.lower(root.get("reference")), pattern));
            predicates.add(cb.like(cb.lower(root.get("recruiterId")), pattern));
            predicates.add(cb.like(cb.lower(root.get("teamLeadId")), pattern));
            predicates.add(cb.like(cb.lower(root.get("status")), pattern));
            predicates.add(cb.like(cb.lower(root.get("passport")), pattern));
            predicates.add(cb.like(cb.lower(root.get("salesExecutive")), pattern));
            predicates.add(cb.like(cb.lower(root.get("remoteOnsite")), pattern));
            predicates.add(cb.like(cb.lower(root.get("technology")), pattern));
            predicates.add(cb.like(cb.lower(root.get("marketingVisa")), pattern));
            predicates.add(cb.like(cb.lower(root.get("actualVisa")), pattern));
            predicates.add(cb.like(cb.lower(root.get("experience")), pattern));
            predicates.add(cb.like(cb.lower(root.get("location")), pattern));
            predicates.add(cb.like(cb.lower(root.get("linkedInUrl")), pattern));
            predicates.add(cb.like(cb.lower(root.get("relocation")), pattern));
            predicates.add(cb.like(cb.lower(root.get("billRate")), pattern));
            predicates.add(cb.like(cb.lower(root.get("payroll")), pattern));
            predicates.add(cb.like(cb.lower(root.get("remarks")), pattern));

            // ✅ NEW: Add salesExecutiveId field to search
            predicates.add(cb.like(cb.lower(root.get("salesExecutiveId")), pattern));

            // ✅ date fields (converted to string using MySQL date_format)
            predicates.add(cb.like(
                    cb.function("date_format", String.class, root.get("originalDOB"), cb.literal("%Y-%m-%d")), pattern));
            predicates.add(cb.like(
                    cb.function("date_format", String.class, root.get("editedDOB"), cb.literal("%Y-%m-%d")), pattern));
            predicates.add(cb.like(
                    cb.function("date_format", String.class, root.get("marketingStartDate"), cb.literal("%Y-%m-%d")), pattern));

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    // ✅ Recruiter + keyword search with isAssignAll support
    public static Specification<Consultant> recruiterSearch(String recruiterId, String keyword) {
        return Specification.<Consultant>where((root, query, cb) ->
                        cb.or(
                                cb.equal(root.get("recruiterId"), recruiterId),
                                cb.isTrue(root.get("isAssignAll"))
                        ))
                .and(createSearchSpecification(keyword));
    }

    // ✅ TeamLead + keyword search with isAssignAll support
    public static Specification<Consultant> teamLeadSearch(String teamLeadId, String keyword) {
        return Specification.<Consultant>where((root, query, cb) ->
                        cb.or(
                                cb.equal(root.get("teamLeadId"), teamLeadId),
                                cb.isTrue(root.get("isAssignAll"))
                        ))
                .and(createSearchSpecification(keyword));
    }

    // ✅ Sales Executive + keyword search with isAssignAll support
    public static Specification<Consultant> salesExecutiveSearch(String salesExecutiveId, String keyword) {
        return Specification.<Consultant>where((root, query, cb) ->
                        cb.or(
                                cb.equal(root.get("salesExecutiveId"), salesExecutiveId),
                                cb.isTrue(root.get("isAssignAll"))
                        ))
                .and(createSearchSpecification(keyword));
    }
}
