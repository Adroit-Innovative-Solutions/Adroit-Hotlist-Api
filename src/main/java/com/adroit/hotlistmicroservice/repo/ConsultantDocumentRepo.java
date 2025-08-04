package com.adroit.hotlistmicroservice.repo;

import com.adroit.hotlistmicroservice.model.ConsultantDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsultantDocumentRepo extends JpaRepository<ConsultantDocument, Long> {


    List<ConsultantDocument> findByConsultant_ConsultantId(String consultantId);


}
