package com.uni.ethesis.exceptions;

public class ProfessorAlreadyAssignedException extends RuntimeException {
    public ProfessorAlreadyAssignedException(String message) {
        super(message);
    }
}
