package com.adroit.hotlistmicroservice.dto;

import lombok.Data;

@Data
public class AssociatedUser {
    private String userId;
    private String employeeId;
    private String userName;
    private String employeeName;
}
