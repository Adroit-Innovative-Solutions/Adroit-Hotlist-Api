package com.adroit.hotlistmicroservice.exception;

public class InvalidFileTypeException extends RuntimeException{

    public InvalidFileTypeException(String message) {
        super(message);
    }
}
