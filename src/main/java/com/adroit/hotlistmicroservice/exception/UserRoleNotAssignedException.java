package com.adroit.hotlistmicroservice.exception;

public class UserRoleNotAssignedException extends RuntimeException{
    public UserRoleNotAssignedException(String message) {
        super(message);
    }
}
