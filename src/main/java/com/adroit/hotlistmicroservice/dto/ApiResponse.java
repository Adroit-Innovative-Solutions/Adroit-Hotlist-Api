package com.adroit.hotlistmicroservice.dto;

import com.adroit.hotlistmicroservice.exception.ErrorDto;
import com.adroit.hotlistmicroservice.exception.ErrorResponse;

public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private ErrorDto error;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, String message, T data, ErrorDto error) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.error = error;
    }


    // ✅ Static helper for success responses
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, null);
    }

    // ✅ Static helper for error responses (compatible with your ErrorDto)
    public static <T> ApiResponse<T> error(String message, int errorCode, String errorMessage) {
        ErrorDto errorDto = new ErrorDto(errorCode, errorMessage);
        return new ApiResponse<>(false, message, null, errorDto);
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

    public ErrorDto getError() {
        return error;
    }

    public void setError(ErrorDto error) {
        this.error = error;
    }
}
