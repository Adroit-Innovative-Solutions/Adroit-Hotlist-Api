package com.adroit.hotlistmicroservice.controller;

import com.adroit.hotlistmicroservice.client.UserServiceClient;
import com.adroit.hotlistmicroservice.dto.*;
import com.adroit.hotlistmicroservice.service.ConsultantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@CrossOrigin(origins = "http://192.168.0.139:3000")
@RestController
@RequestMapping("/hotlist")
public class ConsultantController {

    @Autowired
    private ConsultantService consultantService;
    @Autowired
    private UserServiceClient userServiceClient;

    private static final Logger logger = LoggerFactory.getLogger(ConsultantController.class);

    @PostMapping("/addConsultant")
    public ResponseEntity<ApiResponse<ConsultantAddedResponse>> addConsultant(
            @ModelAttribute ConsultantDto hotList,
            @RequestParam(value = "resumes",required = false) List<MultipartFile> resumes,
            @RequestParam(value = "documents",required = false) List<MultipartFile> documents
    ) throws IOException {

        logger.info("resumes ------------------------>>:: {}",resumes);
        ConsultantAddedResponse consultantResponse = consultantService.addConsultant(hotList, resumes, documents);
        ApiResponse<ConsultantAddedResponse> response=new ApiResponse<>(true,"Consultant Created",consultantResponse,null);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
     @GetMapping("/allConsultants")
     public ResponseEntity<ApiResponse<PageResponse<ConsultantDto>>> getAllConsultants(
            @RequestParam (defaultValue = "0") int page,
            @RequestParam (defaultValue = "10") int size
     ){
         Pageable pageable= PageRequest.of(
                 page, size
         , Sort.Direction.DESC,"updatedTimeStamp");

      Page<ConsultantDto> hotList= consultantService.getAllConsultants(pageable);
      PageResponse<ConsultantDto> pageResponse=new PageResponse<>(hotList);
      ApiResponse<PageResponse<ConsultantDto>> response=new ApiResponse<>(true,"HotList data fetched.",pageResponse,null);

      return new ResponseEntity<>(response,HttpStatus.OK);
    }
//    @PostMapping("/addManually")
//    public String addConsultManually(@RequestBody List<ConsultantDto> list){
//
//        return hotListService.addConsultantManually(list);
//
//    }
//    @PostMapping("/addConsultant")
//    public ResponseEntity<ApiResponse<ConsultantAddedResponse>> addConsultant(
//            @RequestBody ConsultantDto hotList
//    ) throws IOException {
//
//        ConsultantAddedResponse consultantResponse = consultantService.addConsultant(hotList);
//        ApiResponse<ConsultantAddedResponse> response=new ApiResponse<>(true,"Consultant Created",consultantResponse,null);
//
//        return new ResponseEntity<ApiResponse<ConsultantAddedResponse>>(response, HttpStatus.CREATED);
//    }

    @PutMapping(value = "/updateConsultant/{consultantId}",consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ConsultantAddedResponse>> updateConsultant(
            @PathVariable String consultantId,
            @RequestBody ConsultantDto consultantDto
    ){
       ConsultantAddedResponse response= consultantService.updateConsultant(consultantId,consultantDto);
       ApiResponse<ConsultantAddedResponse> apiResponse=new ApiResponse<>(
               true,
               "Consultant details updated successfully.",
               response,
               null);
       return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }
    @DeleteMapping("/deleteConsultant/{consultantId}")
    public ApiResponse<DeleteConsultantResponse> deleteConsultantById(
            @PathVariable String consultantId
    ){
        DeleteConsultantResponse  response= consultantService.deleteConsultant(consultantId);
        ApiResponse<DeleteConsultantResponse> apiResponse=new ApiResponse<>(
                true,
                "Consultant Deleted successfully.",
                response,
                null
        );
        return apiResponse;
    }

    @GetMapping("/consultant/{consultantId}")
    public ResponseEntity<ApiResponse<ConsultantDto>> getConsultantByConsultantId(
            @PathVariable String consultantId,
            @RequestParam (defaultValue = "0") int page,
            @RequestParam (defaultValue = "10") int size
    ){
        ConsultantDto hotList= consultantService.getConsultantByID(consultantId);
        ApiResponse<ConsultantDto> response=new ApiResponse<>(true,"HotList data fetched.",hotList,null);

        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @GetMapping("/search/{keyword}")
    public ResponseEntity<ApiResponse<Page<ConsultantDto>>> search(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @PathVariable String keyword
    ){
         Pageable pageable=PageRequest.of(page,
                 size,
                 Sort.Direction.DESC,"updatedTimeStamp");

        Page<ConsultantDto> response= consultantService.search(pageable,keyword);

        ApiResponse<Page<ConsultantDto>> apiResponse=new ApiResponse<>(true,"Data Fetched",response,null);

        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    @GetMapping("/getUsers")
    public List<String> getUserNames() {

        return userServiceClient.getUserNames();
    }

}
