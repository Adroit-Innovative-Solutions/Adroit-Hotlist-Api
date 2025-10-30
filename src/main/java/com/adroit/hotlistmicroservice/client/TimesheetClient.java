package com.adroit.hotlistmicroservice.client;

import com.adroit.hotlistmicroservice.dto.ApiResponse;
import com.adroit.hotlistmicroservice.dto.EmployeeLeaveSummaryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "timesheet-service", url = "http://mulya-timesheet-prod:7072/timesheet")
public interface TimesheetClient {

        @PostMapping("/leave-initialization")
        ApiResponse<EmployeeLeaveSummaryDto> initializeLeave(@RequestBody EmployeeLeaveSummaryDto leaveSummaryDto);
    }



