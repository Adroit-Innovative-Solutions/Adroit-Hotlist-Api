package com.adroit.hotlistmicroservice.service;

import com.adroit.hotlistmicroservice.client.UserServiceClient;
import com.adroit.hotlistmicroservice.dto.ConsultantAddedResponse;
import com.adroit.hotlistmicroservice.dto.ConsultantDto;
import com.adroit.hotlistmicroservice.dto.DeleteConsultantResponse;
import com.adroit.hotlistmicroservice.exception.ConsultantAlreadyExistsException;
import com.adroit.hotlistmicroservice.exception.ConsultantNotFoundException;
import com.adroit.hotlistmicroservice.filevalidator.FileValidator;
import com.adroit.hotlistmicroservice.model.Consultant;
import com.adroit.hotlistmicroservice.model.ConsultantDocument;
import com.adroit.hotlistmicroservice.repo.ConsultantDocumentRepo;
import com.adroit.hotlistmicroservice.repo.ConsultantRepo;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ConsultantService {

     @Autowired
     ConsultantRepo consultantRepo;
     @Autowired
     UserServiceClient userServiceClient;
     @Autowired
     ConsultantDocumentRepo consultantDocumentRepo;

    public static final Logger logger= LoggerFactory.getLogger(ConsultantService.class);

    public String generateConsultantId(){

        String maxConsultantId= consultantRepo.findMaxConsultantId();
        int nextNum=1;
        if(maxConsultantId!=null && maxConsultantId.startsWith("CONS")){
            nextNum=Integer.parseInt(maxConsultantId.substring(4))+1;
        }
        return String.format("CONS%05d",nextNum);

    }

    public ConsultantAddedResponse addConsultant(ConsultantDto dto, List<MultipartFile> resumes, List<MultipartFile> documents) throws IOException {
        Consultant consultant = convertDtoToEntity(dto);
        List<Consultant> existedHotList=consultantRepo.findByEmailIdAndPersonalContact(consultant.getEmailId(),consultant.getPersonalContact());
        if(!existedHotList.isEmpty()) throw new ConsultantAlreadyExistsException("A consultant with the same email and personal contact already exists in the system.");

        consultant.setConsultantId(generateConsultantId());
        consultant.setConsultantAddedTimeStamp(LocalDateTime.now());
        consultant.setUpdatedTimeStamp(LocalDateTime.now());

        // saving without documents
        consultantRepo.save(consultant);

        Tika tika=new Tika();
        // Saving Resume as BLOB
        if(resumes!=null) {
            if (!resumes.isEmpty()) {
                for (MultipartFile resume : resumes) {
                    if (!resume.isEmpty()) {
                        FileValidator.validateStrictFile(resume);
                        String mimeType = FileValidator.mapFileNameToFileType(resume.getOriginalFilename());
                        logger.info("Resume File Mime Type {}", tika.detect(resume.getInputStream()));
                        ConsultantDocument doc = saveDocument(resume, "RESUME", mimeType, consultant);
                        consultant.getDocuments().add(doc);
                    }
                }
            }
        }
        if(documents!=null) {
            if (!documents.isEmpty()) {
                // Saving Documents as BLOB
                for (MultipartFile document : documents) {
                    if (!document.isEmpty()) {
                        FileValidator.validateStrictFile(document);
                        String mimeType = FileValidator.mapFileNameToFileType(document.getOriginalFilename());
                        logger.info("Document MimeType : {}", tika.detect(document.getInputStream()));
                        ConsultantDocument doc = saveDocument(document, "DOCUMENT", mimeType, consultant);
                        consultant.getDocuments().add(doc);
                    }
                }
            }
        }
        // Save again to update with documents
        consultantRepo.save(consultant);

        ConsultantAddedResponse response = new ConsultantAddedResponse();
        response.setConsultantId(consultant.getConsultantId());
        response.setName(consultant.getName());
        response.setRecruiter(consultant.getRecruiter());
        response.setAddedTimeStamp(consultant.getConsultantAddedTimeStamp());

        return response;
    }
    public ConsultantDocument saveDocument(MultipartFile file,String DocumentType,String fileType,Consultant consultant)
            throws IOException {

        ConsultantDocument consultantDocument=new ConsultantDocument();
        consultantDocument.setConsultant(consultant);
        consultantDocument.setFileName(file.getOriginalFilename());
        consultantDocument.setFileData(file.getBytes());
        consultantDocument.setDocumentType(DocumentType);
        consultantDocument.setFileType(fileType);
        consultantDocument.setCreatedAt(LocalDateTime.now());

        return consultantDocumentRepo.save(consultantDocument);
    }
    public static Consultant  convertDtoToEntity(ConsultantDto dto) {
        Consultant entity = new Consultant();

        entity.setName(dto.getName());
        entity.setEmailId(dto.getEmailId());
        entity.setGrade(dto.getGrade());
        entity.setMarketingContact(dto.getMarketingContact());
        entity.setPersonalContact(dto.getPersonalContact());
        entity.setReference(dto.getReference());
        entity.setRecruiter(dto.getRecruiter());
        entity.setTeamLead(dto.getTeamLead());
        entity.setStatus(dto.getStatus());
        entity.setPassport(dto.getPassport());
        entity.setSalesExecutive(dto.getSalesExecutive());
        entity.setRemoteOnsite(dto.getRemoteOnsite());
        entity.setTechnology(dto.getTechnology());
        entity.setExperience(dto.getExperience());
        entity.setLocation(dto.getLocation());
        entity.setOriginalDOB(dto.getOriginalDOB());
        entity.setEditedDOB(dto.getEditedDOB());
        entity.setLinkedInUrl(dto.getLinkedInUrl());
        entity.setRelocation(dto.getRelocation());
        entity.setBillRate(dto.getBillRate());
        entity.setPayroll(dto.getPayroll());
        entity.setMarketingStartDate(dto.getMarketingStartDate());
        entity.setRemarks(dto.getRemarks());
        entity.setMarketingVisa(dto.getMarketingVisa());
        entity.setActualVisa(dto.getActualVisa());

        return entity;
    }

    public static ConsultantDto convertEntityToDTO(Consultant entity) {
        ConsultantDto dto = new ConsultantDto();

        dto.setConsultantId(entity.getConsultantId());
        dto.setName(entity.getName());
        dto.setEmailId(entity.getEmailId());
        dto.setGrade(entity.getGrade());
        dto.setMarketingContact(entity.getMarketingContact());
        dto.setPersonalContact(entity.getPersonalContact());
        dto.setReference(entity.getReference());
        dto.setRecruiter(entity.getRecruiter());
        dto.setTeamLead(entity.getTeamLead());
        dto.setStatus(entity.getStatus());
        dto.setPassport(entity.getPassport());
        dto.setSalesExecutive(entity.getSalesExecutive());
        dto.setRemoteOnsite(entity.getRemoteOnsite());
        dto.setTechnology(entity.getTechnology());
        dto.setExperience(entity.getExperience());
        dto.setLocation(entity.getLocation());
        dto.setOriginalDOB(entity.getOriginalDOB());
        dto.setEditedDOB(entity.getEditedDOB());
        dto.setLinkedInUrl(entity.getLinkedInUrl());
        dto.setRelocation(entity.getRelocation());
        dto.setBillRate(entity.getBillRate());
        dto.setPayroll(entity.getPayroll());
        dto.setMarketingStartDate(entity.getMarketingStartDate());
        dto.setRemarks(entity.getRemarks());
        dto.setUpdatedTimeStamp(entity.getUpdatedTimeStamp());
        dto.setConsultantAddedTimeStamp(entity.getConsultantAddedTimeStamp());
        dto.setActualVisa(entity.getActualVisa());
        dto.setMarketingVisa(entity.getMarketingVisa());
        return dto;
    }

    public Consultant updateExistingHotListWithUpdatedHotList(Consultant existingConsultant, Consultant updatedConsultant) {

        if (updatedConsultant.getName() != null)
            existingConsultant.setName(updatedConsultant.getName());
        if (updatedConsultant.getEmailId() != null)
            existingConsultant.setEmailId(updatedConsultant.getEmailId());
        if (updatedConsultant.getGrade() != null)
            existingConsultant.setGrade(updatedConsultant.getGrade());
        if (updatedConsultant.getMarketingContact() != null)
            existingConsultant.setMarketingContact(updatedConsultant.getMarketingContact());
        if (updatedConsultant.getPersonalContact() != null)
            existingConsultant.setPersonalContact(updatedConsultant.getPersonalContact());
        if (updatedConsultant.getReference() != null)
            existingConsultant.setReference(updatedConsultant.getReference());
        if (updatedConsultant.getRecruiter() != null)
            existingConsultant.setRecruiter(updatedConsultant.getRecruiter());
        if (updatedConsultant.getTeamLead() != null)
            existingConsultant.setTeamLead(updatedConsultant.getTeamLead());
        if (updatedConsultant.getStatus() != null)
            existingConsultant.setStatus(updatedConsultant.getStatus());
        if (updatedConsultant.getPassport() != null)
            existingConsultant.setPassport(updatedConsultant.getPassport());
        if (updatedConsultant.getSalesExecutive() != null)
            existingConsultant.setSalesExecutive(updatedConsultant.getSalesExecutive());
        if (updatedConsultant.getRemoteOnsite() != null)
            existingConsultant.setRemoteOnsite(updatedConsultant.getRemoteOnsite());
        if (updatedConsultant.getTechnology() != null)
            existingConsultant.setTechnology(updatedConsultant.getTechnology());
        if (updatedConsultant.getMarketingVisa() != null)
            existingConsultant.setMarketingVisa(updatedConsultant.getMarketingVisa());
        if (updatedConsultant.getActualVisa() != null)
            existingConsultant.setActualVisa(updatedConsultant.getActualVisa());
        if (updatedConsultant.getExperience() != null)
            existingConsultant.setExperience(updatedConsultant.getExperience());
        if (updatedConsultant.getLocation() != null)
            existingConsultant.setLocation(updatedConsultant.getLocation());
        if (updatedConsultant.getOriginalDOB() != null)
            existingConsultant.setOriginalDOB(updatedConsultant.getOriginalDOB());
        if (updatedConsultant.getEditedDOB() != null)
            existingConsultant.setEditedDOB(updatedConsultant.getEditedDOB());
        if (updatedConsultant.getLinkedInUrl() != null)
            existingConsultant.setLinkedInUrl(updatedConsultant.getLinkedInUrl());
        if (updatedConsultant.getRelocation() != null)
            existingConsultant.setRelocation(updatedConsultant.getRelocation());
        if (updatedConsultant.getBillRate() != null)
            existingConsultant.setBillRate(updatedConsultant.getBillRate());
        if (updatedConsultant.getPayroll() != null)
            existingConsultant.setPayroll(updatedConsultant.getPayroll());
        if (updatedConsultant.getMarketingStartDate() != null)
            existingConsultant.setMarketingStartDate(updatedConsultant.getMarketingStartDate());
        if (updatedConsultant.getRemarks() != null)
            existingConsultant.setRemarks(updatedConsultant.getRemarks());

            existingConsultant.setUpdatedTimeStamp(LocalDateTime.now());

        return existingConsultant;
    }

    public ConsultantAddedResponse updateConsultant(String consultantId, ConsultantDto dto) {

        Optional<Consultant> optionalConsultant = consultantRepo.findById(consultantId);
        if (optionalConsultant.isEmpty()){
            throw new ConsultantNotFoundException("No Consultant Found With Id "+consultantId);
        }
        List<Consultant> duplicates= consultantRepo.findByEmailIdAndPersonalContact(dto.getEmailId(),dto.getPersonalContact());
        for(Consultant consultant: duplicates){
            if(!consultant.getConsultantId().equals(consultantId)) {
                throw new ConsultantAlreadyExistsException("A consultant with the same email and personal contact already exists in the system.");
            }
        }
        Consultant existingConsultant=optionalConsultant.get();
        Consultant updatedConsultant=convertDtoToEntity(dto);

        Consultant finalConsultant=updateExistingHotListWithUpdatedHotList(existingConsultant,updatedConsultant);

        consultantRepo.save(finalConsultant);

        ConsultantAddedResponse response=new ConsultantAddedResponse();
        response.setConsultantId(finalConsultant.getConsultantId());
        response.setName(finalConsultant.getName());
        response.setRecruiter(finalConsultant.getRecruiter());
        response.setAddedTimeStamp(LocalDateTime.now());

        return response;
    }

    public DeleteConsultantResponse deleteConsultant(String consultantId){
        Optional<Consultant> optionalConsultant = consultantRepo.findById(consultantId);
        if (optionalConsultant.isEmpty()){
            throw new ConsultantNotFoundException("No Consultant Found With Id "+consultantId);
        }
        consultantRepo.deleteById(consultantId);
        DeleteConsultantResponse response=new DeleteConsultantResponse();
        response.setConsultantId(consultantId);
        response.setAddedTimeStamp(LocalDateTime.now());
        return response;
    }
    public Page<ConsultantDto> getAllConsultants(Pageable pageable){

        Page<Consultant> list= consultantRepo.findAll(pageable);
        Page<ConsultantDto> dtoList=list.map(ConsultantService::convertEntityToDTO);

        return dtoList;
    }

    public ConsultantDto getConsultantByID(String consultantId){

        Optional<Consultant> optionalHotList= consultantRepo.findById(consultantId);
        if (optionalHotList.isEmpty()) throw new ConsultantNotFoundException("No Consultant Found with ID :"+consultantId);
        Consultant consultant=optionalHotList.get();
        ConsultantDto dtoList=convertEntityToDTO(consultant);

        return dtoList;
    }
//    public String addConsultantManually(List<ConsultantDto> list){
//
//        list.stream()
//                .map(HotListService::convertDtoToEntity)
//                .forEach(hotList -> {
//                    hotList.setConsultantId(this.generateConsultantId());
//                    hotList.setConsultantAddedTimeStamp(LocalDateTime.now());
//                    hotList.setUpdatedTimeStamp(LocalDateTime.now());
//                    hotListRepo.save(hotList);
//                });
//
//        return "Success" ;
//    }

    public Page<ConsultantDto> search(Pageable pageable, String keyword){

        Page<Consultant> pageableHotList= consultantRepo.searchHotlist(keyword,pageable);

        Page<ConsultantDto> pageableHotListDto= pageableHotList.map(ConsultantService::convertEntityToDTO);

        return pageableHotListDto;
    }



}
