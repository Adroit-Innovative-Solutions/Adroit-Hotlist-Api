package com.adroit.hotlistmicroservice.service;

import com.adroit.hotlistmicroservice.dto.DeleteDocumentDTO;
import com.adroit.hotlistmicroservice.dto.DocumentAddedResponse;
import com.adroit.hotlistmicroservice.dto.DocumentDetailsDTO;
import com.adroit.hotlistmicroservice.exception.DocumentNotFoundException;
import com.adroit.hotlistmicroservice.filevalidator.FileValidator;
import com.adroit.hotlistmicroservice.mapper.ConsultantDocumentMapper;
import com.adroit.hotlistmicroservice.model.Consultant;
import com.adroit.hotlistmicroservice.model.ConsultantDocument;
import com.adroit.hotlistmicroservice.repo.ConsultantDocumentRepo;
import com.adroit.hotlistmicroservice.repo.ConsultantRepo;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConsultantDocumentService {

    @Autowired
    ConsultantDocumentRepo consultantDocumentRepo;
    @Autowired
    ConsultantRepo consultantRepo;
    @Autowired
    ConsultantService consultantService;
    @Autowired
    ConsultantDocumentMapper consultantDocumentMapper;

    private static final Logger logger= LoggerFactory.getLogger(ConsultantDocumentService.class);
    public ConsultantDocument downloadDocument(Long documentId){

         Optional<ConsultantDocument> optionalConsultantDocument=consultantDocumentRepo.findById(documentId);
         if(optionalConsultantDocument.isEmpty()){
             throw new DocumentNotFoundException("DocumentId Not Found with ID: "+documentId);
         }
         ConsultantDocument consultantDocument=optionalConsultantDocument.get();
             byte[] documentBytes=consultantDocument.getFileData();
        if (documentBytes == null || documentBytes.length == 0) {
            log.error("Document is Missing for ID {} ", documentId);
                 throw new DocumentNotFoundException("Document is missing for ID: "+documentId);
        }
        return consultantDocument;
    }
    public List<DocumentDetailsDTO> getDocumentDetails(String consultantId){

        List<ConsultantDocument> consultantDocument=consultantDocumentRepo.findByConsultant_ConsultantId(consultantId);
        List<DocumentDetailsDTO> documentDetailsDTOS=consultantDocument.stream()
                .filter(document-> document.getIsDeleted()==false)
               .map(consultantDocumentMapper::toDocumentDTO)
               .collect(Collectors.toList());

      return documentDetailsDTOS;
    }
    public DeleteDocumentDTO deleteDocument(long documentId,String userId){

      Optional<ConsultantDocument> optionalConsultantDocument=consultantDocumentRepo.findById(documentId);
    if(optionalConsultantDocument.isEmpty())
      throw new DocumentNotFoundException("No Documents Found With ID :"+documentId);

        ConsultantDocument consultantDocument=optionalConsultantDocument.get();
          consultantDocument.setIsDeleted(true);
          consultantDocument.setDeletedBy(userId);
          consultantDocument.setDeletedAt(LocalDateTime.now());
        consultantDocumentRepo.save(consultantDocument);

        DeleteDocumentDTO response=new DeleteDocumentDTO();
        response.setConsultantId(consultantDocument.getConsultant().getConsultantId());
        response.setDocumentId(consultantDocument.getDocumentId());
        response.setFileName(consultantDocument.getFileName());
    return response;
    }

    public DocumentAddedResponse addDocument(String consultantId, List<MultipartFile> resumes, List<MultipartFile> documents) throws IOException {

        Optional<Consultant> optionalConsultant = consultantRepo.findById(consultantId);
        Consultant consultant = optionalConsultant.get();

        Tika tika = new Tika();
        // Saving Resume as BLOB
        for (MultipartFile resume : resumes) {
            if (!resume.isEmpty()) {
                FileValidator.validateStrictFile(resume);
                String mimeType = FileValidator.mapFileNameToFileType(resume.getOriginalFilename());
                logger.info("Resume File Mime Type {}", tika.detect(resume.getInputStream()));
                ConsultantDocument doc = consultantService.saveDocument(resume, "RESUME", mimeType, consultant);
                consultant.getDocuments().add(doc);
            }
        }
        // Saving Documents as BLOB
        for (MultipartFile document : documents) {
            if (!document.isEmpty()) {
                FileValidator.validateStrictFile(document);
                String mimeType = FileValidator.mapFileNameToFileType(document.getOriginalFilename());
                logger.info("Document MimeType : {}", tika.detect(document.getInputStream()));
                ConsultantDocument doc = consultantService.saveDocument(document, "DOCUMENT", mimeType, consultant);
                consultant.getDocuments().add(doc);
            }
        }
        consultantRepo.save(consultant);
        DocumentAddedResponse response = new DocumentAddedResponse();
        response.setConsultantId(consultantId);
        response.setMessage("Documents Added Successfully");

        return response;
    }
}
