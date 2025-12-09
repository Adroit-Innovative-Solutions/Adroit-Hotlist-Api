package com.adroit.hotlistmicroservice.service;

import com.adroit.hotlistmicroservice.client.UserServiceClient;
import com.adroit.hotlistmicroservice.dto.*;
import com.adroit.hotlistmicroservice.exception.ResourceNotFoundException;
import com.adroit.hotlistmicroservice.mapper.RTRInterviewMapper;
import com.adroit.hotlistmicroservice.model.RTRInterview;
import com.adroit.hotlistmicroservice.model.RateTermsConfirmation;
import com.adroit.hotlistmicroservice.repo.ConsultantRepo;
import com.adroit.hotlistmicroservice.repo.RTRInterviewRepository;
import com.adroit.hotlistmicroservice.repo.RateTermsConfirmationRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class RTRInterviewService {

    @Autowired
    RTRInterviewRepository rtrInterviewRepository;
    @Autowired
    RateTermsConfirmationRepository rateTermsConfirmationRepository;
    @Autowired
    RTRInterviewMapper rtrInterviewMapper;
    @Autowired
    ConsultantRepo consultantRepo;

    @Autowired
    private UserServiceClient userServiceClient;


    public InterviewAddedDto scheduleInterview(ScheduleInterviewDto interviewDto,String userId) {

       RateTermsConfirmation  rtr=rateTermsConfirmationRepository.findById(interviewDto.getRtrId()).
               orElseThrow(()-> new ResourceNotFoundException("NO RTR Found with ID "+interviewDto.getRtrId()));
       if(rtr.getIsDeleted()) throw new ResourceNotFoundException("NO RTR Found with ID "+interviewDto.getRtrId());

       Optional<RateTermsConfirmation> rtrInterview= rateTermsConfirmationRepository.findByRtrIdAndIsDeleted(interviewDto.getRtrId(),true);
       if(rtrInterview.isPresent())
           throw new ResourceNotFoundException("Interview Already Scheduled For RTR ID "+interviewDto.getRtrId());

       RTRInterview interview=rtrInterviewMapper.rtrToRTRInterview(rtr);

       interview.setInterviewId(generateInterviewId());
       interview.setInterviewLevel(interviewDto.getInterviewLevel());
       interview.setInterviewDateTime(interviewDto.getInterviewDateTime());
       interview.setInterviewerEmailId(interviewDto.getInterviewerEmailId());
       interview.setZoomLink(interviewDto.getZoomLink());
       interview.setCreatedBy(userId);
       interview.setRemarks(interviewDto.getRemarks());
       interview.setCreatedAt(LocalDateTime.now());
       interview.setUpdatedAt(LocalDateTime.now());

        addInterviewHistory(interview, interviewDto.getInterviewLevel(), "SCHEDULED");

       RTRInterview savedInterview=rtrInterviewRepository.save(interview);

       return new InterviewAddedDto(savedInterview.getInterviewId(),savedInterview.getRtrId(),
               savedInterview.getConsultantId(), savedInterview.getConsultantName());
    }

    public InterviewAddedDto updateInterview(UpdateInterviewDto updateInterviewDto, String userId) {

        RTRInterview rtrInterview=getInterviewIsNotDeleted(updateInterviewDto.getInterviewId());

        rtrInterviewMapper.updateInterviewFromDto(updateInterviewDto, rtrInterview);
        rtrInterview.setUpdatedBy(userId);
        rtrInterview.setUpdatedAt(LocalDateTime.now());
        if("PLACED".equalsIgnoreCase(updateInterviewDto.getInterviewStatus())){
            rtrInterview.setIsPlaced(true);
        }else {
            rtrInterview.setIsPlaced(false);
        }
        addInterviewHistory(rtrInterview, updateInterviewDto.getInterviewLevel(), updateInterviewDto.getInterviewStatus());

        RTRInterview savedInterview=rtrInterviewRepository.save(rtrInterview);

        return new InterviewAddedDto(savedInterview.getInterviewId(),savedInterview.getRtrId(),
                savedInterview.getConsultantId(), savedInterview.getConsultantName());
    }

    private void addInterviewHistory(RTRInterview interview, String level, String status) {
        List<Map<String, Object>> historyList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        // Read existing history
        if (interview.getInterviewHistory() != null && !interview.getInterviewHistory().isEmpty()) {
            try {
                historyList = mapper.readValue(
                        interview.getInterviewHistory(),
                        new TypeReference<List<Map<String, Object>>>() {}
                );
            } catch (Exception e) {
                // Log error and continue with empty list
                // e.g., logger.warn("Failed to read interview history", e);
            }
        }

        // Add new entry
        Map<String, Object> newEntry = new HashMap<>();
        newEntry.put("interviewLevel", level);
        newEntry.put("interviewStatus", status);
        newEntry.put("timestamp", LocalDateTime.now().toString());
        historyList.add(newEntry);

        // Save back as JSON string
        try {
            interview.setInterviewHistory(mapper.writeValueAsString(historyList));
        } catch (Exception e) {
            throw new RuntimeException("Failed to update interview history", e);
        }
    }

    public void deleteInterview(String interviewId,String userId){

        RTRInterview rtrInterview=getInterviewIsNotDeleted(interviewId);

        rtrInterview.setIsDeleted(true);
        rtrInterview.setDeletedBy(userId);
        rtrInterview.setDeletedAt(LocalDateTime.now());

        rtrInterviewRepository.save(rtrInterview);
    }

    public RTRInterviewDto getInterviewById(String interviewId){
        return rtrInterviewMapper.rtrEntityToRTRDto(getInterviewIsNotDeleted(interviewId));
    }

    public  RTRInterview getInterviewIsNotDeleted(String interviewId){
        log.info("Fetching Interview ID "+interviewId);
       return rtrInterviewRepository.findByInterviewIdAndIsDeleted(interviewId,false)
                .orElseThrow(()-> new ResourceNotFoundException("No Interview Found With ID :"+interviewId));
    }

    public Page<RTRInterviewDto> getAllInterviews(String keyword, Map<String,Object> filters, Pageable pageable){

        Page<RTRInterviewDto> map = rtrInterviewRepository.allInterviews(keyword, filters, pageable)
                .map(rtrInterviewMapper::rtrEntityToRTRDto);

        map.forEach(dto -> {
            try {
                if (dto.getCreatedBy() != null) {
                    ResponseEntity<ApiResponse<UserDto>> response = userServiceClient.getUserByUserID(dto.getCreatedBy());
                    ApiResponse<UserDto> apiResponse = response.getBody();

                    if (apiResponse != null && apiResponse.getData() != null) {
                        dto.setCreatedBy(apiResponse.getData().getUserName());
                    } else {
                        dto.setCreatedBy("Unknown");
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to fetch username for userId: {}", dto.getCreatedBy(), e);
                dto.setCreatedBy("Unknown");
            }
        });
        return map;
    }

    public Page<RTRInterviewDto> getSalesInterviews(String userId,String keyword,Map<String,Object> filters,Pageable pageable){

        return rtrInterviewRepository.salesInterviews(userId, keyword, filters, pageable)
                .map(rtrInterviewMapper::rtrEntityToRTRDto);
    }

    public Page<RTRInterviewDto> getTeamInterviews(String userId,String keyword,Map<String,Object> filters,Pageable pageable){

       List<String> teamConsultants= consultantRepo.findConsultantIdsByTeamLeadId(userId);
      return rtrInterviewRepository.teamInterviews(teamConsultants,keyword,filters,pageable)
               .map(rtrInterviewMapper::rtrEntityToRTRDto);
    }

    public RTRInterviewDto getInterviewsByRtrId(String rtrId){

        return rtrInterviewMapper.rtrEntityToRTRDto(rtrInterviewRepository.findByRtrIdAndIsDeleted(rtrId,false));
    }

    public String generateInterviewId(){
            String lastRtrId=rtrInterviewRepository.findTopByOrderByInterviewIdDesc()
                    .map(RTRInterview::getInterviewId)
                    .orElse("INTER000000");

            int num=Integer.parseInt(lastRtrId.replace("INTER",""))+1;
            return String.format("INTER%06d",num);
    }
}
