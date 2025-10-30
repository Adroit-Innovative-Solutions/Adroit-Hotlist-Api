package com.adroit.hotlistmicroservice.exception;

public class PlacementsNotFoundException extends RuntimeException{

    public PlacementsNotFoundException(String message){
        super(message);
    }
}
