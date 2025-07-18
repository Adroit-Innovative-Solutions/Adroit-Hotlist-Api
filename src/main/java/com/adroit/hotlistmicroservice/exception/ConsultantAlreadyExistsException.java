package com.adroit.hotlistmicroservice.exception;

public class ConsultantAlreadyExistsException extends RuntimeException{

    public ConsultantAlreadyExistsException(String message) {
        super(message);
    }
}
