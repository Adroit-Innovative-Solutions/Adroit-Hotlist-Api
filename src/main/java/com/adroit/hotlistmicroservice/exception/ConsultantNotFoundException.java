package com.adroit.hotlistmicroservice.exception;

public class ConsultantNotFoundException extends RuntimeException{

    public ConsultantNotFoundException(String message) {
        super(message);
    }
}
