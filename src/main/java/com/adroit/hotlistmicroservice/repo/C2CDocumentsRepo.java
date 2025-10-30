package com.adroit.hotlistmicroservice.repo;

import com.adroit.hotlistmicroservice.model.C2CDocuments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface C2CDocumentsRepo extends JpaRepository<C2CDocuments, Long> {

    /**
     * Fetch all documents for a given placement.
     */
    List<C2CDocuments> findByPlacementDetails_PlacementId(String placementId);

    /**
     * Fetch all documents for a given employer.
     */
    List<C2CDocuments> findByEmployer_EmpId(Long empId);
}
