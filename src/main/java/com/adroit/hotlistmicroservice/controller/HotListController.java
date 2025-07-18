package com.adroit.hotlistmicroservice.controller;

import com.adroit.hotlistmicroservice.dto.*;
import com.adroit.hotlistmicroservice.service.HotListService;
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


//@CrossOrigin(origins = {"http://35.188.150.92", "http://192.168.0.140:3000",
//        "http://192.168.0.139:3002",
//        "http://localhost:3000","http://192.168.0.135:8080"})
@RestController
@RequestMapping("/hotlist")
public class HotListController {

    @Autowired
    private HotListService hotListService;

    private static final Logger logger = LoggerFactory.getLogger(HotListController.class);

//    @PostMapping("/addConsultant")
//    public ResponseEntity<ApiResponse<ConsultantAddedResponse>> addConsultant(
//            @ModelAttribute HotListDto hotList,
//            @RequestParam(value = "actualVisaFile",required = false) MultipartFile actualVisaFile,
//            @RequestParam(value = "marketingVisaFile",required = false) MultipartFile marketingVisaFile
//    ) throws IOException {
//
//        ConsultantAddedResponse consultantResponse = hotListService.addConsultant(hotList, actualVisaFile, marketingVisaFile);
//        ApiResponse<ConsultantAddedResponse> response=new ApiResponse<>(true,"Consultant Created",consultantResponse,null);
//
//        return new ResponseEntity<ApiResponse<ConsultantAddedResponse>>(response, HttpStatus.OK);
//    }
     @GetMapping("/allConsultants")
     public ResponseEntity<ApiResponse<PageResponse<HotListDto>>> getAllConsultants(
            @RequestParam (defaultValue = "0") int page,
            @RequestParam (defaultValue = "10") int size
     ){
         Pageable pageable= PageRequest.of(
                 page, size
         , Sort.Direction.DESC,"updatedTimeStamp");

      Page<HotListDto> hotList= hotListService.getAllConsultants(pageable);
      PageResponse<HotListDto> pageResponse=new PageResponse<>(hotList);
      ApiResponse<PageResponse<HotListDto>> response=new ApiResponse<>(true,"HotList data fetched.",pageResponse,null);

      return new ResponseEntity<>(response,HttpStatus.OK);
    }
//    @PostMapping("/addManually")
//    public String addConsultManually(@RequestBody List<HotListDto> list){
//
//        return hotListService.addConsultantManually(list);
//
//    }
    @PostMapping("/addConsultant")
    public ResponseEntity<ApiResponse<ConsultantAddedResponse>> addConsultant(
            @RequestBody HotListDto hotList
    ) throws IOException {

        ConsultantAddedResponse consultantResponse = hotListService.addConsultant(hotList);
        ApiResponse<ConsultantAddedResponse> response=new ApiResponse<>(true,"Consultant Created",consultantResponse,null);

        return new ResponseEntity<ApiResponse<ConsultantAddedResponse>>(response, HttpStatus.CREATED);
    }

    @PutMapping(value = "/updateConsultant/{consultantId}",consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ConsultantAddedResponse>> updateConsultant(
            @PathVariable String consultantId,
            @RequestBody HotListDto hotList
    ){
       ConsultantAddedResponse response=hotListService.updateConsultant(consultantId,hotList);
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
        DeleteConsultantResponse  response=hotListService.deleteConsultant(consultantId);
        ApiResponse<DeleteConsultantResponse> apiResponse=new ApiResponse<>(
                true,
                "Consultant Deleted successfully.",
                response,
                null
        );
        return apiResponse;
    }

    @GetMapping("/consultant/{consultantId}")
    public ResponseEntity<ApiResponse<HotListDto>> getConsultantByConsultantId(
            @PathVariable String consultantId,
            @RequestParam (defaultValue = "0") int page,
            @RequestParam (defaultValue = "10") int size
    ){
        HotListDto hotList= hotListService.getConsultantByID(consultantId);
        ApiResponse<HotListDto> response=new ApiResponse<>(true,"HotList data fetched.",hotList,null);

        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @GetMapping("/search/{keyword}")
    public ResponseEntity<ApiResponse<Page<HotListDto>>> search(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @PathVariable String keyword
    ){
         Pageable pageable=PageRequest.of(page,
                 size,
                 Sort.Direction.DESC,"updatedTimeStamp");

        Page<HotListDto> response=hotListService.search(pageable,keyword);

        ApiResponse<Page<HotListDto>> apiResponse=new ApiResponse<>(true,"Data Fetched",response,null);

        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }
}
