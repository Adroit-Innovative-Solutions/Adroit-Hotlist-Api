package com.adroit.hotlistmicroservice.repo;

import com.adroit.hotlistmicroservice.model.Consultant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ConsultantRepo extends JpaRepository<Consultant,String> {


    @Query("SELECT MAX(consultantId) FROM Consultant WHERE consultantId LIKE 'CONS%'")
    String findMaxConsultantId();

    List<Consultant> findByEmailIdAndPersonalContact(String emailId, String personalContact);

    Page<Consultant> findAll(Pageable pageable);

    @Query("SELECT h FROM Consultant h WHERE " +
            "LOWER(h.consultantId) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(h.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(h.emailId) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(h.grade) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(h.marketingContact) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(h.personalContact) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(h.reference) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(h.recruiter) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(h.teamLead) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(h.status) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(h.passport) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(h.salesExecutive) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(h.remoteOnsite) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(h.technology) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(h.marketingVisa) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(h.actualVisa) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(h.experience) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(h.location) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(h.linkedInUrl) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(h.relocation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(h.billRate) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(h.payroll) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(CAST(h.originalDOB AS string)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(CAST(h.editedDOB AS string)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(CAST(h.marketingStartDate AS string)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(h.remarks) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Consultant> searchHotlist(
            @Param("keyword") String keyword,
            Pageable pageable);

}
