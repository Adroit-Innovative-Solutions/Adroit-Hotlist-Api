package com.adroit.hotlistmicroservice.repo;

import com.adroit.hotlistmicroservice.model.C2CEmployerDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface C2CEmployerDetailsRepo extends JpaRepository<C2CEmployerDetails, Long> {

    /**
     * Fetch all employers with their documents and placement details eagerly loaded.
     * This ensures the documents list is NOT empty in response.
     */
    @Query("SELECT DISTINCT e FROM C2CEmployerDetails e " +
            "LEFT JOIN FETCH e.documents d " +
            "LEFT JOIN FETCH e.placementDetails p")
    List<C2CEmployerDetails> findAllWithDocuments();

    @Query("SELECT DISTINCT e FROM C2CEmployerDetails e " +
            "LEFT JOIN FETCH e.documents d " +
            "LEFT JOIN FETCH e.placementDetails p " +
            "WHERE p.placementId = :placementId")
    List<C2CEmployerDetails> findByPlacementDetails_PlacementId(String placementId);

    @Query("SELECT DISTINCT e FROM C2CEmployerDetails e " +
            "LEFT JOIN FETCH e.documents d " +
            "LEFT JOIN FETCH e.placementDetails p " +
            "WHERE e.empId = :empId")
    Optional<C2CEmployerDetails> findByEmpId(Long empId);


    /**
     * Optional utility: find by company name (case-insensitive).
     */
    @Query("SELECT e FROM C2CEmployerDetails e WHERE LOWER(e.companyName) = LOWER(:companyName)")
    Optional<C2CEmployerDetails> findByCompanyNameIgnoreCase(String companyName);
}
