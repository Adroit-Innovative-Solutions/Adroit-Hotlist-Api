package com.adroit.hotlistmicroservice.service;

import com.adroit.hotlistmicroservice.dto.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class DirectRTRFileService {

    @Autowired
    private RateTermsConfirmationService rtrService;

    @Autowired
    private ConsultantService consultantService;

    @Transactional
    public ApiResponse<RTRAddedResponse> addRTR(String userId, RateTermsConfirmationRequest directRTRRequest, ConsultantDto hotList, List<MultipartFile> resumes, List<MultipartFile> documents, boolean isAssignAll) throws IOException {
        ConsultantAddedResponse consultantResponse = consultantService.addConsultant(hotList, resumes, documents,isAssignAll, true);
        directRTRRequest.setConsultantId(consultantResponse.getConsultantId());
        RTRAddedResponse response=rtrService.createRTR(userId, directRTRRequest);
        ApiResponse<RTRAddedResponse> apiResponse=new ApiResponse<>(true,"Direct RTR is create along with the consultant : "+consultantResponse.getConsultantId(),response,null);
        return apiResponse;
    }
}
