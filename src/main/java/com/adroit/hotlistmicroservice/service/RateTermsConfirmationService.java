package com.adroit.hotlistmicroservice.service;

import com.adroit.hotlistmicroservice.client.UserServiceClient;
import com.adroit.hotlistmicroservice.dto.*;
import com.adroit.hotlistmicroservice.exception.ResourceNotFoundException;
import com.adroit.hotlistmicroservice.mapper.RateTermsConfirmationMapper;
import com.adroit.hotlistmicroservice.model.RateTermsConfirmation;
import com.adroit.hotlistmicroservice.repo.ConsultantRepo;
import com.adroit.hotlistmicroservice.repo.RateTermsConfirmationRepository;
import com.adroit.hotlistmicroservice.repo.UserDetailsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    @Autowired
    RestTemplate restTemplate;

    @Autowired
    UserDetailsRepository userDetailsRepository;

    @Value("${user.microservice.url}")
    private String userMicroserviceUrl;

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

    public Page<RateTermsConfirmationDTO> getRTRList(String keyword, LocalDateTime fromDate, LocalDateTime toDate, Map<String,Object> filters,Pageable pageable){

        Page<RateTermsConfirmationDTO> map = rtrRepository.allRTRs(keyword, fromDate, toDate, filters, pageable)
                .map(rtrMapper::toDtoFromEntity);
        populateCreatedByName(map);
        return map;
    }

    public Page<RateTermsConfirmationDTO> getCoordinatorRTRList(
            String userId,
            String keyword,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Map<String, Object> filters,
            Pageable pageable) {

        if (userId == null || userId.isBlank()) {
            throw new ResourceNotFoundException("User ID is required for coordinator RTR list");
        }

        Map<String, Object> safeFilters = filters == null ? new HashMap<>() : filters;
        Set<String> teamMemberIds = getCoordinatorTeamMemberIds(userId);
        if (teamMemberIds.isEmpty()) {
            throw new ResourceNotFoundException("No RTRs found for coordinator");
        }

        Page<RateTermsConfirmationDTO> dtoPage = rtrRepository.coordinatorRtrs(
                        teamMemberIds,
                        keyword,
                        fromDate,
                        toDate,
                        safeFilters,
                        pageable)
                .map(rtrMapper::toDtoFromEntity);

        populateCreatedByName(dtoPage);
        return dtoPage;
    }

    public Page<RateTermsConfirmationDTO> getRTRListByDate(String keyword, Map<String,Object> filters, Pageable pageable, String date){

        Page<RateTermsConfirmation> page = rtrRepository.rtrsByDate(keyword, filters, pageable, date);
        Page<RateTermsConfirmationDTO> dtoPage = page.map(rtrMapper::toDtoFromEntity);
        populateCreatedByName(dtoPage);

        return dtoPage;
    }

    public Page<RateTermsConfirmationDTO> getCoordinatorRTRListByDate(
            String userId,
            String keyword,
            Map<String, Object> filters,
            Pageable pageable,
            String date) {

        if (userId == null || userId.isBlank()) {
            throw new ResourceNotFoundException("User ID is required for coordinator RTR list");
        }

        Map<String, Object> safeFilters = filters == null ? new HashMap<>() : filters;
        Set<String> teamMemberIds = getCoordinatorTeamMemberIds(userId);
        if (teamMemberIds.isEmpty()) {
            throw new ResourceNotFoundException("No RTRs found for coordinator");
        }

        Page<RateTermsConfirmationDTO> dtoPage = rtrRepository.coordinatorRtrsByDate(
                        teamMemberIds,
                        keyword,
                        safeFilters,
                        pageable,
                        date)
                .map(rtrMapper::toDtoFromEntity);

        populateCreatedByName(dtoPage);
        return dtoPage;
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

    private Set<String> getCoordinatorTeamMemberIds(String coordinatorUserId) {
        String teamsUrl = userMicroserviceUrl + "/users/AllAssociatedUsers";
        ResponseEntity<TeamDTO[]> teamsResponse = restTemplate.getForEntity(teamsUrl, TeamDTO[].class);
        TeamDTO[] teams = teamsResponse.getBody();

        if (teams == null || teams.length == 0) {
            return Collections.emptySet();
        }

        Set<String> teamMemberIds = new HashSet<>();

        Arrays.stream(teams)
                .filter(team -> isCoordinatorAssignedToTeam(team, coordinatorUserId))
                .forEach(team -> {
                    if (team.getTeamLeadId() != null && !team.getTeamLeadId().isBlank()) {
                        teamMemberIds.add(team.getTeamLeadId());
                    }
                    teamMemberIds.addAll(extractUserIds(team.getRecruiters()));
                    teamMemberIds.addAll(extractUserIds(team.getEmployees()));
                    teamMemberIds.addAll(extractUserIds(team.getSalesExecutives()));
                    teamMemberIds.addAll(extractUserIds(team.getCoordinators()));
                });

        return teamMemberIds;
    }

    private boolean isCoordinatorAssignedToTeam(TeamDTO team, String coordinatorUserId) {
        if (team.getCoordinators() == null) {
            return false;
        }

        return team.getCoordinators().stream()
                .anyMatch(coordinator -> coordinatorUserId.equals(getAssociatedUserId(coordinator)));
    }

    private Set<String> extractUserIds(List<AssociatedUser> users) {
        if (users == null) {
            return Collections.emptySet();
        }

        Set<String> userIds = new HashSet<>();
        users.forEach(user -> {
            String userId = getAssociatedUserId(user);
            if (userId != null && !userId.isBlank()) {
                userIds.add(userId);
            }
        });
        return userIds;
    }

    private String getAssociatedUserId(AssociatedUser user) {
        if (user == null) {
            return null;
        }
        return user.getUserId() != null ? user.getUserId() : user.getEmployeeId();
    }

    private void populateCreatedByName(Page<RateTermsConfirmationDTO> dtoPage) {
        dtoPage.forEach(dto -> {
            dto.setCreatedByName(userDetailsRepository.findUserNameByUserId(dto.getCreatedBy()));
        });
    }

    public Page<RateTermsConfirmationDTO> getSalesRTRListByDate(String userId, String keyword, Map<String, Object> filters, Pageable pageable, String date) {
        return rtrRepository.salesRTRsByDate(userId, keyword, filters, pageable, date)
                .map(rtrMapper::toDtoFromEntity);
    }

    public Page<RateTermsConfirmationDTO> getTeamRtrsByDate(String userId, String keyword, Map<String, Object> filters, Pageable pageable, String date) {
        List<String> teamConsultants = consultantRepo.findConsultantIdsByTeamLeadId(userId);
        return rtrRepository.teamRtrsByDate(teamConsultants, keyword, filters, pageable, date)
                .map(rtrMapper::toDtoFromEntity);
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


    public ApiResponse<UserDto> getUserByUserID(String userId) {
        String url = "http://localhost:8083/"+userId;
        ResponseEntity<ApiResponse<UserDto>> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<ApiResponse<UserDto>>() {},
                        userId
                );

        return response.getBody();
    }
}
