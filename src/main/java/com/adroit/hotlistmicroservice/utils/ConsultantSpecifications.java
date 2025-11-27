package com.adroit.hotlistmicroservice.utils;

import com.adroit.hotlistmicroservice.model.Consultant;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ConsultantSpecifications {

    private static final Set<String> ALLOWED_FIELDS=Set.of(
            "consultantId", "name", "emailId", "grade", "marketingContact",
            "personalContact", "reference", "recruiterId", "teamLeadId",
            "recruiterName", "teamleadName", "status", "passport",
            "salesExecutive", "salesExecutiveId", "remoteOnsite", "technology",
            "experience", "location", "originalDOB", "editedDOB", "linkedInUrl",
            "relocation", "billRate", "payroll", "marketingStartDate", "remarks",
            "consultantAddedTimeStamp", "updatedTimeStamp", "marketingVisa",
            "actualVisa", "isAssignAll", "movedToHotlist", "deletedBy",
            "isDeleted", "deletedAt","approvalStatus");

    public static Specification<Consultant> createSearchSpecification(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            String pattern = "%" + keyword.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();

            List<String> stringFields = List.of(
                    "consultantId", "name", "emailId", "grade", "marketingContact",
                    "personalContact", "reference", "recruiterId", "teamLeadId", "recruiterName",
                    "teamleadName", "status", "passport", "salesExecutive", "salesExecutiveId",
                    "remoteOnsite", "technology", "experience", "location", "linkedInUrl",
                    "relocation", "billRate", "payroll", "remarks", "marketingVisa",
                    "actualVisa","approvalStatus"
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

    public static  Specification<Consultant> createFiltersSpecification(Map<String,Object> filters){
        return (root,query,criteriaBuilder)->{
            if (filters.isEmpty()){
                return criteriaBuilder.conjunction();
            }
            List<Predicate> predicates=new ArrayList<>();

            filters.forEach((field,value)->{
                 if(value!=null && ALLOWED_FIELDS.contains(field)) {
                     switch (field) {
                         case "consultantId":
                         case "name":
                         case "emailId":
                         case "grade":
                         case "marketingContact":
                         case "personalContact":
                         case "reference":
                         case "recruiterId":
                         case "teamLeadId":
                         case "recruiterName":
                         case "teamleadName":
                         case "status":
                         case "passport":
                         case "salesExecutive":
                         case "salesExecutiveId":
                         case "remoteOnsite":
                         case "technology":
                         case "experience":
                         case "location":
                         case "linkedInUrl":
                         case "relocation":
                         case "billRate":
                         case "payroll":
                         case "remarks":
                         case "marketingVisa":
                         case "actualVisa":
                         case "approvalStatus":
                             predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get(field)),value.toString().toLowerCase()+"%"));
                             break;
                         case "isAssignAll":
                         case "movedToHotlist":
                         case "isDeleted":
                             predicates.add(criteriaBuilder.equal(root.get(field),Boolean.valueOf(value.toString())));
                             break;
                         case "originalDOB":
                         case "editedDOB":
                         case "marketingStartDate":
                             if (value instanceof String && !value.toString().isBlank()) {
                                 try {
                                     LocalDate date = LocalDate.parse(value.toString());
                                     predicates.add(criteriaBuilder.equal(root.get(field), date));
                                 } catch (DateTimeParseException e) {
                                     // handle invalid format
                                 }
                             }
                             break;
                         case "consultantAddedTimeStamp":
                         case "updatedTimeStamp":
                         case "deletedAt":
                             if (value instanceof String && !value.toString().isBlank()) {
                                 try {
                                     LocalDateTime dateTime = LocalDateTime.parse(value.toString());
                                     predicates.add(criteriaBuilder.equal(root.get(field), dateTime));
                                 } catch (DateTimeParseException e) {
                                     // Optionally log or ignore invalid format
                                 }
                             }
                             break;
                     }
                 }
            });
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    public static Specification<Consultant> recruiterSearch(String recruiterId, String keyword,Map<String,Object> filters,String statusFilter) {
        return Specification.<Consultant>where(isNotDeleted())
                .and(isMovedToHotlist())
                .and((root, query, cb) ->
                        cb.or(
                                cb.equal(root.get("recruiterId"), recruiterId),
                                cb.isTrue(root.get("isAssignAll"))
                        ))
                .and(createSearchSpecification(keyword))
                .and(createFiltersSpecification(filters))
                .and(applyStatusFilter(statusFilter));
    }

    public static Specification<Consultant> teamLeadSearch(String teamLeadId, String keyword,Map<String,Object> filters,String statusFilter) {
        return Specification.<Consultant>where(isNotDeleted())
                .and(isMovedToHotlist())
                .and((root, query, cb) ->
                        cb.or(
                                cb.equal(root.get("teamLeadId"), teamLeadId),
                                cb.isTrue(root.get("isAssignAll"))
                        ))
                .and(createSearchSpecification(keyword))
                .and(createFiltersSpecification(filters))
                .and(applyStatusFilter(statusFilter));
    }
    public static Specification<Consultant> salesExecutiveSearch(String salesExecutiveId,String keyword,Map<String,Object> filters,String statusFilter){
        return Specification.<Consultant>where(isNotDeleted())
                .and(isMovedToHotlist())
                .and((root, query, criteriaBuilder) ->
                    criteriaBuilder.or(
                            criteriaBuilder.equal(root.get("salesExecutiveId"),salesExecutiveId),
                            criteriaBuilder.isTrue(root.get("isAssignAll"))
                    ))
                .and(createSearchSpecification(keyword))
                .and(createFiltersSpecification(filters))
                .and(applyStatusFilter(statusFilter));
     }
     public static Specification<Consultant> allConsultantsSearch(String keyword,Map<String,Object> filters, String statusFilter){
        return Specification.<Consultant>where(isNotDeleted())
                .and(isMovedToHotlist())
                .and(createSearchSpecification(keyword))
                .and(createFiltersSpecification(filters))
                .and(applyStatusFilter(statusFilter));
     }

     public static Specification<Consultant> allW2ConsultantsSearch(String keyword,Map<String,Object> filters, String statusFilter){
        return Specification.<Consultant>where(isNotDeleted())
                .and(isMovedToHotlist())
                .and(isW2Payroll())
                .and(createSearchSpecification(keyword))
                .and(createFiltersSpecification(filters))
                .and(applyStatusFilter(statusFilter));
     }

     public static Specification<Consultant> isW2Payroll(){
        return ((root, query, criteriaBuilder) -> 
                criteriaBuilder.equal(criteriaBuilder.lower(root.get("payroll")), "w2"));
    }

     public static  Specification<Consultant> isNotDeleted(){
        return ((root, query, criteriaBuilder) -> criteriaBuilder.isFalse(root.get("isDeleted")));
    }
    public static Specification<Consultant> isMovedToHotlist(){
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.isTrue(root.get("movedToHotlist")));
    }
    public static Specification<Consultant> yetToOnBoardConsultants(String keyword,Map<String,Object> filters,String statusFilter){
        return Specification.<Consultant>where(isNotDeleted())
                .and((root, query, criteriaBuilder) ->
                        criteriaBuilder.or(criteriaBuilder.isFalse(root.get("movedToHotlist"))))
                .and(createSearchSpecification(keyword))
                .and(createFiltersSpecification(filters))
                .and(applyStatusFilter(statusFilter));
    }

    private static Specification<Consultant> applyStatusFilter(String statusFilter) {
        return (root, query, criteriaBuilder) -> {
            if (statusFilter == null || statusFilter.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(criteriaBuilder.lower(root.get("status")), statusFilter.toLowerCase());
        };
    }
}
