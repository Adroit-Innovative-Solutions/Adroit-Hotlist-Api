package com.adroit.hotlistmicroservice.client;

import com.adroit.hotlistmicroservice.dto.ApiResponse;
import com.adroit.hotlistmicroservice.dto.EmployeeWithRole;
import com.adroit.hotlistmicroservice.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name="user",
        url = "${user.microservice.url}",
        configuration = FeignClientConfiguration.class)
public interface UserServiceClient {

    @GetMapping("/users/allUsers")
     ApiResponse<List<UserDto>> getAllUsers();

    @GetMapping("users/user/{userId}")
     ResponseEntity<ApiResponse<UserDto>> getUserByUserID(@PathVariable String userId);


}
