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
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            String pattern = "%" + keyword.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();

            List<String> stringFields = List.of(
                    "consultantId", "name", "emailId", "grade",
                    "marketingContact", "personalContact", "reference",
                    "recruiterId", "teamLeadId", "status", "passport",
                    "salesExecutive", "remoteOnsite", "technology",
                    "marketingVisa", "actualVisa", "experience", "location",
                    "linkedInUrl", "relocation", "billRate", "payroll",
                    "remarks", "salesExecutiveId"
            );
            // Building Predicates Dynamically
            for (String field:stringFields){
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get(field)),pattern));
            }

            // date fields (converting string using MySQL date_format
            predicates.add(criteriaBuilder.like(
                    criteriaBuilder.function("date_format", String.class, root.get("originalDOB"), criteriaBuilder.literal("%Y-%m-%d")), pattern));
            predicates.add(criteriaBuilder.like(
                    criteriaBuilder.function("date_format", String.class, root.get("editedDOB"), criteriaBuilder.literal("%Y-%m-%d")), pattern));
            predicates.add(criteriaBuilder.like(
                    criteriaBuilder.function("date_format", String.class, root.get("marketingStartDate"), criteriaBuilder.literal("%Y-%m-%d")), pattern));

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Consultant> recruiterSearch(String recruiterId, String keyword) {
        return Specification.<Consultant>where(isNotDeleted())
                .and(isMovedToHotlist())
                .and((root, query, cb) ->
                        cb.or(
                                cb.equal(root.get("recruiterId"), recruiterId),
                                cb.isTrue(root.get("isAssignAll"))
                        ))
                .and(createSearchSpecification(keyword));
    }

    public static Specification<Consultant> teamLeadSearch(String teamLeadId, String keyword) {
        return Specification.<Consultant>where(isNotDeleted())
                .and(isMovedToHotlist())
                .and((root, query, cb) ->
                        cb.or(
                                cb.equal(root.get("teamLeadId"), teamLeadId),
                                cb.isTrue(root.get("isAssignAll"))
                        ))
                .and(createSearchSpecification(keyword));
    }
    public static Specification<Consultant> salesExecutiveSearch(String salesExecutiveId,String keyword){
        return Specification.<Consultant>where(isNotDeleted())
                .and(isMovedToHotlist())
                .and((root, query, criteriaBuilder) ->
                    criteriaBuilder.or(
                            criteriaBuilder.equal(root.get("salesExecutiveId"),salesExecutiveId),
                            criteriaBuilder.isTrue(root.get("isAssignAll"))
                    ))
                .and(createSearchSpecification(keyword));
     }
     public static Specification<Consultant> allConsultantsSearch(String keyword){
        return Specification.<Consultant>where(isNotDeleted())
                .and(isMovedToHotlist())
                .and(createSearchSpecification(keyword));
     }

     public static  Specification<Consultant> isNotDeleted(){
        return ((root, query, criteriaBuilder) -> criteriaBuilder.isFalse(root.get("isDeleted")));
    }
    public static Specification<Consultant> isMovedToHotlist(){
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.isTrue(root.get("movedToHotlist")));
    }
    public static Specification<Consultant> yetToOnBoardConsultants(String keyword){
        return Specification.<Consultant>where(isNotDeleted())
                .and((root, query, criteriaBuilder) ->
                        criteriaBuilder.or(criteriaBuilder.isFalse(root.get("movedToHotlist"))))
                .and(createSearchSpecification(keyword));
    }

}
