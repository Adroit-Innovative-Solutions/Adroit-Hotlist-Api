package com.adroit.hotlistmicroservice.utils;

import com.adroit.hotlistmicroservice.model.RTRInterview;
import com.adroit.hotlistmicroservice.model.RateTermsConfirmation;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class RTRSpecifications {


    private static final Set<String> ALLOWED_FIELDS = Set.of(
            "rtrId", "consultantId", "consultantName", "technology",
            "clientId", "clientName", "ratePart", "rtrStatus",
            "salesExecutiveId", "salesExecutive","vendorName","vendorEmailId",
            "vendorMobileNumber","vendorCompany","implementationPartner","vendorLinkedIn","comments"
    );


    public static Specification<RateTermsConfirmation> createSearchSpecification(String keyword){

        return ((root, query, criteriaBuilder) -> {
          if(keyword==null || keyword.trim().isEmpty()){
              return criteriaBuilder.conjunction();
          }

          String pattern="%"+ keyword+ "%";

            List<Predicate>  predicates=new ArrayList<>();

             List<String> stringFields=List.of(
                     "rtrId", "consultantId", "consultantName", "technology",
                     "clientId", "clientName", "ratePart", "rtrStatus",
                     "salesExecutiveId", "salesExecutive","implementationPartner","vendorName",
                     "vendorEmailId","vendorMobileNumber","vendorCompany", "vendorLinkedIn"

             );
            for (String field:stringFields){
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get(field)),pattern));
            }

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        });
    }

    private static Specification<RateTermsConfirmation> createFiltersSpecification(Map<String,Object> filters){

        return ((root, query, criteriaBuilder) -> {
            if(filters.isEmpty()){
                return criteriaBuilder.conjunction();
            }

            List<Predicate> predicates=new ArrayList<>();

            filters.forEach((field,value)->{
              if (value!=null && ALLOWED_FIELDS.contains(field)){
                  switch (field){
                      case "rtrId":
                      case "consultantId":
                      case "consultantName":
                      case "technology":
                      case "clientId":
                      case "clientName":
                      case "ratePart":
                      case "rtrStatus":
                      case "salesExecutiveId":
                      case "salesExecutive":
                      case "vendorName":
                      case "vendorEmailId":
                      case "vendorMobileNumber":
                      case "vendorCompany":
                      case "implementationPartner":
                      case "vendorLinkedIn":
                      case "comments":
                          predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get(field)),value.toString()+"%"));
                          break;
                  }
              }
            });
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });
    }

    public static Specification<RateTermsConfirmation> allRTRs(String keyword, LocalDateTime fromDate, LocalDateTime toDate, Map<String,Object> filters){

        return Specification.where(isNotDeleted())
                .and(createDateRangeSpecification(fromDate, toDate))
                .and(createFiltersSpecification(filters))
                .and(createSearchSpecification(keyword));

    }

    private static Specification<RateTermsConfirmation> createDateRangeSpecification(LocalDateTime fromDate, LocalDateTime toDate) {
        return (root, query, criteriaBuilder) -> {
            if (fromDate == null && toDate == null) {
                return null;
            }
            // Add logging
            System.out.println("Spec - fromDate: " + fromDate);
            System.out.println("Spec - toDate: " + toDate);

            if (fromDate != null && toDate != null) {
                // Both dates provided - range filter
                return criteriaBuilder.between(root.get("createdAt"), fromDate, toDate);
            } else if (fromDate != null) {
                // Only from date - greater than or equal
                return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), fromDate);
            } else {
                // Only to date - less than or equal
                return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), toDate);
            }
        };
    }

    public static Specification<RateTermsConfirmation> salesRTRs(String userId,String keyword,Map<String,Object> filters){

        return Specification.where(isNotDeleted())
                .and((root, query, criteriaBuilder) ->
                        criteriaBuilder.or(
                               // criteriaBuilder.equal(root.get("salesExecutiveId"),userId),
                                criteriaBuilder.equal(root.get("createdBy"), userId)
                        ))
                .and(createSearchSpecification(keyword))
                .and(createFiltersSpecification(filters));
    }

    public static Specification<RateTermsConfirmation> consultantRTRs(String consultantId,String keyword,Map<String,Object> filters){

        return Specification.where(isNotDeleted())
                .and(((root, query, criteriaBuilder) ->
                        criteriaBuilder.or(
                                criteriaBuilder.equal(root.get("consultantId"),consultantId)
                        )))
                .and(createSearchSpecification(keyword))
                .and(createFiltersSpecification(filters));
    }

    private static Specification<RateTermsConfirmation> isNotDeleted(){
        return(root, query, criteriaBuilder) ->
            criteriaBuilder.isFalse(root.get("isDeleted"));
    }

    public static Specification<RateTermsConfirmation> teamRtrs(List<String> consultantIds, String keyword, Map<String, Object> filters) {
        return Specification
                .where(isNotDeleted())
                .and((root, query, criteriaBuilder) -> {
                    if (consultantIds != null && !consultantIds.isEmpty()) {
                        return root.get("consultantId").in(consultantIds);
                    } else {
                        return criteriaBuilder.conjunction();
                    }
                })
                .and(createFiltersSpecification(filters))
                .and(createSearchSpecification(keyword));
    }

    public static Specification<RateTermsConfirmation> rtrsByDate(String keyword, Map<String, Object> filters, String date) {
        return Specification.where(isNotDeleted())
                .and(isCreatedOnDate(date))
                .and(createFiltersSpecification(filters))
                .and(createSearchSpecification(keyword));
    }

    private static Specification<RateTermsConfirmation> isCreatedOnDate(String date) {
        return (root, query, criteriaBuilder) -> {
            java.time.LocalDate targetDate = (date != null && !date.isEmpty()) 
                ? java.time.LocalDate.parse(date) 
                : java.time.LocalDate.now();
            return criteriaBuilder.equal(
                    criteriaBuilder.function("DATE", java.time.LocalDate.class, root.get("createdAt")),
                    targetDate
            );
        };
    }
}
