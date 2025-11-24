package com.adroit.hotlistmicroservice.service;

import com.adroit.hotlistmicroservice.client.UserServiceClient;
import com.adroit.hotlistmicroservice.config.EmailNotificationUtil;
import com.adroit.hotlistmicroservice.dto.*;
import com.adroit.hotlistmicroservice.exception.ConsultantAlreadyExistsException;
import com.adroit.hotlistmicroservice.exception.ConsultantNotFoundException;
import com.adroit.hotlistmicroservice.exception.UserNotFoundException;
import com.adroit.hotlistmicroservice.exception.UserRoleNotAssignedException;
import com.adroit.hotlistmicroservice.filevalidator.FileValidator;
import com.adroit.hotlistmicroservice.mapper.ConsultantMapper;
import com.adroit.hotlistmicroservice.model.Consultant;
import com.adroit.hotlistmicroservice.model.ConsultantDocument;
import com.adroit.hotlistmicroservice.repo.ConsultantDocumentRepo;
import com.adroit.hotlistmicroservice.repo.ConsultantRepo;
import jakarta.transaction.Transactional;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
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
     @Autowired
    EmailNotificationUtil emailNotificationUtil;

    public static final Logger logger= LoggerFactory.getLogger(ConsultantService.class);

    public String generateConsultantId(){
        String maxConsultantId= consultantRepo.findMaxConsultantId();
        int nextNum=1;
        if(maxConsultantId!=null && maxConsultantId.startsWith("CONS")){
            nextNum=Integer.parseInt(maxConsultantId.substring(4))+1;
        }
        return String.format("CONS%05d",nextNum);
    }
    @Transactional
    public ConsultantAddedResponse addConsultant(ConsultantDto dto, List<MultipartFile> resumes,
                                                 List<MultipartFile> documents, boolean isAssignAll) throws IOException {
        logger.info("Creating new consultant {}", dto.getName());

        setUserNamesFromService(dto);
        validateExistingConsultants(dto);

        Consultant consultantToSave = prepareConsultantForSave(dto, isAssignAll);
        consultantToSave = saveConsultantWithDocuments(consultantToSave, resumes, documents);

        logger.info("Consultant {} Successfully with ID: {}",
                consultantToSave.getConsultantId().startsWith("RESTORED_") ? "Restored" : "Created",
                consultantToSave.getConsultantId());



        return consultantMapper.toConsultantAddedResponse(consultantToSave);
    }


    private void setUserNamesFromService(ConsultantDto dto) {
        try {
            if (isValidUserId(dto.getRecruiterId())) {
                String recruiterName = getUserName(dto.getRecruiterId());
                dto.setRecruiterName(recruiterName);
                logger.info("Recruiter ID: {}, Name: {}", dto.getRecruiterId(), recruiterName);
            }

            if (isValidUserId(dto.getTeamLeadId())) {
                String teamLeadName = getUserName(dto.getTeamLeadId());
                dto.setTeamleadName(teamLeadName);
                logger.info("Team Lead ID: {}, Name: {}", dto.getTeamLeadId(), teamLeadName);
            }

            if (isValidUserId(dto.getSalesExecutiveId())) {
                String salesExecName = getUserName(dto.getSalesExecutiveId());
                dto.setSalesExecutive(salesExecName);
                logger.info("Sales Executive ID: {}, Name: {}", dto.getSalesExecutiveId(), salesExecName);
            }
        } catch (Exception e) {
            logger.warn("Failed to fetch user names from user service", e);
        }
    }

    private boolean isValidUserId(String userId) {
        return userId != null && !userId.trim().isEmpty();
    }

    private String getUserName(String userId) {
        try {
            ResponseEntity<ApiResponse<UserDto>> response = userServiceClient.getUserByUserID(userId);
            if (response != null && response.getBody() != null && response.getBody().getData() != null) {
                return response.getBody().getData().getUserName();
            }
        } catch (Exception e) {
            logger.error("Error fetching user details for ID: {}", userId, e);
        }
        return "Unknown";
    }

    private void validateExistingConsultants(ConsultantDto dto) {
        List<Consultant> existingConsultants = consultantRepo.findByEmailIdAndPersonalContact(
                dto.getEmailId(), dto.getPersonalContact());

        List<Consultant> activeConsultants = filterActiveConsultants(existingConsultants);

        if (!activeConsultants.isEmpty()) {
            logger.warn("Active consultant already exists with email: {} and contact: {}",
                    dto.getEmailId(), dto.getPersonalContact());
            throw new ConsultantAlreadyExistsException("An active consultant with the same email and personal contact already exists.");
        }
    }

    private List<Consultant> filterActiveConsultants(List<Consultant> consultants) {
        if (consultants == null) return Collections.emptyList();

        return consultants.stream()
                .filter(Objects::nonNull)
                .filter(consultant -> !consultant.getIsDeleted())
                .collect(Collectors.toList());
    }

    private Consultant prepareConsultantForSave(ConsultantDto dto, boolean isAssignAll) {
        List<Consultant> existingConsultants = consultantRepo.findByEmailIdAndPersonalContact(
                dto.getEmailId(), dto.getPersonalContact());

        List<Consultant> softDeletedConsultants = filterSoftDeletedConsultants(existingConsultants);

        if (!softDeletedConsultants.isEmpty()) {
            return restoreSoftDeletedConsultant(softDeletedConsultants.get(0), dto, isAssignAll);
        } else {
            return createNewConsultant(dto, isAssignAll);
        }
    }

    private List<Consultant> filterSoftDeletedConsultants(List<Consultant> consultants) {
        if (consultants == null) return Collections.emptyList();

        return consultants.stream()
                .filter(Objects::nonNull)
                .filter(Consultant::getIsDeleted)
                .collect(Collectors.toList());
    }

    private Consultant restoreSoftDeletedConsultant(Consultant deletedConsultant, ConsultantDto dto, boolean isAssignAll) {
        logger.info("Restoring soft-deleted consultant ID: {}", deletedConsultant.getConsultantId());

        consultantMapper.updateConsultantFromDto(dto, deletedConsultant);

        deletedConsultant.setIsDeleted(false);
        deletedConsultant.setDeletedAt(null);
        deletedConsultant.setDeletedBy(null);
        deletedConsultant.setUpdatedTimeStamp(LocalDateTime.now());
        deletedConsultant.setIsAssignAll(isAssignAll);
        deletedConsultant.setMovedToHotlist(false);

        return deletedConsultant;
    }

    private Consultant createNewConsultant(ConsultantDto dto, boolean isAssignAll) {
        logger.info("Creating new consultant");

        Consultant consultant = consultantMapper.toEntity(dto);
        consultant.setIsAssignAll(isAssignAll);
        consultant.setMovedToHotlist(false);
        consultant.setConsultantId(generateConsultantId());
        consultant.setConsultantAddedTimeStamp(LocalDateTime.now());
        consultant.setUpdatedTimeStamp(LocalDateTime.now());
        consultant.setApprovalStatus("NOT_RAISED");

        return consultant;
    }

    private Consultant saveConsultantWithDocuments(Consultant consultant, List<MultipartFile> resumes,
                                                   List<MultipartFile> documents) throws IOException {
        // Save consultant first (without documents)
        Consultant savedConsultant = consultantRepo.save(consultant);

        // Initialize documents list if null
        if (savedConsultant.getDocuments() == null) {
            savedConsultant.setDocuments(new ArrayList<>());
        }

        // Process and save resumes
        processAndSaveFiles(resumes, "RESUME", savedConsultant);

        // Process and save documents
        processAndSaveFiles(documents, "DOCUMENT", savedConsultant);

        // Save again with documents
        return consultantRepo.save(savedConsultant);
    }

    private void processAndSaveFiles(List<MultipartFile> files, String documentType, Consultant consultant) throws IOException {
        if (files == null || files.isEmpty()) {
            return;
        }

        Tika tika = new Tika();

        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                try {
                    FileValidator.validateStrictFile(file);
                    String mimeType = FileValidator.mapFileNameToFileType(file.getOriginalFilename());
                    logger.info("{} File Mime Type: {}", documentType, tika.detect(file.getInputStream()));

                    ConsultantDocument doc =saveDocument(file, documentType, mimeType, consultant);
                    consultant.getDocuments().add(doc);

                } catch (IOException e) {
                    logger.error("Error processing {} file: {}", documentType, file.getOriginalFilename(), e);
                    throw e;
                }
            }
        }
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
        if(updatedConsultant.getDoneBy() != null)
            existingConsultant.setDoneBy(updatedConsultant.getDoneBy());
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
        if(updatedConsultant.getRecruiterName()!=null)
            existingConsultant.setRecruiterName(updatedConsultant.getRecruiterName());
        if(updatedConsultant.getTeamleadName()!=null)
            existingConsultant.setTeamleadName(updatedConsultant.getTeamleadName());

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
        if(dto.getRecruiterId()!=null && !dto.getRecruiterId().isEmpty()){
            dto.setRecruiterName(userServiceClient.getUserByUserID(dto.getRecruiterId()).getBody().getData().getUserName());
        }
        if (dto.getTeamLeadId()!=null && !dto.getTeamLeadId().isEmpty()){
            dto.setTeamleadName(userServiceClient.getUserByUserID(dto.getTeamLeadId()).getBody().getData().getUserName());
        }
        if(dto.getSalesExecutiveId()!=null && !dto.getSalesExecutiveId().isEmpty()){
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

        // Soft delete all associated documents
        softDeleteAllConsultantDocuments(consultantId, userId);

        logger.warn("Consultant {} is Deleted Successfully",consultantId);

        return consultantMapper.toDeleteConsultantResponse(optionalConsultant.get());
    }
    private void softDeleteAllConsultantDocuments(String consultantId, String deletedBy) {
        List<ConsultantDocument> documents = consultantDocumentRepo.findByConsultant_ConsultantIdAndIsDeletedFalse(consultantId);

        if (documents != null && !documents.isEmpty()) {
            for (ConsultantDocument document : documents) {
                document.setIsDeleted(true);
                document.setDeletedAt(LocalDateTime.now());
                document.setDeletedBy(deletedBy);
            }
            consultantDocumentRepo.saveAll(documents);
            logger.info("Soft deleted {} documents for consultant {}", documents.size(), consultantId);
        } else {
            logger.info("No active documents found to delete for consultant {}", consultantId);
        }
    }
    public Page<ConsultantDto> getAllConsultants(String keyword, Map<String,Object> filters, Pageable pageable, String statusFilter) {
        logger.info("Fetching All Consultants with keyword: {}...", keyword);

        Page<Consultant> list = consultantRepo.allConsultants(keyword,filters,statusFilter,pageable);

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

    public Page<ConsultantDto> getConsultantsByRecruiterId(Pageable pageable, String userId, String keyword, Map<String,Object> filters, String statusFilter){

        logger.info("Fetching the Consultants For UserID :{}",userId);
       UserDto user=userServiceClient.getUserByUserID(userId).getBody().getData();
       // if(user!=null){
       //     if(!user.getEntity().equalsIgnoreCase("US")){
       //         logger.warn("User {} does not belong to US entity",userId);
       //         throw new UserNotFoundException("No User Found In US Entity with "+userId);
       //     }
       // }
        Page<Consultant> pageableHotlist=consultantRepo.consultantsByRecruiter(userId, keyword, filters,statusFilter,pageable);
        Page<ConsultantDto> pageableHotlistDto= pageableHotlist.map(consultantMapper::toDTO);

        logger.info("Found {} consultants for user {}",pageableHotlistDto.getTotalElements(),userId);
       return pageableHotlistDto;
    }
    public Page<ConsultantDto> getTeamConsultants(Pageable pageable, String userId, String keyword, Map<String,Object> filters, String statusFilter){

        logger.info("Fetching the Consultants for User ID :{}",userId);
           UserDto user=userServiceClient.getUserByUserID(userId).getBody().getData();
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
               pageableHotList = consultantRepo.consultantsByTeamLead(userId, keyword,filters ,statusFilter,pageable);
          }else {
              String teamLeadId=user.getTeamAssignments().getFirst().getTeamLeadId();
               pageableHotList = consultantRepo.consultantsByTeamLead(teamLeadId, keyword,filters, statusFilter,pageable);
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
    public Page<ConsultantDto> getSalesExecutiveConsultants(String salesExecutiveId, String keyword, Pageable pageable, Map<String,Object> filters, String statusFilter){
        logger.info("Fetching the Consultants for Sales Executive ID :{}",salesExecutiveId);
       UserDto userDto=userServiceClient.getUserByUserID(salesExecutiveId).getBody().getData();
       if(userDto==null){
           logger.error("No User Found With ID {}",salesExecutiveId);
           throw new UserNotFoundException("No User Found With ID "+salesExecutiveId);
       }
       Page<Consultant> pageableHotList=consultantRepo.consultantsBySalesExecutive(salesExecutiveId,keyword,filters,statusFilter,pageable);
        logger.info("Found {} consultants for SalesExecutive {}",pageableHotList.getTotalElements(),salesExecutiveId);
       return pageableHotList.map(consultantMapper::toDTO);
    }
    public Page<ConsultantDto> getYetToOnBoardList(String keyword,Pageable pageable,Map<String,Object> filters,String statusFilter){

        Page<Consultant> pageableYetToOnBoardList=consultantRepo.yetToOnBoardConsultants(keyword,filters,statusFilter,pageable);
         return pageableYetToOnBoardList.map(consultantMapper::toDTO);
    }
    public ConsultantAddedResponse moveToHotlist(String consultantId,String userId){
        Optional<Consultant> optionalConsultant=consultantRepo.findById(consultantId);
        if(optionalConsultant.isEmpty()){
            throw new ConsultantNotFoundException("No Consultant Found With ID "+consultantId);
        }
        Consultant consultant=optionalConsultant.get();
        consultant.setMovedToHotlist(true);
       Consultant savedConsultant=consultantRepo.save(consultant);

        String approvedBy;
       if(userId!=null){
           approvedBy=getUserNameByUserId(userId);
       }
       else {
           approvedBy="NA";
       }
        Map<String,String> emails=new HashMap<>();
       if(consultant.getIsAssignAll()){
           emails.putAll(getUserEmailIdsByRole("SALESEXECUTIVE"));
           emails.putAll(getUserEmailIdsByRole("TEAMLEAD"));
           emails.putAll(getUserEmailIdsByRole("RECRUITER"));
       }else{
           if(consultant.getSalesExecutiveId()!=null) {
               emails.put(consultant.getSalesExecutive(), getUserEmailByUserId(consultant.getSalesExecutiveId()));
           }else if(consultant.getTeamLeadId()!=null){
               emails.put(consultant.getTeamleadName(), getUserEmailByUserId(consultant.getTeamLeadId()));
           }
       }
       emailNotificationUtil.notifyTeamForApprovedConsultant(
               emails,consultantId,consultant.getName(),consultant.getTechnology(),consultant.getTeamleadName()
               ,consultant.getSalesExecutive(),consultant.getRecruiterName(),approvedBy
       );
       return consultantMapper.toConsultantAddedResponse(savedConsultant);
    }
    public ConsultantAddedResponse moveToYetToOnBoard(String consultantId){
        Optional<Consultant> optionalConsultant=consultantRepo.findById(consultantId);
        if(optionalConsultant.isEmpty()){
            throw new ConsultantNotFoundException("No Consultant Found With ID "+consultantId);
        }
        Consultant consultant=optionalConsultant.get();
        consultant.setMovedToHotlist(false);
        Consultant savedConsultant=consultantRepo.save(consultant);
        return consultantMapper.toConsultantAddedResponse(savedConsultant);
    }

    public void modifyApprovalStatus(String userId,String consultantId,boolean isApproved){

           Consultant consultant=consultantRepo.findById(consultantId).orElseThrow(()-> new ConsultantNotFoundException("No Consultant Found With ID :"+consultantId));
           if(consultant.getTeamLeadId()==null || consultant.getTeamLeadId().isEmpty())
               throw new UserRoleNotAssignedException("TEAM LEAD not assigned for consultant with ID: " + consultantId);
           if(consultant.getRecruiterId()==null || consultant.getRecruiterId().isEmpty())
               throw new UserRoleNotAssignedException("Recruiter not assigned for consultant with ID: " + consultantId);
           Consultant modifiedConsultant=changeApprovalStatus(consultant,isApproved,userId);

           modifiedConsultant.setUpdatedBy(userId);
           modifiedConsultant.setUpdatedTimeStamp(LocalDateTime.now());
           consultantRepo.save(modifiedConsultant);
    }

    public Consultant changeApprovalStatus(Consultant consultant,boolean isApproved,String userId){

        Set<String> approvalStatusList=Set.of(
                "NOT_RAISED","TL_PENDING","ADMIN_PENDING",
                "SADMIN_PENDING","APPROVED","REJECTED");
        String currentStatus=consultant.getApprovalStatus();

        //List<String> emailIds=new ArrayList<>();
        Map<String,String> emailIds=new HashMap<>();
        if(isApproved) {
            switch (currentStatus){
                case "NOT_RAISED":
                    //emailIds.put(getUserNameByUserId(consultant.getTeamLeadId()), getUserEmailByUserId(consultant.getTeamLeadId()));
//                    emailNotificationUtil.sendConsultantApprovalRequestEmail(
//                            emailIds,consultant.getConsultantId(),consultant.getName(), consultant.getTechnology()
//                            ,consultant.getTeamleadName(),consultant.getSalesExecutive(),consultant.getRecruiterName()
//                            ,getUserNameByUserId(userId));
                    consultant.setApprovalStatus("TL_PENDING");
                    break;
                case "TL_PENDING":
                    //emailIds=getUserEmailIdsByRole("ADMIN");
//                    emailNotificationUtil.sendConsultantApprovalRequestEmail(
//                            emailIds,consultant.getConsultantId(),consultant.getName(), consultant.getTechnology()
//                            ,consultant.getTeamleadName(),consultant.getSalesExecutive(),consultant.getRecruiterName()
//                            ,getUserNameByUserId(userId));
                    consultant.setApprovalStatus("ADMIN_PENDING");
                    break;
                case "ADMIN_PENDING":
                    //emailIds=getUserEmailIdsByRole("SUPERADMIN");
                    logger.info("ADMIN_PENDING :-- Email sending to Super Admin");
//                    emailNotificationUtil.sendConsultantApprovalRequestEmail(
//                            emailIds,consultant.getConsultantId(),consultant.getName(), consultant.getTechnology()
//                            ,consultant.getTeamleadName(),consultant.getSalesExecutive(),consultant.getRecruiterName()
//                            ,getUserNameByUserId(userId));
                    consultant.setApprovalStatus("SADMIN_PENDING");
                    break;
                case "SADMIN_PENDING":
                   // emailIds.put(consultant.getRecruiterName(),getUserEmailByUserId(consultant.getRecruiterId()));
                    //emailIds.put(consultant.getTeamleadName(),getUserEmailByUserId(consultant.getTeamLeadId()));
//                    emailNotificationUtil.sendConsultantApprovedEmail(
//                            emailIds,consultant.getConsultantId(),consultant.getName(), consultant.getTechnology()
//                            ,consultant.getTeamleadName(),consultant.getSalesExecutive(),consultant.getRecruiterName()
//                            ,getUserNameByUserId(userId));
                    consultant.setApprovalStatus("APPROVED");
                    break;
                case "REJECTED":
                    consultant.setApprovalStatus("TL_PENDING");
                    break;
                default:
                    // already APPROVED or REJECTED do nothing
                    break;
            }
        }  else {
//            emailIds.put(consultant.getRecruiterName(),getUserEmailByUserId(consultant.getRecruiterId()));
//            emailIds.put(consultant.getTeamleadName(),getUserEmailByUserId(consultant.getTeamLeadId()));
//            emailNotificationUtil.sendConsultantRejectedEmail(
//                    emailIds,consultant.getConsultantId(),consultant.getName(), consultant.getTechnology()
//                    ,consultant.getTeamleadName(),consultant.getSalesExecutive(),consultant.getRecruiterName()
//                    ,getUserNameByUserId(userId));
            consultant.setApprovalStatus("REJECTED");
        }
       return consultant;
    }

    public String getUserEmailByUserId(String userId){

        UserDto userDto=userServiceClient.getUserByUserID(userId).getBody().getData();

        return userDto.getEmail();
    }
    public String getUserNameByUserId(String userId){

       return userServiceClient.getUserByUserID(userId).getBody().getData().getUserName();
    }
    public Map<String,String> getUserEmailIdsByRole(String role){
        if(!role.equalsIgnoreCase("SUPERADMIN")) {
            return userServiceClient.getAllUsers().getData()
                    .stream()
                    .filter(userDto -> "US".equalsIgnoreCase(userDto.getEntity()))
                    .filter(userDto -> userDto.getRoles().stream().anyMatch(roles -> roles.equalsIgnoreCase(role)))
                    .collect(Collectors.toMap(
                            UserDto::getUserName,
                            UserDto::getEmail
                    ));
        }else{
            logger.info("Fetching Primary Super Admin data...");
            return userServiceClient.getAllUsers().getData()
                    .stream()
                    .filter(userDto -> userDto.getIsPrimarySuperAdmin())
                    .collect(Collectors.toMap(
                            UserDto::getUserName,
                            UserDto::getEmail
                    ));
        }
    }

}
