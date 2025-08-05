package com.adroit.hotlistmicroservice.controller;

import com.adroit.hotlistmicroservice.dto.ApiResponse;
import com.adroit.hotlistmicroservice.dto.DeleteDocumentDTO;
import com.adroit.hotlistmicroservice.dto.DocumentAddedResponse;
import com.adroit.hotlistmicroservice.dto.DocumentDetailsDTO;
import com.adroit.hotlistmicroservice.model.ConsultantDocument;
import com.adroit.hotlistmicroservice.repo.ConsultantDocumentRepo;
import com.adroit.hotlistmicroservice.service.ConsultantDocumentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = {"http://35.188.150.92", "http://192.168.0.140:3000", "http://192.168.0.139:3000","https://mymulya.com","http://localhost:3000","http://192.168.0.135:8080","http://192.168.0.135",
        "http://182.18.177.16"})
@Slf4j
@RestController
@RequestMapping("/hotlist")
public class ConsultantDocumentController {

    @Autowired
    ConsultantDocumentService consultantDocumentService;

    @GetMapping("/download-document/{documentId}")
    public ResponseEntity<byte[]> downloadDocumentByID(@PathVariable Long documentId) throws IOException, IOException {
        ConsultantDocument consultantDocument = consultantDocumentService.downloadDocument(documentId);
        byte[] documentBytes = consultantDocument.getFileData();

        // Use Tika to detect MIME type from file content
        Tika tika = new Tika();
        String mimeType = tika.detect(new ByteArrayInputStream(documentBytes));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(mimeType));
        headers.setContentDispositionFormData("attachment", consultantDocument.getFileName());

        return new ResponseEntity<>(documentBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/getDocumentDetails/{consultantId}")
    public ResponseEntity<ApiResponse<List<DocumentDetailsDTO>>> getDocumentDetails(
            @PathVariable String consultantId){

        List<DocumentDetailsDTO> response=consultantDocumentService.getDocumentDetails(consultantId);

        ApiResponse<List<DocumentDetailsDTO>> apiResponse=new ApiResponse<>(true,"Document Details Fetched",response,null);
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }
    @DeleteMapping("/deleteDocument/{documentId}")
    public ResponseEntity<ApiResponse<DeleteDocumentDTO>> deleteDocument(
            @PathVariable long documentId
    ){
       DeleteDocumentDTO response=consultantDocumentService.deleteDocument(documentId);
       ApiResponse<DeleteDocumentDTO> apiResponse=new ApiResponse<>(true,"Document Deleted Successful",response,null);

       return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }
    @PostMapping("/addDocument/{consultantId}")
    public ResponseEntity<ApiResponse<DocumentAddedResponse>> addDocument(
            @PathVariable String consultantId,
            @RequestParam(value = "resumes",required = false) List<MultipartFile> resumes,
            @RequestParam(value = "documents",required = false) List<MultipartFile> documents
            ) throws IOException {

        DocumentAddedResponse response=consultantDocumentService.addDocument(consultantId,resumes,documents);

        ApiResponse<DocumentAddedResponse> apiResponse=new ApiResponse<>(true,"New Documents Added",response,null);

        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }
}
