package com.adroit.hotlistmicroservice.controller;

import com.adroit.hotlistmicroservice.dto.*;
import com.adroit.hotlistmicroservice.service.RTRInterviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"http://35.188.150.92", "http://192.168.0.140:3000", "http://192.168.0.139:3000","https://mymulya.com","http://localhost:3000","http://192.168.0.135:8080","http://192.168.0.135",
        "http://182.18.177.16","http://192.168.1.151:3000","http://192.168.0.193:3000"})
@RequestMapping("/hotlist")
public class RTRInterviewController {

    @Autowired
    RTRInterviewService rtrInterviewService;

   @PostMapping("/schedule-rtrInterview/{userId}")
   public ResponseEntity<ApiResponse<InterviewAddedDto>> scheduleInterview(
           @PathVariable String userId,
           @RequestBody ScheduleInterviewDto interviewDto
   ){
       ApiResponse apiResponse=new ApiResponse(true,"Interview Scheduled For RTR ID "+interviewDto.getRtrId()
               ,rtrInterviewService.scheduleInterview(interviewDto,userId),null);
       return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
  }

  @PutMapping("/update-rtrInterview/{userId}")
  public ResponseEntity<ApiResponse<InterviewAddedDto>> updateInterview(
          @PathVariable String userId,
          @RequestBody UpdateInterviewDto updateInterviewDto
  ){
      ApiResponse apiResponse=new ApiResponse(true,"Interview Updated For Interview ID "+updateInterviewDto.getInterviewId()
              ,rtrInterviewService.updateInterview(updateInterviewDto,userId),null);
      return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @DeleteMapping("/delete-rtrInterview/{interviewId}/{userId}")
  public ResponseEntity<ApiResponse<Void>> deleteInterview(
          @PathVariable String interviewId,
          @PathVariable String userId
  ){
       rtrInterviewService.deleteInterview(interviewId, userId);
       ApiResponse apiResponse=new ApiResponse(true,"Interview Deleted Successfully",null,null);
       return new ResponseEntity<>(apiResponse,HttpStatus.OK);
  }
  @GetMapping("/rtrInterview-id/{interviewId}")
  public ResponseEntity<ApiResponse<RTRInterviewDto>> getInterviewById(
          @PathVariable String interviewId
  ){
       ApiResponse apiResponse=new ApiResponse<>(true,"Interview Fetched Successfully",rtrInterviewService.getInterviewById(interviewId),null);
       return new ResponseEntity<>(apiResponse,HttpStatus.OK);
   }

  @GetMapping("/rtrInterviews-list")
  public ResponseEntity<ApiResponse<PageResponse<RTRInterviewDto>>> getAllInterviews(
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size,
          @RequestParam (required = false) String keyword,
          @RequestParam (required = false) Map<String,Object> filters,
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate fromDate,

          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate toDate
          ){
      Pageable pageable= PageRequest.of(
              page,
              size,
              Sort.Direction.DESC,"interviewDateTime");

       Page<RTRInterviewDto> rtrInterviewDtoPage=rtrInterviewService.getAllInterviews(keyword,filters,fromDate,toDate,pageable);
       PageResponse pageResponse=new PageResponse(rtrInterviewDtoPage);
       ApiResponse apiResponse=new ApiResponse(true,"Interviews Fetched Successfully",pageResponse,null);

      return new ResponseEntity<>(apiResponse,HttpStatus.OK);
  }
    @GetMapping("/salesRtrInterviews-list/{userId}")
    public ResponseEntity<ApiResponse<PageResponse<RTRInterviewDto>>> getSalesInterviews(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam (required = false) String keyword,
            @RequestParam (required = false) Map<String,Object> filters
    ){
        Pageable pageable= PageRequest.of(
                page,
                size,
                Sort.Direction.DESC,"updatedAt");

        Page<RTRInterviewDto> rtrInterviewDtoPage=rtrInterviewService.getSalesInterviews(userId,keyword,filters,pageable);
        PageResponse pageResponse=new PageResponse(rtrInterviewDtoPage);
        ApiResponse apiResponse=new ApiResponse(true,"Interviews Fetched Successfully",pageResponse,null);

        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    @GetMapping("/teamRtrInterviews-list/{userId}")
    public ResponseEntity<ApiResponse<PageResponse<RTRInterviewDto>>> getTeamInterviews(
            @PathVariable String userId,
            @RequestParam (defaultValue = "0") int page,
            @RequestParam (defaultValue = "10") int size,
            @RequestParam (required = false) String keyword,
            @RequestParam (required = false) Map<String,Object> filters
    ){
       Pageable pageable=PageRequest.of(page,size, Sort.Direction.DESC,"updatedAt");

       Page<RTRInterviewDto> rtrInterviewDtoPage=rtrInterviewService.getTeamInterviews(userId,keyword,filters,pageable);
       PageResponse pageResponse=new PageResponse<>(rtrInterviewDtoPage);
       ApiResponse apiResponse=new ApiResponse<>(true,"Team Interviews Fetched Successfully",pageResponse,null);

       return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    @GetMapping("/rtrInterviewsByRtrId/{rtrId}")
    public ResponseEntity<ApiResponse<PageResponse<RTRInterviewDto>>> getRtrInterviewsByRtrId(
            @PathVariable String rtrId
    ){
        ApiResponse apiResponse=new ApiResponse<>(true,"Team Interviews Fetched Successfully", rtrInterviewService.getInterviewsByRtrId(rtrId),null);

        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

}
