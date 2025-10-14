package com.adroit.hotlistmicroservice.repo;

import com.adroit.hotlistmicroservice.dto.RateTermsConfirmationDTO;
import com.adroit.hotlistmicroservice.model.RateTermsConfirmation;
import com.adroit.hotlistmicroservice.utils.RTRSpecifications;
import org.apache.coyote.Request;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Map;
import java.util.Optional;

public interface RateTermsConfirmationRepository extends JpaRepository<RateTermsConfirmation,String>, JpaSpecificationExecutor<RateTermsConfirmation> {


    Optional<RateTermsConfirmation> findTopByOrderByRtrIdDesc();

   default Page<RateTermsConfirmation> allRTRs(String keyword, Map<String,Object> filters, Pageable pageable){
      return findAll(RTRSpecifications.allRTRs(keyword,filters),pageable);
   }

   default Page<RateTermsConfirmation> salesRTRs(String userId,String keyword,Map<String,Object> filters,Pageable pageable){
       return findAll(RTRSpecifications.salesRTRs(userId, keyword, filters),pageable);
   }

   default Page<RateTermsConfirmation> consultantRTRs(String consultantId,String keyword,Map<String,Object> filters,Pageable pageable){
       return findAll(RTRSpecifications.consultantRTRs(consultantId,keyword,filters),pageable);
   }

   Optional<RateTermsConfirmation> findById(String rtrId);

}
