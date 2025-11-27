package com.adroit.hotlistmicroservice.service;

import com.adroit.hotlistmicroservice.client.UserServiceClient;
import com.adroit.hotlistmicroservice.dto.*;
import com.adroit.hotlistmicroservice.exception.ResourceNotFoundException;
import com.adroit.hotlistmicroservice.mapper.RateTermsConfirmationMapper;
import com.adroit.hotlistmicroservice.model.RateTermsConfirmation;
import com.adroit.hotlistmicroservice.repo.ConsultantRepo;
import com.adroit.hotlistmicroservice.repo.RateTermsConfirmationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RateTermsConfirmationService {

    @Autowired
    RateTermsConfirmationRepository rtrRepository;
    @Autowired
    RateTermsConfirmationMapper rtrMapper;
    @Autowired
    ConsultantService consultantService;
    @Autowired
    ConsultantRepo consultantRepo;
    @Autowired
    UserServiceClient userServiceClient;

    public RTRAddedResponse createRTR(String userId, RateTermsConfirmationRequest rtrDto){

        RateTermsConfirmation rtr=rtrMapper.entityFromRequest(rtrDto);
        rtr.setRtrId(generateRtrId());
        rtr.setCreatedBy(userId);
        ConsultantDto consultant=consultantService.getConsultantByID(rtrDto.getConsultantId());
        rtr.setConsultantName(consultant.getName());
        rtr.setSalesExecutiveId(consultant.getSalesExecutiveId());
        rtr.setSalesExecutive(consultant.getSalesExecutive());
        rtr.setTechnology(consultant.getTechnology());
        RateTermsConfirmation savedRTR=rtrRepository.save(rtr);
        return rtrMapper.toRtrAddedResponse(savedRTR);
    }

    public RTRAddedResponse updateRTR(String rtrId, String userId, RTRUpdateDTO rtrUpdateDTO){

        RateTermsConfirmation existingRTR=rtrRepository.findById(rtrId).orElseThrow(()-> new ResourceNotFoundException("No RTR Found with ID :"+ rtrId+"to Update"));
        if(existingRTR.getIsDeleted()) throw new ResourceNotFoundException("No RTR Found with ID :"+ rtrId+"to Update");
        rtrMapper.updateRTRFromDto(rtrUpdateDTO,existingRTR);
        existingRTR.setUpdatedBy(userId);
        existingRTR.setUpdatedAt(LocalDateTime.now());
       RateTermsConfirmation savedRTR=rtrRepository.save(existingRTR);
       return new RTRAddedResponse(rtrId, savedRTR.getConsultantId(), savedRTR.getConsultantName(), savedRTR.getClientName());
    }
    public String generateRtrId(){
        String lastRtrId=rtrRepository.findTopByOrderByRtrIdDesc()
                .map(RateTermsConfirmation::getRtrId)
                .orElse("RTR000000");

        int num=Integer.parseInt(lastRtrId.replace("RTR",""))+1;
        return String.format("RTR%06d",num);
    }

    public Page<RateTermsConfirmationDTO> getRTRList(String keyword, Map<String,Object> filters, Pageable pageable){

     return rtrRepository.allRTRs(keyword,filters,pageable)
              .map(rtrMapper::toDtoFromEntity);
    }

    public Page<RateTermsConfirmationDTO> getRTRListByDate(String keyword, Map<String,Object> filters, Pageable pageable, String date){

     return rtrRepository.rtrsByDate(keyword,filters,pageable,date)
              .map(rtrMapper::toDtoFromEntity);
    }

    public Page<RateTermsConfirmationDTO> getSalesRTRList(String userId,String keyword,Map<String ,Object> filters,Pageable pageable){

        return rtrRepository.salesRTRs(userId, keyword, filters, pageable)
                .map(rtrMapper::toDtoFromEntity);
    }

    public Page<RateTermsConfirmationDTO> getTeamRtrs(String userId, String keyword, Map<String, Object> filters, Pageable pageable) {
        List<String> teamConsultants = consultantRepo.findConsultantIdsByTeamLeadId(userId);
        log.info("No. of consultants found: {} | Consultant IDs: {}", teamConsultants.size(), teamConsultants);

        Page<RateTermsConfirmationDTO> dtoPage = rtrRepository.teamRtrs(teamConsultants, keyword, filters, pageable)
                .map(rtrMapper::toDtoFromEntity);

        // ✅ Populate createdByName using existing Feign client call
        dtoPage.forEach(dto -> {
            try {
                if (dto.getCreatedBy() != null) {
                    ResponseEntity<ApiResponse<UserDto>> response = userServiceClient.getUserByUserID(dto.getCreatedBy());
                    ApiResponse<UserDto> apiResponse = response.getBody();

                    if (apiResponse != null && apiResponse.getData() != null) {
                        dto.setCreatedByName(apiResponse.getData().getUserName());
                    } else {
                        dto.setCreatedByName("Unknown");
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to fetch username for userId: {}", dto.getCreatedBy(), e);
                dto.setCreatedByName("Unknown");
            }
        });

        return dtoPage;
    }


    public RateTermsConfirmationDTO getRTRById(String rtrId){

        RateTermsConfirmation rateTermsConfirmation=rtrRepository.findById(rtrId).orElseThrow(()-> new ResourceNotFoundException("No RTR Found With ID :"+rtrId));
        if(rateTermsConfirmation.getIsDeleted()) throw new ResourceNotFoundException("No RTR Found with ID :"+ rtrId+"to Update");
        return rtrMapper.toDtoFromEntity(rateTermsConfirmation);
    }

    public Page<RateTermsConfirmationDTO> getRTRByConsultant(String consultantId,String keyword,Map<String,Object> filters,Pageable pageable){

        Page<RateTermsConfirmation> rateTermsConfirmationPage=rtrRepository.consultantRTRs(consultantId,keyword,filters,pageable);
        return rateTermsConfirmationPage.map(rtrMapper::toDtoFromEntity);
    }

    public Map<String,Object> deleteRTR(String rtrId, String userId) {

        RateTermsConfirmation rtr=rtrRepository.findById(rtrId).orElseThrow(()-> new ResourceNotFoundException("No RTR Found With ID: "+rtrId));

        rtr.setIsDeleted(true);
        rtr.setDeletedAt(LocalDateTime.now());
        rtr.setDeletedBy(userId);

        RateTermsConfirmation deletedRTR=rtrRepository.save(rtr);

        Map<String,Object> deleteResponse=new HashMap<>();
        deleteResponse.put("rtrId",deletedRTR.getRtrId());
        deleteResponse.put("consultantId",deletedRTR.getConsultantId());
        deleteResponse.put("deletedAt",deletedRTR.getDeletedAt());
        deleteResponse.put("deletedBy",deletedRTR.getDeletedBy());

        return deleteResponse;

    }
}
