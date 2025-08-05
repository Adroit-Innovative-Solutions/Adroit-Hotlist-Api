package com.adroit.hotlistmicroservice.client;

import com.adroit.hotlistmicroservice.dto.EmployeeWithRole;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name="user",
        url = "${user.microservice.url}",
        configuration = FeignClientConfiguration.class)
public interface UserServiceClient {

    @GetMapping("/users/employee")
    public List<EmployeeWithRole> getUserNames();


}
