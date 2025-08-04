package com.adroit.hotlistmicroservice.exception;

public class MaxUploadSizeExceededException extends RuntimeException{

    public MaxUploadSizeExceededException(String message) {
        super(message);
    }
}
