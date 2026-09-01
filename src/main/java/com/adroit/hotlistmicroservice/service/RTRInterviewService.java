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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
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

    @Autowired
    private RestTemplate restTemplate;

    @Value("${user.microservice.url}")
    private String userMicroserviceUrl;

    public InterviewAddedDto scheduleInterview(ScheduleInterviewDto interviewDto, String userId) {

        log.info("Scheduling interview for RTR ID: {} by user: {}", interviewDto.getRtrId(), userId);

        RateTermsConfirmation rtr = rateTermsConfirmationRepository.findById(interviewDto.getRtrId())
                .orElseThrow(() -> new ResourceNotFoundException("NO RTR Found with ID " + interviewDto.getRtrId()));

        if (rtr.getIsDeleted()) {
            throw new ResourceNotFoundException("NO RTR Found with ID " + interviewDto.getRtrId());
        }

        // Check if interview already exists for this RTR
        RTRInterview existingInterview = rtrInterviewRepository.findByRtrIdAndIsDeleted(interviewDto.getRtrId(), false);
        if (existingInterview != null) {
            throw new ResourceNotFoundException("Interview Already Scheduled For RTR ID " + interviewDto.getRtrId());
        }

        RTRInterview interview = rtrInterviewMapper.rtrToRTRInterview(rtr);

        // CRITICAL FIX: Set the sales executive ID from the userId parameter
        interview.setRtrSalesExecutiveId(userId);
        log.info("Setting rtrSalesExecutiveId to: {}", userId);

        // Try to get the user name from user service
        try {
            ResponseEntity<ApiResponse<UserDto>> response = userServiceClient.getUserByUserID(userId);
            ApiResponse<UserDto> apiResponse = response.getBody();
            if (apiResponse != null && apiResponse.getData() != null) {
                interview.setRtrSalesExecutive(apiResponse.getData().getUserName());
                log.info("Set rtrSalesExecutive to: {}", apiResponse.getData().getUserName());
            } else {
                interview.setRtrSalesExecutive(userId);
                log.warn("User service returned null for userId: {}, using userId as name", userId);
            }
        } catch (Exception e) {
            log.warn("Failed to fetch user name for userId: {}, using userId as display name", userId);
            interview.setRtrSalesExecutive(userId);
        }

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

        RTRInterview savedInterview = rtrInterviewRepository.save(interview);
        log.info("Interview saved successfully with ID: {}, rtrSalesExecutiveId: {}",
                savedInterview.getInterviewId(), savedInterview.getRtrSalesExecutiveId());

        return new InterviewAddedDto(savedInterview.getInterviewId(), savedInterview.getRtrId(),
                savedInterview.getConsultantId(), savedInterview.getConsultantName());
    }

    public InterviewAddedDto updateInterview(UpdateInterviewDto updateInterviewDto, String userId) {

        log.info("Updating interview: {} by user: {}", updateInterviewDto.getInterviewId(), userId);

        RTRInterview rtrInterview = getInterviewIsNotDeleted(updateInterviewDto.getInterviewId());

        rtrInterviewMapper.updateInterviewFromDto(updateInterviewDto, rtrInterview);
        rtrInterview.setUpdatedBy(userId);
        rtrInterview.setUpdatedAt(LocalDateTime.now());

        addInterviewHistory(rtrInterview, updateInterviewDto.getInterviewLevel(), updateInterviewDto.getInterviewStatus());

        RTRInterview savedInterview = rtrInterviewRepository.save(rtrInterview);

        return new InterviewAddedDto(savedInterview.getInterviewId(), savedInterview.getRtrId(),
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
                log.warn("Failed to read interview history", e);
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

    public void deleteInterview(String interviewId, String userId) {

        RTRInterview rtrInterview = getInterviewIsNotDeleted(interviewId);

        rtrInterview.setIsDeleted(true);
        rtrInterview.setDeletedBy(userId);
        rtrInterview.setDeletedAt(LocalDateTime.now());

        rtrInterviewRepository.save(rtrInterview);
    }

    public RTRInterviewDto getInterviewById(String interviewId) {
        return rtrInterviewMapper.rtrEntityToRTRDto(getInterviewIsNotDeleted(interviewId));
    }

    public RTRInterview getInterviewIsNotDeleted(String interviewId) {
        log.info("Fetching Interview ID " + interviewId);
        return rtrInterviewRepository.findByInterviewIdAndIsDeleted(interviewId, false)
                .orElseThrow(() -> new ResourceNotFoundException("No Interview Found With ID :" + interviewId));
    }

    public Page<RTRInterviewDto> getAllInterviews(String keyword, Map<String, Object> filters, LocalDate fromDate, LocalDate toDate, Pageable pageable) {

        Page<RTRInterviewDto> map = rtrInterviewRepository.allInterviews(keyword, filters, fromDate, toDate, pageable)
                .map(rtrInterviewMapper::rtrEntityToRTRDto);
        getRTRInterviewDtoWithUserName(map);
        return map;
    }

    public Page<RTRInterviewDto> getSalesInterviews(
            String userId,
            String keyword,
            Map<String, Object> filters,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable) {

        log.info("Fetching sales interviews for userId: {}", userId);

        Map<String, Object> safeFilters = filters == null ? new HashMap<>() : filters;
        Page<RTRInterviewDto> map = rtrInterviewRepository.salesInterviews(
                        userId, keyword, safeFilters, fromDate, toDate, pageable)
                .map(rtrInterviewMapper::rtrEntityToRTRDto);

        log.info("Found {} sales interviews for userId: {}", map.getTotalElements(), userId);

        getRTRInterviewDtoWithUserName(map);
        return map;
    }

    public Page<RTRInterviewDto> getTeamInterviews(
            String userId,
            String keyword,
            Map<String, Object> filters,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable) {

        List<String> teamConsultants = consultantRepo.findConsultantIdsByTeamLeadId(userId);
        Map<String, Object> safeFilters = filters == null ? new HashMap<>() : filters;
        Page<RTRInterviewDto> map = rtrInterviewRepository.teamInterviews(
                        teamConsultants, keyword, safeFilters, fromDate, toDate, pageable)
                .map(rtrInterviewMapper::rtrEntityToRTRDto);
        getRTRInterviewDtoWithUserName(map);
        return map;
    }

    public Page<RTRInterviewDto> getCoordinatorInterviews(
            String userId,
            String keyword,
            Map<String, Object> filters,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable) {

        if (userId == null || userId.isBlank()) {
            throw new ResourceNotFoundException("User ID is required for coordinator interviews");
        }

        Map<String, Object> safeFilters = filters == null ? new HashMap<>() : filters;
        Set<String> teamMemberIds = getCoordinatorTeamMemberIds(userId);
        if (teamMemberIds.isEmpty()) {
            throw new ResourceNotFoundException("No interviews found for coordinator");
        }

        Page<RTRInterviewDto> map = rtrInterviewRepository.coordinatorInterviews(
                        teamMemberIds,
                        keyword,
                        safeFilters,
                        fromDate,
                        toDate,
                        pageable)
                .map(rtrInterviewMapper::rtrEntityToRTRDto);

        getRTRInterviewDtoWithUserName(map);
        return map;
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

    public RTRInterviewDto getInterviewsByRtrId(String rtrId) {
        RTRInterview interview = rtrInterviewRepository.findByRtrIdAndIsDeleted(rtrId, false);
        if (interview == null) {
            throw new ResourceNotFoundException("No Interview Found For RTR ID: " + rtrId);
        }
        return rtrInterviewMapper.rtrEntityToRTRDto(interview);
    }

    public String generateInterviewId() {
        String lastRtrId = rtrInterviewRepository.findTopByOrderByInterviewIdDesc()
                .map(RTRInterview::getInterviewId)
                .orElse("INTER000000");

        int num = Integer.parseInt(lastRtrId.replace("INTER", "")) + 1;
        return String.format("INTER%06d", num);
    }

    public void getRTRInterviewDtoWithUserName(Page<RTRInterviewDto> map) {
        for (RTRInterviewDto dto : map) {
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
        }
    }
    public Page<RTRInterviewDto> getConsultantInterviews(
            String consultantId,
            String keyword,
            Map<String, Object> filters,
            Pageable pageable) {

        Page<RTRInterviewDto> page = rtrInterviewRepository
                .consultantInterviews(
                        consultantId,
                        keyword,
                        filters,
                        pageable)
                .map(rtrInterviewMapper::rtrEntityToRTRDto);

        getRTRInterviewDtoWithUserName(page);

        return page;
    }
}