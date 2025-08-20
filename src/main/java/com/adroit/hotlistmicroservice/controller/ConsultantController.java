package com.adroit.hotlistmicroservice.controller;

import com.adroit.hotlistmicroservice.client.UserServiceClient;
import com.adroit.hotlistmicroservice.dto.*;
import com.adroit.hotlistmicroservice.service.ConsultantService;
import org.apache.catalina.User;
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

@CrossOrigin(origins = {"http://35.188.150.92", "http://192.168.0.140:3000", "http://192.168.0.139:3000","https://mymulya.com","http://localhost:3000","http://192.168.0.135:8080","http://192.168.0.135",
        "http://182.18.177.16","http://192.168.1.151:3000"})
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
    public ResponseEntity<ApiResponse<PageResponse<ConsultantDto>>> search(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @PathVariable String keyword
    ){
         Pageable pageable=PageRequest.of(page,
                 size,
                 Sort.Direction.DESC,"updatedTimeStamp");
        Page<ConsultantDto> response= consultantService.search(pageable,keyword);
        PageResponse<ConsultantDto> pageResponse=new PageResponse<>(response);
        ApiResponse<PageResponse<ConsultantDto>> apiResponse=new ApiResponse<>(true,"Data Fetched",pageResponse,null);
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }
//    @GetMapping("/getEmployees/{role}")
//    public ResponseEntity<EmployeeDropDownDto> getEmployeesByRole(
//            @PathVariable String role
//    ){
//        consultantService.getEmployeeDetailsByRole(role);
//    }
    @GetMapping("/consultantsByUserId/{userId}")
    public ResponseEntity<ApiResponse<PageResponse<ConsultantDto>>> consultantsByUserId(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @PathVariable String userId
    ){
        Pageable pageable=PageRequest.of(
                page,
                size,
                Sort.Direction.DESC,"updatedTimeStamp");
       Page<ConsultantDto> response=consultantService.getConsultantsByUserId(pageable,userId);
        PageResponse<ConsultantDto> pageResponse=new PageResponse<>(response);
       ApiResponse<PageResponse<ConsultantDto>> apiResponse=new ApiResponse<>(true,"Consultant Data Fetched",pageResponse,null);
       return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }
    @GetMapping("/getUsers/{role}")
    public List<EmployeeDropDownDto> getUserNames(
            @PathVariable String role
    ) {
        return consultantService.getEmployeeDetailsByRole(role);
    }
    @GetMapping("/getTeamConsultants/{userId}")
    public ResponseEntity<ApiResponse<PageResponse<ConsultantDto>>> getTeamConsultants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @PathVariable String userId
    ){
        Pageable pageable=PageRequest.of(
                page,
                size,
                Sort.Direction.DESC,"updatedTimeStamp");

       Page<ConsultantDto> response=consultantService.getTeamConsultants(pageable,userId);
        PageResponse<ConsultantDto> pageResponse=new PageResponse<>(response);
       ApiResponse<PageResponse<ConsultantDto>> apiResponse=new ApiResponse<>(true,"HotList Data Fetched",pageResponse,null);
       return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<UserDto>> getUserByUserId(@PathVariable String userId){

        return userServiceClient.getUserByUserID(userId);
    }
    @GetMapping("/user/allUsers")
    public ResponseEntity<ApiResponse<PageResponse<UserDto>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
         Pageable pageable=PageRequest.of(page,size);
         Page<UserDto> response=consultantService.getAllUSEntityUsers(pageable);
         PageResponse<UserDto> pageResponse=new PageResponse<>(response);
         ApiResponse<PageResponse<UserDto>> apiResponse=new ApiResponse<>(true,"Users Data Fetched",pageResponse,null);

         return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }
}
