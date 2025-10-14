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
        Pageable pageable= PageRequest.of(page,size, Sort.Direction.DESC,"updatedAt");
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
        Pageable pageable=PageRequest.of(page,size,Sort.Direction.DESC,"updatedAt");
        Page<RateTermsConfirmationDTO> rateTermsConfirmationDTOPage=rtrService.getSalesRTRList(userId,keyword,filters,pageable);

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
        Pageable pageable=PageRequest.of(page,size,Sort.Direction.DESC,"updatedAt");

        Page<RateTermsConfirmationDTO> rateTermsConfirmationDTOPage=rtrService.getRTRByConsultant(consultantId,keyword,filters,pageable);
        PageResponse<RateTermsConfirmationDTO> pageResponse=new PageResponse<>(rateTermsConfirmationDTOPage);
        ApiResponse apiResponse=new ApiResponse<>(true,"RTR data Fetched Successfully",pageResponse,null);
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
