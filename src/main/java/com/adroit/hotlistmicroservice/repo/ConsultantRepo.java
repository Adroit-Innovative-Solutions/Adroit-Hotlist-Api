package com.adroit.hotlistmicroservice.repo;

import com.adroit.hotlistmicroservice.model.Consultant;
import com.adroit.hotlistmicroservice.utils.ConsultantSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;





public interface ConsultantRepo extends JpaRepository<Consultant,String>, JpaSpecificationExecutor<Consultant> {


    @Query("SELECT MAX(consultantId) FROM Consultant WHERE consultantId LIKE 'CONS%'")
    String findMaxConsultantId();



    List<Consultant> findByEmailIdAndPersonalContact(String emailId, String personalContact);

    Page<Consultant> findAll(Pageable pageable);

    default Page<Consultant> searchHotlistByUtil(String keyword, Pageable pageable) {
        return findAll(ConsultantSpecifications.createSearchSpecification(keyword), pageable);
    }
    default Page<Consultant> allConsultants(String keyword, Map<String,Object> filters,String statusFilter,Pageable pageable){
        return findAll(ConsultantSpecifications.allConsultantsSearch(keyword,filters,statusFilter),pageable);
    }

    default Page<Consultant> allW2Consultants(String keyword, Map<String,Object> filters,String statusFilter,Pageable pageable){
        return findAll(ConsultantSpecifications.allW2ConsultantsSearch(keyword,filters,statusFilter),pageable);
    }

    default Page<Consultant> consultantsByRecruiter(String recruiterId, String keyword, Map<String,Object> filters,String statusFilter,Pageable pageable) {
        return findAll(ConsultantSpecifications.recruiterSearch(recruiterId, keyword,filters,statusFilter), pageable);
    }

    default Page<Consultant> consultantsByTeamLead(String teamLeadId, String keyword,Map<String,Object> filters,String statusFilter, Pageable pageable) {
        return findAll(ConsultantSpecifications.teamLeadSearch(teamLeadId, keyword,filters,statusFilter), pageable);
    }

    default Page<Consultant> consultantsBySalesExecutive(String salesExecutiveId, String keyword,Map<String,Object> filters,String statusFilter, Pageable pageable) {
        return findAll(ConsultantSpecifications.salesExecutiveSearch(salesExecutiveId, keyword,filters,statusFilter), pageable);
    }
    default Page<Consultant> yetToOnBoardConsultants(String keyword,Map<String,Object> filters, String statusFilter,Pageable pageable) {
        return findAll(ConsultantSpecifications.yetToOnBoardConsultants(keyword,filters,statusFilter),pageable);
    }

    default Page<Consultant> onHoldConsultants(String keyword, Map<String,Object> filters, String statusFilter, Pageable pageable) {
        return findAll(ConsultantSpecifications.onHoldConsultants(keyword,filters,statusFilter),pageable);
    }

    default Page<Consultant> activeConsultants(String keyword, Map<String,Object> filters, String statusFilter, Pageable pageable) {
        return findAll(ConsultantSpecifications.activeConsultants(keyword,filters,statusFilter),pageable);
    }
    @Query("SELECT c.consultantId FROM Consultant c WHERE c.teamLeadId= :teamLeadId AND isDeleted= false")
    List<String> findConsultantIdsByTeamLeadId(@Param("teamLeadId") String teamLeadId);



}
