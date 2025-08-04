package com.adroit.hotlistmicroservice.exception;

public class DocumentNotFoundException extends RuntimeException{

    public DocumentNotFoundException(String message) {
        super(message);
    }
}
