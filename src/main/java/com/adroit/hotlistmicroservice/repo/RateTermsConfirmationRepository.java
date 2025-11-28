package com.adroit.hotlistmicroservice.repo;

import com.adroit.hotlistmicroservice.model.RateTermsConfirmation;
import com.adroit.hotlistmicroservice.utils.RTRSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RateTermsConfirmationRepository extends JpaRepository<RateTermsConfirmation,String>, JpaSpecificationExecutor<RateTermsConfirmation> {


    Optional<RateTermsConfirmation> findTopByOrderByRtrIdDesc();

   default Page<RateTermsConfirmation> allRTRs(String keyword, LocalDateTime fromDate, LocalDateTime toDate,Map<String, Object> filters, Pageable pageable){
      return findAll(RTRSpecifications.allRTRs(keyword, fromDate, toDate, filters),pageable);
   }

   default Page<RateTermsConfirmation> salesRTRs(String userId,String keyword,Map<String,Object> filters,Pageable pageable){
       return findAll(RTRSpecifications.salesRTRs(userId, keyword, filters),pageable);
   }

    default Page<RateTermsConfirmation> teamRtrs(List<String> consultantIds, String keyword, Map<String,Object> filters, Pageable pageable){
        return findAll(RTRSpecifications.teamRtrs(consultantIds,keyword,filters),pageable);
    }
   default Page<RateTermsConfirmation> consultantRTRs(String consultantId,String keyword,Map<String,Object> filters,Pageable pageable){
       return findAll(RTRSpecifications.consultantRTRs(consultantId,keyword,filters),pageable);
   }

   default Page<RateTermsConfirmation> rtrsByDate(String keyword,Map<String,Object> filters,Pageable pageable,String date){
       return findAll(RTRSpecifications.rtrsByDate(keyword,filters,date),pageable);
   }

   Optional<RateTermsConfirmation> findById(String rtrId);

    Optional<RateTermsConfirmation> findByRtrIdAndIsDeleted(String rtrId, Boolean isDeleted);

}
