package com.adroit.hotlistmicroservice.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.util.ArrayList;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidFileTypeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFileTypeException(InvalidFileTypeException ex) {
        ErrorResponse.ErrorDto error=new ErrorResponse.ErrorDto(400, ex.getMessage());
        ErrorResponse response = new ErrorResponse(
                false,
                "Invalid File Type",
                null,
                error
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {

        ErrorResponse.ErrorDto error=new ErrorResponse.ErrorDto(413,ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                false,
                "File size exceeds the maximum allowed size of 10 MB.",
                null,
                error
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.PAYLOAD_TOO_LARGE);  // Return 413 Payload Too Large
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex){

        ErrorResponse.ErrorDto error=new ErrorResponse.ErrorDto(500,ex.getMessage());
        ErrorResponse response=new ErrorResponse(false,"Exception",null,error);
        return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorResponse> handleMultipartException(MultipartException ex) {

        ErrorResponse.ErrorDto error=new ErrorResponse.ErrorDto(413,ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                false,
                "Multipart File Exception",
                null,
                error
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.PAYLOAD_TOO_LARGE);  // Return 413 Payload Too Large
    }
    @ExceptionHandler(ConsultantAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleConsultantAlreadyExistsException(ConsultantAlreadyExistsException e){

        ErrorResponse.ErrorDto error=new ErrorResponse.ErrorDto(409,e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                false,
                "Consultant already exists. Please check the details.",
                new ArrayList(),
                error
        );
        return new ResponseEntity<>(errorResponse,HttpStatus.CONFLICT);
    }
    @ExceptionHandler(ConsultantNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleConsultantNotFoundException(ConsultantNotFoundException e){

        ErrorResponse.ErrorDto error=new ErrorResponse.ErrorDto(404,e.getMessage());
        ErrorResponse errorResponse=new ErrorResponse(
                false,
                "Consultant Not Exists",
                new ArrayList<>(),
                error
        );
        return new ResponseEntity<>(errorResponse,HttpStatus.NOT_FOUND);
    }
}
