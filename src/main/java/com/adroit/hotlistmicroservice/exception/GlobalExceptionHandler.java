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


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex){

        ErrorDto error=new ErrorDto(500,ex.getMessage());
        ErrorResponse response=new ErrorResponse(false,"Exception",null,error);
        return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorResponse> handleMultipartException(MultipartException ex) {

        ErrorDto error=new ErrorDto(413,ex.getMessage());
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

        ErrorDto error=new ErrorDto(409,e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                false,
                "Consultant already exists. Please check the details.",
                new ArrayList(),
                error
        );
        return new ResponseEntity<>(errorResponse,HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidFileTypeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFileType(InvalidFileTypeException ex) {
       ErrorDto error = new ErrorDto(
                400,
                "Allowed formats: PDF (.pdf), Word (.docx, .doc). " + ex.getMessage()
        );

        ErrorResponse response = new ErrorResponse(
                false,
                "Invalid file type",
                new ArrayList<>(),
                error
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Handle oversized files
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxSizeExceeded(MaxUploadSizeExceededException ex) {
        ErrorDto error = new ErrorDto(
                413,
                "Max file size is 10MB. " + ex.getMessage()
        );

        ErrorResponse response = new ErrorResponse(
                false,
                "File too large",
                new ArrayList<>(),
                error
        );
        return new ResponseEntity<>(response, HttpStatus.PAYLOAD_TOO_LARGE);
    }
    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDocumentNotFoundException(DocumentNotFoundException e){
        ErrorDto error = new ErrorDto(
                404,
                  e.getMessage()
        );
        ErrorResponse response = new ErrorResponse(
                false,
                "No Documents Available",
                new ArrayList<>(),
                error
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException e){
        ErrorDto error=new ErrorDto(404,e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                false,
                "User not Found Exception",
                new ArrayList(),
                error
        );
        return new ResponseEntity<>(errorResponse,HttpStatus.CONFLICT);
    }
    @ExceptionHandler(UserRoleNotAssignedException.class)
    public ResponseEntity<ErrorResponse> handleUserRoleNotAssignedException(UserRoleNotAssignedException e){
        ErrorDto error=new ErrorDto(400,e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                false,
                "TL/Recruiter not Assigned Exception",
                new ArrayList(),
                error
        );
        return new ResponseEntity<>(errorResponse,HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(FeignClientException.class)
    public ResponseEntity<ErrorResponse> handleFeignClientException(FeignClientException e){

        ErrorDto error=new ErrorDto(404,e.getErrorMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                false,
                e.getMessage(),
                new ArrayList(),
                error
        );
        return new ResponseEntity<>(errorResponse,HttpStatus.CONFLICT);
    }
}
