package com.adroit.hotlistmicroservice.repo;

import com.adroit.hotlistmicroservice.model.RTRInterview;
import com.adroit.hotlistmicroservice.model.RateTermsConfirmation;
import com.adroit.hotlistmicroservice.utils.RTRInterviewSpecification;
import com.adroit.hotlistmicroservice.utils.RTRSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RTRInterviewRepository extends JpaRepository<RTRInterview,String>, JpaSpecificationExecutor<RTRInterview> {


    Optional<RTRInterview> findTopByOrderByInterviewIdDesc();

    default Page<RTRInterview> allInterviews(String keyword, Map<String,Object> filters, LocalDate fromDate, LocalDate toDate, Pageable pageable){
        return findAll(RTRInterviewSpecification.allInterviews(keyword, filters, fromDate, toDate),pageable);
    }

    default Page<RTRInterview> salesInterviews(String userId,String keyword,Map<String,Object> filters,Pageable pageable){
        return findAll(RTRInterviewSpecification.salesInterviews(keyword,filters,userId),pageable);
    }

    Optional<RTRInterview> findByInterviewIdAndIsDeleted(String interviewId,Boolean isDeleted);

    default Page<RTRInterview> teamInterviews(List<String> consultantIds, String keyword, Map<String,Object> filters, Pageable pageable){
        return findAll(RTRInterviewSpecification.teamInterviews(consultantIds,keyword,filters),pageable);
    }

    RTRInterview findByRtrIdAndIsDeleted(String rtrId,Boolean isDeleted);
}
