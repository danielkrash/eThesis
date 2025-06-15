package com.uni.ethesis.exceptions;

/**
 * Exception thrown when an invalid status transition is attempted.
 * This is used to enforce business rules around status changes.
 */
public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(String message) {
        super(message);
    }

    public InvalidStatusTransitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
