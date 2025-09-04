package com.adroit.hotlistmicroservice.service;

import com.adroit.hotlistmicroservice.client.UserServiceClient;
import com.adroit.hotlistmicroservice.dto.*;
import com.adroit.hotlistmicroservice.exception.ConsultantAlreadyExistsException;
import com.adroit.hotlistmicroservice.exception.ConsultantNotFoundException;
import com.adroit.hotlistmicroservice.exception.UserNotFoundException;
import com.adroit.hotlistmicroservice.filevalidator.FileValidator;
import com.adroit.hotlistmicroservice.mapper.ConsultantMapper;
import com.adroit.hotlistmicroservice.model.Consultant;
import com.adroit.hotlistmicroservice.model.ConsultantDocument;
import com.adroit.hotlistmicroservice.repo.ConsultantDocumentRepo;
import com.adroit.hotlistmicroservice.repo.ConsultantRepo;
import com.adroit.hotlistmicroservice.utils.ConsultantSpecifications;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class ConsultantService {

     @Autowired
     ConsultantRepo consultantRepo;
     @Autowired
     UserServiceClient userServiceClient;
     @Autowired
     ConsultantDocumentRepo consultantDocumentRepo;
     @Autowired
     private ConsultantMapper consultantMapper;

    public static final Logger logger= LoggerFactory.getLogger(ConsultantService.class);

    public String generateConsultantId(){
        String maxConsultantId= consultantRepo.findMaxConsultantId();
        int nextNum=1;
        if(maxConsultantId!=null && maxConsultantId.startsWith("CONS")){
            nextNum=Integer.parseInt(maxConsultantId.substring(4))+1;
        }
        return String.format("CONS%05d",nextNum);
    }
    public ConsultantAddedResponse addConsultant(ConsultantDto dto, List<MultipartFile> resumes, List<MultipartFile> documents,boolean isAssignAll) throws IOException {
        logger.info("Creating new consultant {}",dto.getName());
        logger.info("Recruiter ID :{}",dto.getRecruiterId());
        if(dto.getRecruiterId() != null && !dto.getRecruiterId().isEmpty() && !dto.getRecruiterId().isBlank()) {
            logger.info("Recruiter ID :{}", dto.getRecruiterId());
            dto.setRecruiterName(userServiceClient.getUserByUserID(dto.getRecruiterId()).getBody().getData().getUserName());
        }

        if (dto.getTeamLeadId() != null && !dto.getTeamLeadId().isEmpty() && !dto.getTeamLeadId().isBlank()) {
            logger.info("Team Lead ID :{}", dto.getTeamLeadId());
            dto.setTeamleadName(userServiceClient.getUserByUserID(dto.getTeamLeadId()).getBody().getData().getUserName());
        }

        if(dto.getSalesExecutiveId() != null && !dto.getSalesExecutiveId().isEmpty() && !dto.getSalesExecutiveId().isBlank()) {
            logger.info("Sales Executive ID :{}", dto.getSalesExecutiveId());
            dto.setSalesExecutive(userServiceClient.getUserByUserID(dto.getSalesExecutiveId()).getBody().getData().getUserName());
        }
        Consultant consultant = consultantMapper.toEntity(dto);
         consultant.setIsAssignAll(isAssignAll);
         consultant.setMovedToHotlist(false);
        List<Consultant> existedHotList=consultantRepo.findByEmailIdAndPersonalContact(consultant.getEmailId(),consultant.getPersonalContact());
        if(!existedHotList.isEmpty()){
            logger.warn("A consultant with the same email and personal contact already exists in the system");
            throw new ConsultantAlreadyExistsException("A consultant with the same email and personal contact already exists in the system.");
        }
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
                    if (!resume.isEmpty())
                    {
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
        Consultant savedConsultant=consultantRepo.save(consultant);
        logger.info("Consultant Created Successfully {}",savedConsultant.getConsultantId());

        return consultantMapper.toConsultantAddedResponse(savedConsultant);
    }
    public ConsultantDocument saveDocument(MultipartFile file,String documentType,String fileType,Consultant consultant)
            throws IOException {

        logger.info("saving the {} type File In DataBase ",documentType);
        ConsultantDocument consultantDocument=new ConsultantDocument();
        consultantDocument.setConsultant(consultant);
        consultantDocument.setFileName(file.getOriginalFilename());
        consultantDocument.setFileData(file.getBytes());
        consultantDocument.setDocumentType(documentType);
        consultantDocument.setFileType(fileType);
        consultantDocument.setCreatedAt(LocalDateTime.now());
        logger.info("saved the {} type File In DataBase ",documentType);
        return consultantDocumentRepo.save(consultantDocument);
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
        if (updatedConsultant.getRecruiterId() != null)
            existingConsultant.setRecruiterId(updatedConsultant.getRecruiterId());
        if (updatedConsultant.getTeamLeadId() != null)
            existingConsultant.setTeamLeadId(updatedConsultant.getTeamLeadId());
        if (updatedConsultant.getStatus() != null)
            existingConsultant.setStatus(updatedConsultant.getStatus());
        if (updatedConsultant.getPassport() != null)
            existingConsultant.setPassport(updatedConsultant.getPassport());
        if (updatedConsultant.getSalesExecutiveId() != null)
            existingConsultant.setSalesExecutiveId(updatedConsultant.getSalesExecutiveId());
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

            existingConsultant.setIsAssignAll(updatedConsultant.getIsAssignAll());

            existingConsultant.setUpdatedTimeStamp(LocalDateTime.now());

        return existingConsultant;
    }

    public ConsultantAddedResponse updateConsultant(String consultantId, ConsultantDto dto) {

        logger.info("Updating the Consultant {}",consultantId);
        Optional<Consultant> optionalConsultant = consultantRepo.findById(consultantId);
        if (optionalConsultant.isEmpty()){
            logger.warn("No Consultants Found With ID: {}",consultantId);
            throw new ConsultantNotFoundException("No Consultant Found With Id "+consultantId);
        }
        List<Consultant> duplicates= consultantRepo.findByEmailIdAndPersonalContact(dto.getEmailId(),dto.getPersonalContact());
        for(Consultant consultant: duplicates){
            if(!consultant.getConsultantId().equals(consultantId)) {
                logger.warn("Consultant Already exists with same Email {} and personal contact {}",dto.getEmailId(),dto.getPersonalContact());
                throw new ConsultantAlreadyExistsException("A consultant with the same email and personal contact already exists in the system.");
            }
        }
        if(dto.getRecruiterId()!=null){
            dto.setRecruiterName(userServiceClient.getUserByUserID(dto.getRecruiterId()).getBody().getData().getUserName());
        }
        if (dto.getTeamLeadId()!=null){
            dto.setTeamleadName(userServiceClient.getUserByUserID(dto.getTeamLeadId()).getBody().getData().getUserName());
        }
        if(dto.getSalesExecutiveId()!=null){
            dto.setSalesExecutive(userServiceClient.getUserByUserID(dto.getSalesExecutiveId()).getBody().getData().getUserName());
        }
        Consultant existingConsultant=optionalConsultant.get();
        Consultant updatedConsultant=consultantMapper.toEntity(dto);
        Consultant finalConsultant=updateExistingHotListWithUpdatedHotList(existingConsultant,updatedConsultant);
        consultantRepo.save(finalConsultant);
        logger.info("Consultant {} is updated Successfully");

        return consultantMapper.toConsultantAddedResponse(finalConsultant);
    }

    public DeleteConsultantResponse deleteConsultant(String consultantId,String userId){
        logger.info("Deleting the Consultant : {}",consultantId);
        UserDto user=userServiceClient.getUserByUserID(userId).getBody().getData();
        if (user==null) throw new UserNotFoundException("No User Found With ID "+userId);
        Optional<Consultant> optionalConsultant = consultantRepo.findById(consultantId);
        if (optionalConsultant.isEmpty()){
            logger.warn("No Consultant Found With ID : {}",consultantId);
            throw new ConsultantNotFoundException("No Consultant Found With Id "+consultantId);
        }
        Consultant consultant= optionalConsultant.get();
        consultant.setIsDeleted(true);
        consultant.setDeletedBy(userId);
        consultant.setDeletedAt(LocalDateTime.now());
        consultantRepo.save(consultant);
        logger.warn("Consultant {} is Deleted Successfully",consultantId);

        return consultantMapper.toDeleteConsultantResponse(optionalConsultant.get());
    }
    public Page<ConsultantDto> getAllConsultants(Pageable pageable, String keyword) {
        logger.info("Fetching All Consultants with keyword: {}...", keyword);

        Page<Consultant> list = consultantRepo.allConsultants(keyword, pageable);

        Page<ConsultantDto> dtoList = list.map(consultantMapper::toDTO);
        logger.info("Fetched {} consultants with keyword: {}", dtoList.getTotalElements(), keyword);
        return dtoList;
    }

    public ConsultantDto getConsultantByID(String consultantId){

        logger.info("Fetching consultant details for Consultant ID: {}",consultantId);
        Optional<Consultant> optionalHotList= consultantRepo.findById(consultantId);
        if (optionalHotList.isEmpty()) throw new ConsultantNotFoundException("No Consultant Found with ID :"+consultantId);
        Consultant consultant=optionalHotList.get();
        ConsultantDto dtoList=consultantMapper.toDTO(consultant);
        logger.info("Found consultant details for Consultant ID: {}",consultantId);
        return dtoList;
    }
    public Page<ConsultantDto> search(Pageable pageable, String keyword){

        logger.info("Searching Consultants with keyword : '{}' , page: {} ,size :{}",keyword,pageable.getPageNumber(),pageable.getPageSize());
        Page<Consultant> pageableHotList= consultantRepo.searchHotlistByUtil(keyword,pageable);

        Page<ConsultantDto> pageableHotListDto= pageableHotList.map(consultantMapper::toDTO);
        logger.info("Found {} consultants matching keyword '{}'", pageableHotList.getTotalElements(), keyword);
        return pageableHotListDto;
    }
    private static EmployeeDropDownDto convertUserDtoToEmployeeDropDownDto(UserDto userDto) {
        EmployeeDropDownDto dto=new EmployeeDropDownDto();
        dto.setEmployeeId(userDto.getUserId());
        dto.setEmployeeName(userDto.getUserName());
        return dto;
    }
    public List<EmployeeDropDownDto> getEmployeeDetailsByRole(String role){

        logger.info("Fetching Employee Details For role {}",role);
        List<UserDto> employees=userServiceClient.getAllUsers().getData();
        logger.info("Filtering Employees by US Entity...");
        List<UserDto> employeesByRole=employees.stream()
                .filter(employee -> "US".equalsIgnoreCase(employee.getEntity()))
                .filter(employee-> employee.getRoles().contains(role))
                .collect(Collectors.toList());

        List<EmployeeDropDownDto> dropDownEmployees=employeesByRole.stream()
               .map(ConsultantService::convertUserDtoToEmployeeDropDownDto)
               .collect(Collectors.toList());
        logger.info("Fetched {} Employees For Role {}",dropDownEmployees.size(),role);
        return dropDownEmployees;
    }

    public Page<ConsultantDto> getConsultantsByUserId(Pageable pageable,String userId,String keyword){

        logger.info("Fetching the Consultants For UserID :{}",userId);
       UserDto user=userServiceClient.getUserByUserID(userId).getBody().getData();
       // if(user!=null){
       //     if(!user.getEntity().equalsIgnoreCase("US")){
       //         logger.warn("User {} does not belong to US entity",userId);
       //         throw new UserNotFoundException("No User Found In US Entity with "+userId);
       //     }
       // }
        Page<Consultant> pageableHotlist=consultantRepo.consultantsByRecruiter(userId, keyword, pageable);
        Page<ConsultantDto> pageableHotlistDto= pageableHotlist.map(consultantMapper::toDTO);

        logger.info("Found {} consultants for user {}",pageableHotlistDto.getTotalElements(),userId);
       return pageableHotlistDto;
    }
    public Page<ConsultantDto> getTeamConsultants(Pageable pageable,String userId,String keyword){

        logger.info("Fetching the Consultants for User ID :{}",userId);
           UserDto user=userServiceClient.getUserByUserID(userId).getBody().getData();
           logger.info("user from the User Micro service {} :: {}",user.getUserId(),user.getAssociatedTeamLeadId());
        ResponseEntity<ApiResponse<UserDto>> response = userServiceClient.getUserByUserID(userId);
        logger.info("Raw user response: {}", response);
           if(user!=null){
              if(!user.getEntity().equalsIgnoreCase("US")){
                  logger.warn("User {} does not belong to US entity",userId);
                  throw new UserNotFoundException("No User Found In US Entity with "+userId);
              }
           }
          Set<String> roles=user.getRoles();
          boolean isTeamLead=roles.stream().anyMatch(role -> role.equalsIgnoreCase("TEAMLEAD"));
          logger.info("isTeamLead ---------------> {}",isTeamLead);
          Page<Consultant> pageableHotList;
          if(isTeamLead) {
               pageableHotList = consultantRepo.consultantsByTeamLead(userId, keyword, pageable);
          }else {
              String teamLeadId=user.getAssociatedTeamLeadId();
               pageableHotList = consultantRepo.consultantsByTeamLead(teamLeadId, keyword, pageable);
          }
        Page<ConsultantDto> hotListDtoPage=pageableHotList.map(consultantMapper::toDTO);

        logger.info("Found {} consultants for TeamLead {}",hotListDtoPage.getTotalElements(),userId);
       return hotListDtoPage;
    }
    public Page<UserDto> getAllUSEntityUsers(Pageable pageable){
        logger.info("Fetching the All US Users...");
        List<UserDto> users=userServiceClient.getAllUsers().getData().stream()
                .filter(user -> user.getEntity().equalsIgnoreCase("US"))
                .collect(Collectors.toList());

        int start=Math.min((int) pageable.getOffset(),users.size());
        int end=Math.min(start + pageable.getPageSize(), users.size());

        List<UserDto> pagedList=users.subList(start,end);
        logger.info("Found {} US users ",users.size());
       return new PageImpl<>(pagedList,pageable, users.size());
    }
    public Page<ConsultantDto> getSalesExecutiveConsultants(String salesExecutiveId,String keyword,Pageable pageable){
        logger.info("Fetching the Consultants for Sales Executive ID :{}",salesExecutiveId);
       UserDto userDto=userServiceClient.getUserByUserID(salesExecutiveId).getBody().getData();
       if(userDto==null){
           logger.error("No User Found With ID {}",salesExecutiveId);
           throw new UserNotFoundException("No User Found With ID "+salesExecutiveId);
       }
       Page<Consultant> pageableHotList=consultantRepo.consultantsBySalesExecutive(salesExecutiveId,keyword,pageable);
        logger.info("Found {} consultants for SalesExecutive {}",pageableHotList.getTotalElements(),salesExecutiveId);
       return pageableHotList.map(consultantMapper::toDTO);
    }
    public Page<ConsultantDto> getYetToOnBoardList(String keyword,Pageable pageable){

        Page<Consultant> pageableYetToOnBoardList=consultantRepo.yetToOnBoardConsultants(keyword,pageable);
         return pageableYetToOnBoardList.map(consultantMapper::toDTO);
    }
    public ConsultantAddedResponse moveToHotlist(String consultantId){
        Optional<Consultant> optionalConsultant=consultantRepo.findById(consultantId);
        if(optionalConsultant.isEmpty()){
            throw new ConsultantNotFoundException("No Consultant Found With ID "+consultantId);
        }
        Consultant consultant=optionalConsultant.get();
        consultant.setMovedToHotlist(true);
       Consultant savedConsultant=consultantRepo.save(consultant);
       return consultantMapper.toConsultantAddedResponse(savedConsultant);
    }
}
