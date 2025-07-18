package com.adroit.hotlistmicroservice.dto;

import com.adroit.hotlistmicroservice.exception.ErrorResponse;

public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private ErrorResponse.ErrorDto error;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, String message, T data, ErrorResponse.ErrorDto error) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.error = error;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public ErrorResponse.ErrorDto getError() {
        return error;
    }

    public void setError(ErrorResponse.ErrorDto error) {
        this.error = error;
    }
}
