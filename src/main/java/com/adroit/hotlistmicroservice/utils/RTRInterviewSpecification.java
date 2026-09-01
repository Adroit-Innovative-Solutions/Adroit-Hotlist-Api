package com.adroit.hotlistmicroservice.utils;

import com.adroit.hotlistmicroservice.model.RTRInterview;
import com.adroit.hotlistmicroservice.model.UserDetails;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class RTRInterviewSpecification {

    private static final Set<String> ALLOWED_FIELDS = Set.of(
            "interviewId", "rtrId", "consultantId", "consultantName", "consultantEmailId",
            "technology", "clientId", "clientName", "salesExecutiveId", "salesExecutive",
            "interviewLevel", "interviewStatus", "interviewDateTime",
            "interviewerEmailId", "createdBy"
    );

    public static Specification<RTRInterview> createSearchSpecification(String keyword){

        return ((root, query, criteriaBuilder) -> {
            if(keyword==null || keyword.trim().isEmpty()){
                return criteriaBuilder.conjunction();
            }

            String pattern="%"+ keyword+ "%";

            List<Predicate> predicates=new ArrayList<>();

            List<String> stringFields=List.of(
                    "interviewId", "rtrId", "consultantId", "consultantName", "consultantEmailId",
                    "technology", "clientId", "clientName", "salesExecutiveId", "salesExecutive",
                    "interviewLevel", "interviewStatus",
                    "interviewerEmailId"

            );
            for (String field:stringFields){
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get(field)),pattern));
            }

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        });
    }

    private static Specification<RTRInterview> createFiltersSpecification(Map<String,Object> filters){

        return ((root, query, criteriaBuilder) -> {
            if(filters == null || filters.isEmpty()){
                return criteriaBuilder.conjunction();
            }

            List<Predicate> predicates=new ArrayList<>();

            filters.forEach((field,value)->{
                if (value!=null && ALLOWED_FIELDS.contains(field)){
                    switch (field){
                            case "interviewId":
                            case "rtrId":
                            case "consultantId":
                            case "consultantName":
                            case "consultantEmailId":
                            case "technology":
                            case "clientId":
                            case "clientName":
                            case "salesExecutiveId":
                            case "salesExecutive":
                            case "interviewLevel":
                            case "interviewStatus":
                            case "interviewerEmailId":
                                predicates.add(criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get(field).as(String.class)),
                                        value.toString().toLowerCase() + "%"
                                ));
                                break;
                            case "createdBy":
                                predicates.add(createdByNameLike(root, query, criteriaBuilder, value.toString()));
                                break;
                            case "interviewDateTime":
                                LocalDate interviewDate = parseFlexibleDate(value.toString());
                                if (interviewDate != null) {
                                    predicates.add(criteriaBuilder.between(
                                            root.get("interviewDateTime"),
                                            interviewDate.atStartOfDay(),
                                            interviewDate.atTime(LocalTime.MAX)
                                    ));
                                }
                                break;
                        }
                    }
            });

            LocalDate interviewFrom = parseFlexibleDate(asFilterString(filters.get("interviewDateTimeFrom")));
            LocalDate interviewTo = parseFlexibleDate(asFilterString(filters.get("interviewDateTimeTo")));
            if (interviewFrom != null || interviewTo != null) {
                if (interviewFrom == null) {
                    interviewFrom = interviewTo;
                }
                if (interviewTo == null) {
                    interviewTo = interviewFrom;
                }
                predicates.add(criteriaBuilder.between(
                        root.get("interviewDateTime"),
                        interviewFrom.atStartOfDay(),
                        interviewTo.atTime(LocalTime.MAX)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });
    }

    public static Specification<RTRInterview> createdAtDateFilter(
            LocalDate fromDate,
            LocalDate toDate) {

        return (root, query, cb) -> {

            if (fromDate == null && toDate == null) {
                return cb.conjunction();
            }

            // Same-day filtering when only one date is provided
            if (fromDate != null && toDate == null) {
                LocalDateTime start = fromDate.atStartOfDay();
                LocalDateTime end = fromDate.atTime(LocalTime.MAX);
                return cb.between(root.get("createdAt"), start, end);
            }

            if (fromDate == null && toDate != null) {
                LocalDateTime start = toDate.atStartOfDay();
                LocalDateTime end = toDate.atTime(LocalTime.MAX);
                return cb.between(root.get("createdAt"), start, end);
            }

            // Range filtering when both dates are provided
            LocalDateTime start = fromDate.atStartOfDay();
            LocalDateTime end = toDate.atTime(LocalTime.MAX);

            return cb.between(root.get("createdAt"), start, end);
        };
    }

    private static Predicate createdByNameLike(
            Root<RTRInterview> root,
            jakarta.persistence.criteria.CriteriaQuery<?> query,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
            String value) {
        Subquery<String> subquery = query.subquery(String.class);
        Root<UserDetails> userRoot = subquery.from(UserDetails.class);
        String pattern = "%" + value.toLowerCase() + "%";
        subquery.select(userRoot.get("userId"));
        subquery.where(criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(userRoot.get("userName")), pattern),
                criteriaBuilder.like(criteriaBuilder.lower(userRoot.get("userId")), pattern)
        ));
        return root.get("createdBy").in(subquery);
    }

    private static String asFilterString(Object value) {
        return value == null ? null : value.toString();
    }

    private static LocalDate parseFlexibleDate(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        String value = rawValue.trim();
        try {
            if (value.length() >= 10 && value.charAt(4) == '-' && value.charAt(7) == '-') {
                return LocalDate.parse(value.substring(0, 10));
            }
            return LocalDate.parse(value);
        } catch (Exception ignored) {
            return null;
        }
    }


    public static Specification<RTRInterview> allInterviews(String keyword, Map<String, Object> filters, LocalDate fromDate, LocalDate toDate){
        return Specification.where(isNotDeleted())
                .and(createFiltersSpecification(filters))
                .and(createSearchSpecification(keyword))
                .and(createdAtDateFilter(fromDate, toDate));
    }

    public static Specification<RTRInterview> salesInterviews(
            String keyword,
            Map<String,Object> filters,
            String userId,
            LocalDate fromDate,
            LocalDate toDate){
        return Specification.where(isNotDeleted())
                .and((root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get("createdBy"), userId))
                .and(createSearchSpecification(keyword))
                .and(createFiltersSpecification(filters))
                .and(createdAtDateFilter(fromDate, toDate));
    }

    public static Specification<RTRInterview> teamInterviews(
            List<String> consultantIds,
            String keyword,
            Map<String, Object> filters,
            LocalDate fromDate,
            LocalDate toDate) {
        return Specification
                .where(isNotDeleted())
                .and((root, query, criteriaBuilder) -> {
                    if (consultantIds == null || consultantIds.isEmpty()) {
                        return criteriaBuilder.disjunction();
                    }
                    return root.get("consultantId").in(consultantIds);
                })
                .and(createFiltersSpecification(filters))
                .and(createSearchSpecification(keyword))
                .and(createdAtDateFilter(fromDate, toDate));
    }

    public static Specification<RTRInterview> coordinatorInterviews(
            Set<String> teamMemberIds,
            String keyword,
            Map<String, Object> filters,
            LocalDate fromDate,
            LocalDate toDate) {
        return Specification
                .where(isNotDeleted())
                .and((root, query, criteriaBuilder) -> {
                    if (teamMemberIds == null || teamMemberIds.isEmpty()) {
                        return criteriaBuilder.disjunction();
                    }

                    return root.get("createdBy").in(teamMemberIds);
                })
                .and(createFiltersSpecification(filters))
                .and(createSearchSpecification(keyword))
                .and(createdAtDateFilter(fromDate, toDate));
    }

    private static Specification<RTRInterview> isNotDeleted(){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isFalse(root.get("isDeleted"));
    }

    public static Specification<RTRInterview> consultantInterviews(
            String consultantId,
            String keyword,
            Map<String, Object> filters) {

        return Specification.where(isNotDeleted())
                .and((root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get("consultantId"), consultantId)
                )
                .and(createSearchSpecification(keyword))
                .and(createFiltersSpecification(filters));
    }


}
