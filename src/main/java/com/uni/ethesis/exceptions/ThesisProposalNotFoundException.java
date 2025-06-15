package com.uni.ethesis.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ThesisProposalNotFoundException extends RuntimeException {
    public ThesisProposalNotFoundException(String message) {
        super(message);
    }
}
