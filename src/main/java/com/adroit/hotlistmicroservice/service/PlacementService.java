package com.adroit.hotlistmicroservice.service;
import com.adroit.hotlistmicroservice.client.TimesheetClient;
import com.adroit.hotlistmicroservice.client.UserServiceClient;
import com.adroit.hotlistmicroservice.config.EmailService;
import com.adroit.hotlistmicroservice.dto.*;
import com.adroit.hotlistmicroservice.exception.CandidateAlreadyExistsException;
import com.adroit.hotlistmicroservice.exception.CandidateNotFoundException;
import com.adroit.hotlistmicroservice.exception.ResourceNotFoundException;
import com.adroit.hotlistmicroservice.exception.UserNotFoundException;
import com.adroit.hotlistmicroservice.model.PlacementDetails;
import com.adroit.hotlistmicroservice.repo.PlacementRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class PlacementService {

    @Autowired
    private PlacementRepository placementRepository;

    @Autowired
    private UserServiceClient userClient;

    @Autowired
    private EmailService emailregisterService;

    @Autowired
    private TimesheetClient timesheetClient;


    private static final Logger logger = LoggerFactory.getLogger(PlacementService.class);



    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    private String generateCustomId() {
        List<Integer> existingNumbers = placementRepository.findAll().stream()
                .map(PlacementDetails::getPlacementId)
                .filter(id -> id != null && id.matches("PLMNT\\d{4}"))
                .map(id -> Integer.parseInt(id.replace("PLMNT", "")))
                .toList();

        int nextNumber = existingNumbers.stream().max(Integer::compare).orElse(0) + 1;
        return String.format("PLMNT%04d", nextNumber);
    }


    @Transactional
    public PlacementResponseDto savePlacement(PlacementDto placementDto) {
        PlacementDetails placementDetails = convertToEntity(placementDto);

        processCashTermsAndNetPay(placementDetails);

        // Check for existing placement by candidateContactNo and clientName
        PlacementDetails existingPlacement = placementRepository
                .findByConsultantContactNoAndClientName(placementDto.getConsultantContactNo(), placementDto.getClientName());

        if (existingPlacement != null) {
            throw new CandidateAlreadyExistsException("Placement already exists for this candidate and client.");
        }
        // Generate custom ID
        placementDetails.setPlacementId(generateCustomId());
        logger.info("Generated ID is: {}", placementDetails.getPlacementId());

        // ✅ Set defaults if not provided
        if (placementDetails.getEmployeeWorkingType() == null) {
            placementDetails.setEmployeeWorkingType("MONTHLY");
        }
        if (placementDetails.getProjectStatus() == null) {
            placementDetails.setProjectStatus("Active");
        }

        // Calculate margin using the service method
        placementDetails.setMargin(calculateMargin(placementDetails));

        //Save entity
        PlacementDetails saved = placementRepository.save(placementDetails);
        boolean isPlaced = "Active".equalsIgnoreCase(saved.getProjectStatus());
        //return response DTO
        return new PlacementResponseDto(
                saved.getPlacementId(),
                saved.getConsultantFullName(),
                saved.getConsultantContactNo(),
                isPlaced
        );
    }



    public PlacementResponseDto updatePlacement(String id, PlacementDto dto) {
        PlacementDetails existing = placementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Placement not found with ID: " + id));

        // Update basic candidate info
        if (dto.getConsultantEmailId() != null && !dto.getConsultantEmailId().equals(existing.getConsultantEmailId())) {
            existing.setConsultantEmailId(dto.getConsultantEmailId());
        }
        if (dto.getConsultantContactNo() != null && !dto.getConsultantContactNo().equals(existing.getConsultantContactNo())) {
            existing.setConsultantContactNo(dto.getConsultantContactNo());
        }
        Optional.ofNullable(dto.getConsultantFullName()).ifPresent(existing::setConsultantFullName);
        Optional.ofNullable(dto.getTechnology()).ifPresent(existing::setTechnology);
        Optional.ofNullable(dto.getClientName()).ifPresent(existing::setClientName);
        Optional.ofNullable(dto.getVendorName()).ifPresent(existing::setVendorName);
        Optional.ofNullable(dto.getStartDate()).ifPresent(start -> existing.setStartDate(LocalDate.parse(start, formatter)));
        Optional.ofNullable(dto.getEndDate()).ifPresent(end -> existing.setEndDate(LocalDate.parse(end, formatter)));
        Optional.ofNullable(dto.getRecruiterName()).ifPresent(existing::setRecruiterName);
        Optional.ofNullable(dto.getSales()).ifPresent(existing::setSales);
        Optional.ofNullable(dto.getEmploymentType()).ifPresent(existing::setEmploymentType);
        Optional.ofNullable(dto.getRemarks()).ifPresent(existing::setRemarks);
        Optional.ofNullable(dto.getProjectStatus()).ifPresent(existing::setProjectStatus);
        Optional.ofNullable(dto.getStatusMessage()).ifPresent(existing::setStatusMessage);

        // Update financials
        Optional.ofNullable(dto.getPayRate()).ifPresent(existing::setPayRate);
        Optional.ofNullable(dto.getBillRateFromClient()).ifPresent(existing::setBillRateFromClient);
        Optional.ofNullable(dto.getBillRateToConsultant()).ifPresent(existing::setBillRateToConsultant);
        Optional.ofNullable(dto.getCashTerms()).ifPresent(existing::setCashTerms);
        Optional.ofNullable(dto.getReferals()).ifPresent(existing::setReferals);

        // Recalculate margin using service method
        existing.setMargin(calculateMargin(existing));

        // Recalculate cashTerms formatting and netPay
        processCashTermsAndNetPay(existing);

        // Update employee working type if provided
        Optional.ofNullable(dto.getEmployeeWorkingType()).ifPresent(existing::setEmployeeWorkingType);

        PlacementDetails updated = placementRepository.save(existing);
        return convertToResponseDto(updated);
    }


    public void deletePlacement(String id) {
        if (!placementRepository.existsById(id)) {
            throw new ResourceNotFoundException("Placement not found with ID: " + id);
        }
        placementRepository.deleteById(id);
    }

    public PlacementResponseDto getPlacementById(String id) {
        PlacementDetails placement = placementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Placement not found with ID: " + id));

        LocalDate now = LocalDate.now();
        if ("active".equalsIgnoreCase(placement.getProjectStatus()) &&
                placement.getEndDate() != null &&
                now.isAfter(placement.getEndDate())) {

            placement.setProjectStatus("completed");
            placementRepository.save(placement);
        }

        return convertToResponseDto(placement);
    }

    public List<PlacementDetails> getAllPlacements(LocalDate startDate, LocalDate endDate) {
        LocalDate now = LocalDate.now();

        logger.info("Fetching placements between {} and {}", startDate, endDate);

        // Fetch by placement startDate between given dates
        List<PlacementDetails> allPlacements = placementRepository.findPlacementsByCreatedAtOrStartDateBetween(startDate, endDate);
        logger.info("Total placements found: {}", allPlacements.size());

        List<PlacementDetails> updatedPlacements = new ArrayList<>();
        for (PlacementDetails placement : allPlacements) {
            // Update status if endDate has passed and status is still active
            if ("active".equalsIgnoreCase(placement.getProjectStatus()) &&
                    placement.getEndDate() != null &&
                    now.isAfter(placement.getEndDate())) {

                placement.setProjectStatus("completed");
                placementRepository.save(placement); // Save the change
            }
            if (!"inactive".equalsIgnoreCase(placement.getProjectStatus())) {
                boolean isLogin = false;
                String candidateEmail = placement.getConsultantEmailId();

                if (candidateEmail != null && !candidateEmail.isEmpty()) {
                    try {
                        // Fetch user by email to get userId
                        ResponseEntity<ApiResponse<UserDetailsDTO>> userResp = userClient.getUserByEmail(candidateEmail);

                        if (userResp.getBody() != null && userResp.getBody().getData() != null) {
                            String userId = userResp.getBody().getData().getUserId();

                            if (userId != null && !userId.isEmpty()) {
                                // Fetch login status by userId
                                ResponseEntity<ApiResponse<UserLoginStatusDTO>> loginResp = userClient.getLoginStatusByUserId(userId);

                                if (loginResp.getBody() != null && loginResp.getBody().getData() != null) {
                                    isLogin = loginResp.getBody().getData().isLogin();
                                }
                                System.out.println(isLogin);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Error fetching login status for candidateEmail {}: {}", candidateEmail, e.getMessage());
                    }
                }
                placement.setLogin(isLogin);  // Transient field, not persisted
                updatedPlacements.add(placement);
            }
        }
        logger.info("Filtered placements count: {}", updatedPlacements.size());
        return updatedPlacements;
    }

    public List<PlacementDetails> getPlacementsByCandidateEmail(String email) {
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);             // 1st of current month
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth()); // last day of current month

        logger.info("Fetching placements with candidateEmailId={} between {} and {}", email);
        // Fetch placements filtered by email and date range (implement this repo method)
        List<PlacementDetails> placements = placementRepository
                .findByConsultantEmailId(email);

        logger.info("Placements found: {}", placements.size());

        List<PlacementDetails> filteredPlacements = new ArrayList<>();

        for (PlacementDetails placement : placements) {
            // Update status if needed
            if ("active".equalsIgnoreCase(placement.getProjectStatus()) &&
                    placement.getEndDate() != null &&
                    now.isAfter(placement.getEndDate())) {

                placement.setProjectStatus("completed");
                placementRepository.save(placement);
            }
            if (!"inactive".equalsIgnoreCase(placement.getProjectStatus())) {
                filteredPlacements.add(placement);
            }
        }
        logger.info("Filtered placements count: {}", filteredPlacements.size());

        return filteredPlacements;
    }

    public List<PlacementDetails> getPlacementsByCandidateEmailAndDateRange(String email, LocalDate startDate, LocalDate endDate) {
        LocalDate now = LocalDate.now();
        LocalDate endOfCurrentMonth = now.withDayOfMonth(now.lengthOfMonth());

        // Current/future month logic: show this month's and future placements
        List<PlacementDetails> placements = placementRepository
                .findByConsultantEmailIdAndCreatedAtBetween(email, startDate, endDate);

        List<PlacementDetails> filteredPlacements = new ArrayList<>();

        for (PlacementDetails placement : placements) {
            if ("active".equalsIgnoreCase(placement.getProjectStatus())
                    && placement.getEndDate() != null
                    && now.isAfter(placement.getEndDate())) {
                placement.setProjectStatus("completed");
                placementRepository.save(placement);
            }
            if (!"inactive".equalsIgnoreCase(placement.getProjectStatus())) {
                filteredPlacements.add(placement);
            }
        }
        return filteredPlacements;
    }


    private PlacementResponseDto convertToResponseDto(PlacementDetails updated) {
        return new PlacementResponseDto(
                updated.getPlacementId(),
                updated.getConsultantFullName(),
                updated.getConsultantContactNo()

        );
    }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private PlacementDetails convertToEntity(PlacementDto dto) {
        PlacementDetails entity = new PlacementDetails();

        // Basic Info
        entity.setConsultantFullName(dto.getConsultantFullName());
        entity.setConsultantContactNo(dto.getConsultantContactNo());
        entity.setConsultantEmailId(dto.getConsultantEmailId());
        entity.setConsultantId(dto.getConsultantId());
        entity.setTechnology(dto.getTechnology());
        entity.setClientName(dto.getClientName());
        entity.setVendorName(dto.getVendorName());

        // Dates (handle parsing safely)
        if (dto.getStartDate() != null && !dto.getStartDate().isBlank()) {
            entity.setStartDate(LocalDate.parse(dto.getStartDate(), FORMATTER));
        }
        if (dto.getEndDate() != null && !dto.getEndDate().isBlank()) {
            entity.setEndDate(LocalDate.parse(dto.getEndDate(), FORMATTER));
        }

        // Sales & Recruiting
        entity.setSales(dto.getSales());
        entity.setSalesExecutiveId(dto.getSalesExecutiveId());
        entity.setSalesExecutiveName(dto.getSalesExecutiveName());
        entity.setSalesTeamLeadId(dto.getSalesTeamLeadId());
        entity.setSalesTeamLeadName(dto.getSalesTeamLeadName());
        entity.setRecruiterId(dto.getRecruiterId());
        entity.setRecruiterName(dto.getRecruiterName());
        entity.setRecruiterTeamLead(dto.getRecruiterTeamLead());

        // Financials
        entity.setPayRate(dto.getPayRate());
        entity.setBillRateFromClient(dto.getBillRateFromClient());
        entity.setBillRateToConsultant(dto.getBillRateToConsultant());
        entity.setMargin(dto.getMargin());
        entity.setReferals(dto.getReferals());
        entity.setPo_with(dto.getPo_with());
        entity.setProjectBy(dto.getProjectBy());
        entity.setBranch(dto.getBranch());
        entity.setClosedMonth(dto.getClosedMonth());
        entity.setEmploymentType(dto.getEmploymentType());
        entity.setVisaSubmitted(dto.getVisaSubmitted());
        entity.setOriginalVisa(dto.getOriginalVisa());
        entity.setC2cEmployerDetails(dto.getC2cEmployerDetails());
        entity.setCashTerms(dto.getCashTerms());
        entity.setNetPay(dto.getNetPay());

        // Project details
        entity.setProjectStatus(dto.getProjectStatus() != null ? dto.getProjectStatus() : "Active");
        entity.setStatusMessage(dto.getStatusMessage());
        entity.setRemarks(dto.getRemarks());
        entity.setInterviewId(dto.getInterviewId());

        // Defaults and extra fields
        entity.setEmployeeWorkingType(
                dto.getEmployeeWorkingType() != null ? dto.getEmployeeWorkingType() : "MONTHLY"
        );
        entity.setRegister(dto.isRegister());
        entity.setHourlyRate(dto.getHourlyRate() != null ? dto.getHourlyRate() : BigDecimal.ZERO);

        // CreatedAt — fallback to current date if null
        entity.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDate.now());

        return entity;
    }


    public Map<String, Long> getCounts(String recruiterId) {
        YearMonth currentMonth = YearMonth.now(); // current month, e.g., 2025-07
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59, 999_999_999);

        Object[] result;

        if (recruiterId != null && !recruiterId.isBlank()) {
            result = (Object[]) placementRepository.getAllCountsByRecruiterByDateRange(startOfMonth, endOfMonth, recruiterId);
        } else {
            result = (Object[]) placementRepository.getAllCountsByDateRange(startOfMonth, endOfMonth, recruiterId);
        }

        Map<String, Long> counts = new LinkedHashMap<>();

        // Index mapping based on the SELECT order in both queries
        counts.put("requirements", ((Number) result[0]).longValue());
        counts.put("candidates", ((Number) result[1]).longValue());
        counts.put("clients", ((Number) result[2]).longValue());
        counts.put("contractPlacements", ((Number) result[3]).longValue());
        counts.put("fulltimePlacements", ((Number) result[4]).longValue());
        counts.put("bench", ((Number) result[5]).longValue());
        counts.put("users", ((Number) result[6]).longValue());
        counts.put("interviews", ((Number) result[7]).longValue());
        counts.put("internalInterviews", ((Number) result[8]).longValue());
        counts.put("externalInterviews", ((Number) result[9]).longValue());
        counts.put("assigned", ((Number) result[10]).longValue());

        return counts;
    }

    public List<PlacementDetails> getPlacementsByDateRangeWithLoginStatus(LocalDate startDate, LocalDate endDate) {
        LocalDate now = LocalDate.now();
        LocalDate endOfCurrentMonth = now.withDayOfMonth(now.lengthOfMonth());

        List<PlacementDetails> placements = placementRepository
                //.findPlacementsByStartDateBetweenOrStartDateAfter(startDate, endDate, endOfCurrentMonth);
                .findPlacementsByCreatedAtOrStartDateBetween(startDate,endDate);

        List<PlacementDetails> enrichedPlacements = new ArrayList<>();
        for (PlacementDetails placement : placements) {
            if ("active".equalsIgnoreCase(placement.getProjectStatus())
                    && placement.getEndDate() != null
                    && now.isAfter(placement.getEndDate())) {
                placement.setProjectStatus("completed");
                placementRepository.save(placement);
            }
            if (!"inactive".equalsIgnoreCase(placement.getProjectStatus())) {
                boolean isLogin = false;
                String candidateEmail = placement.getConsultantEmailId();
                if (candidateEmail != null && !candidateEmail.isEmpty()) {
                    try {
                        ResponseEntity<ApiResponse<UserDetailsDTO>> userResp = userClient.getUserByEmail(candidateEmail);
                        if (userResp.getBody() != null && userResp.getBody().getData() != null) {
                            String userId = userResp.getBody().getData().getUserId();
                            if (userId != null && !userId.isEmpty()) {
                                ResponseEntity<ApiResponse<UserLoginStatusDTO>> loginResp = userClient.getLoginStatusByUserId(userId);
                                if (loginResp.getBody() != null && loginResp.getBody().getData() != null) {
                                    isLogin = loginResp.getBody().getData().isLogin();
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Error fetching login status for candidateEmail {}: {}", candidateEmail, e.getMessage());
                    }
                }
                placement.setLogin(isLogin);
                enrichedPlacements.add(placement);
            }
        }
        return enrichedPlacements;
    }





    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String generateNextUserId() {
        String sql = """
        SELECT CONCAT(
            'ADRTIN',
            LPAD(COALESCE(MAX(CAST(SUBSTRING(user_id, 7) AS UNSIGNED)), 0) + 1, 4, '0')
        ) AS next_user_id
        FROM user_details
        WHERE user_id LIKE 'ADRTIN%'
    """;
        return jdbcTemplate.queryForObject(sql, String.class);
    }

    public UserDetailsDTO createUserFromExistingPlacement(String placementId) {
        PlacementDetails placement = placementRepository.findById(placementId)
                .orElseThrow(() -> {
                    logger.error("Placement not found with ID: {}", placementId);
                    return new RuntimeException("Placement not found with ID: " + placementId);
                });

        UserDetailsDTO userDto = new UserDetailsDTO();
        userDto.setUserId(generateNextUserId());
        userDto.setUserName(placement.getConsultantFullName());
        userDto.setEmail(placement.getConsultantEmailId());

        // Set additional fields
        userDto.setPersonalemail(placement.getConsultantEmailId());
        userDto.setPhoneNumber(placement.getConsultantContactNo());
        userDto.setDob("1990-01-01");
        userDto.setGender("male");
        userDto.setJoiningDate(LocalDate.parse("2025-08-06")); // Ideally dynamically set
        userDto.setDesignation("Candidate");
        userDto.setStatus("ACTIVE");
        userDto.setRoles(Collections.singleton("EXTERNALEMPLOYEE"));
        userDto.setEntity("IN");

        // Generate random 8-character password
        String randomPassword = PasswordGenerator.generateRandomPassword(8);
        userDto.setPassword(randomPassword);
        userDto.setConfirmPassword(randomPassword);

        try {
            logger.info("Attempting to register user: {}", userDto.getUserId());
            ResponseEntity<ApiResponse<UserDetailsDTO>> response = userClient.registerUser(userDto);
            
            ApiResponse<UserDetailsDTO> apiResponse = response.getBody();
            if (response.getStatusCode().is2xxSuccessful() && apiResponse != null && apiResponse.isSuccess()) {
                // Save placement with register=true
                placement.setRegister(true);
                placementRepository.save(placement);
                logger.info("Placement updated with register=true for placementId: {}", placementId);

                // --- Always Initialize Leave ---
                EmployeeLeaveSummaryDto leaveInitDto = new EmployeeLeaveSummaryDto();
                leaveInitDto.setUserId(userDto.getUserId());
                leaveInitDto.setEmployeeName(userDto.getUserName());
                leaveInitDto.setEmployeeType(placement.getEmploymentType());
                leaveInitDto.setJoiningDate(placement.getStartDate()); // or use userDto.getJoiningDate()
                leaveInitDto.setUpdatedBy(placement.getConsultantFullName());

                logger.info("Calling timesheet microservice to initialize leave for userId: {}", userDto.getUserId());
                ApiResponse<EmployeeLeaveSummaryDto> leaveResponse = timesheetClient.initializeLeave(leaveInitDto);

                if (leaveResponse == null || !leaveResponse.isSuccess()) {
                    String errorCode = leaveResponse != null && leaveResponse.getError() != null
                            ? String.valueOf(leaveResponse.getError().getErrorCode())
                            : "UNKNOWN_ERROR";
                    String errorMessage = leaveResponse != null && leaveResponse.getError() != null
                            ? leaveResponse.getError().getErrorMessage()
                            : "Leave initialization failed";
                    logger.error("Leave initialization failed with error code: {}, message: {}", errorCode, errorMessage);
                    throw new RuntimeException("Leave initialization failed with error code: " + errorCode + ", message: " + errorMessage);
                } else {
                    logger.info("Leave initialized successfully for userId: {}", userDto.getUserId());
                }

                // Send password email ONLY after leave is successfully initialized
                emailregisterService.sendPasswordEmailHtml(userDto.getEmail(), userDto.getUserName(), randomPassword);
                logger.info("Password email sent to: {}", userDto.getEmail());

                logger.info("User registration succeeded for userId: {}", userDto.getUserId());
            } else {
                String errorCode = apiResponse != null && apiResponse.getError() != null
                        ? String.valueOf(apiResponse.getError().getErrorCode())
                        : "UNKNOWN_ERROR";
                String errorMessage = apiResponse != null && apiResponse.getError() != null
                        ? apiResponse.getError().getErrorMessage()
                        : "User registration failed";
                logger.error("User registration failed with error code: {}, message: {}", errorCode, errorMessage);
                throw new RuntimeException("User creation failed with error code: " + errorCode + ", message: " + errorMessage);
            }
        } catch (Exception e) {
            logger.error("Exception occurred during user creation", e);
            throw e; // Re-throw to ensure the caller knows it failed
        }

        return userDto;
    }

    public List<String> getAllVendorNames() {
        List<PlacementDetails> placements = placementRepository.findAll();

        return placements.stream()
                .map(PlacementDetails::getVendorName)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    //helper method to calculate margin
    private BigDecimal calculateMargin(PlacementDetails placement) {
        BigDecimal billRate = placement.getBillRateFromClient() != null
                ? placement.getBillRateFromClient()
                : BigDecimal.ZERO;
        BigDecimal pay = placement.getPayRate() != null
                ? placement.getPayRate()
                : BigDecimal.ZERO;
        BigDecimal referral = placement.getReferals() != null
                ? placement.getReferals()
                : BigDecimal.ZERO;

        return billRate.subtract(pay).subtract(referral);
    }

    private void processCashTermsAndNetPay(PlacementDetails placement) {
        BigDecimal payRate = placement.getPayRate();
        BigDecimal cashTerms = placement.getCashTerms();
        String employmentType = placement.getEmploymentType();

        // Handle employmentType logic
        if (employmentType == null || !employmentType.equalsIgnoreCase("CASH")) {
            // For non-CASH types, cashTerms should be null and netPay = payRate
            placement.setCashTerms(null);

            if (payRate != null) {
                placement.setNetPay(payRate.setScale(2, BigDecimal.ROUND_HALF_UP));
            } else {
                placement.setNetPay(null);
            }

            return;
        }

        // For CASH employment type, calculate netPay based on cashTerms
        if (payRate != null && cashTerms != null) {
            BigDecimal percentage = cashTerms.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            BigDecimal netPay = payRate.subtract(payRate.multiply(percentage));
            placement.setNetPay(netPay.setScale(2, RoundingMode.HALF_UP));
        } else if (payRate != null) {
            // If cashTerms missing but payRate present, assume 0%
            placement.setNetPay(payRate.setScale(2, RoundingMode.HALF_UP));
        } else {
            placement.setNetPay(null);
        }
    }

}
