package com.adroit.hotlistmicroservice.controller;

import com.adroit.hotlistmicroservice.dto.*;
import com.adroit.hotlistmicroservice.dto.ApiResponse;
import com.adroit.hotlistmicroservice.exception.ErrorDto;
import com.adroit.hotlistmicroservice.model.C2CDocuments;
import com.adroit.hotlistmicroservice.repo.C2CDocumentsRepo;
import com.adroit.hotlistmicroservice.service.C2CEmployerDetailsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/c2c-employers")
@CrossOrigin(origins = {
        "http://35.188.150.92", "http://192.168.0.140:3000", "http://192.168.0.139:3000",
        "https://mymulya.com", "http://localhost:3000", "http://192.168.0.135:8080",
        "http://192.168.0.135:80", "http://localhost/", "http://mymulya.com:443",
        "http://182.18.177.16:443", "http://localhost/","http://192.168.1.141:3000"
})

public class C2CEmployerDetailsController {

    private static final Logger logger = LoggerFactory.getLogger(C2CEmployerDetailsController.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private C2CEmployerDetailsService service;

    @Autowired
    private C2CDocumentsRepo c2cDocumentsRepo;

        // Create new employer with documents
        @PostMapping("/save-employers/{placementId}")
        public ResponseEntity<ApiResponse<C2CEmployerDetailsDto>> createEmployer(
                @PathVariable("placementId") String placementId,
                @RequestPart("dto") String dtoJson,
                @RequestPart(value = "documents", required = false) List<MultipartFile> files) {

            try {
                C2CEmployerDetailsDto dto = objectMapper.readValue(dtoJson, C2CEmployerDetailsDto.class);
                dto.setPlacementId(placementId);
                C2CEmployerDetailsDto created = service.createEmployer(dto, files);
                return ResponseEntity.ok(new ApiResponse<>(true, "Employer added successfully", created, null));
            } catch (Exception e) {
                ErrorDto error = new ErrorDto(500, e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse<>(false, "Exception", null, error));
            }
        }

    // Get all employers
    @GetMapping("/getAll")
    public ResponseEntity<ApiResponse<List<C2CEmployerDetailsDto>>> getAllEmployers() {
        List<C2CEmployerDetailsDto> employers = service.getAllEmployers();
        return ResponseEntity.ok(new ApiResponse<>(true, "Employers fetched successfully", employers, null));
    }

    // Get employers by placementId
    @GetMapping("/getByPlacementId/{placementId}")
    public ResponseEntity<ApiResponse<List<C2CEmployerDetailsDto>>> getEmployersByPlacementId(
            @PathVariable String placementId) {
        List<C2CEmployerDetailsDto> employers = service.getEmployersByPlacementId(placementId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Employers fetched successfully for placementId: " + placementId, employers, null));
    }

    // Get employer by empId
    @GetMapping("/getByEmpId/{empId}")
    public ResponseEntity<ApiResponse<C2CEmployerDetailsDto>> getEmployerByEmpId(@PathVariable Long empId) {
        C2CEmployerDetailsDto employer = service.getEmployerByEmpId(empId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Employer fetched successfully with empId: " + empId, employer, null));
    }

    @PutMapping(value = "/update/{empId}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<ApiResponse<C2CEmployerDetailsDto>> updateEmployer(
            @PathVariable Long empId,
            @RequestPart("dto") String dtoJson,
            @RequestPart(value = "documents", required = false) List<MultipartFile> files) {

        try {
            C2CEmployerDetailsDto dto = objectMapper.readValue(dtoJson, C2CEmployerDetailsDto.class);
            Optional<C2CEmployerDetailsDto> updatedEmployer = service.updateEmployer(empId, dto, files);

            if (updatedEmployer.isPresent()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Employer updated successfully", updatedEmployer.get(), null));
            } else {
                ErrorDto error = new ErrorDto(HttpStatus.NOT_FOUND.value(), "No employer exists with ID: " + empId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Update failed", null, error));
            }

        } catch (JsonProcessingException e) {
            ErrorDto error = new ErrorDto(HttpStatus.BAD_REQUEST.value(), "Invalid JSON: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Invalid JSON format", null, error));

        } catch (IOException e) {
            ErrorDto error = new ErrorDto(HttpStatus.BAD_REQUEST.value(), "File processing error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "File processing failed", null, error));

        } catch (Exception e) {
            ErrorDto error = new ErrorDto(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Something went wrong: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Unexpected error", null, error));
        }
    }

    @DeleteMapping("/delete/{empId}")
    public ResponseEntity<?> getEmployer(@PathVariable("empId") String empId) {
        boolean deleted = service.deleteEmployer(empId);

        if (deleted) {
            return ResponseEntity.ok(new ApiResponse<>(true, "Employer deleted successfully", null, null));
        } else {
            ErrorDto error = new ErrorDto(HttpStatus.NOT_FOUND.value(), "No employer found with ID: " + empId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Delete failed", null, error));
        }
    }

    @GetMapping("/download/{docId}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long docId) {
        logger.info("Downloading document with ID: {}", docId);

        Optional<C2CDocuments> docOpt = c2cDocumentsRepo.findById(docId);
        if (docOpt.isEmpty()) {
            logger.warn("Document with ID {} not found", docId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        C2CDocuments document = docOpt.get();

        try {
            // Ensure file data exists
            if (document.getFileType() == null || document.getFileType().length == 0) {
                logger.error("Document data is empty for ID: {}", docId);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }

            // Convert byte[] to downloadable resource
            ByteArrayResource resource = new ByteArrayResource(document.getFileType());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"")
                    .contentLength(document.getSize() != null ? document.getSize() : document.getFileType().length)
                    .body(resource);

        } catch (Exception e) {
            logger.error("Error while downloading document ID {}: {}", docId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}

