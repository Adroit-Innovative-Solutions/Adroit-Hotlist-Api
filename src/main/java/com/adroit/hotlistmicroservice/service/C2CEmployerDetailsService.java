package com.adroit.hotlistmicroservice.service;

import com.adroit.hotlistmicroservice.dto.*;
import com.adroit.hotlistmicroservice.model.*;
import com.adroit.hotlistmicroservice.repo.C2CDocumentsRepo;
import com.adroit.hotlistmicroservice.repo.C2CEmployerDetailsRepo;
import com.adroit.hotlistmicroservice.repo.PlacementRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class C2CEmployerDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(C2CEmployerDetailsService.class);

    @Autowired
    private C2CEmployerDetailsRepo employerRepository;

    @Autowired
    private PlacementRepository placementRepository;

    @Autowired
    private C2CDocumentsRepo documentsRepository;

    // ✅ Create new Employer with documents
    @Transactional
    public C2CEmployerDetailsDto createEmployer(C2CEmployerDetailsDto dto, List<MultipartFile> files) throws IOException {
        logger.info("Creating new C2C Employer: {}", dto.getCompanyName());

        C2CEmployerDetails employer = convertToEntity(dto);

        if (files != null && !files.isEmpty()) {
            List<C2CDocuments> documentEntities = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    logger.info("Processing file: {}", file.getOriginalFilename());

                    C2CDocuments doc = new C2CDocuments();
                    doc.setFileName(file.getOriginalFilename());
                    doc.setFileType(file.getBytes());
                    doc.setSize(file.getSize());
                    doc.setCreatedAt(LocalDateTime.now());
                    doc.setUpdatedAt(LocalDateTime.now());
                    doc.setUpdatedBy("System");
                    doc.setPlacementDetails(employer.getPlacementDetails());

                    // ✅ Important fix — link document to employer
                    doc.setEmployer(employer);

                    documentEntities.add(doc);
                }
            }
            employer.setDocuments(documentEntities);
        }

        C2CEmployerDetails saved = employerRepository.save(employer);
        logger.info("Employer saved successfully with ID {}", saved.getEmpId());

        return convertToDTO(saved);
    }

    // ✅ Get all employers (with documents)
    public List<C2CEmployerDetailsDto> getAllEmployers() {
        logger.info("Fetching all C2C employers with their documents...");

        List<C2CEmployerDetails> employers = employerRepository.findAllWithDocuments();
        List<C2CEmployerDetailsDto> dtoList = new ArrayList<>();

        for (C2CEmployerDetails employer : employers) {
            dtoList.add(convertToDTO(employer));
        }
        return dtoList;
    }

    // ✅ Get employers by placementId
    public List<C2CEmployerDetailsDto> getEmployersByPlacementId(String placementId) {
        logger.info("Fetching all employers for placementId: {}", placementId);

        List<C2CEmployerDetails> employers = employerRepository.findByPlacementDetails_PlacementId(placementId);
        List<C2CEmployerDetailsDto> dtoList = new ArrayList<>();

        for (C2CEmployerDetails employer : employers) {
            // Ensure lazy documents are loaded
            if (employer.getDocuments() != null) {
                employer.getDocuments().size();
            }
            dtoList.add(convertToDTO(employer));
        }
        return dtoList;
    }

    // ✅ Get employer by empId
    public C2CEmployerDetailsDto getEmployerByEmpId(Long empId) {
        logger.info("Fetching employer by empId: {}", empId);

        Optional<C2CEmployerDetails> optionalEmployer = employerRepository.findByEmpId(empId);
        if (optionalEmployer.isPresent()) {
            C2CEmployerDetails employer = optionalEmployer.get();
            if (employer.getDocuments() != null) {
                employer.getDocuments().size();
            }
            return convertToDTO(employer);
        }
        return null;
    }

    @Transactional
    public Optional<C2CEmployerDetailsDto> updateEmployer(Long empId, C2CEmployerDetailsDto dto, List<MultipartFile> files) {
        logger.info("Updating employer with ID: {}", empId);

        Optional<C2CEmployerDetails> optionalEmployer = employerRepository.findByEmpId(empId);
        if (optionalEmployer.isEmpty()) {
            return Optional.empty();
        }

        C2CEmployerDetails existing = optionalEmployer.get();

        // 🔹 Update non-null fields
        if (dto.getCompanyName() != null) existing.setCompanyName(dto.getCompanyName());
        if (dto.getCompanyFullAddress() != null) existing.setCompanyFullAddress(dto.getCompanyFullAddress());
        if (dto.getFederalId() != null) existing.setFederalId(dto.getFederalId());
        if (dto.getNetTerms() != null) existing.setNetTerms(dto.getNetTerms());
        if (dto.getSigningAuthority() != null) existing.setSigningAuthority(dto.getSigningAuthority());
        if (dto.getSigningAuthorityTitle() != null) existing.setSigningAuthorityTitle(dto.getSigningAuthorityTitle());
        if (dto.getEmailId() != null) existing.setEmailId(dto.getEmailId());
        if (dto.getPhoneNo() != null) existing.setPhoneNo(dto.getPhoneNo());
        if (dto.getWebsite() != null) existing.setWebsite(dto.getWebsite());
        if (dto.getPocOfAccountsPerson() != null) existing.setPocOfAccountsPerson(dto.getPocOfAccountsPerson());
        if (dto.getPocEmailId() != null) existing.setPocEmailId(dto.getPocEmailId());
        if (dto.getPocPhoneNumber() != null) existing.setPocPhoneNumber(dto.getPocPhoneNumber());
        if (dto.getBankDetails() != null) existing.setBankDetails(dto.getBankDetails());
        if (dto.getBankName() != null) existing.setBankName(dto.getBankName());
        if (dto.getBankCity() != null) existing.setBankCity(dto.getBankCity());
        if (dto.getBankState() != null) existing.setBankState(dto.getBankState());
        if (dto.getBankZipCode() != null) existing.setBankZipCode(dto.getBankZipCode());
        if (dto.getRoutingNumber() != null) existing.setRoutingNumber(dto.getRoutingNumber());
        if (dto.getBankAccountNumber() != null) existing.setBankAccountNumber(dto.getBankAccountNumber());
        if (dto.getAchRoutingCode() != null) existing.setAchRoutingCode(dto.getAchRoutingCode());
        if (dto.getWireRoutingCode() != null) existing.setWireRoutingCode(dto.getWireRoutingCode());
        if (dto.getAccountType() != null) existing.setAccountType(dto.getAccountType());

        // 🔹 Placement relation
        if (dto.getPlacementId() != null) {
            existing.setPlacementDetails(
                    placementRepository.findById(dto.getPlacementId()).orElse(null)
            );
        }

        // 🔹 Handle file uploads
        try {
            if (files != null && !files.isEmpty()) {

                // ✅ Always ensure list initialized
                if (existing.getDocuments() == null) {
                    existing.setDocuments(new ArrayList<>());
                }

                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        C2CDocuments doc = new C2CDocuments();
                        doc.setFileName(file.getOriginalFilename());
                        doc.setFileType(file.getBytes());
                        doc.setSize(file.getSize());
                        doc.setCreatedAt(LocalDateTime.now()); // ✅ ensure createdAt is set
                        doc.setUpdatedAt(LocalDateTime.now());
                        doc.setUpdatedBy("System");
                        doc.setPlacementDetails(existing.getPlacementDetails());
                        doc.setEmployer(existing); // ✅ ensure emp_id is set

                        existing.getDocuments().add(doc);
                    }
                }

                // ✅ Force persistence of new docs
                documentsRepository.saveAll(existing.getDocuments());
            }
        } catch (IOException e) {
            logger.error("Error uploading files for employer update: {}", e.getMessage());
        }

        // ✅ Persist updates
        C2CEmployerDetails saved = employerRepository.save(existing);

        return Optional.of(convertToDTO(saved));
    }


    // ✅ Delete employer
    @Transactional
    public boolean deleteEmployer(String empId) {
        logger.info("Deleting employer with ID: {}", empId);

        Long employerId;
        try {
            employerId = Long.valueOf(empId);
        } catch (NumberFormatException e) {
            logger.error("Invalid employer ID format: {}", empId);
            return false;
        }

        Optional<C2CEmployerDetails> optionalEmployer = employerRepository.findByEmpId(employerId);
        if (optionalEmployer.isEmpty()) {
            logger.warn("No employer found with ID: {}", empId);
            return false;
        }

        C2CEmployerDetails employer = optionalEmployer.get();

        // 🔹 Explicitly clear documents to trigger orphan removal
        if (employer.getDocuments() != null && !employer.getDocuments().isEmpty()) {
            logger.info("Removing {} associated documents for employer ID: {}", employer.getDocuments().size(), empId);
            employer.getDocuments().clear();
            employerRepository.saveAndFlush(employer); // ✅ ensures orphan removal in DB before parent delete
        }

        // 🔹 Delete employer
        employerRepository.delete(employer);
        employerRepository.flush(); // ✅ ensures immediate DB deletion

        logger.info("Employer and associated documents deleted successfully for ID: {}", empId);
        return true;
    }


    // ✅ Convert Entity → DTO
    private C2CEmployerDetailsDto convertToDTO(C2CEmployerDetails entity) {
        C2CEmployerDetailsDto dto = new C2CEmployerDetailsDto();

        dto.setEmpId(entity.getEmpId());
        dto.setCompanyName(entity.getCompanyName());
        dto.setCompanyFullAddress(entity.getCompanyFullAddress());
        dto.setFederalId(entity.getFederalId());
        dto.setNetTerms(entity.getNetTerms());
        dto.setSigningAuthority(entity.getSigningAuthority());
        dto.setSigningAuthorityTitle(entity.getSigningAuthorityTitle());
        dto.setEmailId(entity.getEmailId());
        dto.setPhoneNo(entity.getPhoneNo());
        dto.setWebsite(entity.getWebsite());
        dto.setPocOfAccountsPerson(entity.getPocOfAccountsPerson());
        dto.setPocEmailId(entity.getPocEmailId());
        dto.setPocPhoneNumber(entity.getPocPhoneNumber());
        dto.setBankDetails(entity.getBankDetails());
        dto.setBankName(entity.getBankName());
        dto.setBankCity(entity.getBankCity());
        dto.setBankState(entity.getBankState());
        dto.setBankZipCode(entity.getBankZipCode());
        dto.setRoutingNumber(entity.getRoutingNumber());
        dto.setBankAccountNumber(entity.getBankAccountNumber());
        dto.setAchRoutingCode(entity.getAchRoutingCode());
        dto.setWireRoutingCode(entity.getWireRoutingCode());
        dto.setAccountType(entity.getAccountType());

        if (entity.getPlacementDetails() != null) {
            dto.setPlacementId(entity.getPlacementDetails().getPlacementId());
        }

        // ✅ Convert documents
        List<C2CDocumentsDto> docDtos = new ArrayList<>();
        if (entity.getDocuments() != null) {
            for (C2CDocuments doc : entity.getDocuments()) {
                C2CDocumentsDto docDto = new C2CDocumentsDto();
                docDto.setDocId(doc.getDocId());
                docDto.setFileName(doc.getFileName());
                docDto.setSize(doc.getSize());
                docDto.setUpdatedAt(doc.getUpdatedAt());
                docDto.setUpdatedBy(doc.getUpdatedBy());
                docDtos.add(docDto);
            }
        }

        dto.setDocuments(docDtos);
        return dto;
    }

    // ✅ Convert DTO → Entity
    private C2CEmployerDetails convertToEntity(C2CEmployerDetailsDto dto) {
        C2CEmployerDetails entity = new C2CEmployerDetails();

        entity.setCompanyName(dto.getCompanyName());
        entity.setCompanyFullAddress(dto.getCompanyFullAddress());
        entity.setFederalId(dto.getFederalId());
        entity.setNetTerms(dto.getNetTerms());
        entity.setSigningAuthority(dto.getSigningAuthority());
        entity.setSigningAuthorityTitle(dto.getSigningAuthorityTitle());
        entity.setEmailId(dto.getEmailId());
        entity.setPhoneNo(dto.getPhoneNo());
        entity.setWebsite(dto.getWebsite());
        entity.setPocOfAccountsPerson(dto.getPocOfAccountsPerson());
        entity.setPocEmailId(dto.getPocEmailId());
        entity.setPocPhoneNumber(dto.getPocPhoneNumber());
        entity.setBankDetails(dto.getBankDetails());
        entity.setBankName(dto.getBankName());
        entity.setBankCity(dto.getBankCity());
        entity.setBankState(dto.getBankState());
        entity.setBankZipCode(dto.getBankZipCode());
        entity.setRoutingNumber(dto.getRoutingNumber());
        entity.setBankAccountNumber(dto.getBankAccountNumber());
        entity.setAchRoutingCode(dto.getAchRoutingCode());
        entity.setWireRoutingCode(dto.getWireRoutingCode());
        entity.setAccountType(dto.getAccountType());

        // ✅ Attach placement
        entity.setPlacementDetails(
                placementRepository.findById(dto.getPlacementId()).orElse(null)
        );

        return entity;
    }
}
