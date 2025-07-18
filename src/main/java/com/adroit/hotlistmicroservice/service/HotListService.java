package com.adroit.hotlistmicroservice.service;

import com.adroit.hotlistmicroservice.dto.ConsultantAddedResponse;
import com.adroit.hotlistmicroservice.dto.DeleteConsultantResponse;
import com.adroit.hotlistmicroservice.dto.HotListDto;
import com.adroit.hotlistmicroservice.exception.ConsultantAlreadyExistsException;
import com.adroit.hotlistmicroservice.exception.ConsultantNotFoundException;
import com.adroit.hotlistmicroservice.model.HotList;
import com.adroit.hotlistmicroservice.repo.HotListRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HotListService {

     @Autowired
     HotListRepo hotListRepo;

  public static final Logger logger= LoggerFactory.getLogger(HotListService.class);

//    public ConsultantAddedResponse addConsultant(HotListDto dto, MultipartFile actualVisaFile, MultipartFile marketingVisaFile) throws IOException {
//        HotList hotList = convertDtoToEntity(dto);
//
//        HotList existedHotList=hotListRepo.findByEmailIdAndPersonalContact(hotList.getEmailId(),hotList.getPersonalContact());
//        if(existedHotList!=null) throw new ConsultantAlreadyExistsException("A consultant with the same email and personal contact already exists in the system.");
//
//        hotList.setConsultantId(generateConsultantId());
//        hotList.setConsultantAddedTimeStamp(LocalDateTime.now());
//        hotList.setUpdatedTimeStamp(LocalDateTime.now());
//
//        // Handle actual visa file
//        if (actualVisaFile != null && !actualVisaFile.isEmpty()) {
//            validateFileSize(actualVisaFile);
//            if (!isValidFileType(actualVisaFile)) {
//                throw new InvalidFileTypeException("Invalid file type. Only PDF, DOC, and DOCX files are allowed.");
//            }
//            byte[] actualVisaData = actualVisaFile.getBytes();
//
//            hotList.setActualVisa(actualVisaData);
//        }
//        // Handle marketing visa file
//        if (marketingVisaFile != null && !marketingVisaFile.isEmpty()) {
//            validateFileSize(marketingVisaFile);
//            if (!isValidFileType(marketingVisaFile)) {
//                throw new InvalidFileTypeException("Invalid file type. Only PDF, DOC, and DOCX files are allowed.");
//            }
//            byte[] marketingVisaData = marketingVisaFile.getBytes();
//            hotList.setMarketingVisa(marketingVisaData);
//
//        }
//        hotListRepo.save(hotList);
//
//        ConsultantAddedResponse response = new ConsultantAddedResponse();
//        response.setConsultantId(hotList.getConsultantId());
//        response.setName(hotList.getName());
//        response.setRecruiter(hotList.getRecruiter());
//        response.setAddedTimeStamp(hotList.getConsultantAddedTimeStamp());
//
//        return response;
//    }
    private boolean isValidFileType(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName != null) {
            String fileExtension = getFileExtension(fileName).toLowerCase();
            return fileExtension.equals("pdf") || fileExtension.equals("docx") || fileExtension.equals("doc");
        }
        return false;
    }
    private String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf(".");
        if (index > 0) {
            return fileName.substring(index + 1);
        }
        return "";
    }
    private void validateFileSize(MultipartFile file) {
        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {

            throw new MaxUploadSizeExceededException(maxSize);
        }
    }
    public static HotList convertDtoToEntity(HotListDto dto) {
        HotList entity = new HotList();

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

    public static HotListDto convertEntityToDTO(HotList entity) {
        HotListDto dto = new HotListDto();

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

    public HotList updateExistingHotListWithUpdatedHotList(HotList existingHotList, HotList updatedHotList) {

        if (updatedHotList.getName() != null)
            existingHotList.setName(updatedHotList.getName());
        if (updatedHotList.getEmailId() != null)
            existingHotList.setEmailId(updatedHotList.getEmailId());
        if (updatedHotList.getGrade() != null)
            existingHotList.setGrade(updatedHotList.getGrade());
        if (updatedHotList.getMarketingContact() != null)
            existingHotList.setMarketingContact(updatedHotList.getMarketingContact());
        if (updatedHotList.getPersonalContact() != null)
            existingHotList.setPersonalContact(updatedHotList.getPersonalContact());
        if (updatedHotList.getReference() != null)
            existingHotList.setReference(updatedHotList.getReference());
        if (updatedHotList.getRecruiter() != null)
            existingHotList.setRecruiter(updatedHotList.getRecruiter());
        if (updatedHotList.getTeamLead() != null)
            existingHotList.setTeamLead(updatedHotList.getTeamLead());
        if (updatedHotList.getStatus() != null)
            existingHotList.setStatus(updatedHotList.getStatus());
        if (updatedHotList.getPassport() != null)
            existingHotList.setPassport(updatedHotList.getPassport());
        if (updatedHotList.getSalesExecutive() != null)
            existingHotList.setSalesExecutive(updatedHotList.getSalesExecutive());
        if (updatedHotList.getRemoteOnsite() != null)
            existingHotList.setRemoteOnsite(updatedHotList.getRemoteOnsite());
        if (updatedHotList.getTechnology() != null)
            existingHotList.setTechnology(updatedHotList.getTechnology());
        if (updatedHotList.getMarketingVisa() != null)
            existingHotList.setMarketingVisa(updatedHotList.getMarketingVisa());
        if (updatedHotList.getActualVisa() != null)
            existingHotList.setActualVisa(updatedHotList.getActualVisa());
        if (updatedHotList.getExperience() != null)
            existingHotList.setExperience(updatedHotList.getExperience());
        if (updatedHotList.getLocation() != null)
            existingHotList.setLocation(updatedHotList.getLocation());
        if (updatedHotList.getOriginalDOB() != null)
            existingHotList.setOriginalDOB(updatedHotList.getOriginalDOB());
        if (updatedHotList.getEditedDOB() != null)
            existingHotList.setEditedDOB(updatedHotList.getEditedDOB());
        if (updatedHotList.getLinkedInUrl() != null)
            existingHotList.setLinkedInUrl(updatedHotList.getLinkedInUrl());
        if (updatedHotList.getRelocation() != null)
            existingHotList.setRelocation(updatedHotList.getRelocation());
        if (updatedHotList.getBillRate() != null)
            existingHotList.setBillRate(updatedHotList.getBillRate());
        if (updatedHotList.getPayroll() != null)
            existingHotList.setPayroll(updatedHotList.getPayroll());
        if (updatedHotList.getMarketingStartDate() != null)
            existingHotList.setMarketingStartDate(updatedHotList.getMarketingStartDate());
        if (updatedHotList.getRemarks() != null)
            existingHotList.setRemarks(updatedHotList.getRemarks());

            existingHotList.setUpdatedTimeStamp(LocalDateTime.now());

        return existingHotList;
    }

    public ConsultantAddedResponse addConsultant(HotListDto dto) throws IOException {
        HotList hotList = convertDtoToEntity(dto);

        List<HotList> existedHotList=hotListRepo.findByEmailIdAndPersonalContact(hotList.getEmailId(),hotList.getPersonalContact());
        if(!existedHotList.isEmpty()) throw new ConsultantAlreadyExistsException("A consultant with the same email and personal contact already exists in the system.");

        hotList.setConsultantId(generateConsultantId());
        hotList.setConsultantAddedTimeStamp(LocalDateTime.now());
        hotList.setUpdatedTimeStamp(LocalDateTime.now());

        hotListRepo.save(hotList);

        ConsultantAddedResponse response = new ConsultantAddedResponse();
        response.setConsultantId(hotList.getConsultantId());
        response.setName(hotList.getName());
        response.setRecruiter(hotList.getRecruiter());
        response.setAddedTimeStamp(hotList.getConsultantAddedTimeStamp());

        return response;
    }
    public String generateConsultantId(){

       String maxConsultantId=hotListRepo.findMaxConsultantId();
        int nextNum=1;
        if(maxConsultantId!=null && maxConsultantId.startsWith("CONS")){
          nextNum=Integer.parseInt(maxConsultantId.substring(4))+1;
       }
       return String.format("CONS%05d",nextNum);
    }

    public ConsultantAddedResponse updateConsultant(String consultantId,HotListDto dto) {

        Optional<HotList> optionalHotList = hotListRepo.findById(consultantId);
        if (optionalHotList.isEmpty()){
            throw new ConsultantNotFoundException("No Consultant Found With Id "+consultantId);
        }
        List<HotList> duplicates=hotListRepo.findByEmailIdAndPersonalContact(dto.getEmailId(),dto.getPersonalContact());
        for(HotList hotList: duplicates){
            if(!hotList.getConsultantId().equals(consultantId)) {
                throw new ConsultantAlreadyExistsException("A consultant with the same email and personal contact already exists in the system.");
            }
        }
        HotList existingHotlist=optionalHotList.get();
        HotList updatedHotList=convertDtoToEntity(dto);
        HotList finalHotlist=updateExistingHotListWithUpdatedHotList(existingHotlist,updatedHotList);

        hotListRepo.save(finalHotlist);

        ConsultantAddedResponse response=new ConsultantAddedResponse();
        response.setConsultantId(finalHotlist.getConsultantId());
        response.setName(finalHotlist.getName());
        response.setRecruiter(finalHotlist.getRecruiter());
        response.setAddedTimeStamp(LocalDateTime.now());

        return response;
    }

    public DeleteConsultantResponse deleteConsultant(String consultantId){
        Optional<HotList> optionalHotList = hotListRepo.findById(consultantId);
        if (optionalHotList.isEmpty()){
            throw new ConsultantNotFoundException("No Consultant Found With Id "+consultantId);
        }
        hotListRepo.deleteById(consultantId);
        DeleteConsultantResponse response=new DeleteConsultantResponse();
        response.setConsultantId(consultantId);
        response.setAddedTimeStamp(LocalDateTime.now());
        return response;
    }
    public Page<HotListDto> getAllConsultants(Pageable pageable){

        Page<HotList> list=hotListRepo.findAll(pageable);

        Page<HotListDto> dtoList=list.map(HotListService::convertEntityToDTO);

        return dtoList;
    }

    public HotListDto getConsultantByID(String consultantId){

        Optional<HotList> optionalHotList=hotListRepo.findById(consultantId);
        if (optionalHotList.isEmpty()) throw new ConsultantNotFoundException("No Consultant Found with ID :"+consultantId);
        HotList hotList=optionalHotList.get();
        HotListDto dtoList=convertEntityToDTO(hotList);

        return dtoList;
    }
//    public String addConsultantManually(List<HotListDto> list){
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

    public Page<HotListDto> search(Pageable pageable,String keyword){

        Page<HotList> pageableHotList=hotListRepo.searchHotlist(keyword,pageable);

        Page<HotListDto> pageableHotListDto= pageableHotList.map(HotListService::convertEntityToDTO);

        return pageableHotListDto;
    }
}
