package com.adroit.hotlistmicroservice.controller;

import com.adroit.hotlistmicroservice.dto.*;
import com.adroit.hotlistmicroservice.service.RateTermsConfirmationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
@CrossOrigin(origins = {"http://35.188.150.92", "http://192.168.0.140:3000", "http://192.168.0.139:3000","https://mymulya.com","http://localhost:3000","http://192.168.0.135:8080","http://192.168.0.135",
        "http://182.18.177.16","http://192.168.1.151:3000","http://192.168.0.193:3000"})
@RestController
@RequestMapping("/hotlist")
public class RateTermsConfirmationController {

    @Autowired
    RateTermsConfirmationService rtrService;

    @PostMapping("/create-rtr/{userId}")
    public ResponseEntity<ApiResponse<RTRAddedResponse>> createRateConfirmation(
            @PathVariable String userId,
            @RequestBody RateTermsConfirmationRequest dto
    ){
        RTRAddedResponse response=rtrService.createRTR(userId, dto);
        ApiResponse<RTRAddedResponse> apiResponse=new ApiResponse<>(true,"RTR Created For Consultant",response,null);

        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }
    @PutMapping("/update-rtr/rtr/{rtrId}/user/{userId}")
    public ResponseEntity<ApiResponse<RTRAddedResponse>> updateRTR(
            @PathVariable String rtrId,
            @PathVariable String userId,
            @RequestBody RTRUpdateDTO rtrUpdateDTO
    ){
        RTRAddedResponse rtrAddedResponse=rtrService.updateRTR(rtrId,userId,rtrUpdateDTO);
        return new ResponseEntity<>(new ApiResponse<>(true,"RTR is updated Successfully",rtrAddedResponse,null),HttpStatus.OK);
    }

    @GetMapping("/rtr-list")
    public ResponseEntity<ApiResponse<Page<RateTermsConfirmationDTO>>> getRtrs(
            @RequestParam (defaultValue = "0") int page,
            @RequestParam (defaultValue = "10") int size,
            @RequestParam (required = false) String keyword,
            @RequestParam (required = false) Map<String,Object> filters
    ){
        Pageable pageable= PageRequest.of(page,size, Sort.Direction.DESC,"createdAt");
        Page<RateTermsConfirmationDTO> rateTermsConfirmationDTOPage=rtrService.getRTRList(keyword,filters,pageable);
        PageResponse<RateTermsConfirmationDTO> pageResponse=new PageResponse<>(rateTermsConfirmationDTOPage);

        ApiResponse apiResponse=new ApiResponse(true,"RTR fetched Successfully",pageResponse,null);
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    @GetMapping("/salesRtr-list/{userId}")
    public ResponseEntity<ApiResponse<Page<RateTermsConfirmationDTO>>> getSalesRTRS(
            @PathVariable String userId,
            @RequestParam (defaultValue = "0") int page,
            @RequestParam (defaultValue = "10") int size,
            @RequestParam (required = false) String keyword,
            @RequestParam (required = false) Map<String,Object> filters
    ){
        Pageable pageable=PageRequest.of(page,size,Sort.Direction.DESC,"createdAt");
        Page<RateTermsConfirmationDTO> rateTermsConfirmationDTOPage=rtrService.getSalesRTRList(userId,keyword,filters,pageable);

        PageResponse<RateTermsConfirmationDTO> pageResponse=new PageResponse<>(rateTermsConfirmationDTOPage);

        ApiResponse apiResponse=new ApiResponse(true,"RTR Data Fetched Successfully",pageResponse,null);
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    @GetMapping("/teamRtr-list/{userId}")
    public ResponseEntity<ApiResponse<Page<RateTermsConfirmationDTO>>> getTeamRtrs(
            @PathVariable String userId,
            @RequestParam (defaultValue = "0") int page,
            @RequestParam (defaultValue = "10") int size,
            @RequestParam (required = false) String keyword,
            @RequestParam (required = false) Map<String,Object> filters
    ){
        Pageable pageable=PageRequest.of(page,size,Sort.Direction.DESC,"createdAt");
        Page<RateTermsConfirmationDTO> rateTermsConfirmationDTOPage=rtrService.getTeamRtrs(userId,keyword,filters,pageable);

        PageResponse<RateTermsConfirmationDTO> pageResponse=new PageResponse<>(rateTermsConfirmationDTOPage);

        ApiResponse apiResponse=new ApiResponse(true,"RTR Data Fetched Successfully",pageResponse,null);
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    @GetMapping("/salesRtr-list-today/{userId}")
    public ResponseEntity<ApiResponse<Page<RateTermsConfirmationDTO>>> getSalesRTRSByDate(
            @PathVariable String userId,
            @RequestParam (defaultValue = "0") int page,
            @RequestParam (defaultValue = "10") int size,
            @RequestParam (required = false) String keyword,
            @RequestParam (required = false) Map<String,Object> filters,
            @RequestParam (required = false) String date
    ){
        Pageable pageable=PageRequest.of(page,size,Sort.Direction.DESC,"createdAt");
        Page<RateTermsConfirmationDTO> rateTermsConfirmationDTOPage=rtrService.getSalesRTRListByDate(userId,keyword,filters,pageable,date);

        PageResponse<RateTermsConfirmationDTO> pageResponse=new PageResponse<>(rateTermsConfirmationDTOPage);

        ApiResponse apiResponse=new ApiResponse(true,"RTR Data Fetched Successfully",pageResponse,null);
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    @GetMapping("/teamRtr-list-today/{userId}")
    public ResponseEntity<ApiResponse<Page<RateTermsConfirmationDTO>>> getTeamRtrsByDate(
            @PathVariable String userId,
            @RequestParam (defaultValue = "0") int page,
            @RequestParam (defaultValue = "10") int size,
            @RequestParam (required = false) String keyword,
            @RequestParam (required = false) Map<String,Object> filters,
            @RequestParam (required = false) String date
    ){
        Pageable pageable=PageRequest.of(page,size,Sort.Direction.DESC,"createdAt");
        Page<RateTermsConfirmationDTO> rateTermsConfirmationDTOPage=rtrService.getTeamRtrsByDate(userId,keyword,filters,pageable,date);

        PageResponse<RateTermsConfirmationDTO> pageResponse=new PageResponse<>(rateTermsConfirmationDTOPage);

        ApiResponse apiResponse=new ApiResponse(true,"RTR Data Fetched Successfully",pageResponse,null);
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    @GetMapping("/rtr-id/{rtrId}")
    public ResponseEntity<ApiResponse<RateTermsConfirmationDTO>> getRTRByRTRId(
            @PathVariable String rtrId
    ){
        ApiResponse apiResponse=new ApiResponse(true,"RTR data fetched Successfully",rtrService.getRTRById(rtrId),null);
         return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    @GetMapping("/rtrs-consultant/{consultantId}")
    public ResponseEntity<ApiResponse<Page<RateTermsConfirmationDTO>>> getRTRsByConsultantId(
            @PathVariable String consultantId,
            @RequestParam (defaultValue = "0") int page,
            @RequestParam (defaultValue = "10") int size,
            @RequestParam (required = false) String keyword,
            @RequestParam (required = false) Map<String,Object> filters
    ){
        Pageable pageable=PageRequest.of(page,size,Sort.Direction.DESC,"createdAt");

        Page<RateTermsConfirmationDTO> rateTermsConfirmationDTOPage=rtrService.getRTRByConsultant(consultantId,keyword,filters,pageable);
        PageResponse<RateTermsConfirmationDTO> pageResponse=new PageResponse<>(rateTermsConfirmationDTOPage);
        ApiResponse apiResponse=new ApiResponse<>(true,"RTR data Fetched Successfully",pageResponse,null);
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }



    @GetMapping("/rtr-list-today")
    public ResponseEntity<ApiResponse<Page<RateTermsConfirmationDTO>>> getTodayRtrs(
            @RequestParam (defaultValue = "0") int page,
            @RequestParam (defaultValue = "10") int size,
            @RequestParam (required = false) String keyword,
            @RequestParam (required = false) Map<String,Object> filters,
            @RequestParam (required = false) String date
    ){
        Pageable pageable= PageRequest.of(page,size, Sort.Direction.DESC,"createdAt");
        Page<RateTermsConfirmationDTO> rateTermsConfirmationDTOPage=rtrService.getRTRListByDate(keyword,filters,pageable,date);
        PageResponse<RateTermsConfirmationDTO> pageResponse=new PageResponse<>(rateTermsConfirmationDTOPage);

        ApiResponse apiResponse=new ApiResponse(true,"RTR fetched Successfully",pageResponse,null);
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    @DeleteMapping("/delete-rtr/{rtrId}/{userId}")
    public ResponseEntity<ApiResponse<Map<String,Object>>> deleteRTR(
            @PathVariable String rtrId,
            @PathVariable String userId
    ){
        Map<String,Object> deleteResponse=rtrService.deleteRTR(rtrId,userId);

        ApiResponse apiResponse=new ApiResponse(true,"RTR Deleted Successfully",deleteResponse,null);

        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }
}
