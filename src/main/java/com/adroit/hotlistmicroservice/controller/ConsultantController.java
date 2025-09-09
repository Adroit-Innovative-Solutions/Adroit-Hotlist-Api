package com.adroit.hotlistmicroservice.controller;

import com.adroit.hotlistmicroservice.client.UserServiceClient;
import com.adroit.hotlistmicroservice.dto.*;
import com.adroit.hotlistmicroservice.model.Consultant;
import com.adroit.hotlistmicroservice.repo.ConsultantRepo;
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
    @Autowired
    private ConsultantRepo consultantRepo;

    private static final Logger logger = LoggerFactory.getLogger(ConsultantController.class);

    @PostMapping("/addConsultant")
    public ResponseEntity<ApiResponse<ConsultantAddedResponse>> addConsultant(
            @ModelAttribute ConsultantDto hotList,
            @RequestParam(value = "resumes",required = false) List<MultipartFile> resumes,
            @RequestParam(value = "documents",required = false) List<MultipartFile> documents,
            @RequestParam(value = "isAssignAll", required = false, defaultValue = "false") boolean isAssignAll
    ) throws IOException {

        logger.info("In Coming Request For New Adding Consultant");
        ConsultantAddedResponse consultantResponse = consultantService.addConsultant(hotList, resumes, documents,isAssignAll);
        ApiResponse<ConsultantAddedResponse> response=new ApiResponse<>(true,"Consultant Created",consultantResponse,null);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @GetMapping("/allConsultants")
    public ResponseEntity<ApiResponse<PageResponse<ConsultantDto>>> getAllConsultants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ){
        logger.info("In Coming Request For Fetching All Consultants..page {} size {} keyword {}",page,size,keyword);
        Pageable pageable = PageRequest.of(
                page, size,
                Sort.Direction.DESC, "updatedTimeStamp"
        );

        Page<ConsultantDto> consultants = consultantService.getAllConsultants(pageable, keyword);
        PageResponse<ConsultantDto> pageResponse = new PageResponse<>(consultants);
        ApiResponse<PageResponse<ConsultantDto>> response = new ApiResponse<>(
                true, "Consultants data fetched.", pageResponse, null
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping(value = "/updateConsultant/{consultantId}",consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ConsultantAddedResponse>> updateConsultant(
            @PathVariable String consultantId,
            @RequestBody ConsultantDto consultantDto
    ){
        logger.info("In Coming Request For Updating Consultant for ID {}",consultantId);
       ConsultantAddedResponse response= consultantService.updateConsultant(consultantId,consultantDto);
       ApiResponse<ConsultantAddedResponse> apiResponse=new ApiResponse<>(
               true,
               "Consultant details updated successfully.",
               response,
               null);
       return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }
    @DeleteMapping("/deleteConsultant/{consultantId}/{userId}")
    public ApiResponse<DeleteConsultantResponse> deleteConsultantById(
            @PathVariable String consultantId,
            @PathVariable String userId
    ){
        logger.info("In Coming Request For Deleting Consultant {}",consultantId);
        DeleteConsultantResponse  response= consultantService.deleteConsultant(consultantId,userId);
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
        logger.info("In Coming Request For Fetching Consultant : {}",consultantId);
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
            @RequestParam(required = false) String keyword,
            @PathVariable String userId
    ){
        Pageable pageable=PageRequest.of(
                page,
                size,
                Sort.Direction.DESC,"updatedTimeStamp");
       Page<ConsultantDto> response=consultantService.getConsultantsByUserId(pageable,userId,keyword);
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
            @RequestParam(required = false) String keyword,
            @PathVariable String userId

    ){
        Pageable pageable=PageRequest.of(
                page,
                size,
                Sort.Direction.DESC,"updatedTimeStamp");

       Page<ConsultantDto> response=consultantService.getTeamConsultants(pageable,userId,keyword);
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
    @GetMapping("/salesExecutiveConsultants/{userId}")
    public ResponseEntity<ApiResponse<Page<ConsultantDto>>> getSalesExecutiveConsultants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @PathVariable String userId
    ){
        Pageable pageable=PageRequest.of(page,
                size,
                Sort.Direction.DESC,"updatedTimeStamp");
        Page<ConsultantDto> pageResponse=consultantService.getSalesExecutiveConsultants(userId,keyword,pageable);
        PageResponse<ConsultantDto> response=new PageResponse<>(pageResponse);
        ApiResponse apiResponse=new ApiResponse<>(true,"Fetched Sales Executive Consultants",response,null);
       return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }
    @GetMapping("/yetToOnBoardConsultants")
    public ResponseEntity<ApiResponse<Page<ConsultantDto>>> getYetOnBoardConsultants(
            @RequestParam (defaultValue = "0") int page,
            @RequestParam (defaultValue = "10") int size,
            @RequestParam (required = false) String keyword
    ){
        Pageable pageable=PageRequest.of(page,size,Sort.Direction.DESC,"updatedTimeStamp");
        Page<ConsultantDto> pageResponse=consultantService.getYetToOnBoardList(keyword,pageable);
        PageResponse<ConsultantDto> response=new PageResponse<>(pageResponse);
        ApiResponse apiResponse=new ApiResponse(true,"Fetched Yet On Board Consultants",response,null);
    return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }
    @PatchMapping("/moveToHotlist/{consultantId}")
    public ResponseEntity<ApiResponse<ConsultantAddedResponse>> movedToHotList(
            @PathVariable String consultantId
    ){
       ConsultantAddedResponse response=consultantService.moveToHotlist(consultantId);
       ApiResponse<ConsultantAddedResponse> apiResponse=new ApiResponse<>(true,"Consultant Moved To Hotlist",response,null);
       return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

}
