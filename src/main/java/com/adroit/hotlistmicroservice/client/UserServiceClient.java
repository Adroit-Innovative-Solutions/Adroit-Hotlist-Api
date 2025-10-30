package com.adroit.hotlistmicroservice.client;

import com.adroit.hotlistmicroservice.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name="user",
        url = "${user.microservice.url}",
        configuration = FeignClientConfiguration.class)
public interface UserServiceClient {

    @GetMapping("/users/allUsers")
     ApiResponse<List<UserDto>> getAllUsers();

    @GetMapping("users/user/{userId}")
     ResponseEntity<ApiResponse<UserDto>> getUserByUserID(@PathVariable String userId);

    @PostMapping("/register")
    ResponseEntity<ApiResponse<UserDetailsDTO>> registerUser(@RequestBody UserDetailsDTO userDto);

    @GetMapping("/email")
    ResponseEntity<ApiResponse<UserDetailsDTO>> getUserByEmail(@RequestParam("email") String email);

    @GetMapping("/{userId}/login-status")
    ResponseEntity<ApiResponse<UserLoginStatusDTO>> getLoginStatusByUserId(@PathVariable("userId") String userId);

}



