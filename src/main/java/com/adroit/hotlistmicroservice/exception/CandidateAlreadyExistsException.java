package com.adroit.hotlistmicroservice.exception;

/**
 * Custom exception to handle cases where a candidate already exists
 * with the same name, email, or contact number.
 */
public class CandidateAlreadyExistsException extends RuntimeException {

    // Constructor with a custom message
    public CandidateAlreadyExistsException(String message) {
        super(message);
    }

    // Optionally, you can include a constructor with a cause
    public CandidateAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
