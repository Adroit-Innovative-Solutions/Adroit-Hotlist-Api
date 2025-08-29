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

public interface ConsultantRepo extends JpaRepository<Consultant,String>, JpaSpecificationExecutor<Consultant> {


    @Query("SELECT MAX(consultantId) FROM Consultant WHERE consultantId LIKE 'CONS%'")
    String findMaxConsultantId();

    List<Consultant> findByEmailIdAndPersonalContact(String emailId, String personalContact);

    Page<Consultant> findAll(Pageable pageable);

    default Page<Consultant> searchHotlistByUtil(String keyword, Pageable pageable) {
        return findAll(ConsultantSpecifications.createSearchSpecification(keyword), pageable);
    }

    default Page<Consultant> consultantsByRecruiter(String recruiterId, String keyword, Pageable pageable) {
        return findAll(ConsultantSpecifications.recruiterSearch(recruiterId, keyword), pageable);
    }

    default Page<Consultant> consultantsByTeamLead(String teamLeadId, String keyword, Pageable pageable) {
        return findAll(ConsultantSpecifications.teamLeadSearch(teamLeadId, keyword), pageable);
    }

    default Page<Consultant> consultantsBySalesExecutive(String salesExecutiveId, String keyword, Pageable pageable) {
        return findAll(ConsultantSpecifications.salesExecutiveSearch(salesExecutiveId, keyword), pageable);
    }

}
